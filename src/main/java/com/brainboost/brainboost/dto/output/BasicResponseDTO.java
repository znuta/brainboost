package com.brainboost.brainboost.dto.output;

import com.brainboost.brainboost.dto.enums.Status;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BasicResponseDTO extends StandardResponseDTO {

    @JsonProperty
    private Object data;

    @JsonProperty
    private long pendingInvite;

    public BasicResponseDTO() {}

    public BasicResponseDTO(Status status) {
        super(status);
    }

    public BasicResponseDTO(Status status, Object data) {
        this.status = status;
        this.data = data;
    }

    public BasicResponseDTO(Status status, Object data, long pendingInvite) {
        this.status = status;
        this.data = data;
        this.pendingInvite = pendingInvite;
    }
}
