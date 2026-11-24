package com.resolvex.controller;

import com.resolvex.dto.ApiResponse;
import com.resolvex.dto.AuthDtos;
import com.resolvex.entity.Category;
import com.resolvex.entity.Department;
import com.resolvex.repository.CategoryRepository;
import com.resolvex.security.UserPrincipal;
import com.resolvex.service.TeamService;
import com.resolvex.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Organization & Users", description = "User management, teams, departments, and ticket categories")
public class UserController {

    private final UserService userService;
    private final TeamService teamService;
    private final CategoryRepository categoryRepository;

    public UserController(UserService userService, TeamService teamService, CategoryRepository categoryRepository) {
        this.userService = userService;
        this.teamService = teamService;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get all users (Admin/Manager)")
    public ResponseEntity<ApiResponse<List<AuthDtos.UserDto>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers()));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<AuthDtos.UserDto>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }

    @PatchMapping("/users/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle user active/deactivated status (Admin only)")
    public ResponseEntity<ApiResponse<AuthDtos.UserDto>> toggleUserActive(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        AuthDtos.UserDto updated = userService.toggleUserActive(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("User status updated", updated));
    }

    @GetMapping("/departments")
    @Operation(summary = "Get all organizational departments")
    public ResponseEntity<ApiResponse<List<Department>>> getDepartments() {
        return ResponseEntity.ok(ApiResponse.success(teamService.getAllDepartments()));
    }

    @GetMapping("/teams")
    @Operation(summary = "Get all support and engineering teams")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTeams() {
        return ResponseEntity.ok(ApiResponse.success(teamService.getAllTeams()));
    }

    @PostMapping("/teams")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a team")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createTeam(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        Long deptId = body.get("departmentId") != null ? Long.valueOf(body.get("departmentId").toString()) : null;
        String desc = (String) body.get("description");
        return ResponseEntity.ok(ApiResponse.success("Team created", teamService.createTeam(name, deptId, desc)));
    }

    @GetMapping("/teams/{teamId}/members")
    @Operation(summary = "Get team members")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTeamMembers(@PathVariable Long teamId) {
        return ResponseEntity.ok(ApiResponse.success(teamService.getTeamMembers(teamId)));
    }

    @PostMapping("/teams/{teamId}/members")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Add member to team")
    public ResponseEntity<ApiResponse<String>> addTeamMember(
            @PathVariable Long teamId,
            @RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        Boolean isLead = body.get("isTeamLead") != null && Boolean.parseBoolean(body.get("isTeamLead").toString());
        teamService.addTeamMember(teamId, userId, isLead);
        return ResponseEntity.ok(ApiResponse.success("Member added to team", "OK"));
    }

    @DeleteMapping("/teams/{teamId}/members/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Remove member from team")
    public ResponseEntity<ApiResponse<String>> removeTeamMember(@PathVariable Long teamId, @PathVariable Long userId) {
        teamService.removeTeamMember(teamId, userId);
        return ResponseEntity.ok(ApiResponse.success("Member removed from team", "OK"));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get active ticket categories")
    public ResponseEntity<ApiResponse<List<Category>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryRepository.findByActiveTrue()));
    }
}
