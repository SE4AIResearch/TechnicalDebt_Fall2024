package edu.rit.se.csv_html_creator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.awt.*;
import java.io.*;
import java.sql.*;
import java.util.Properties;

public class csvhtml_creator {
//    private final String dbURI;
    private String folderName = "";

    public csvhtml_creator(String dbLink) throws IOException, SQLException {
        System.out.println("Create csv file report...");

        Date currentDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddhhmmss");
        String creationDate = formatter.format(currentDate);
        setFolderName("reports-" + creationDate);

        boolean success = (new File(folderName)).mkdirs();
        boolean His = (new File(folderName + "/html")).mkdirs();


        String old_comment;
        String project="";
        String new_comment= "";
        String satd_id;
        String resolution="";
        String commit_hash = "";
        String author_name="";
        String satd_instance_id;
        String containing_method;
        String method_body;
        String method_declaration;


//        dbURI = "jdbc:sqlite:satd.db";
        Connection conn = DriverManager.getConnection(dbLink);
        


        PreparedStatement comments = conn.prepareStatement(
    "SELECT SATD.satd_id, " +
        "Projects.p_name AS project_name, " +
        "SATD.satd_instance_id, " +
        "SATD.resolution, " +
        "SecondCommit.commit_hash AS resolution_commit, " +
        "FirstCommit.author_name, " +
        "FirstFile.f_path AS v1_path, " +
        "FirstFile.containing_class AS v1_class, " +
        "FirstFile.containing_method AS v1_method, " +
        "FirstFile.f_comment AS v1_comment, " +
        "SecondCommit.commit_hash AS v2_commit, " +
        "SecondCommit.commit_date AS v2_commit_date, " +
        "SecondCommit.author_date AS v2_author_date, " +
        "SecondFile.f_path AS v2_path, " +
        "SecondFile.containing_class AS v2_class, " +
        "SecondFile.containing_method AS v2_method, " +
        "SecondFile.method_declaration AS method_declaration, " +
        "SecondFile.method_body AS method_body, " +
        "SecondFile.f_comment AS v2_comment " +
    "FROM SATD " +  
        "INNER JOIN SATDInFile AS FirstFile ON SATD.first_file = FirstFile.f_id " + 
        "INNER JOIN SATDInFile AS SecondFile ON SATD.second_file = SecondFile.f_id " +
        "INNER JOIN Commits AS FirstCommit ON SATD.first_commit = FirstCommit.commit_hash " +
        "INNER JOIN Commits AS SecondCommit ON SATD.second_commit = SecondCommit.commit_hash " +
        "INNER JOIN Projects ON SATD.p_id = Projects.p_id " +
    "ORDER BY satd_id DESC;"
);



        ResultSet commentsResults = comments.executeQuery();

        CSVPrinter csvPrinter = csvInitializer();

        while (commentsResults.next()) {
            try {
                project =        commentsResults.getString("project_name");
                satd_id =        commentsResults.getString("satd_id");
                old_comment =    commentsResults.getString("v1_comment");
                new_comment =    commentsResults.getString("v2_comment");
                resolution =     commentsResults.getString("resolution");
                commit_hash =    commentsResults.getString("resolution_commit");
                author_name =    commentsResults.getString("author_name");
                satd_instance_id=commentsResults.getString("satd_instance_id");
                containing_method=commentsResults.getString("v2_method");
                method_declaration=commentsResults.getString("method_declaration");
                method_body=commentsResults.getString("method_body");

                // This piece of code is to fix the inverted method body and method declaration issue.
                if (!isMethodDeclaration(method_declaration, containing_method)) {
                    method_declaration = commentsResults.getString("method_body");
                    method_body = commentsResults.getString("method_declaration");
                }

                csvPrinter.printRecord(satd_id,satd_instance_id,
                        project, author_name,commit_hash,old_comment, new_comment, resolution, containing_method, method_declaration, method_body);
                csvPrinter.flush();
            }catch (Exception e){

            }
        }

        commentsResults.close();
        comments.close();
        System.out.println("Done!");
        createHtml();

    }

    public static boolean isMethodDeclaration(String rawDeclaration, String methodSignature) {
        String lowerCaseDeclaration = rawDeclaration.toLowerCase().trim();

        return lowerCaseDeclaration.startsWith("public ")
                || lowerCaseDeclaration.startsWith("protected ")
                || lowerCaseDeclaration.startsWith("private ")
                || lowerCaseDeclaration.startsWith("static ")
                || lowerCaseDeclaration.startsWith("void ")
                // In case no access modifier is being used.
                || (!methodSignature.isEmpty() && rawDeclaration.startsWith(methodSignature.trim().substring(0, methodSignature.trim().indexOf("("))));
    }

