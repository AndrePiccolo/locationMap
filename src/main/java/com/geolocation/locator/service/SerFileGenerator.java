package com.geolocation.locator.service;

import com.geolocation.locator.generator.CountryBoundariesSerializer;
import com.geolocation.locator.generator.GeoJsonReader;
import com.geolocation.locator.generator.JosmCountryBoundariesReader;

import com.geolocation.locator.countryboundaries.CountryBoundaries;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.Geometry;
import com.geolocation.locator.generator.CountryBoundariesGenerator;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

@Service
public class SerFileGenerator {

    public String fileGenerate(FileInputStream inputStream, int width, int height, boolean isJson) throws IOException {

        GeometryCollection geometries;

        if(isJson){
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1)
            {
                baos.write(buffer, 0, length);
            }
            geometries = (GeometryCollection) new GeoJsonReader().read(baos.toString("UTF-8"));
        }else{
            geometries =  new JosmCountryBoundariesReader().read(new InputStreamReader(inputStream, "UTF-8"));
        }

        Set<String> excludeCountries = new HashSet<>();
        excludeCountries.add("FX");
        excludeCountries.add("EU");

        List<Geometry> geometryList = new ArrayList<>(geometries.getNumGeometries());
        for (int i = 0; i < geometries.getNumGeometries(); i++)
        {
            Geometry g = geometries.getGeometryN(i);
            Object id = ((Map)g.getUserData()).get("id");
            if (id instanceof String && !excludeCountries.contains(id)) {
                g.setUserData(id);
                geometryList.add(g);
            }
        }

        System.out.print("Generating index...");

        CountryBoundariesGenerator generator = new CountryBoundariesGenerator();
        generator.setProgressListener(new CountryBoundariesGenerator.ProgressListener()
        {
            String percentDone = "";

            @Override public void onProgress(float progress)
            {
                char[] chars = new char[percentDone.length()];
                Arrays.fill(chars, '\b');
                System.out.print(chars);
                percentDone = "" + String.format(Locale.US, "%.1f", 100*progress) + "%";
                System.out.print(percentDone);
            }
        });

        CountryBoundaries boundaries = generator.generate(width, height, geometryList);

        try(FileOutputStream fos = new FileOutputStream("boundariestest.ser"))
        {
            DataOutputStream dos = new DataOutputStream(fos);
            new CountryBoundariesSerializer().write(boundaries, dos);
        }

        return "Process completed";
    }
}
