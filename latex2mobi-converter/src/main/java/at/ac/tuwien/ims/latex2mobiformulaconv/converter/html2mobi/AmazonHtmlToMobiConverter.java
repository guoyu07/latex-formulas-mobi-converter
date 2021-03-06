package at.ac.tuwien.ims.latex2mobiformulaconv.converter.html2mobi;

import org.apache.commons.cli.Option;
import org.apache.commons.exec.*;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

/*
 * The MIT License (MIT)
 * latex2mobi -- LaTeX Formulas to Mobi Converter
 * Copyright (c) 2014 Michael Auß
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * <p/>
 * <p/>
 * For Third Party Software Licenses read LICENSE file in base dir.
 */

/**
 * Converts an HTML file with Amazon's Kindlegen executable to a Mobi ebook
 *
 * @author Michael Auß
 *         Created: 21.05.14 00:13
 */
public class AmazonHtmlToMobiConverter implements HtmlToMobiConverter {
    private static final Logger logger = Logger.getLogger(AmazonHtmlToMobiConverter.class);

    private String command = "kindlegen";
    private Path execPath = null;

    @Override
    public File convertToMobi(File htmlFile) {
        logger.debug("Enter convertToMobi()...");

        if (htmlFile == null) {
            logger.error("Document is null, aborting...");
            System.exit(1);
        }

        CommandLine cmdLine;
        if (execPath != null) {
            // Run the configured kindlegen executable
            logger.info("Kindlegen will be run from: " + execPath.toString());
            cmdLine = new CommandLine(execPath.toFile());
        } else {
            // Run in system PATH environment
            logger.info("Kindlegen will be run within the PATH variable.");
            cmdLine = new CommandLine(command);
        }

        // Run configuration
        cmdLine.addArgument(Paths.get(htmlFile.toURI()).toAbsolutePath().toString());
        cmdLine.addArgument("-c0");

        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

        ExecuteWatchdog watchdog = new ExecuteWatchdog(60 * 1000);
        Executor executor = new DefaultExecutor();
        executor.setExitValue(1);
        executor.setWatchdog(watchdog);
        StringWriter stdoutWriter = new StringWriter();
        StringWriter stderrWriter = new StringWriter();
        WriterOutputStream writerOutputStream = new WriterOutputStream(stdoutWriter, Charset.forName("UTF-8"));
        WriterOutputStream writerErrorStream = new WriterOutputStream(stderrWriter, Charset.forName("UTF-8"));

        ExecuteStreamHandler kindlegenStreamHandler = new PumpStreamHandler(writerOutputStream, writerErrorStream);
        executor.setStreamHandler(kindlegenStreamHandler);

        logger.debug("Launching kindlegen:");
        logger.debug(cmdLine.toString());

        try {
            executor.execute(cmdLine, resultHandler);
        } catch (IOException e) {
            logger.error("Kindlegen failed to execute:");
            logger.error(e.getMessage(), e);
            System.exit(-1);
        }

        try {
            resultHandler.waitFor();
            int exitValue = resultHandler.getExitValue();

            logger.debug("Kindlegen execution's exit value: " + exitValue);
            ExecuteException executeException = resultHandler.getException();
            if (executeException != null && executeException.getCause() != null) {

                String exceptionKlass = executeException.getCause().getClass().getCanonicalName();
                String exceptionMessage = executeException.getCause().getMessage();
                if (exceptionKlass.endsWith("IOException") || exceptionMessage.contains("Cannot run program \"kindlegen\"")) {
                    logger.error("Kindlegen could not be run! Exiting...");
                    logger.debug(executeException);
                    System.exit(1);
                }
                logger.debug(exceptionKlass + ": " + exceptionMessage);
            }

        } catch (InterruptedException e) {
            logger.error("Kindlegen's execution got interrupted: ");
            logger.error(e.getMessage(), e);
        }

        try {
            String stderrOutput = stderrWriter.getBuffer().toString();
            stderrWriter.close();
            if (stderrOutput.isEmpty() == false) {
                logger.error("Kindlegen logged some errors:");
                logger.error(stderrOutput);
            }
        } catch (IOException e) {
            logger.error("Error closing kindlegen's stderr buffer");
            logger.error(e.getMessage(), e);
        }

        String output = "";
        try {
            output += stdoutWriter.getBuffer().toString();
            stdoutWriter.close();

        } catch (IOException e) {
            logger.error("Error closing kindlegen's stdout buffer:");
            logger.error(e.getMessage(), e);
        }

        logger.debug("Kindlegen output: \n" + output);

        String mobiFilename = htmlFile.getName().toString().replace(".html", ".mobi").toString();
        logger.debug("Moving Kindlegen output file: " + mobiFilename);

        Path tempMobiFilepath = Paths.get(htmlFile.toURI()).getParent().resolve(mobiFilename);
        return tempMobiFilepath.toFile();
    }


    @Override
    public Option getExecOption() {
        Option option = new Option("k", "kindlegen-exec", true, "Amazon KindleGen executable location");
        option.setArgs(1);
        return option;
    }

    @Override
    public void setExecPath(Path execPath) {
        this.execPath = execPath;
    }
}
