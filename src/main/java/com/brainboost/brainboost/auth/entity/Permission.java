package com.brainboost.brainboost.auth.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Where;



@Entity
@Table(name = "dashboard_permission")
@Where(clause ="del_Flag='N'" )
@Data
public class Permission extends AbstractEntity {

    private String description;

    @Column(unique = true)
    private String code;
    private String category;
}
