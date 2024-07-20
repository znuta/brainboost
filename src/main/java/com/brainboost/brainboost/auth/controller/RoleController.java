package com.brainboost.brainboost.auth.controller;

import com.brainboost.brainboost.auth.dto.input.CreateRoleInputDTO;
import com.brainboost.brainboost.auth.dto.input.PermissionDTO;
import com.brainboost.brainboost.auth.dto.input.UpdateRoleInputDTO;
import com.brainboost.brainboost.auth.service.RoleService;
import com.brainboost.brainboost.basicController.Controller;
import com.brainboost.brainboost.dto.output.BasicResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/role")
@Slf4j
@RequiredArgsConstructor
public class RoleController extends Controller {

    private final RoleService roleService;


    @PostMapping("/create")
    @PreAuthorize("hasAuthority('USER_MANAGEMENT')")
    public BasicResponseDTO createRole(@RequestBody @Valid CreateRoleInputDTO dto, HttpServletRequest request) throws Exception {
        return updateHttpStatus(roleService.createRole(dto, request));
    }

    @PutMapping("/{roleId}/update")
    @PreAuthorize("hasAuthority('USER_MANAGEMENT')")
    public BasicResponseDTO updateRole(@RequestBody UpdateRoleInputDTO dto, @PathVariable("roleId") Long id, HttpServletRequest request) throws Exception {
        return updateHttpStatus(roleService.updateRole(dto,id,request));
    }

    @PatchMapping("/{roleId}/remove_permission")
    @PreAuthorize("hasAuthority('USER_MANAGEMENT')")
    public BasicResponseDTO removePermission(@RequestBody PermissionDTO dto, @PathVariable("roleId") Long id, HttpServletRequest request) throws Exception {
        return updateHttpStatus(roleService.removePermissionFromRole(dto,id,request));
    }

    @GetMapping()
    @PreAuthorize("hasAuthority('USER_MANAGEMENT')")
    public BasicResponseDTO findRoleByName(@RequestParam String name) throws Exception {
        return updateHttpStatus(roleService.findRoleByName(name));
    }

    @GetMapping("/{roleId}")
    @PreAuthorize("hasAuthority('USER_MANAGEMENT')")
    public BasicResponseDTO findRoleById(@PathVariable("roleId") Long roleId) throws Exception {
        return updateHttpStatus(roleService.findRoleById(roleId));
    }

    @DeleteMapping("/{roleId}/delete")
    @PreAuthorize("hasAuthority('USER_MANAGEMENT')")
    public BasicResponseDTO deleteRole(@PathVariable("userId") Long id, HttpServletRequest request ) throws BadRequestException {
        return updateHttpStatus(roleService.deleteRole(id,request));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('USER_MANAGEMENT')")
    public BasicResponseDTO getAllRoles(@RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize) throws Exception {
        return updateHttpStatus(roleService.getAllRoles(pageNo,pageSize));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('USER_MANAGEMENT')")
    public BasicResponseDTO searchRoles(@RequestParam("pattern") String pattern,@RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize) throws Exception {
        return updateHttpStatus(roleService.searchRoles(pattern,pageNo,pageSize));
    }

}
