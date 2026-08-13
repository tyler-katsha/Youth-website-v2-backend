package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Exceptions.*;
import com.tyler.YouthEngedi.Repository.UserRepository;
import com.tyler.YouthEngedi.annotations.LogExecutionTime;
import com.tyler.YouthEngedi.jwts.JwtTokenProvider;
import com.tyler.YouthEngedi.models.PasswordResetRequest;
import com.tyler.YouthEngedi.models.User;
import com.tyler.YouthEngedi.models.dtos.*;
import com.tyler.YouthEngedi.models.enums.AuthProvider;
import com.tyler.YouthEngedi.models.enums.Role;
import com.tyler.YouthEngedi.models.mappers.UserMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;

    private final UserMapper userMapper;
    private final CloudinaryService cloudinaryService;
//    private final UserCacheService userCacheService;
//    private final VerificationTokenService verificationTokenService;
//    private final ActiveUserService activeUserService;
//    private final RedisTemplate<String,Object> redisTemplate;
//    private final JwtTokenProvider jwtTokenProvider;
    private final CookieService cookieService;


    public UserService(CookieService cookieService,UserRepository userRepository,JwtTokenProvider tokenProvider,CloudinaryService cloudinaryService,UserMapper userMapper){
        this.cookieService =cookieService;
        this.cloudinaryService = cloudinaryService;
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
    }

//    @Value("${app.jwt.expiration-milliseconds}")
//    private long maxAge;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    @LogExecutionTime("Register new user")
    public String register(UserRegisterRequest request){

        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());

        if(existingUser.isPresent()){
            throw new AuthorizationException(String.format("Existing user with email: %s already exists. Please use a different email.",request.getEmail()));
        }

        User user = userMapper.toUser(request);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setEnabled(true);
        user.setBio(request.getBio());

        if(request.getProfileImageUrl() != null && !request.getProfileImageUrl().isEmpty()){
                String imageUrl = cloudinaryService.upload(request.getProfileImageUrl());
                user.setProfileImageUrl(imageUrl);
        }

        // verificationTokenService.sendVerificationLink(user);
        userRepository.save(user);

        return "Registration successful";
    }

    @LogExecutionTime(value="Login user")
    public String login(UserLoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new AuthorizationException("Invalid credentials"));

        if(!user.isEnabled()){
            // verificationTokenService.sendVerificationLink(user);
            throw new LockedAccountException("Account is Locked. Please continue as guest and contact an youth leader or admin or verify account before attempting to login.");
        }

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new AuthorizationException("Invalid credentials");
        }

        // String token = tokenProvider.generateToken(user);

        // String cookie = cookieService.issueToken(token);

        // activeUserService.incrementActiveUserCount();

        return tokenProvider.generateToken(user);
    }

    @LogExecutionTime(value = "Fetching all users in UserService class",doSave = false)
    public Page<UserResponse> findAll(int page, int size) {

            return userRepository.findAll(PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"createdAt"))).map(userMapper::mapToResponse);
    }

    @LogExecutionTime(value = "Fetching a user by id in UserService class",doSave = false)
    public UserResponse findById(long userId) {

//        UserCache cachedUser = userCacheService.get(email);
//
//        if(cachedUser != null){
//            UserResponse response = userMapper.mapToResponse(cachedUser);
//            return ResponseEntity.ok(response);
//        }

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

//        UserResponse response = userMapper.mapToResponse(user);
//        UserCache dto = toCacheDTO(user);
//
//        userCacheService.put(email,dto);

//        UserResponse response = userMapper.mapToResponse(dto);
        return userMapper.mapToResponse(user);
    }

    @LogExecutionTime(value="Updating user profile in UserService class",doSave = false)
    public UserProfileResponse updateProfile(ProfileRequest request,long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getName() != null) user.setName(request.getName());
        if (request.getBio() != null) user.setBio(request.getBio());

        MultipartFile image = request.getImage();

        if (image != null && !image.isEmpty()) {
            String url = cloudinaryService.upload(request.getImage(),user.getId());
            user.setProfileImageUrl(url);
        }
        else if(request.getPreviewUrl() != null) {
            user.setProfileImageUrl(request.getPreviewUrl());
        }
        else{
            user.setProfileImageUrl(null);
        }

        User saved = userRepository.save(user);

        // userCacheService.evict(user.getEmail());

        return userMapper.mapToProfileResponse(saved);
    }

    @LogExecutionTime(value="Updating user role in UserService class",doSave = false)
    public UserResponse upgradeMemberRole(String email){

        User existingUser = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Set<Role> tempRoles = new HashSet<>();

        if(existingUser.getRoles().contains(Role.YOUTH_LEADER) && existingUser.getRoles().size() == 1){

            tempRoles.add(Role.MEMBER);
            tempRoles.add(Role.YOUTH_LEADER);

            existingUser.setRoles(tempRoles);

            userRepository.save(existingUser);
            return userMapper.mapToResponse(existingUser);
        }

        if(existingUser.getRoles().contains(Role.ADMIN) && existingUser.getRoles().size() == 1){
            tempRoles.add(Role.MEMBER);
            tempRoles.add(Role.YOUTH_LEADER);
            tempRoles.add(Role.ADMIN);

            existingUser.setRoles(tempRoles);

            userRepository.save(existingUser);
            return userMapper.mapToResponse(existingUser);
        }

        if(existingUser.getRoles().contains(Role.ADMIN)){
            return userMapper.mapToResponse(existingUser);
        }

        Role nextRole = getNextRole(existingUser.getRoles());

        Set<Role> roles = existingUser.getRoles();
        roles.add(nextRole);
        existingUser.setRoles(roles);

        userRepository.save(existingUser);

        return userMapper.mapToResponse(existingUser);
    }

    @LogExecutionTime(value = "Updating user role in UserService class",doSave = false)
    public UserResponse downgradeMemberRole(String email){
        User existingUser = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String loggedInEmail = authentication.getName();

        boolean selfDowngrade = loggedInEmail.equals(email);

        if(selfDowngrade){
            long adminCount = userRepository.countByRolesContains(Role.ADMIN);

            if(adminCount <= 1){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You are the last administrator");
            }
        }
        if(existingUser.getRoles().isEmpty()){
            Set<Role> roles = new HashSet<>();

            roles.add(Role.MEMBER);
            existingUser.setRoles(roles);
            return userMapper.mapToResponse(existingUser);
        }
        if(existingUser.getRoles().size() == 1){
            return userMapper.mapToResponse(existingUser);
        }

        Set<Role> roles = removeRoles(existingUser.getRoles());

        existingUser.setRoles(roles);

        return userMapper.mapToResponse(existingUser);
    }



    @LogExecutionTime(value = "Hard Delete in UserService class",doSave = false)
    public String deleteAccount(long userId){

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        userRepository.delete(user);

        return "User was removed successfully";
        // long time = jwtTokenProvider.getRemainingSessionTimeInSeconds(token);
        // long hours = time / 60;
        // return "Successfully deactivated your account time remaining from session: " + hours + " hrs remaining";
    }

    @LogExecutionTime(value = "Soft Delete in UserService class",doSave = false)
    public String deactivateMember(String email){
            User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));

            user.setEnabled(false);
            userRepository.save(user);

            return "User was deactivated successfully";
    }

    @LogExecutionTime(value = "Activate in UserService class",doSave = false)
    public String activateMember(String email){

            User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));

            user.setEnabled(true);
            userRepository.save(user);

            return "User was activated successfully";
    }

    @Transactional
    public void enableMember(User user){
            user.setEnabled(true);
            userRepository.save(user);
    }

    @LogExecutionTime(value = "Logout User",doSave = false)
    public String logout(HttpServletResponse response) {

        Cookie cookie = cookieService.resetToken();

        response.addCookie(cookie);

        // activeUserService.decrementActiveUserCount();

        return "Logged out successfully";
    }

    @LogExecutionTime(value = "Continue as Guest")
    public String continueAsGuest() {
        String guestId = "guest_" + UUID.randomUUID();

        User guestUser = User.builder()
                .id(Long.MAX_VALUE)
                .email(guestId)
                .build();

        // activeUserService.incrementActiveUserCount();
        //return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,cookieService.issueToken(token,maxAge/2)).body("Successfully continued as guest");
        return tokenProvider.generateToken(guestUser);
    }

