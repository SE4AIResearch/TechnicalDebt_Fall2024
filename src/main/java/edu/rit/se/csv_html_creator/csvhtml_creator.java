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
    private final String dbURI;
    private final String user;
    private final String pass;
    private String folderName = "";

    public csvhtml_creator(String propertiesPath) throws IOException, SQLException {
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


        final Properties properties = new Properties();
        properties.load(new FileInputStream(new File(propertiesPath)));

        dbURI = String.format("jdbc:mysql://%s:%s/%s?useSSL=%s",
                properties.getProperty("URL"),
                properties.getProperty("PORT"),
                properties.getProperty("DB"),
                properties.getProperty("USE_SSL"));
        user = properties.getProperty("USERNAME");
        pass = properties.getProperty("PASSWORD");
        Connection conn = DriverManager.getConnection(dbURI, user, pass);


        PreparedStatement comments = conn.prepareStatement("SELECT SATD.satd_id, " +
                "Projects.p_name as project_name, SATD.satd_instance_id, " +
                "SATD.resolution,SecondCommit.commit_hash as resolution_commit, FirstCommit.author_name, " +
                "FirstFile.f_path as v1_path, FirstFile.containing_class as v1_class," +
                " FirstFile.containing_method as v1_method, FirstFile.f_comment as v1_comment," +
                " SecondCommit.commit_hash as v2_commit, SecondCommit.commit_date as v2_commit_date," +
                " SecondCommit.author_date as v2_author_date, SecondFile.f_path as v2_path, " +
                "SecondFile.containing_class as v2_class," +
                " SecondFile.containing_method as v2_method," +
                " SecondFile.f_comment as v2_comment " +
                "FROM satd.SATD INNER JOIN satd.SATDInFile as FirstFile ON SATD.first_file = FirstFile.f_id INNER JOIN satd.SATDInFile as SecondFile ON SATD.second_file = SecondFile.f_id INNER JOIN satd.Commits as FirstCommit ON SATD.first_commit = FirstCommit.commit_hash  AND SATD.p_id = FirstCommit.p_id INNER JOIN satd.Commits as SecondCommit ON SATD.second_commit = SecondCommit.commit_hash AND SATD.p_id = SecondCommit.p_id INNER JOIN satd.Projects ON SATD.p_id=Projects.p_id ORDER BY satd_id DESC;");


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
                csvPrinter.printRecord(satd_id,satd_instance_id,
                        project, author_name,commit_hash,old_comment, new_comment, resolution, containing_method);
                csvPrinter.flush();
            }catch (Exception e){

            }
        }

        commentsResults.close();
        comments.close();
        System.out.println("Done!");
        createHtml();

    }

    private CSVPrinter csvInitializer() throws IOException {
        System.out.println("Create HTML report...");
        File csv = new File(folderName + "/SATD report.csv");
        CSVPrinter csvPrinter = null;
        if (!csv.exists()){
            FileWriter csvWriter = new FileWriter(folderName + "/SATD report.csv",true);
            csvPrinter = new CSVPrinter(csvWriter,
                    CSVFormat.DEFAULT.withHeader("satd id","satd instance",
                            "project","committer name","Commit Hash","old comment","New Comment","resolution", "Containing Method"));
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
                "<th> Commit Hash</th> <th>old comment</th> <th>New Comment</th> <th>resolution</th> <th>Containing Method</th></tr>");



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
                        + "</td> </tr>");
                commentHistoryHtml(record.get(0),record.get(1),record.get(2),record.get(3),record.get(4),record.get(5),record.get(6),record.get(7),record.get(8));
            }

        }
        bw.write("</table>");
        bw.write("</body></html>");
        bw.close();
        System.out.println("Done!");
        System.out.println("Results available at folder: " + folderName);

    }
    private void commentHistoryHtml(String s, String s1, String s2, String s3, String s4, String s5, String s6, String s7, String s8) throws IOException {


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
                    "<th> Commit Hash</th> <th>old comment</th> <th>New Comment</th> <th>resolution</th> <th>Containing Method</th> </tr>");


            bw.write("<tr><td>" + s +
                    "</td> <td>" + s1 +
                    "</td><td>" + s2 +
                    "</td><td>" + s3
                    + "</td><td>" + s4
                    + "</td> <td>" + s5
                    + "</td> <td>" + s6
                    + "</td> <td>" + s7
                    + "</td> <td>" + s8
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
