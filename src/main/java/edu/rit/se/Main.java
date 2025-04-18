package edu.rit.se;

import edu.rit.se.csv_html_creator.csvhtml_creator;
import edu.rit.se.satd.SATDMiner;
import edu.rit.se.satd.api.AzureModel;
import edu.rit.se.satd.refactoring.RefactoringMiner;
import edu.rit.se.satd.comment.IgnorableWords;
import edu.rit.se.satd.detector.SATDDetectorImpl;
import edu.rit.se.satd.mining.diff.CommitToCommitDiff;
import edu.rit.se.satd.writer.SQLiteOutputWriter;
import edu.rit.se.satd.writer.OutputWriter;
import edu.rit.se.util.SimilarityUtil;
import org.apache.commons.cli.*;
import org.eclipse.jgit.diff.DiffAlgorithm;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class Main {
    private static final String ARG_NAME_DB_FILE  = "d";
    private static final String ARG_NAME_REPOS_FILE = "r";
    private static final String ARG_NAME_GH_USERNAME = "u";
    private static final String ARG_NAME_GH_PASSWORD = "p";
    private static final String ARG_NAME_IGNORE_WORDS = "i";
    private static final String ARG_NAME_DIFF_ALGORITHM = "a";
    private static final String ARG_NAME_ERROR_OUTPUT = "e";
    private static final String ARG_NAME_NORMALIZED_LEVENSHTEIN_DISTANCE = "l";
    private static final String PROJECT_NAME_CLI = "satd-analyzer";

    public static void main(String[] args) throws Exception {
        Options options = getOptions();
        String dbLink = "";
        try {
// Check for help option
// This is done first to allow both an optional help option and required args
            if (checkForHelpOption(args)) {
                return; // Only need to print the help options, so we are done here
            }

// Parse from command line
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            final String reposFile = cmd.getOptionValue(ARG_NAME_REPOS_FILE);
            final String dbFile = cmd.getOptionValue(ARG_NAME_DB_FILE);


            if (dbFile == null || dbFile.length() < 4 || !dbFile.endsWith(".db")) {
                throw new IllegalArgumentException("Invalid database file path: must end with '.db'");
            }

            Path dbPath = Paths.get(dbFile);
            String dbDirPath = dbPath.getParent() != null ? dbPath.getParent().toString() : "";
            String dbFilePath = dbPath.getFileName().toString();

            dbLink = String.format("jdbc:sqlite:%s", dbFile);


            if( cmd.hasOption(ARG_NAME_IGNORE_WORDS) ) {
                populateIgnoredWordsFile(cmd.getOptionValue(ARG_NAME_IGNORE_WORDS));
            } else {
                IgnorableWords.noIgnorableWords();
            }

            if( cmd.hasOption(ARG_NAME_DIFF_ALGORITHM) ) {
                final String algoName = cmd.getOptionValue(ARG_NAME_DIFF_ALGORITHM).toUpperCase();
                switch (algoName) {
                    case "MYERS":
                        CommitToCommitDiff.diffAlgo = DiffAlgorithm.getAlgorithm(
                                DiffAlgorithm.SupportedAlgorithm.MYERS);
                        break;
                    case "HISTOGRAM":
                        CommitToCommitDiff.diffAlgo = DiffAlgorithm.getAlgorithm(
                                DiffAlgorithm.SupportedAlgorithm.HISTOGRAM);
                        break;
                    default:
                        System.err.println("Invalid diff algorithm supplied: " + algoName +
                                "\nDefaulted to using Myers.");
                }
            }

            if( !cmd.hasOption(ARG_NAME_ERROR_OUTPUT) ) {
                SATDMiner.disableErrorOutput();
            }

            SimilarityUtil.setLevenshteinDistanceMin(
                    Double.parseDouble(
                            cmd.getOptionValue(
                                    ARG_NAME_NORMALIZED_LEVENSHTEIN_DISTANCE, "0.5")));

            // Read the supplied repos from the file
            final File inFile = new File(reposFile);
            final Scanner inFileReader = new Scanner(inFile);
            // Find the SATD in each supplied repository
            while (inFileReader.hasNext()) {

                final String[] repoEntry = inFileReader.next().split(",");

                if( repoEntry.length > 0 ) {

                    final SATDMiner miner = new SATDMiner(repoEntry[0], new SATDDetectorImpl());

                    final String headCommit = repoEntry.length > 1 ? repoEntry[1] : null;

                    // Set username and password if supplied
                    if (cmd.hasOption(ARG_NAME_GH_USERNAME)) {
                        miner.setGithubUsername(cmd.getOptionValue(ARG_NAME_GH_USERNAME));
                    }
                    if (cmd.hasOption(ARG_NAME_GH_PASSWORD)) {
                        miner.setGithubPassword(cmd.getOptionValue(ARG_NAME_GH_PASSWORD));
                    }

                    SQLiteOutputWriter writer = new SQLiteOutputWriter(dbLink);
                    File dbCheck = new File(dbFile);
                    if (!dbCheck.exists()) {
                        writer.makeTables();
                    }
                    miner.writeRepoSATD(miner.getBaseCommit(headCommit), writer);
                  
                    //AzureModel.classiffySATD(writer, repoEntry[0] );
                    RefactoringMiner.mineRemovalRefactorings(writer, repoEntry[0] );
                  

                    writer.close();
                    miner.cleanRepo();

                }
//                if(!dbLink.isEmpty()) {
//                    csvhtml_creator csvHtmlCreater = new csvhtml_creator(dbLink, dbDirPath);
//                }
//                else{
//                    throw new Error("db file not specified");
//                }

            }
        } catch (ParseException e) {
            System.err.println(e.getLocalizedMessage());
        }
    }

    /**
     * @return the options for the CLI
     */
    private static Options getOptions() {
        // CLI Logic
        return new Options()
                .addOption(Option.builder(ARG_NAME_DB_FILE)
                        .longOpt("db-file")
                        .hasArg()
                        .argName("FILE")
                        .desc("file containing SQLite database")
                        .required()
                        .build())
                .addOption(Option.builder(ARG_NAME_REPOS_FILE)
                        .longOpt("repos")
                        .hasArg()
                        .argName("FILE")
                        .desc(".csv file containing git repositories" +
                                "\n<repository-REQUIRED>,<terminal_commit-OPTIONAL>")
                        .required()
                        .build())
                .addOption(Option.builder(ARG_NAME_GH_USERNAME)
                        .longOpt("username")
                        .hasArg()
                        .argName("USERNAME")
                        .desc("username for Github authentication")
                        .build())
                .addOption(Option.builder(ARG_NAME_GH_PASSWORD)
                        .longOpt("password")
                        .hasArg()
                        .argName("PASSWORD")
                        .desc("password for Github authentication")
                        .build())
                .addOption(Option.builder(ARG_NAME_IGNORE_WORDS)
                        .longOpt("ignore")
                        .hasArg()
                        .argName("WORDS")
                        .desc("a text file containing words to ignore. \n" +
                                "Comments containing any word in the text file will be ignored")
                        .build())
                .addOption(Option.builder(ARG_NAME_DIFF_ALGORITHM)
                        .longOpt("diff-algorithm")
                        .hasArg()
                        .argName("ALGORITHM")
                        .desc("the algorithm to use for diffing (Must be supported by JGit): \n" +
                                "- MYERS (default)\n" +
                                "- HISTOGRAM")
                        .build())
                .addOption(Option.builder(ARG_NAME_ERROR_OUTPUT)
                        .longOpt("show-errors")
                        .desc("shows errors in output")
                        .build())
                .addOption(Option.builder(ARG_NAME_NORMALIZED_LEVENSHTEIN_DISTANCE)
                        .longOpt("n_levenshtein")
                        .hasArg()
                        .type(Number.class)
                        .argName("0.0-1.0")
                        .desc("the normalized levenshtein distance threshold which determines what similarity " +
                                "must be met to qualify SATD instances as changed")
                        .build());
    }

    /**
     * Checks for and prints the help menu if requested
     * @param args the args given to the program
     * @return true if help was requested, else false
     */
    private static boolean checkForHelpOption(String[] args) {
        // Parse from command line
        Options helpOptions = new Options();
        Option helpOption = Option.builder("h")
                .longOpt("help")
                .desc("display help menu")
                .build();

        helpOptions.addOption(helpOption);
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(helpOptions, args);

            if (cmd.hasOption("h")) {
                HelpFormatter hf = new HelpFormatter();
                hf.printHelp(PROJECT_NAME_CLI, getOptions().addOption(helpOption));
                return true;
            }
        } catch (ParseException e) {
            // A non-help header was found
        }
        return false;
    }

    private static void populateIgnoredWordsFile(String fileName) throws Exception {
        final File f = new File(fileName);
        final Scanner r = new Scanner(f);
        final Set<String> words = new HashSet<>();
        while (r.hasNextLine()) {
            words.addAll(Arrays.asList(r.next().trim().split(" ")));
        }
        IgnorableWords.populateIgnorableWords(words);
        r.close();
    }
}
