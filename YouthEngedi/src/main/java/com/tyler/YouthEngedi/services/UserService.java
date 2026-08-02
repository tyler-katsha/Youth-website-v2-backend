package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Exceptions.AuthorizationException;
import com.tyler.YouthEngedi.Exceptions.LockedAccountException;
import com.tyler.YouthEngedi.Exceptions.PasswordResetException;
import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.Repository.UserRepository;
import com.tyler.YouthEngedi.annotations.AuditAction;
import com.tyler.YouthEngedi.annotations.LogExecutionTime;
import com.tyler.YouthEngedi.annotations.RateLimited;
import com.tyler.YouthEngedi.jwts.JwtTokenProvider;
import com.tyler.YouthEngedi.models.User;
import com.tyler.YouthEngedi.models.dtos.*;
import com.tyler.YouthEngedi.models.enums.AuthProvider;
import com.tyler.YouthEngedi.models.enums.Role;
import com.tyler.YouthEngedi.models.mappers.UserMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtTokenProvider tokenProvider;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CloudinaryService cloudinaryService;
    @Autowired
    private SocialLoginService socialLoginService;
//    @Autowired
//    private UserCacheService userCacheService;
    @Autowired
    private VerificationTokenService verificationTokenService;
//    @Autowired
//    private ActiveUserService activeUserService;
//    @Autowired
//    private RedisTemplate<String,Object> redisTemplate;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private CookieService cookieService;

    @Value("${app.jwt.expiration-milliseconds}")
    private long maxAge;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    @LogExecutionTime("Register new user")
    public ResponseEntity<?> register(UserRegisterRequest request){

        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());

        if(existingUser.isPresent()){
            if(existingUser.get().isEnabled()){
                return new ResponseEntity<>("User Already exist and Verified", HttpStatus.FOUND);
            } else{
                verificationTokenService.sendVerificationLink(existingUser.get());

                return new ResponseEntity<>("Verification Email resent. Check your inbox", HttpStatus.OK);
            }
        }

        User user = userMapper.toUser(request);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setEnabled(false);

        if(request.getProfileImageUrl() != null && !request.getProfileImageUrl().isEmpty()){
            try{
                String imageUrl = cloudinaryService.upload(request.getProfileImageUrl());
                user.setProfileImageUrl(imageUrl);
            } catch (Exception e){
                return new ResponseEntity<>("Failed to upload profile Image",HttpStatus.CONTENT_TOO_LARGE);
            }
        }

        userRepository.save(user);

        return new ResponseEntity<>("Registration successful. Verification email sent. Check your inbox.", HttpStatus.CREATED);
    }

    @LogExecutionTime(value="Login user")
    public ResponseEntity<String> login(UserLoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new AuthorizationException("Invalid credentials"));

        if(!user.isEnabled()){
            verificationTokenService.sendVerificationLink(user);
            throw new LockedAccountException("Account is Locked. Please continue as guest and contact an youth leader or admin or verify account before attempting to login.");
        }
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new AuthorizationException("Invalid credentials");
        }

        String token = tokenProvider.generateToken(user);

        String cookie = cookieService.issueToken(token);

        CompletableFuture.runAsync(() -> {
            updateUserOnlineStatus(user.getId(),true);
        });

        // activeUserService.incrementActiveUserCount();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,cookie).body("Login successfully");
    }

    @LogExecutionTime(value = "Fetching all users in UserService class",doSave = false)
    public ResponseEntity<Page<UserResponse>> findAll(int page, int size) {

        try{
            Page<User> userPage = userRepository.findAll(PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"createdAt")));

            Page<UserResponse> responsePage = userPage.map(userMapper::mapToResponse);

            return ResponseEntity.ok(responsePage);
        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @LogExecutionTime(value = "Fetching a user by id in UserService class",doSave = false)
    public ResponseEntity<UserResponse> findById(long userId) {

//        UserCache cachedUser = userCacheService.get(email);
//
//        if(cachedUser != null){
//            UserResponse response = userMapper.mapToResponse(cachedUser);
//            return ResponseEntity.ok(response);
//        }

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserResponse response = userMapper.mapToResponse(user);
//        UserCache dto = toCacheDTO(user);
//
//        userCacheService.put(email,dto);

//        UserResponse response = userMapper.mapToResponse(dto);
        return ResponseEntity.ok(response);
    }
    @LogExecutionTime(value="Updating user profile in UserService class",doSave = false)
    public ResponseEntity<UserProfileResponse> updateProfile(ProfileRequest request,long userId) {

        try{
            User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

            if (request.getName() != null) user.setName(request.getName());
            if (request.getBio() != null) user.setBio(request.getBio());
            if (request.getImage() != null && !request.getImage().isEmpty()) {
                String url = cloudinaryService.upload(request.getImage(),user.getId());
                user.setProfileImageUrl(url);
            }

            user.setUpdatedAt(LocalDateTime.now());

            User saved = userRepository.save(user);

            // userCacheService.evict(user.getEmail());

            return ResponseEntity.ok(userMapper.mapToProfileResponse(saved));
        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @LogExecutionTime(value="Updating user role in UserService class",doSave = false)
    public void upgradeMemberRole(String email){
        try{
            User existingUser = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));

            if(existingUser.getRoles().contains(Role.YOUTH_LEADER) && existingUser.getRoles().size() == 1){
                Set<Role> roles = new HashSet<>();
                roles.add(Role.MEMBER);
                roles.add(Role.YOUTH_LEADER);

                existingUser.setRoles(roles);
                return;
            }

            if(existingUser.getRoles().contains(Role.ADMIN) && existingUser.getRoles().size() == 1){
                Set<Role> roles = new HashSet<>();
                roles.add(Role.MEMBER);
                roles.add(Role.YOUTH_LEADER);
                roles.add(Role.ADMIN);

                existingUser.setRoles(roles);
                return;
            }
            if(existingUser.getRoles().contains(Role.ADMIN)){
                return;
            }

            Role nextRole = getNextRole(existingUser.getRoles());

            Set<Role> roles = existingUser.getRoles();
            roles.add(nextRole);
            existingUser.setRoles(roles);

            existingUser.setUpdatedAt(LocalDateTime.now());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @LogExecutionTime("Updating user role in UserService class")
    public void downgradeMemberRole(String email){
        try{
            User existingUser = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));

            if(existingUser.getRoles().isEmpty()){
                Set<Role> roles = new HashSet<>();

                roles.add(Role.MEMBER);
                existingUser.setRoles(roles);
                return;
            }
            if(existingUser.getRoles().size() == 1){
                return;
            }

            Set<Role> roles = removeRoles(existingUser.getRoles());

            existingUser.setRoles(roles);

            existingUser.setUpdatedAt(LocalDateTime.now());
        } catch (Exception e){
            e.printStackTrace();
        }
    }



    @LogExecutionTime("Soft Delete in UserService class")
    public ResponseEntity<String> deleteAccount(long userId,String token){

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setEnabled(true);
        userRepository.save(user);

        long time = jwtTokenProvider.getRemainingSessionTimeInSeconds(token);
        long hours = time / 60;
        return ResponseEntity.ok("Successfully deactivated your account time remaining from session: " + hours + " hrs remaining");
    }

    @LogExecutionTime(value = "Soft Delete in UserService class",doSave = false)
    public ResponseEntity<?> deactivateMember(String email){

        try{
            User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // user.setDeleted(true);
            user.setEnabled(false);
            User updatedUser = userRepository.save(user);

            return ResponseEntity.ok(updatedUser);
        } catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @LogExecutionTime(value = "Soft Activate in UserService class",doSave = false)
    public ResponseEntity<?> activateMember(String email){

        try{
            User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));

            user.setEnabled(true);
            User updatedUser = userRepository.save(user);

            return ResponseEntity.ok(updatedUser);
        } catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public void enableMember(User user){
            user.setEnabled(true);
            userRepository.save(user);
    }

    @LogExecutionTime(value = "Logout User")
    public ResponseEntity<?> logout(HttpServletResponse response,long userId) {

        Cookie cookie = cookieService.resetToken();

        response.addCookie(cookie);

        CompletableFuture.runAsync(() -> {
            updateUserOnlineStatus(userId,false);
        });

        // activeUserService.decrementActiveUserCount();

        return ResponseEntity.ok("Logged out successfully");
    }


    public ResponseEntity<?> loginWithOAuth2(SocialLoginRequest request) {
            return socialLoginService.socialLogin(request);
    }

    @LogExecutionTime(value = "Continue as Guest")
    public ResponseEntity<?> continueAsGuest() {
        String guestId = "guest_" + UUID.randomUUID();

        User guestUser = User.builder().id(Long.MAX_VALUE).email(guestId).build();
        String token = tokenProvider.generateToken(guestUser);

        // activeUserService.incrementActiveUserCount();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,cookieService.issueToken(token,maxAge/2)).body("Successfully continued as guest");
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

    public void resetPassword(long userId,String password) {

        User existingUser = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User doesn't exist"));

        if(existingUser.getAuthProvider().equals(AuthProvider.OAUTH2)){
            throw new PasswordResetException("Cannot change password for a OAuth based user");
        }
        existingUser.setPassword(passwordEncoder.encode(password));
        userRepository.save(existingUser);
    }

    @Transactional
    public void updateUserOnlineStatus(long userId,boolean isOnline){
        userRepository.updateOnlineStatus(userId,isOnline);
    }
}
