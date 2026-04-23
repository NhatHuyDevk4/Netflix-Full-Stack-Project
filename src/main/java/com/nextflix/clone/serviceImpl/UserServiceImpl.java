package com.nextflix.clone.serviceImpl;

import com.nextflix.clone.dao.UserRepository;
import com.nextflix.clone.dto.request.UserRequest;
import com.nextflix.clone.dto.response.MessageResponse;
import com.nextflix.clone.dto.response.PageResponse;
import com.nextflix.clone.dto.response.UserResponse;
import com.nextflix.clone.entity.User;
import com.nextflix.clone.enums.Role;
import com.nextflix.clone.exception.EmailAlreadyExistsException;
import com.nextflix.clone.service.EmailService;
import com.nextflix.clone.service.UserService;
import com.nextflix.clone.util.PaginationUtils;
import com.nextflix.clone.util.ServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ServiceUtils serviceUtils;

    @Autowired
    private EmailService emailService;


    @Override
    public MessageResponse createUser(UserRequest userRequest) {
        // B1: Validate email uniqueness
        if(userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
            throw  new EmailAlreadyExistsException("Email already exists: " + userRequest.getEmail());
        }

        // B2: Validate role
        validateRole(userRequest.getRole());

        // B3: Create user entity
        User user = new User();
        user.setEmail(userRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setFullName(userRequest.getFullName());
        user.setRole(Role.valueOf(userRequest.getRole().toUpperCase()));
        user.setActive(true);

        // B4: Generate verification token and send email
        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        user.setVerificationTokenExpiry(Instant.now().plusSeconds(86400)); // 24 hours
        userRepository.save(user);
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);
        return new MessageResponse("User created successfully. Please check your email to verify your account.");
    }

    @Override
    public MessageResponse updateUser(Long id, UserRequest userRequest) {
        User user = serviceUtils.getUserByIdOrThrow(id);
        ensureNotLastActiveAdmin(user);
        validateRole(userRequest.getRole());

        user.setFullName(userRequest.getFullName());
        user.setRole(Role.valueOf(userRequest.getRole().toUpperCase()));
        userRepository.save(user);
        return new MessageResponse("User updated successfully.");
    }

    @Override
    public PageResponse<UserResponse> getAllUsers(int page, int size, String search) {
        Pageable pageable = PaginationUtils.createPageRequest(page, size, "id");

        Page<User> userPage;

        if(search != null && !search.isEmpty()) {
            userPage = userRepository.searchUsers(search.trim(), pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        // :: là cú pháp method reference trong Java, tương đương với lambda expression user -> UserResponse.fromEntity(user)
        return PaginationUtils.toPageResponse(userPage, UserResponse::fromEntity);
    }

    @Override
    public MessageResponse deleteUser(Long id, String currentUsername) {
        User user = serviceUtils.getUserByIdOrThrow(id);

        if(user.getEmail().equals(currentUsername)) {
            throw new RuntimeException("Users cannot delete your own account.");
        }

        ensureNotLastAdmin(user, "delete");

        userRepository.deleteById(id);

        return new MessageResponse("User deleted successfully.");
    }

    @Override
    public MessageResponse toggleUserStatus(Long id, String currentUserEmail) {
        User user = serviceUtils.getUserByIdOrThrow(id);

        if(user.getEmail().equals(currentUserEmail)) {
            throw new RuntimeException("Users cannot change your own status.");
        }
        ensureNotLastActiveAdmin(user);
        user.setActive(!user.isActive());
        userRepository.save(user);
        return new MessageResponse("User status toggled successfully.");
    }

    @Override
    public MessageResponse changeUserRole(Long id, UserRequest userRequest) {
        User user = serviceUtils.getUserByIdOrThrow(id);
        validateRole(userRequest.getRole());

        Role newRole = Role.valueOf(userRequest.getRole().toUpperCase());

        if( user.getRole() == Role.ADMIN && newRole != Role.USER ) {
            ensureNotLastAdmin(user, "change role of");
        }

        user.setRole(newRole);
        userRepository.save(user);
        return new MessageResponse("User role changed successfully.");
    }

    // Hàm này sẽ được gọi trước khi thực hiện các thay đổi có thể làm mất quyền admin của người dùng, như delete hoặc change role
    // Nếu người dùng hiện tại là admin, thì đếm số lượng admin còn lại trong hệ thống
    // Nếu chỉ còn 1 admin (người dùng hiện tại), thì không cho phép thực hiện thay đổi và ném ra một exception
    private void ensureNotLastAdmin(User user, String delete) {
        if(user.getRole() == Role.ADMIN) {
            long adminCount = userRepository.countByRole(Role.ADMIN);
            if(adminCount <= 1) {
                throw new RuntimeException("Cannot " + delete + " the last admin.");
            }
        }
    }


    // Hàm này sẽ được gọi trước khi thực hiện các thay đổi có thể làm mất quyền admin của người dùng, như deactivate hoặc thay đổi role
    // Nếu người dùng hiện tại là admin và đang active, thì đếm số lượng admin active còn lại trong hệ thống
    // Nếu chỉ còn 1 admin active (người dùng hiện tại), thì không cho phép thực hiện thay đổi và ném ra một exception
    private void ensureNotLastActiveAdmin(User user) {
        if(user.isActive() && user.getRole() == Role.ADMIN) {
            long activeAdminCount = userRepository.countByRoleAndActive(Role.ADMIN, true);
            if(activeAdminCount <= 1) {
                throw new RuntimeException("Cannot deactivate or change role of the last active admin.");
            }
        }
    }

    private void validateRole(String role) {
        if(Arrays.stream(Role.values()).noneMatch(r -> r.name().equalsIgnoreCase(role))) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }
}
