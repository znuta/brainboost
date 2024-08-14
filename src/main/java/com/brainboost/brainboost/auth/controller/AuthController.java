package com.brainboost.brainboost.auth.controller;

import com.brainboost.brainboost.auth.dto.input.*;
import com.brainboost.brainboost.auth.dto.output.LoginResponseDTO;
import com.brainboost.brainboost.auth.service.UserService;
import com.brainboost.brainboost.basicController.Controller;
import com.brainboost.brainboost.dto.output.BasicResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController extends Controller {

    private final UserService userService;

    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }
    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody @Valid LoginInputDTO dto, HttpServletRequest request) throws Exception {
        return updateHttpStatus(userService.login(dto,authenticationManager,request));
    }

    @PostMapping("/add_user")
    @PreAuthorize("hasAuthority('ADD_USER_PERMISSION')")
    public BasicResponseDTO addNewUser(@RequestBody @Valid AddUserInputDTO dto, HttpServletRequest request) {
        return updateHttpStatus(userService.addUser(dto,request));
    }

    @PatchMapping("/begin_reset_password")
    public BasicResponseDTO beginResetPassword(@RequestBody @Valid BeginResetPasswordDTO dto) throws Exception {
        return updateHttpStatus(userService.beginResetPassword(dto));
    }

    @PatchMapping("/verify_otp")
    public BasicResponseDTO verifyOtp(@RequestParam String code, @RequestBody @Valid ResetCodeInputDTO dto) throws Exception {
        return updateHttpStatus(userService.verifyResetCode(dto,code));
    }

    @PatchMapping("/reset_password")
    public BasicResponseDTO resetPassword(@RequestParam String code,@RequestBody @Valid ChangePasswordDTO dto) throws Exception {
        return updateHttpStatus(userService.resetPassword(dto,code));
    }


    @PatchMapping("/logout")
    public BasicResponseDTO logout(HttpServletRequest request) throws BadRequestException {
        return updateHttpStatus(userService.logout(request));
    }

}
