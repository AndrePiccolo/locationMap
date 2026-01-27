package com.geolocation.locator.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import de.westnordost.countryboundaries.CountryBoundaries;
import de.westnordost.countryboundaries.CountryBoundariesUtils;


import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

@RestController
public class LocatorController {

    @GetMapping(path = "/getlocation")
    public ResponseEntity<List<String>> getLocation(@RequestParam(name = "latitude") double latitude, @RequestParam(name = "longitude") double longitude){
        CountryBoundaries countryBoundaries = null;

        try(InputStream fis = getClass().getClassLoader().getResourceAsStream("boundaries/boundaries.ser")){
            if(fis == null){
                throw new FileNotFoundException("Resource not found: boundaries.ser");
            }
            countryBoundaries = CountryBoundariesUtils.deserializeFrom(fis);
        }catch (Exception e){
            e.printStackTrace();
        }

        List<String> ids = countryBoundaries.getIds(longitude, latitude);
        return new ResponseEntity<>(ids, HttpStatus.OK);
    }

}
