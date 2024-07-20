package com.brainboost.brainboost.auth.dto.input;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import jakarta.validation.GroupSequence;

import java.util.List;

@GroupSequence({
        CreateRoleInputDTO.First.class,
        CreateRoleInputDTO.Second.class,
        CreateRoleInputDTO.Third.class,
        CreateRoleInputDTO.class
})
@Data
public class CreateRoleInputDTO {

    @NotBlank(message = "{role.name.notEmpty}", groups = First.class)
    private String name;

    private List<String> permissions;



    interface First {
    }

    interface Second {
    }

    interface Third {
    }
}
