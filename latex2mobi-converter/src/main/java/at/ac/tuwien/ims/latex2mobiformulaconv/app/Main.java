package at.ac.tuwien.ims.latex2mobiformulaconv.app;

import org.apache.commons.cli.*;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

/**
 * @author mauss
 *         Created: 29.04.14 22:55
 */
public class Main {
    private static final String CONFIGURATION_FILENAME = "configuration.properties";
    private static Logger logger = Logger.getRootLogger();
    private static Configuration config;
    private static Options options;

    public static void main(String[] args) {
        logger.debug("main() started.");

        try {
            logger.debug("Loading configuration.");
            config = new PropertiesConfiguration(CONFIGURATION_FILENAME);
        } catch (ConfigurationException e) {
            logger.error(e.getMessage(), e);
            // TODO Exception handling
        }


        options = new Options();

        options.addOption("i", "input", true, "input file");
        options.addOption("o", "output", true, "output file");
        options.addOption("p", "pandoc-exec", true, "pandoc executable location");
        options.addOption("h", "help", false, "show this help");


        CommandLineParser parser = new PosixParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption('h')) {
                // Show usage, ignore other parameters and quit
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("latex2mobi", options);
                logger.debug("Help called, main() exit.");
                System.exit(0);
            }

            // TODO Input File handling

            // TODO Output File/directory handling

            // TODO Pandoc executable handling


        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
            // TODO Exception handling
        }


        logger.debug("main() exit.");
    }
}
