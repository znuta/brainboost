package com.brainboost.brainboost.auth.entity;

import com.brainboost.brainboost.auth.dto.enums.RoleType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Where;

import java.util.Collection;
import java.util.Date;

@Entity
@Table(name = "dashboard_role")
@Where(clause ="del_Flag='N'" )
@Data
public class Roles extends AbstractEntity {

    @Column(unique = true)
    private String name;

    private String description;

    @ManyToMany(fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    @JoinTable(name = "role_dashboard_permission", joinColumns =
    @JoinColumn(name = "role_id", referencedColumnName = "id"), inverseJoinColumns =
    @JoinColumn(name = "permission_id", referencedColumnName = "id") )
    private Collection<Permission> permissions;

    private Date lastUpdatedTime;

    private long teamMembers = 0L;

    private RoleType roleType;

    private String createdBy;
}
