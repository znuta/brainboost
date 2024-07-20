package com.brainboost.brainboost.auth.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Where;

import java.util.Date;

@Data
@Entity
@Table(name = "app_user")
@Where(clause ="del_Flag='N'" )
public class AppUser extends AbstractEntity {

    @Column(unique = true)
    protected String userName;
    @Column(name="last_name")
    private String lastName;

    @Column(name="first_name")
    private String firstName;

    @Column(name="email")
    private String email;
    protected String phoneNumber;
    @JsonIgnore
    protected String password;

    protected String status;
    protected boolean active;

    @JsonIgnore
    protected Date lastLoginDate;

    protected Date lastUpdated;

    protected boolean pendingPasswordReset;

    @JsonIgnore
    protected int otpCode;

    @Column(name="role")
    @ManyToOne
    private Roles role;
}
