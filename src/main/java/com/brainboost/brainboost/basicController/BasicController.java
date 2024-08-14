package com.brainboost.brainboost.basicController;

import com.brainboost.brainboost.exception.BadRequestException;
import com.brainboost.brainboost.integration.s3.DTO.PreSignDto;
import com.brainboost.brainboost.integration.s3.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/public")
@Slf4j
@RequiredArgsConstructor
public class BasicController extends Controller {

    private final StorageService storageService;


    @PostMapping("/presign-url/upload-file")
    public ResponseEntity<String> getUploadPresignURL(@RequestBody PreSignDto fileKey){
        try {
            String presignUrl = storageService.generatePresignedUrl(fileKey.getFileKey());
            return ResponseEntity.ok(presignUrl);
        } catch (BadRequestException ex) {

            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/presign-url/view-file")
    public ResponseEntity<String> getViewPresignURL(@RequestBody PreSignDto fileKey){
        try {
            String presignUrl = storageService.generateGetPresignedUrl(fileKey.getFileKey());
            return ResponseEntity.ok(presignUrl);
        } catch (BadRequestException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }


}
