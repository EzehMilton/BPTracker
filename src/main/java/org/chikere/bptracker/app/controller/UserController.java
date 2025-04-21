package org.chikere.bptracker.app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.chikere.bptracker.app.model.User;
import org.chikere.bptracker.app.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for handling user-related requests
 */
@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Display the login page
     * @return the login page view
     */
    @GetMapping("/login")
    public String login() {
        return "user/login";
    }

    /**
     * Display the registration page
     * @param model the model
     * @return the registration page view
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", User.Role.values());
        return "user/register";
    }

    /**
     * Process the registration form
     * @param user the user to register
     * @param bindingResult the binding result
     * @param redirectAttributes the redirect attributes
     * @return the redirect URL
     */
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, 
                              BindingResult bindingResult, 
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "user/register";
        }
        
        try {
            userService.createUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! You can now log in.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }
    }

    /**
     * Display the user profile page
     * @param model the model
     * @return the profile page view
     */
    @GetMapping("/profile")
    public String showProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        model.addAttribute("user", user);
        return "user/profile";
    }

    /**
     * Display the change password page
     * @param model the model
     * @return the change password page view
     */
    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model) {
        model.addAttribute("passwordForm", new PasswordChangeForm());
        return "user/change-password";
    }

    /**
     * Process the change password form
     * @param passwordForm the password change form
     * @param bindingResult the binding result
     * @param redirectAttributes the redirect attributes
     * @return the redirect URL
     */
    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute("passwordForm") PasswordChangeForm passwordForm,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "user/change-password";
        }
        
        if (!passwordForm.getNewPassword().equals(passwordForm.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.passwordForm", "Passwords do not match");
            return "user/change-password";
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        
        try {
            userService.changePassword(user.getId(), passwordForm.getNewPassword());
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully!");
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error changing password: " + e.getMessage());
            return "redirect:/change-password";
        }
    }

    /**
     * Display the user management page (admin only)
     * @param model the model
     * @return the user management page view
     */
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('NURSE')")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    /**
     * Display the user edit page (admin only)
     * @param id the user ID
     * @param model the model
     * @return the user edit page view
     */
    @GetMapping("/admin/users/{id}/edit")
    @PreAuthorize("hasRole('NURSE')")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + id));
        model.addAttribute("user", user);
        model.addAttribute("roles", User.Role.values());
        return "admin/edit-user";
    }

    /**
     * Process the user edit form (admin only)
     * @param id the user ID
     * @param user the updated user
     * @param bindingResult the binding result
     * @param redirectAttributes the redirect attributes
     * @return the redirect URL
     */
    @PostMapping("/admin/users/{id}/edit")
    @PreAuthorize("hasRole('NURSE')")
    public String updateUser(@PathVariable Long id,
                            @Valid @ModelAttribute("user") User user,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/edit-user";
        }
        
        try {
            userService.updateUser(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully!");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating user: " + e.getMessage());
            return "redirect:/admin/users/" + id + "/edit";
        }
    }

    /**
     * Delete a user (admin only)
     * @param id the user ID
     * @param redirectAttributes the redirect attributes
     * @return the redirect URL
     */
    @PostMapping("/admin/users/{id}/delete")
    @PreAuthorize("hasRole('NURSE')")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * Form object for changing password
     */
    public static class PasswordChangeForm {
        private String newPassword;
        private String confirmPassword;

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }
    }
}