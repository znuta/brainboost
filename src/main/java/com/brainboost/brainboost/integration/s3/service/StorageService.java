package com.brainboost.brainboost.integration.s3.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Date;

@Service
@Slf4j
public class StorageService {

    @Value("${aws.s3.bucket-name}")
    private String bucketName;


    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private S3Presigner s3Presigner;

    public String generatePresignedUrl(String fileKey) {
        try {
            // Set the expiration time for the pre-signed URL (e.g., 1 hour)
            Duration expiration = Duration.ofHours(1);

            // Generate a pre-signed URL for the client to upload the file
            Date expirationDate = new Date(System.currentTimeMillis() + expiration.toMillis());

            GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, fileKey)
                    .withMethod(HttpMethod.PUT) // Specify the HTTP method for uploading
                    .withExpiration(expirationDate);

            URL presignedUrl = s3Client.generatePresignedUrl(generatePresignedUrlRequest);

            // Return the pre-signed URL as a URI
            return presignedUrl.toString();

        } catch (Exception e) {
            // Handle exceptions appropriately
            throw new RuntimeException("Error generating pre-signed URL for upload", e);
        }
    }
    public String generateGetPresignedUrl(String fileKey) {
        try {

            // Set the expiration time for the pre-signed URL (e.g., 1 hour)
            Duration expiration = Duration.ofHours(1);
            log.info(fileKey);

            // Generate a pre-signed URL for the client to upload or download the file
            software.amazon.awssdk.services.s3.model.GetObjectRequest getObjectRequest = software.amazon.awssdk.services.s3.model.GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(expiration)
                    .getObjectRequest(getObjectRequest)
                    .build();

            // Return the pre-signed URL as a URI
            return s3Presigner.presignGetObject(presignRequest).url().toString();

        } catch (Exception e) {
            log.info("___Bucket___", e);
            throw new RuntimeException("Error generating pre-signed URL", e);
        }
    }

    public String uploadFile(MultipartFile file) {
        File fileObj = convertMultiPartFileToFile(file);
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        s3Client.putObject(new PutObjectRequest(bucketName, bucketName+fileName, fileObj).withCannedAcl(CannedAccessControlList.PublicRead));
        fileObj.delete();
        URL s3Url = s3Client.getUrl(bucketName, bucketName+fileName);
        log.info("S3 url is " + s3Url.toExternalForm());
        return s3Url.toExternalForm();
    }


    public byte[] downloadFile(String fileName) {
        S3Object s3Object = s3Client.getObject(bucketName, bucketName+fileName);
        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        try {
            byte[] content = IOUtils.toByteArray(inputStream);
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public String deleteFile(String fileName) {
        s3Client.deleteObject(bucketName, bucketName+fileName);
        return fileName + " removed ...";
    }


    private File convertMultiPartFileToFile(MultipartFile file) {
        File convertedFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            log.error("Error converting multipartFile to file", e);
        }
        return convertedFile;
    }

    public boolean isFileValid(String fileName) {
        if(fileName!=null && !StringUtils.isEmpty(fileName)){
            int lastIndexOfDot = fileName.lastIndexOf('.');
            if(lastIndexOfDot != -1) {
                String fileExtension = fileName.substring(lastIndexOfDot, fileName.length());
                log.info("the file extension {}", fileExtension);
                if (fileExtension.equals(".png") || fileExtension.equals(".jpg") || fileExtension.equals(".pdf")) {
                    return true;
                }
            }
        }
        return false;
    }
}
