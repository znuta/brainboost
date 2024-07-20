package com.brainboost.brainboost.basicController;


import com.brainboost.brainboost.dto.output.StandardResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;


import static com.brainboost.brainboost.dto.enums.Status.*;

@Slf4j
public class Controller {

    @Autowired
    private HttpServletResponse response;

    @Autowired
    private HttpServletRequest request;

    protected <T extends StandardResponseDTO> T updateHttpStatus(T responseDTO) {
        switch (responseDTO.getStatus()) {
            case SUCCESS:
                response.setStatus(HttpStatus.OK.value());
                break;
            case CREATED:
                response.setStatus(HttpStatus.CREATED.value());
                break;
            case INTERNAL_ERROR:
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                break;
            case NOT_FOUND:
                response.setStatus(HttpStatus.NOT_FOUND.value());
                break;
            case NO_CONTENT:
                response.setStatus(HttpStatus.NO_CONTENT.value());
                break;
            case FORBIDDEN:
                response.setStatus(HttpStatus.FORBIDDEN.value());
                break;
            default:
                response.setStatus(HttpStatus.BAD_REQUEST.value());
        }

        log.info("request to endpoint: " + request.getRequestURI());
        try {
            String response = new ObjectMapper().writeValueAsString(responseDTO);
//            log.info("returning: " + response);

        } catch (JsonProcessingException e) {
            log.error("couldn't echo response");
        }

        return responseDTO;
    }
}
