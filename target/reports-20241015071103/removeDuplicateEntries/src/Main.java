import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileWriter;
import java.io.IOException;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        removeDuplicateEntries();
    }

    public static void removeDuplicateEntries() {
        Date currentDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss"); // Changed to HH for 24-hour format
        String creationDate = formatter.format(currentDate);
        String inputFilePath = "C:\\Users\\bc\\SATDBailiff_fork\\TechnicalDebt_Fall2024\\target\\reports-20241015071103\\SATD report.csv";
        String outputFilePath = "C:\\Users\\bc\\SATDBailiff_fork\\TechnicalDebt_Fall2024\\target\\reports-20241015071103\\SATD_report_clean_new.csv";


        File inputFile = new File(inputFilePath);
        File outputFile = new File(outputFilePath);

        List<String[]> lines = new ArrayList<>();

        // Using try-with-resources to ensure resources are closed automatically
        try (FileReader fileReader = new FileReader(inputFile);
             CSVReader csvReader = new CSVReader(fileReader)) {

            String[] values;
            while ((values = csvReader.readNext()) != null) {
                lines.add(values);
            }

        } catch (FileNotFoundException e) {
            System.err.println("Input file not found: " + outputFilePath);
            e.printStackTrace();
            return; // Exit the method if input file is missing
        } catch (IOException e) {
            System.err.println("I/O error while reading the input file.");
            e.printStackTrace();
            return; // Exit the method on I/O errors
        } catch (CsvValidationException e) {
            System.err.println("CSV validation error while parsing the input file.");
            e.printStackTrace();
            return; // Exit the method on CSV validation errors
        }

        // Remove duplicate rows using a Set
        Set<List<String>> cleanEntries = new LinkedHashSet<>();
        for (String[] row : lines) {
            cleanEntries.add(Arrays.asList(row));
        }

        // Create parent directory if it doesn't exist
        if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists()) {
            boolean dirsCreated = outputFile.getParentFile().mkdirs();
            if (!dirsCreated) {
                System.err.println("Failed to create directories for output file: " + outputFilePath);
                return;
            }
        }

        // Write cleaned data to a new CSV file using OpenCSV
        try (FileWriter fileWriter = new FileWriter(outputFile);
             com.opencsv.CSVWriter csvWriter = new com.opencsv.CSVWriter(fileWriter)) {

            for (List<String> row : cleanEntries) {
                csvWriter.writeNext(row.toArray(new String[0]));
            }

        } catch (IOException e) {
            System.err.println("I/O error while writing to the output file.");
            e.printStackTrace();
            return; // Exit the method on I/O errors
        }

        System.out.println("Duplicate entries removed and written to: " + outputFilePath);
    }
}
