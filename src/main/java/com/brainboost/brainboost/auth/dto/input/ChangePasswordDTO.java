package com.brainboost.brainboost.auth.dto.input;

import lombok.Data;

import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@GroupSequence({
        ChangePasswordDTO.First.class,
        ChangePasswordDTO.Second.class,
        ChangePasswordDTO.Third.class,
        ChangePasswordDTO.class
})
@Data
public class  ChangePasswordDTO {

    @NotNull(message = "{user.newPassword.notEmpty}", groups = First.class)
    @Size(min = 8, max = 20, message = "{user.newPassword.sizeError}", groups = Second.class)
    private String newPassword;

    @NotNull(message = "{user.confirmPassword.notEmpty}", groups = First.class)
    private String confirmPassword;



    interface First {
    }

    interface Second {
    }

    interface Third {
    }
}
