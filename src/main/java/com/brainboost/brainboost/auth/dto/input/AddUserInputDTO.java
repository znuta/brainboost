package com.brainboost.brainboost.auth.dto.input;

import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@GroupSequence({
        AddUserInputDTO.First.class,
        AddUserInputDTO.Second.class,
        AddUserInputDTO.Third.class,
        AddUserInputDTO.class
})
@Data
public class AddUserInputDTO {

    @NotBlank(message = "{user.firstname.notEmpty}", groups = First.class)
    @Size(min = 3, max = 50, message = "{user.firstname.sizeError}", groups = Second.class)
    private String firstname;

    @NotBlank(message = "{user.lastname.notEmpty}", groups = First.class)
    @Size(min = 3, max = 50, message = "{user.lastname.sizeError}", groups = Second.class)
    private String lastname;

    @NotBlank(message = "{user.email.notEmpty}", groups = First.class)
    @Email(message = "{user.email.validEmail}", groups = Second.class)
    private String email;

    private String phoneNumber;

    @NotBlank(message = "{user.role.notEmpty}", groups = First.class)
    private String role;

    interface First {
    }

    interface Second {
    }

    interface Third {
    }

}
