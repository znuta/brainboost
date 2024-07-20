package com.brainboost.brainboost.auth.dto.input;

import lombok.Data;

import java.util.List;


@Data
public class UpdateRoleInputDTO {

    private String name;

    private List<String> permissions;

}
