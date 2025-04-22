package org.chikere.bptracker.app.controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.chikere.bptracker.app.model.User;
import org.chikere.bptracker.app.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for managing users
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRestController {

    private final UserService userService;

    /**
     * Get all users (admin only)
     * @return list of all users
     */
    @GetMapping
    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Get user by ID (admin or self)
     * @param id user ID
     * @param authentication current user authentication
     * @return user
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        // Allow access if user is admin or accessing their own profile
        if (currentUser.getRole() == User.Role.NURSE || currentUser.getId().equals(id)) {
            return userService.getUserById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Get current user profile
     * @param authentication current user authentication
     * @return current user
     */
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(currentUser);
    }

    /**
     * Update user (admin only)
     * @param id user ID
     * @param user updated user data
     * @return updated user
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody @Valid User user) {
        try {
            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update current user profile
     * @param userData updated user data
     * @param authentication current user authentication
     * @return updated user
     */
    @PutMapping("/me")
    public ResponseEntity<User> updateCurrentUser(@RequestBody Map<String, String> userData, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        try {
            User user = new User();
            user.setFullName(userData.getOrDefault("fullName", currentUser.getFullName()));
            user.setEmail(userData.getOrDefault("email", currentUser.getEmail()));

            // Don't allow role change through this endpoint
            user.setRole(currentUser.getRole());
            user.setEnabled(currentUser.isEnabled());

            // Update password if provided
            if (userData.containsKey("password") && !userData.get("password").isEmpty()) {
                user.setPassword(userData.get("password"));
            }

            User updatedUser = userService.updateUser(currentUser.getId(), user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Change password
     * @param passwordData password data containing old and new password
     * @param authentication current user authentication
     * @return success message
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwordData, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        if (!passwordData.containsKey("newPassword") || passwordData.get("newPassword").isEmpty()) {
            return ResponseEntity.badRequest().body("New password is required");
        }

        try {
            userService.changePassword(currentUser.getId(), passwordData.get("newPassword"));
            return ResponseEntity.ok().body(Map.of("message", "Password changed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to change password");
        }
    }

    /**
     * Delete user (admin only)
     * @param id user ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
