package com.resolvex.service;

import com.resolvex.dto.ApiResponse;
import com.resolvex.dto.AuthDtos;
import com.resolvex.entity.Department;
import com.resolvex.entity.Role;
import com.resolvex.entity.User;
import com.resolvex.enums.RoleName;
import com.resolvex.exception.BadRequestException;
import com.resolvex.exception.ResourceNotFoundException;
import com.resolvex.repository.DepartmentRepository;
import com.resolvex.repository.RoleRepository;
import com.resolvex.repository.UserRepository;
import com.resolvex.security.JwtUtils;
import com.resolvex.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final AuditService auditService;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       DepartmentRepository departmentRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtils jwtUtils,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.auditService = auditService;
    }

    @Transactional
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail().trim(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));

        auditService.logAction(user, user.getUsername(), "LOGIN", "USER", user.getId(), "User logged in successfully", "API");

        return new AuthDtos.AuthResponse(jwt, toDto(user));
    }

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername().trim())) {
            throw new BadRequestException("Username is already taken!");
        }

        if (userRepository.existsByEmail(request.getEmail().trim())) {
            throw new BadRequestException("Email is already registered!");
        }

        RoleName roleName = RoleName.CUSTOMER;
        if (request.getRole() != null) {
            try {
                roleName = RoleName.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                roleName = RoleName.CUSTOMER;
            }
        }

        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.CUSTOMER, "End-user customer role")));

        User user = new User(
                request.getUsername().trim(),
                request.getEmail().trim().toLowerCase(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName().trim(),
                role
        );
        user.setPhone(request.getPhone());

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId()).orElse(null);
            user.setDepartment(dept);
        }

        User saved = userRepository.save(user);

        // Auto authenticate and generate token
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        auditService.logAction(saved, saved.getUsername(), "REGISTER", "USER", saved.getId(), "User registered account", "API");

        return new AuthDtos.AuthResponse(jwt, toDto(saved));
    }

    @Transactional(readOnly = true)
    public List<AuthDtos.UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AuthDtos.UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return toDto(user);
    }

    @Transactional
    public AuthDtos.UserDto updateProfile(Long userId, AuthDtos.UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone());
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        User saved = userRepository.save(user);
        return toDto(saved);
    }

    @Transactional
    public void changePassword(Long userId, AuthDtos.PasswordResetRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password does not match");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        auditService.logAction(user, user.getUsername(), "PASSWORD_CHANGE", "USER", user.getId(), "Changed account password", "API");
    }

    @Transactional
    public AuthDtos.UserDto toggleUserActive(Long userId, Long adminUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setActive(!Boolean.TRUE.equals(user.getActive()));
        User saved = userRepository.save(user);

        User admin = userRepository.findById(adminUserId).orElse(null);
        auditService.logAction(admin, admin != null ? admin.getUsername() : "ADMIN", "TOGGLE_USER_ACTIVE", "USER",
                user.getId(), "Set active status to " + user.getActive(), "API");

        return toDto(saved);
    }

    public AuthDtos.UserDto toDto(User u) {
        AuthDtos.UserDto dto = new AuthDtos.UserDto();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setEmail(u.getEmail());
        dto.setFullName(u.getFullName());
        dto.setRole(u.getRole().getName().name());
        if (u.getDepartment() != null) {
            dto.setDepartmentId(u.getDepartment().getId());
            dto.setDepartmentName(u.getDepartment().getName());
        }
        dto.setPhone(u.getPhone());
        dto.setAvatarUrl(u.getAvatarUrl());
        dto.setActive(u.getActive());
        return dto;
    }
}
