package com.brainboost.brainboost.dto.enums;

import java.util.Arrays;
import java.util.List;

public enum UserType {

    ADMIN("ADMIN"),
    AGENT("AGENT");

    private String description;

    UserType(String description){
        this.description = description;
    }

    public static List<UserType> getUserTypes(){
        return Arrays.asList(UserType.values());
    }
}