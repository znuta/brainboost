package com.brainboost.brainboost.auth.dto.input;

import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;



@GroupSequence({
        LoginInputDTO.First.class,
        LoginInputDTO.Second.class,
        LoginInputDTO.Third.class,
        LoginInputDTO.class
})
@Data
public class LoginInputDTO {

    @NotNull(message = "user not found", groups = First.class)
    private String email;

    @NotNull(message = "{user.password.notEmpty}", groups = First.class)
    @Size(min = 8, max = 150, message = "{user.password.sizeError}", groups = Second.class)
    private String password;

    interface First {
    }

    interface Second {
    }

    interface Third {
    }
}
