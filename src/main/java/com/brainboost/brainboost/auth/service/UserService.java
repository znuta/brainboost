package com.brainboost.brainboost.auth.service;

import com.brainboost.brainboost.auth.dto.input.*;
import com.brainboost.brainboost.auth.dto.output.LoginResponseDTO;
import com.brainboost.brainboost.auth.entity.AppUser;
import com.brainboost.brainboost.auth.entity.Roles;
import com.brainboost.brainboost.auth.repository.RolesRepository;
import com.brainboost.brainboost.auth.repository.UserRepository;
import com.brainboost.brainboost.auth.util.GenericUtil;
import com.brainboost.brainboost.cache.PasswordRequestCacheHandler;
import com.brainboost.brainboost.config.TokenProvider;
import com.brainboost.brainboost.dto.enums.Status;
import com.brainboost.brainboost.dto.output.BasicResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final RolesRepository rolesRepository;
    private final PasswordRequestCacheHandler cacheHandler;
    private final PasswordEncoder passwordEncoder;



    public UserService(UserRepository userRepository, TokenProvider tokenProvider, RolesRepository rolesRepository, PasswordRequestCacheHandler cacheHandler,  PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.rolesRepository = rolesRepository;
        this.cacheHandler = cacheHandler;
        this.passwordEncoder = passwordEncoder;

    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
        return new org.springframework.security.core.userdetails.User(username, user.getPassword(), getAuthority(user));
    }

    private Collection<SimpleGrantedAuthority> getAuthority(AppUser user) {
        return user.getRole().getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getCode()))
                .collect(Collectors.toSet());
    }

    public LoginResponseDTO login(LoginInputDTO loginInputDTO, AuthenticationManager authenticationManager, HttpServletRequest request) throws Exception {
        log.info("{}", loginInputDTO.getEmail());
        Optional<AppUser> userOptional = userRepository.findByEmail(loginInputDTO.getEmail());
        if (!userOptional.isPresent()) {
            return new LoginResponseDTO(Status.NOT_FOUND, "User doesn't exist");
        }

        AppUser user = userOptional.get();
        if (!passwordEncoder.matches(loginInputDTO.getPassword(), user.getPassword())) {
            return new LoginResponseDTO(Status.BAD_REQUEST, "Incorrect Password");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginInputDTO.getEmail(), loginInputDTO.getPassword(), getAuthority(user))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = tokenProvider.generateJWTToken(authentication);
        user.setLastLoginDate(new Date());
        userRepository.save(user);

        return new LoginResponseDTO(Status.SUCCESS, token, user);
    }

    public BasicResponseDTO logout(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            authentication.setAuthenticated(false);
        }
        return new BasicResponseDTO(Status.SUCCESS);
    }

    @SneakyThrows
    public BasicResponseDTO beginResetPassword(BeginResetPasswordDTO dto) {
        Optional<AppUser> userOptional = userRepository.findByEmail(dto.getEmail());
        if (!userOptional.isPresent()) {
            return new BasicResponseDTO(Status.NOT_FOUND, "User doesn't exist");
        }
        AppUser user = userOptional.get();
        int otp = GenericUtil.generateOtpCode();
        user.setOtpCode(otp);
        userRepository.save(user);
        String code = GenericUtil.generateAlphaNumeric(12);
        cacheHandler.addToCache(code, dto.getEmail());
        return new BasicResponseDTO(Status.SUCCESS);
    }

    public BasicResponseDTO verifyResetCode(ResetCodeInputDTO dto, String code) {
        String email = cacheHandler.getFromCache(code);
        Optional<AppUser> userOptional = userRepository.findByEmail(email);
        if (!userOptional.isPresent()) {
            return new BasicResponseDTO(Status.NOT_FOUND, "User doesn't exist");
        }
        AppUser user = userOptional.get();
        if (dto.getOtpCode() != user.getOtpCode()) {
            return new BasicResponseDTO(Status.BAD_REQUEST, "Invalid OTP Code");
        }
        return new BasicResponseDTO(Status.SUCCESS, code);
    }

    public BasicResponseDTO resetPassword(ChangePasswordDTO dto, String code) {
        String email = cacheHandler.getFromCache(code);
        Optional<AppUser> userOptional = userRepository.findByEmail(email);
        if (!userOptional.isPresent()) {
            return new BasicResponseDTO(Status.NOT_FOUND, "User doesn't exist");
        }

        AppUser user = userOptional.get();
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            return new BasicResponseDTO(Status.BAD_REQUEST, "Password doesn't match");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setPendingPasswordReset(Boolean.FALSE);
        user.setActive(Boolean.TRUE);
        user.setLastUpdated(new Date());
        userRepository.save(user);
        cacheHandler.removeCache(code);
        return new BasicResponseDTO(Status.SUCCESS, user);
    }

    @SneakyThrows
    public BasicResponseDTO addUser(AddUserInputDTO dto, HttpServletRequest request) {
        Optional<AppUser> userOptional = userRepository.findByEmail(dto.getEmail());
        if (userOptional.isPresent()) {
            return new BasicResponseDTO(Status.BAD_REQUEST, "A user with this email already exist");
        }

        AppUser user = new AppUser();
        user.setFirstName(dto.getFirstname());
        user.setLastName(dto.getLastname());
        user.setEmail(dto.getEmail());
        user.setUserName(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setPendingPasswordReset(Boolean.TRUE);
        user.setRole(getRole(dto.getRole()));
        user.setLastLoginDate(new Date());
        user.setStatus("A");

        String password = GenericUtil.generateAlphaNumeric(8);
        user.setPassword(passwordEncoder.encode(password));
        String code = GenericUtil.generateAlphaNumeric(12);
        cacheHandler.addToCache(code, dto.getEmail());

        userRepository.save(user);

        return new BasicResponseDTO(Status.SUCCESS, user);
    }

    private Roles getRole(String role) {
        Roles roles = rolesRepository.findByName(role).orElseThrow(() -> new IllegalArgumentException("Role not found"));
        roles.setTeamMembers(roles.getTeamMembers() + 1);
        rolesRepository.save(roles);
        return roles;
    }

    public BasicResponseDTO findUser(String firstName, String lastName, String email) {
        List<AppUser> allUsers = userRepository.findByFirstNameOrLastName(firstName, lastName);
        if (!allUsers.isEmpty()) {
            return new BasicResponseDTO(Status.SUCCESS, allUsers);
        }
        Optional<AppUser> userOptional = userRepository.findByFirstNameOrLastNameOrEmail(firstName, lastName, email);
        if (!userOptional.isPresent()) {
            return new BasicResponseDTO(Status.BAD_REQUEST, "User doesn't exist");
        }
        return new BasicResponseDTO(Status.SUCCESS, userOptional.get());
    }

    public BasicResponseDTO findUserById(Long id) {
        Optional<AppUser> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            return new BasicResponseDTO(Status.BAD_REQUEST, "User doesn't exist");
        }
        return new BasicResponseDTO(Status.SUCCESS, userOptional.get());
    }

    public BasicResponseDTO deleteUser(Long id, HttpServletRequest request) {
        Optional<AppUser> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            return new BasicResponseDTO(Status.BAD_REQUEST, "User doesn't exist");
        }
        AppUser user = userOptional.get();
        Roles roles = rolesRepository.findByName(user.getRole().getName()).orElseThrow(() -> new IllegalArgumentException("Role not found"));
        roles.setTeamMembers(roles.getTeamMembers() - 1);
        rolesRepository.save(roles);
        userRepository.delete(user);
        return new BasicResponseDTO(Status.SUCCESS);
    }

    public BasicResponseDTO updateUser(AddUserInputDTO dto, Long id, HttpServletRequest request) {
        Optional<AppUser> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            return new BasicResponseDTO(Status.BAD_REQUEST, "User doesn't exist");
        }

        AppUser user = userOptional.get();
        user.setFirstName(dto.getFirstname());
        user.setLastName(dto.getLastname());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setRole(getRole(dto.getRole()));
        user.setStatus("A");
        user.setLastUpdated(new Date());
        userRepository.save(user);

        return new BasicResponseDTO(Status.SUCCESS);
    }
}
