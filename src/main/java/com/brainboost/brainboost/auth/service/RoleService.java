package com.brainboost.brainboost.auth.service;


import com.brainboost.brainboost.auth.dto.enums.RoleType;
import com.brainboost.brainboost.auth.dto.input.CreateRoleInputDTO;
import com.brainboost.brainboost.auth.dto.input.PermissionDTO;
import com.brainboost.brainboost.auth.dto.input.UpdateRoleInputDTO;
import com.brainboost.brainboost.auth.entity.Permission;
import com.brainboost.brainboost.auth.entity.Roles;
import com.brainboost.brainboost.auth.repository.PermissionRepository;
import com.brainboost.brainboost.auth.repository.RolesRepository;
import com.brainboost.brainboost.config.TokenProvider;
import com.brainboost.brainboost.dto.enums.Status;
import com.brainboost.brainboost.dto.output.BasicResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RolesRepository rolesRepository;

    private final PermissionRepository permissionRepository;


    private final TokenProvider tokenProvider;


    public BasicResponseDTO createRole(CreateRoleInputDTO dto, HttpServletRequest request) throws BadRequestException {

      try{
          Optional<Roles> rolesOptional = rolesRepository.findByName(dto.getName());

          if(rolesOptional.isPresent()) {
              return new BasicResponseDTO(Status.BAD_REQUEST, "Role already exist");
          }
          Roles roles = new Roles();
          roles.setName(dto.getName().toUpperCase());
          roles.setPermissions(getAllPermission(dto.getPermissions()));
          roles.setCreatedBy(tokenProvider.getFirstname() + " " + tokenProvider.getLastname());
          roles.setRoleType(RoleType.CUSTOM);

          rolesRepository.save(roles);

          return new BasicResponseDTO(Status.CREATED, roles);
      } catch (Exception ex) {
          throw new BadRequestException(ex.getMessage());
      }
    }

    public BasicResponseDTO updateRole(UpdateRoleInputDTO dto, Long id, HttpServletRequest request) throws BadRequestException {

      try{
          Optional<Roles> rolesOptional = rolesRepository.findById(id);

          if(!rolesOptional.isPresent()) {
              return new BasicResponseDTO(Status.BAD_REQUEST, "Role doesn't exist");
          }
          Roles roles = updateRoleObject(dto,rolesOptional.get());
          roles.setLastUpdatedTime(new Date());
          rolesRepository.save(roles);

          return new BasicResponseDTO(Status.CREATED, roles);
      }catch (Exception ex) {

          throw new BadRequestException(ex.getMessage());
      }
    }

    public BasicResponseDTO removePermissionFromRole(PermissionDTO dto, Long id, HttpServletRequest request) throws BadRequestException {
        try{
            Optional<Roles> rolesOptional = rolesRepository.findById(id);

            if(!rolesOptional.isPresent()) {
                return new BasicResponseDTO(Status.BAD_REQUEST, "Role doesn't exist");
            }
            Permission permission = permissionRepository.findByCode(dto.getPermission());
            if(permission.equals(null)) {
                return new BasicResponseDTO(Status.BAD_REQUEST, "Permission doesn't exist");
            }
            Roles roles = rolesOptional.get();
            roles.getPermissions().remove(permission);
            roles.setLastUpdatedTime(new Date());
            rolesRepository.save(roles);

            return new BasicResponseDTO(Status.CREATED, roles);
        }catch (Exception ex) {

            throw new BadRequestException(ex.getMessage());
        }
    }

    public BasicResponseDTO findRoleByName(String name){

        Optional<Roles> rolesOptional = rolesRepository.findByName(name);

        if(!rolesOptional.isPresent()) {
            return new BasicResponseDTO(Status.BAD_REQUEST, "Role doesn't exist");
        }

        return new BasicResponseDTO(Status.SUCCESS, rolesOptional.get());
    }

    public BasicResponseDTO findRoleById(Long id){

        Optional<Roles> rolesOptional = rolesRepository.findById(id);

        if(!rolesOptional.isPresent()) {
            return new BasicResponseDTO(Status.BAD_REQUEST, "Role doesn't exist");
        }

        return new BasicResponseDTO(Status.SUCCESS, rolesOptional.get());
    }

    public BasicResponseDTO getAllRoles(int pageNo, int pageSize) {

        Pageable pageable = PageRequest.of(pageNo,pageSize);

        List<Roles> roles = rolesRepository.findAll(pageable).toList();

        return new BasicResponseDTO(Status.SUCCESS,roles);

    }

    public BasicResponseDTO searchRoles(String pattern, int pageNo, int pageSize) {
        try{

            Pageable pageable = PageRequest.of(pageNo,pageSize);

            List<Roles> roles = rolesRepository.findUsingPattern(pattern,pageable).toList();

            return new BasicResponseDTO(Status.SUCCESS, roles);
        }catch (Exception ex) {
            return new BasicResponseDTO(Status.BAD_REQUEST, ex.getMessage());
        }
    }
    public BasicResponseDTO fetchRoles(){
        List<Roles> allRoles = rolesRepository.findAll();
        log.info("{}",allRoles);
        return new BasicResponseDTO(Status.SUCCESS, allRoles);
    }

    public BasicResponseDTO deleteRole(Long id, HttpServletRequest request) throws BadRequestException {
        Optional<Roles> rolesOptional = rolesRepository.findById(id);

        try{
            if(!rolesOptional.isPresent()) {
                return new BasicResponseDTO(Status.BAD_REQUEST, "Role doesn't exist");
            }
            Roles roles = rolesOptional.get();
            rolesRepository.delete(roles);

            return new BasicResponseDTO(Status.NO_CONTENT);
        }catch (Exception ex) {

            throw new BadRequestException(ex.getMessage());
        }
    }
    private Collection<Permission> getAllPermission(List<String> permissionsList) {
        List<Permission> permissions = new ArrayList<>();
        permissionsList.forEach(name -> {
            Permission permission = permissionRepository.findByCode(name.toUpperCase());
            permissions.add(permission);
        });
        return permissions;
    }

    private Roles updateRoleObject(UpdateRoleInputDTO dto, Roles role) {
        if(Objects.nonNull(dto.getName())){
            role.setName(dto.getName());
        }
        if(Objects.nonNull(dto.getPermissions())) {
            role.getPermissions().addAll(getAllPermission(dto.getPermissions()));
        }

        return role;
    }



}
