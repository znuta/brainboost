package com.brainboost.brainboost.auth.dto.input;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import jakarta.validation.GroupSequence;

@GroupSequence({
        BeginResetPasswordDTO.First.class,
        BeginResetPasswordDTO.Second.class,
        BeginResetPasswordDTO.Third.class,
        BeginResetPasswordDTO.class
})
@Data
public class BeginResetPasswordDTO {

    @Email(message = "{user.email.validEmail}", groups = Second.class)
    @NotBlank(message = "{user.email.notEmpty}", groups = First.class)
    private String email;

    interface First {
    }

    interface Second {
    }

    interface Third {
    }
}
