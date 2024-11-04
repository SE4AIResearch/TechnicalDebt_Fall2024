import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;

public class CSVtoSQLite {
    public static void main(String[] args) {
        String csvFilePath = args[0];
        String sqliteDbPath = args[1];
        try (Connection conn = DriverManager.getConnection(sqliteDbPath);
             Statement statement = conn.createStatement()) {

            String dropTableSQL = "DROP TABLE IF EXISTS satd;";
            statement.executeUpdate(dropTableSQL);

            String createTableSQL = "CREATE TABLE IF NOT EXISTS satd ("
                                  + "satd_id TEXT, "
                                  + "satd_instance TEXT, "
                                  + "project TEXT, "
                                  + "committer_name TEXT, "
                                  + "commit_hash TEXT, "
                                  + "old_comment TEXT, "
                                  + "new_comment TEXT, "
                                  + "resolution TEXT, "
                                  + "method_signature TEXT, "
                                  + "method_declaration TEXT, "
                                  + "method_body TEXT"
                                  + ");";
            statement.executeUpdate(createTableSQL);

            String insertSQL = "INSERT INTO satd (satd_id, satd_instance, project, committer_name, commit_hash, old_comment, new_comment, resolution, method_signature, method_declaration, method_body) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = conn.prepareStatement(insertSQL);
                 BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {

                String line;
                boolean isFirstLine = true;
                while ((line = br.readLine()) != null) {

                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }
                    String[] values = line.split(",");
                    
                    if (values.length >= 11) {
                        preparedStatement.setString(1, values[0].trim()); 
                        preparedStatement.setString(2, values[1].trim()); 
                        preparedStatement.setString(3, values[2].trim()); 
                        preparedStatement.setString(4, values[3].trim()); 
                        preparedStatement.setString(5, values[4].trim()); 
                        preparedStatement.setString(6, values[5].trim()); 
                        preparedStatement.setString(7, values[6].trim()); 
                        preparedStatement.setString(8, values[7].trim()); 
                        preparedStatement.setString(9, values[8].trim()); 
                        preparedStatement.setString(10, values[9].trim()); 
                        preparedStatement.setString(11, values[10].trim()); 
                        preparedStatement.executeUpdate();
                    }
                }
            } catch (IOException e) {
                System.out.println("Error reading CSV file: " + e.getMessage());
            }
            System.out.println("Data successfully inserted from CSV to SQLite!");

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
