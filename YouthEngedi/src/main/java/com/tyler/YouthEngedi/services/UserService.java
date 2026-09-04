package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Exceptions.*;
import com.tyler.YouthEngedi.Repository.UserRepository;
import com.tyler.YouthEngedi.jwts.JwtTokenProvider;
import com.tyler.YouthEngedi.models.PasswordResetRequest;
import com.tyler.YouthEngedi.models.User;
import com.tyler.YouthEngedi.models.UserPrincipal;
import com.tyler.YouthEngedi.models.dtos.*;
import com.tyler.YouthEngedi.models.enums.AuthProvider;
import com.tyler.YouthEngedi.models.enums.Role;
import com.tyler.YouthEngedi.models.mappers.UserMapper;
import com.tyler.YouthEngedi.redis.GenericRedisService;
import com.tyler.YouthEngedi.utils.GuestManager;
import com.tyler.YouthEngedi.utils.WebSocketHelper;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static com.tyler.YouthEngedi.utils.IdManager.delegateIds;
import static com.tyler.YouthEngedi.utils.IdManager.releaseId;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final UserMapper userMapper;
    private final CloudinaryService cloudinaryService;
    private final ApplicationEventPublisher publisher;
    private final GenericRedisService redisService;

    private static final String USER_ID_KEY_PREFIX = "user:id:";
    private static final String USER_EMAIL_KEY_PREFIX = "user:email:";
    private static final String USER_PAGE_KEY_PREFIX = "users:page:";
    private static final Duration USER_CACHE_TTL = Duration.ofHours(1);
    private static final Duration PAGE_CACHE_TTL = Duration.ofMinutes(15);

    public UserService(UserRepository userRepository, JwtTokenProvider tokenProvider, CloudinaryService cloudinaryService, UserMapper userMapper, ApplicationEventPublisher publisher, GenericRedisService redisService) {
        this.cloudinaryService = cloudinaryService;
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.publisher = publisher;
        this.redisService = redisService;
    }

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    private void evictUserCache(Long userId, String email) {
        if (userId != null) {
            redisService.delete(USER_ID_KEY_PREFIX + userId);
        }
        if (email != null && !email.isBlank()) {
            redisService.delete(USER_EMAIL_KEY_PREFIX + email);
        }
        redisService.deleteByPattern(USER_PAGE_KEY_PREFIX + "*");
    }

    private void cacheUserResponse(Long userId, String email, UserResponse response) {
        if (userId != null) {
            redisService.set(USER_ID_KEY_PREFIX + userId, response, USER_CACHE_TTL);
        }
        if (email != null && !email.isBlank()) {
            redisService.set(USER_EMAIL_KEY_PREFIX + email, response, USER_CACHE_TTL);
        }
    }

    public String register(UserRegisterRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());

        if (existingUser.isPresent()) {
            throw new AuthorizationException(String.format("Existing user with email: %s already exists. Please use a different email.", request.getEmail()));
        }

        User user = userMapper.toUser(request);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setEnabled(true);
        user.setBio(request.getBio());

        if (request.getProfileImageUrl() != null && !request.getProfileImageUrl().isEmpty()) {
            String imageUrl = cloudinaryService.upload(request.getProfileImageUrl());
            user.setProfileImageUrl(imageUrl);
        }

        userRepository.save(user);
        return "Registration successful";
    }

    public String login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthorizationException("Invalid credentials"));

        if (!user.isEnabled()) {
            throw new LockedAccountException("Account is Locked. Please continue as guest and contact an youth leader or admin or verify account before attempting to login.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthorizationException("Invalid credentials");
        }

        var event = WebSocketHelper.buildLogin(user);

        publisher.publishEvent(event);

        return tokenProvider.generateToken(user);
    }

    public void logout(long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User doesn't exist"));

        var event = WebSocketHelper.buildLogout(user);

        publisher.publishEvent(event);

        evictUserCache(userId,user.getEmail());
    }

    public Page<UserResponse> findAll(int page, int size) {
        String cacheKey = USER_PAGE_KEY_PREFIX + page + ":size:" + size;

        var cached = redisService.get(cacheKey, CachedPageResponse.class);

        if (cached.isPresent()) {
            return cached.get().toPage();
        }

        var userPage = userRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))).map(userMapper::mapToResponse);

        var responseToCache = CachedPageResponse.of(userPage);

        redisService.set(cacheKey, responseToCache, PAGE_CACHE_TTL);

        return userPage;
    }

    public UserResponse findById(long userId) {

        String cacheKey = USER_ID_KEY_PREFIX + userId;

        Optional<UserResponse> cached = redisService.get(cacheKey, UserResponse.class);

        if (cached.isPresent()) {
            return cached.get();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserResponse response = userMapper.mapToResponse(user);

        cacheUserResponse(user.getId(), user.getEmail(), response);
        return response;
    }

    public UserProfileResponse updateProfile(ProfileRequest request, long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getName() != null) user.setName(request.getName());
        if (request.getBio() != null) user.setBio(request.getBio());

        var image = request.getImage();
        if (image != null && !image.isEmpty()) {
            var url = cloudinaryService.upload(request.getImage(), user.getId());
            user.setProfileImageUrl(url);
        } else if (request.getPreviewUrl() != null) {
            user.setProfileImageUrl(request.getPreviewUrl());
        } else {
            user.setProfileImageUrl(null);
        }

        var saved = userRepository.save(user);

        evictUserCache(saved.getId(), saved.getEmail());

        return userMapper.mapToProfileResponse(saved);
    }

    public UserResponse upgradeMemberRole(String email) {
        User existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Set<Role> tempRoles = new HashSet<>();

        if (existingUser.getRoles().contains(Role.YOUTH_LEADER) && existingUser.getRoles().size() == 1) {
            tempRoles.add(Role.MEMBER);
            tempRoles.add(Role.YOUTH_LEADER);
            existingUser.setRoles(tempRoles);
            userRepository.save(existingUser);
            evictUserCache(existingUser.getId(), existingUser.getEmail());
            return userMapper.mapToResponse(existingUser);
        }

        if (existingUser.getRoles().contains(Role.ADMIN) && existingUser.getRoles().size() == 1) {
            tempRoles.add(Role.MEMBER);
            tempRoles.add(Role.YOUTH_LEADER);
            tempRoles.add(Role.ADMIN);
            existingUser.setRoles(tempRoles);
            userRepository.save(existingUser);
            evictUserCache(existingUser.getId(), existingUser.getEmail());
            return userMapper.mapToResponse(existingUser);
        }

        if (existingUser.getRoles().contains(Role.ADMIN)) {
            return userMapper.mapToResponse(existingUser);
        }

        Role nextRole = getNextRole(existingUser.getRoles());
        Set<Role> roles = existingUser.getRoles();
        roles.add(nextRole);
        existingUser.setRoles(roles);

        userRepository.save(existingUser);
        evictUserCache(existingUser.getId(), existingUser.getEmail());

        return userMapper.mapToResponse(existingUser);
    }

    public UserResponse downgradeMemberRole(String email) {
        User existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loggedInEmail = authentication.getName();

        if (loggedInEmail.equals(email)) {
            long adminCount = userRepository.countByRolesContains(Role.ADMIN);
            if (adminCount <= 1) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are the last administrator");
            }
        }

        if (existingUser.getRoles().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            roles.add(Role.MEMBER);
            existingUser.setRoles(roles);
            userRepository.save(existingUser);
            evictUserCache(existingUser.getId(), existingUser.getEmail());
            return userMapper.mapToResponse(existingUser);
        }

        if (existingUser.getRoles().size() == 1) {
            return userMapper.mapToResponse(existingUser);
        }

        Set<Role> roles = removeRoles(existingUser.getRoles());
        existingUser.setRoles(roles);
        userRepository.save(existingUser);

        evictUserCache(existingUser.getId(), existingUser.getEmail());

        return userMapper.mapToResponse(existingUser);
    }

    public String deleteAccount(long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String email = user.getEmail();
        userRepository.delete(user);

        evictUserCache(userId, email);

        return "User was removed successfully";
    }

    public String deactivateMember(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        toggleEnabled(user, false);
        evictUserCache(user.getId(), user.getEmail());

        return "User was deactivated successfully";
    }

    public String activateMember(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        toggleEnabled(user, true);
        evictUserCache(user.getId(), user.getEmail());

        return "User was activated successfully";
    }

    @Transactional
    public void toggleEnabled(User user, boolean toggle) {
        user.setEnabled(toggle);
        userRepository.save(user);
    }

    public String continueAsGuest() {
        String guestEmail = "guest_" + UUID.randomUUID();

        var id = delegateIds(); // fetches from a pool of ids

        User guestUser = User.builder()
                .id(id)
                .email(guestEmail)
                .build();

        GuestManager.addGuest(userMapper.toGuest(guestUser));

        var event = WebSocketHelper.buildGuest(guestUser);


        publisher.publishEvent(event);

        return tokenProvider.generateToken(guestUser);
    }

    public void redirectGuest(String token) {
        System.out.println(token);
        var guestId = tokenProvider.extractUserId(token);

        var existingGuest = GuestManager.discardGuest(guestId);

        if(existingGuest == null){
            return;
        }

        var event = WebSocketHelper.buildGuestDestroyed(existingGuest);

        publisher.publishEvent(event);
    }

    public User getCurrentAdmin() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            if (email != null) {
                return userRepository.findByEmail(email)
                        .orElseThrow(() -> new ResourceNotFoundException("Admin not found in the database"));
            }
        } catch (NullPointerException e) {
            throw new ResourceNotFoundException("Admin doesnt exist");
        }
        return null;
    }

    public Role getNextRole(Set<Role> roles) {
        if (roles.contains(Role.ADMIN)) {
            throw new IllegalArgumentException("Administrators cannot request a higher role");
        }
        if (roles.contains(Role.YOUTH_LEADER)) {
            return Role.ADMIN;
        }
        return Role.YOUTH_LEADER;
    }

    private Role getLastRole(Set<Role> roles) {
        Role lastRole = null;
        for (Role role : roles) {
            lastRole = role;
        }
        return lastRole;
    }

    private Set<Role> removeRoles(Set<Role> roles) {
        Set<Role> newSet = new LinkedHashSet<>();
        Role role = getLastRole(roles);

        if (role.equals(Role.ADMIN)) {
            newSet.add(Role.MEMBER);
            newSet.add(Role.YOUTH_LEADER);
        } else if (role.equals(Role.YOUTH_LEADER)) {
            newSet.add(Role.MEMBER);
        }
        return newSet;
    }

    public void resetPassword(PasswordResetRequest request) {
        var existingUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User doesn't exist"));

        if (existingUser.getAuthProvider() == null) {
            existingUser.setAuthProvider(AuthProvider.LOCAL);
        }

        if (existingUser.getAuthProvider().equals(AuthProvider.OAUTH2)) {
            throw new PasswordResetException("Cannot change password for a OAuth based user");
        }

        existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(existingUser);

        evictUserCache(existingUser.getId(), existingUser.getEmail());
    }
}