package com.brainboost.brainboost.auth.service;

import com.brainboost.brainboost.auth.entity.Permission;
import com.brainboost.brainboost.auth.repository.PermissionRepository;
import com.brainboost.brainboost.config.TokenProvider;
import com.brainboost.brainboost.dto.enums.Status;
import com.brainboost.brainboost.dto.output.BasicResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionService {


    private final TokenProvider tokenProvider;
    private final PermissionRepository permissionRepository;



    public BasicResponseDTO fetchPermissions() {
        List<Permission> permissions = permissionRepository.findAll();
        return new BasicResponseDTO(Status.SUCCESS,permissions);
    }



}