//    public Set<String> findActiveUsers() {
//        Set<Object> raw = redisTemplate.opsForSet()
//                .members(ActiveUserService.ACTIVE_USERS_KEY);
//
//        if (raw == null) return new HashSet<>();
//
//        return raw.stream().map(String::valueOf).collect(Collectors.toSet());
//    }

//    public List<UserStatusResponse> getAllUsersWithStatus() {
//
//        List<User> allUsers = userRepository.findAll();
//        Set<String> activeUsers = findActiveUsers();
//
//        return allUsers.stream()
//                .map(user -> {
//                    boolean isOnline = activeUsers.contains(user.getEmail());
//
//                    return UserStatusResponse.builder()
//                            .email(user.getEmail())
//                            .isOnline(isOnline)
//                            .build();
//                }).toList();
//    }

//    private UserCache toCacheDTO(User user) {
//
//        return UserCache
//                .builder()
//                .id(user.getId())
//                .email(user.getEmail())
//                .name(user.getName())
//                .dateOfBirth(user.getDateOfBirth())
//                .bio(user.getBio())
//                .profileImageUrl(user.getProfileImageUrl())
//                .roles(user.getRoles())
//                .isDeleted(user.isDeleted())
//                .isOnline(user.isOnline())
//                .build();
//    }

    public User getCurrentAdmin(){
        String email;
        try{
            email = SecurityContextHolder.getContext().getAuthentication().getName();
            if(email != null){
                return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Admin not found in the database"));
            }
        } catch(NullPointerException e){
            throw new ResourceNotFoundException("Admin doesnt exist");
        }
        return null;
    }

    public Role getNextRole(Set<Role> roles) {

        if(roles.contains(Role.ADMIN)){
            throw new IllegalArgumentException("Administrators cannot request a higher role");
        }

        if(roles.contains(Role.YOUTH_LEADER)){
            return Role.ADMIN;
        }

        return Role.YOUTH_LEADER;
    }

    private Role getLastRole(Set<Role> roles) {

        Role lastRole = null;

        for(Role role:roles){
            lastRole = role;
        }

        return lastRole;
    }

    private Set<Role> removeRoles(Set<Role> roles) {

        Set<Role> newSet = new LinkedHashSet<>();

        Role role = getLastRole(roles);

        if(role.equals(Role.ADMIN)){
            newSet.add(Role.MEMBER);
            newSet.add(Role.YOUTH_LEADER);
        } else if(role.equals(Role.YOUTH_LEADER)){
            newSet.add(Role.MEMBER);
        }

        return newSet;
    }

    public void resetPassword(PasswordResetRequest request) {

        User existingUser = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new ResourceNotFoundException("User doesn't exist"));

        if(existingUser.getAuthProvider() == null) existingUser.setAuthProvider(AuthProvider.LOCAL);

        if(existingUser.getAuthProvider().equals(AuthProvider.OAUTH2)){
            throw new PasswordResetException("Cannot change password for a OAuth based user");
        }

        existingUser.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(existingUser);
    }

}
