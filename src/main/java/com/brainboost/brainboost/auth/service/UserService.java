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
import com.brainboost.brainboost.dto.enums.UserType;
import com.brainboost.brainboost.dto.output.BasicResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    private final TokenProvider tokenProvider;

    private final RolesRepository rolesRepository;

    private final PasswordRequestCacheHandler cacheHandler;


    private BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder(12);

    @Value("${host.url}")
    private String hostUrl;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        return new org.springframework.security.core.userdetails.User(username, user.getPassword(), getAuthority(user));
    }

    private Collection<SimpleGrantedAuthority> getAuthority(AppUser user) {
        Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
        List<String> codes = user.getRole().getPermissions().stream()
                .map(permission -> permission.getCode())
                .collect(Collectors.toList());
        for (String permission : codes) {
            authorities.add(new SimpleGrantedAuthority(permission));
        }
        return authorities;
    }

    public LoginResponseDTO login(LoginInputDTO loginInputDTO, AuthenticationManager authenticationManager, HttpServletRequest request) throws Exception {

        log.info("{}", loginInputDTO.getUsername());
        try{
            Optional<AppUser> user = userRepository.findByUserName(loginInputDTO.getUsername());
            log.info("{}", user.get());

            if(!user.isPresent()){
                return new LoginResponseDTO(Status.NOT_FOUND, "User doesn't exist");
            }

            if(!new BCryptPasswordEncoder(12).matches(loginInputDTO.getPassword(), user.get().getPassword())){
                return new LoginResponseDTO(Status.BAD_REQUEST, "Incorrect Password");
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginInputDTO.getUsername(),
                            loginInputDTO.getPassword(),getAuthority(user.get())
                    )
            );

            log.info(String.valueOf(authentication));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            AppUser userObject = user.get();

            String token = tokenProvider.generateJWTToken(authentication);

            userObject.setLastLoginDate(new Date());
            userRepository.save(userObject);
            return new LoginResponseDTO(Status.SUCCESS,token,userObject);
        } catch (Exception ex){

            log.info("message {}", ex.getMessage());
            throw new BadRequestException(ex.getMessage());
        }
    }

    public BasicResponseDTO logout(HttpServletRequest request) throws BadRequestException {
       try{
           Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
           if(authentication != null){
               authentication.setAuthenticated(false);
           }

           return new BasicResponseDTO(Status.SUCCESS);
       }catch (Exception ex){

           throw new BadRequestException(ex.getMessage());
       }
    }

    @SneakyThrows
    public BasicResponseDTO beginResetPassword(BeginResetPasswordDTO dto) {
        Optional<AppUser> userOptional = userRepository.findByEmail(dto.getEmail());

        if(!userOptional.isPresent()) {
            return new BasicResponseDTO(Status.NOT_FOUND, "User doesn't exist");
        }
        AppUser user = userOptional.get();
        int otp = GenericUtil.generateOtpCode();
        user.setOtpCode(otp);
        userRepository.save(user);
        String code = GenericUtil.generateAlphaNumeric(12);
        cacheHandler.addToCache(code,dto.getEmail());
        //TODO implement email or sms system to send message
        return new BasicResponseDTO(Status.SUCCESS);
    }


    public BasicResponseDTO verifyResetCode(ResetCodeInputDTO dto, String code) {

        String email = cacheHandler.getFromCache(code);

        Optional<AppUser> userOptional = userRepository.findByEmail(email);

        if(!userOptional.isPresent()) {
            return new BasicResponseDTO(Status.NOT_FOUND, "User doesn't exist");
        }
        AppUser user = userOptional.get();

        if(dto.getOtpCode() != user.getOtpCode()) {
            return new BasicResponseDTO(Status.BAD_REQUEST, "Invalid OTP Code");
        }

        return new BasicResponseDTO(Status.SUCCESS, code);
    }
    public BasicResponseDTO resetPassword(ChangePasswordDTO dto, String code){

        String email = cacheHandler.getFromCache(code);

        Optional<AppUser> userOptional = userRepository.findByEmail(email);

        if(!userOptional.isPresent()) {
            return new BasicResponseDTO(Status.NOT_FOUND, "User doesn't exist");
        }

        AppUser user = userOptional.get();

        if(!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            return new BasicResponseDTO(Status.BAD_REQUEST,"Password doesn't match");
        }

        user.setPassword(bcryptEncoder.encode(dto.getNewPassword()));
        user.setPendingPasswordReset(Boolean.FALSE);
        user.setActive(Boolean.TRUE);
        user.setLastUpdated(new Date());
        userRepository.save(user);
        cacheHandler.removeCache(code);
        return new BasicResponseDTO(Status.SUCCESS,user);
    }

    @SneakyThrows
    public BasicResponseDTO addUser(AddUserInputDTO dto, HttpServletRequest request) {

        try{
            Optional<AppUser> userOptional = userRepository.findByEmail(dto.getEmail());
            if(userOptional.isPresent()){
                return new BasicResponseDTO(Status.BAD_REQUEST,"A user with this email already exist");
            }

            Optional<AppUser> userName = userRepository.findByUserName(dto.getFirstname());

            AppUser user = new AppUser();
            user.setFirstName(dto.getFirstname());
            user.setLastName(dto.getLastname());

            user.setEmail(dto.getEmail());
            user.setPhoneNumber(dto.getPhoneNumber());
            user.setPendingPasswordReset(Boolean.TRUE);
            user.setRole(getRole(dto.getRole()));
            user.setLastLoginDate(new Date());

            user.setStatus("A");
            String password = GenericUtil.generateAlphaNumeric(8);
            user.setPassword(bcryptEncoder.encode(password));
            String code = GenericUtil.generateAlphaNumeric(12);
            cacheHandler.addToCache(code,dto.getEmail());


            userRepository.save(user);

            return new BasicResponseDTO(Status.SUCCESS,user);
        }catch (Exception ex) {
            return new BasicResponseDTO(Status.BAD_REQUEST, ex.getMessage());
        }
    }



    private Roles getRole(String role) {
        Roles roles = rolesRepository.findByName(role).get();
        roles.setTeamMembers(roles.getTeamMembers() + 1);
        rolesRepository.save(roles);
        return roles;
    }

    public BasicResponseDTO findUser(String firstName, String lastName, String email) {

        List<AppUser> allUsers = userRepository.findByFirstNameOrLastName(firstName,lastName);

        if(!allUsers.isEmpty()) {
            return new BasicResponseDTO(Status.SUCCESS, allUsers);
        }

        Optional<AppUser> userOptional = userRepository.findByFirstNameOrLastNameOrEmail(firstName,lastName,email);

        if(!userOptional.isPresent()) {
            return new BasicResponseDTO(Status.BAD_REQUEST, "User doesn't exist");
        }

        return new BasicResponseDTO(Status.SUCCESS,userOptional.get());
    }

    public BasicResponseDTO findUserById (Long id) {

        Optional<AppUser> userOptional = userRepository.findById(id);

        if(!userOptional.isPresent()) {
            return new BasicResponseDTO(Status.BAD_REQUEST, "User doesn't exist");
        }

        return new BasicResponseDTO(Status.SUCCESS, userOptional.get());
    }


    public BasicResponseDTO deleteUser(Long id, HttpServletRequest request) throws BadRequestException {
        Optional<AppUser> userOptional = userRepository.findById(id);

        try{
           if(!userOptional.isPresent()) {
               return new BasicResponseDTO(Status.BAD_REQUEST, "User doesn't exist");
           }
            AppUser user = userOptional.get();

           Roles roles = rolesRepository.findByName(user.getRole().getName()).get();
           roles.setTeamMembers(roles.getTeamMembers() - 1);
           rolesRepository.save(roles);
           userRepository.delete(user);

           return new BasicResponseDTO(Status.NO_CONTENT);
       }catch (Exception ex) {

            throw new BadRequestException(ex.getMessage());
       }
    }

    public BasicResponseDTO fetchUsers() {
        List<AppUser> users = userRepository.findAll();
        return  new BasicResponseDTO(Status.SUCCESS,users);
    }

    public BasicResponseDTO getAllUsers(int pageNo, int pageSize) {

        Pageable pageable = PageRequest.of(pageNo,pageSize);

        List<AppUser> users = userRepository.findAll(pageable).toList();

        long count = users.stream().filter(user -> !user.isActive()).count();

        if(users.isEmpty()) {
            users = userRepository.findAll();
            count = users.stream().filter(user -> !user.isActive()).count();
        }

        return new BasicResponseDTO(Status.SUCCESS,users,count);

    }



    public BasicResponseDTO fetchUserTypes(){
        List<UserType> userTypeList = UserType.getUserTypes();
        return new BasicResponseDTO(Status.SUCCESS,userTypeList);
    }







}
