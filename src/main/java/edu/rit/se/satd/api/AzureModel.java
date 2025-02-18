package edu.rit.se.satd.api;

import com.google.gson.Gson;

import edu.rit.se.satd.writer.OutputWriter;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.*;
import java.net.*;
import java.sql.SQLException;
import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class AzureModel {

    /**
     * Classifies removed satd comments in a repo
     * @param writer
     * @param projectURL
     */
    public static void classiffySATD (OutputWriter writer, String projectURL){
        try {

        URL aURL = new URL(projectURL);
        String projectName = aURL.getPath().replaceFirst("\\/", "");
        System.out.println("--- Classifying removed SATD for " + projectName + " ---");

        // Get removed SATD comments from project
        System.out.println("Locating removed SATD ...");
        Map<String, String> satdMap = writer.getRemovedSATD(projectName, projectURL);

        // Directly write results to the database
        System.out.println("Saving results ...");
        writer.writePredictionResults(satdMap);         
        }catch(IOException e){
            System.out.println(e);
        } catch (SQLException e){
            System.out.println(e);
        }
    }

}
