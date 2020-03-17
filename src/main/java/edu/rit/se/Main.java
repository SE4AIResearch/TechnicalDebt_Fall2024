package edu.rit.se;

import edu.rit.se.satd.SATDMiner;
import edu.rit.se.satd.detector.SATDDetectorImpl;
import edu.rit.se.satd.writer.MySQLOutputWriter;
import org.apache.commons.cli.*;

import java.io.File;
import java.util.Scanner;


public class Main {

    private static final String ARG_NAME_DB_PROPS = "d";
    private static final String ARG_NAME_REPOS_FILE = "r";
    private static final String ARG_NAME_GH_USERNAME = "u";
    private static final String ARG_NAME_GH_PASSWORD = "p";
    private static final String PROJECT_NAME_CLI = "satd-analyzer";

    public static void main(String[] args) throws Exception {

        Options options = getOptions();

        try {

            // Check for help option
            // This is done first to allow both an optional help option and required args
            if( checkForHelpOption(args) ) {
                return; // Only need to print the help options so we are done here
            }

            // Parse from command line
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            final String reposFile = cmd.getOptionValue(ARG_NAME_REPOS_FILE);
            final String dbPropsFile = cmd.getOptionValue(ARG_NAME_DB_PROPS);

            // Read the supplied repos from the file
            final File inFile = new File(reposFile);
            final Scanner inFileReader = new Scanner(inFile);

            // Find the SATD in each supplied repository
            while (inFileReader.hasNext()) {

                final SATDMiner miner = new SATDMiner(inFileReader.next(), new SATDDetectorImpl());

                // Set username and password if supplied
                if( cmd.hasOption(ARG_NAME_GH_USERNAME) ) {
                    miner.setGithubUsername(cmd.getOptionValue(ARG_NAME_GH_USERNAME));
                }
                if( cmd.hasOption(ARG_NAME_GH_PASSWORD) ) {
                    miner.setGithubPassword(cmd.getOptionValue(ARG_NAME_GH_PASSWORD));
                }

                miner.writeRepoSATD(miner.getBaseCommit(null), new MySQLOutputWriter(dbPropsFile));

                miner.cleanRepo();
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
                .addOption(Option.builder(ARG_NAME_DB_PROPS)
                        .longOpt("db-props")
                        .hasArg()
                        .argName("FILE")
                        .desc(".properties file containing database properties")
                        .required()
                        .build())
                .addOption(Option.builder(ARG_NAME_REPOS_FILE)
                        .longOpt("repos")
                        .hasArg()
                        .argName("FILE")
                        .desc("new-line separated file containing git repositories")
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
}
