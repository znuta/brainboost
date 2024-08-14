package com.brainboost.brainboost.integration.s3.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PreSignDto {

    private String fileKey;

}