    private CSVPrinter csvInitializer() throws IOException {
        System.out.println("Create HTML report...");
        File csv = new File(folderName + "/SATD report.csv");
        CSVPrinter csvPrinter = null;
        if (!csv.exists()){
            FileWriter csvWriter = new FileWriter(folderName + "/SATD report.csv",true);
            csvPrinter = new CSVPrinter(csvWriter,
                    CSVFormat.DEFAULT.withHeader("satd id","satd instance",
                            "project","committer name","Commit Hash","old comment","New Comment","resolution", "Method Signature", "Method Declaration", "Method Body"));
        }else {
            FileWriter csvWriter = new FileWriter(folderName + "/SATD_final.csv",true);
            csvPrinter = new CSVPrinter(csvWriter, CSVFormat.DEFAULT);
        }
        return csvPrinter;

    }

    public void createHtml() throws IOException {
        FileReader reader = new FileReader(folderName + "/SATD report.csv");
        Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(reader);
        File f = new File(folderName + "/SATD report.html");
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        bw.write("<html><head>\n" +
                "<style> table, th, td {\n" +
                "  border: 1px solid black;\n" +
                "  border-collapse: collapse;\n" +
                "}th {\n" +
                "  background: lightblue;\n" +
                "}"+
                "</style>\n" +
                "</head> <body><h1>SATD</h1>");
        bw.write("<table>");
        bw.write("<tr><th>satd id</th>" +
                " <th>satd instance id</th> " +
                " <th>project</th> " +
                "<th>committer name </th> " +
                "<th> Commit Hash</th> <th>old comment</th> <th>New Comment</th> <th>resolution</th> <th>Method Signature</th><th>Method Declaration</th><th>Method Body</th></tr>");



        for (CSVRecord record : records) {

            if (record.get(0).equals("satd id")){

            }
            else {
                bw.write("<tr><td>" + record.get(0)+"</a>"
                        + "</td> <td><a href=\"html/"+record.get(1)+".html\">" + record.get(1) +
                        "</td><td>" + record.get(2)+
                        "</td><td>" + record.get(3)
                        + "</td><td>" + record.get(4)
                        + "</td> <td>" + record.get(5)
                        + "</td> <td>" + record.get(6)
                        + "</td> <td>" + record.get(7)
                        + "</td> <td>" + record.get(8)
                        + "</td> <td>" + record.get(9)
                        + "</td> <td>" + record.get(10)
                        + "</td> </tr>");
                commentHistoryHtml(record.get(0),record.get(1),record.get(2),record.get(3),record.get(4),record.get(5),record.get(6),record.get(7),record.get(8),record.get(9), record.get(10));
            }

        }
        bw.write("</table>");
        bw.write("</body></html>");
        bw.close();
        System.out.println("Done!");
        System.out.println("Results available at folder: " + folderName);

    }
    private void commentHistoryHtml(String s, String s1, String s2, String s3, String s4, String s5, String s6, String s7, String s8, String s9, String s10) throws IOException {


        File f = new File(folderName+"/html/"+s1+".html");
        if(f.exists()){
            Document doc = Jsoup.parse(f, "UTF-8", "");
            Elements span = doc.children();
            String newComment = "<tr><td>" + s
                    + "</td> <td>" + s1
                    +"</td><td>" + s2
                    +"</td><td>" + s3
                    + "</td><td>" + s4
                    + "</td> <td>" + s5
                    + "</td> <td>" + s6
                    + "</td> <td>" + s7
                    + "</td> <td>" + s8
                    + "</td> <td>" + s9
                    + "</td> <td>" + s10
                    + "</td> </tr>";

            doc.select("table").append(newComment);
            String html = doc.html();
            BufferedWriter htmlWriter =
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream(folderName+"/html/"+s1+".html"), "UTF-8"));
            htmlWriter.write(html);
            htmlWriter.close();

        }else {
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write("<html><head>\n" +
                    "<style> table, th, td {\n" +
                    "  border: 1px solid black;\n" +
                    "  border-collapse: collapse;\n" +
                    "}th {\n" +
                    "  background: lightblue;\n" +
                    "}" +
                    "</style>\n" +
                    "</head> <body><h1>SATD</h1>");
            bw.write("<table>");
            bw.write("<tr><th>satd id</th>" +
                    " <th>satd instance id</th> " +
                    " <th>project</th> " +
                    "<th>committer name </th> " +
                    "<th> Commit Hash</th> <th>old comment</th> <th>New Comment</th> <th>resolution</th> <th>Method Signature</th> <th>Method Declaration</th> <th>Method Body</th> </tr>");


            bw.write("<tr><td>" + s +
                    "</td> <td>" + s1 +
                    "</td><td>" + s2 +
                    "</td><td>" + s3
                    + "</td><td>" + s4
                    + "</td> <td>" + s5
                    + "</td> <td>" + s6
                    + "</td> <td>" + s7
                    + "</td> <td>" + s8
                    + "</td> <td>" + s9
                    + "</td> <td>" + s10
                    + "</td> </tr>");


            bw.write("</table>");
            bw.write("</body></html>");
            bw.close();
        }
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getFolderName() {
        return folderName;
    }
}