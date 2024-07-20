package com.brainboost.brainboost.auth.entity;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;

public interface SerializableEntity<T> {
    String serialize() throws JsonProcessingException;

    void deserialize(String data) throws JsonParseException, JsonMappingException, IOException;

}
