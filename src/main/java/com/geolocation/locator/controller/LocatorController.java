package com.geolocation.locator.controller;

import com.geolocation.locator.service.SerFileGenerator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.geolocation.locator.countryboundaries.CountryBoundaries;
import org.springframework.web.multipart.MultipartFile;


import java.io.*;
import java.util.List;
import java.util.logging.Filter;

@RestController
public class LocatorController {

    private final SerFileGenerator fileGenerator;

    public LocatorController(SerFileGenerator fileGenerator){
        this.fileGenerator = fileGenerator;
    }

    @PostMapping(path = "/generate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> generateSer(
            @RequestParam MultipartFile file,
            @RequestParam(defaultValue = "360") int width,
            @RequestParam(defaultValue = "180") int height
            ) throws IOException {
        if(file.isEmpty()){
            return ResponseEntity.badRequest().build();
        }

        File tempFile = File.createTempFile("upload", null);
        file.transferTo(tempFile);

        byte[] serData = fileGenerator.fileGenerate(new FileInputStream(tempFile), width, height);

        if(serData == null || serData.length < 10){
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok().body("DONE");
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachement; filename=\"boundaries_gen.ser\"")
//                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(serData.length))
//                .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                .body(serData);
    }

    @GetMapping(path = "/getlocation")
    public ResponseEntity<List<String>> getLocation(@RequestParam(name = "latitude") double latitude, @RequestParam(name = "longitude") double longitude){
        CountryBoundaries countryBoundaries = null;

        try(InputStream fis = getClass().getClassLoader().getResourceAsStream("boundaries/boundaries.ser")){
            if(fis == null){
                throw new FileNotFoundException("Resource not found: boundaries.ser");
            }
            countryBoundaries = CountryBoundaries.load(fis);
        }catch (Exception e){
            e.printStackTrace();
        }

        List<String> ids = countryBoundaries.getIds(longitude, latitude);
        return new ResponseEntity<>(ids, HttpStatus.OK);
    }

}
