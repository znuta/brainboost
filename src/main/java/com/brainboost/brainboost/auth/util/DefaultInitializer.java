package com.brainboost.brainboost.auth.util;


import com.brainboost.brainboost.auth.dto.enums.Category;
import com.brainboost.brainboost.auth.dto.enums.PermissionEnum;
import com.brainboost.brainboost.auth.dto.enums.RoleEnum;
import com.brainboost.brainboost.auth.entity.AppUser;
import com.brainboost.brainboost.auth.entity.Permission;
import com.brainboost.brainboost.auth.entity.Roles;
import com.brainboost.brainboost.auth.repository.PermissionRepository;
import com.brainboost.brainboost.auth.repository.RolesRepository;
import com.brainboost.brainboost.auth.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class DefaultInitializer implements
        ApplicationListener<ContextRefreshedEvent> {

    boolean alreadySetup = false;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RolesRepository rolesRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Value("${super.admin.firstname}")
    private String firstName;

    @Value("${super.admin.lastname}")
    private String lastName;

    @Value("${super.admin.email}")
    private String email;

    @Value("${super.admin.username}")
    private String userName;

    @Value("${super.admin.password}")
    private String password;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    @SneakyThrows
    public void onApplicationEvent(ContextRefreshedEvent event) {

        Optional<AppUser> userOptional = userRepository.findByEmail(email);
        if(userOptional.isPresent()){
            return;
        }

        AppUser user = new AppUser();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUserName(email);
        user.setLastLoginDate(new Date());
        user.setActive(true);
        user.setPassword(passwordEncoder.encode(password));
        user.setActive(true);
        user.setStatus("A");

        List<Permission> permissions = new ArrayList<>();

        permissions.addAll(createUserPermission());
        permissions.addAll(createCoursePermission());

        permissionRepository.saveAll(permissions);

        createAdminRole();

        Optional<Roles> roles = rolesRepository.findByName(RoleEnum.ADMIN.name());
        log.info("{}",roles.get().getPermissions().size());
        user.setRole(roles.get());
        roles.get().setTeamMembers(1);
        rolesRepository.save(roles.get());
        log.info("size after setting {}",user.getRole().getPermissions().size());

        userRepository.save(user);
        alreadySetup = true;

    }

    @Transactional
    List<Permission> createUserPermission() {

        List<Permission> userPermission = new ArrayList<>();

        if(!permissionRepository.findAllByCategory(Category.USER.name()).isEmpty()){
            return userPermission;
        }

        userPermission.add(createPermissionObject(Category.USER, PermissionEnum.ADD_USER_PERMISSION));
        userPermission.add(createPermissionObject(Category.USER, PermissionEnum.VIEW_USER_PERMISSION));
        userPermission.add(createPermissionObject(Category.USER, PermissionEnum.DELETE_USER_PERMISSION));
        userPermission.add(createPermissionObject(Category.USER, PermissionEnum.EDIT_USER_PERMISSION));
        userPermission.add(createPermissionObject(Category.USER, PermissionEnum.DELETE_TEACHER_PERMISSION));
        userPermission.add(createPermissionObject(Category.USER, PermissionEnum.VIEW_STUDENT_PERMISSION));
        userPermission.add(createPermissionObject(Category.USER, PermissionEnum.VIEW_TEACHER_PERMISSION));
        userPermission.add(createPermissionObject(Category.USER, PermissionEnum.EDIT_STUDENT_PERMISSION));
        return userPermission;

    }

    @Transactional
    List<Permission> createCoursePermission() {

        List<Permission> userPermission = new ArrayList<>();

        if(!permissionRepository.findAllByCategory(Category.COURSE.name()).isEmpty()){
            return userPermission;
        }

        userPermission.add(createPermissionObject(Category.COURSE, PermissionEnum.ADD_COURSE_PERMISSION));
        userPermission.add(createPermissionObject(Category.COURSE, PermissionEnum.EDIT_COURSE_PERMISSION));
        userPermission.add(createPermissionObject(Category.COURSE, PermissionEnum.DELETE_COURSE_PERMISSION));
        userPermission.add(createPermissionObject(Category.COURSE, PermissionEnum.VIEW_COURSE_PERMISSION));
        return userPermission;

    }

    private static Permission createPermissionObject(Category category, PermissionEnum permissionEnum) {
        Permission permission = new Permission();
        permission.setCategory(category.name());
        permission.setCode(permissionEnum.name());
        return permission;
    }

    @Transactional
    void createAdminRole(){
        Roles superAdmin = new Roles();
        if (checkIFRoleAlreadyExist(RoleEnum.ADMIN)) return;
        superAdmin.setName(RoleEnum.ADMIN.name());
        superAdmin.setPermissions(getAdminPermission());
        log.info("{}",getAdminPermission());
        rolesRepository.save(superAdmin);
    }

    private boolean checkIFRoleAlreadyExist(RoleEnum engineers) {
        Optional<Roles> roles = rolesRepository.findByName(engineers.name());
        if(roles.isPresent()){
            return true;
        }
        return false;
    }

    private List<Permission> getAdminPermission() {
        List<Permission> superAdminPermission = new ArrayList<>();
        superAdminPermission.addAll(permissionRepository.findAll());
        return superAdminPermission;
    }

    private List<Permission> fetchPermissionByCategory(Category category) {
        return permissionRepository.findAllByCategory(category.name());
    }

    private Permission findPermission(PermissionEnum permissionEnum) {
        return permissionRepository.findByCode(permissionEnum.name());
    }









}
