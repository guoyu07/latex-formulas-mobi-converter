package at.ac.tuwien.ims.latex2mobiformulaconv.converter;

import at.ac.tuwien.ims.latex2mobiformulaconv.converter.html2mobi.AmazonHtmlToMobiConverter;
import at.ac.tuwien.ims.latex2mobiformulaconv.converter.html2mobi.HtmlToMobiConverter;
import at.ac.tuwien.ims.latex2mobiformulaconv.converter.latex2html.LatexToHtmlConverter;
import at.ac.tuwien.ims.latex2mobiformulaconv.converter.latex2html.PandocLatexToHtmlConverter;
import at.ac.tuwien.ims.latex2mobiformulaconv.converter.mathml2html.DOMFormulaConverter;
import at.ac.tuwien.ims.latex2mobiformulaconv.converter.mathml2html.FormulaConverter;
import at.ac.tuwien.ims.latex2mobiformulaconv.converter.mathml2html.ImageFormulaConverter;
import at.ac.tuwien.ims.latex2mobiformulaconv.converter.mathml2html.elements.Formula;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
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
 *
 * @author Michael Auß
 *         Date: 17.08.14
 *         Time: 17:35
 *
 * Converts LaTeX input to Mobi files with Pandoc & Amazon Kindlegen
 */
public class Converter {
    private static Logger logger = Logger.getLogger(Converter.class);
    private static final String MAIN_CSS_FILENAME = "main.css";

    // TODO resolve hard coded Implementation bindings
    private static LatexToHtmlConverter latexToHtmlConverter = new PandocLatexToHtmlConverter();
    private static HtmlToMobiConverter htmlToMobiConverter = new AmazonHtmlToMobiConverter();

    private Path workingDirectory;

    public Converter(Path workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    /**
     *  ArrayList of input paths, only the first gets read
     */
    private ArrayList<Path> inputPaths;

    /**
     * if true, the formulas will get replaced with png pictures
     * else the will be represented with html
     */
    private boolean replaceWithPictures = false;
    private boolean debug = false;

    private boolean exportMarkup = false;

    /**
     * the directory path where the result will be written to
     */
    private Path outputPath;

    /**
     * the filename of the result file, if it already exists, a number will automatically be added to this string
     */
    private String filename;

    private String title;

    public boolean isExportMarkup() {
        return exportMarkup;
    }

    public void setExportMarkup(boolean exportMarkup) {
        this.exportMarkup = exportMarkup;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isReplaceWithPictures() {
        return replaceWithPictures;
    }

    public void setReplaceWithPictures(boolean replaceWithPictures) {
        this.replaceWithPictures = replaceWithPictures;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public Path getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(Path outputPath) {
        this.outputPath = outputPath;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public ArrayList<Path> getInputPaths() {
        return inputPaths;
    }

    public void setInputPaths(ArrayList<Path> inputPaths) {
        this.inputPaths = inputPaths;
    }

    /**
     * Converts a single input file from LaTeX to Mobi
     * @return Path of the resulting File
     */
    public Path convert() {
        // TODO iterate over inputPaths
        File inputFile = inputPaths.get(0).toFile();

        // set default title
        if (title == null) {
            title = inputFile.getName();
        }

        // Main Document conversion to HTML, without formulas
        logger.debug("Converting main document to HTML...");
        Document document = latexToHtmlConverter.convert(inputFile, title, workingDirectory);

        logger.info("Parsing LaTeX formulas from resulting HTML...");

        FormulaConverter formulaConverter;
        Map<Integer, Formula> formulaMap = new HashMap<>();

        if (replaceWithPictures) {
            formulaConverter = new ImageFormulaConverter();
        } else {
            formulaConverter = new DOMFormulaConverter();
        }

        formulaConverter.setDebug(debug);

        Map<Integer, String> latexFormulas = formulaConverter.extractFormulas(document);

        if (latexFormulas.isEmpty() == false) {
            Iterator<Integer> it = latexFormulas.keySet().iterator();
            while (it.hasNext()) {
                Integer id = it.next();
                String latexFormula = latexFormulas.get(id);

                Formula formula = formulaConverter.parse(id, latexFormula);

                if (formula != null) {
                    formulaMap.put(id, formula);
                }
            }
            document = formulaConverter.replaceFormulas(document, formulaMap);
        }

        // Saving html file
        File htmlFile = saveHtmlFile(document);



        // Convert to MOBI format
        logger.info("Converting completed HTML to MOBI format...");
        File mobiFile = htmlToMobiConverter.convertToMobi(htmlFile);

        // Save file
        Path resultFilepath = null;
        try {
            // Don't overwrite files
            Path outputFilepath;
            int i = 1;
            String replaceFilename = filename + ".mobi";
            while (true) {
                outputFilepath = outputPath.resolve(replaceFilename);
                if (Files.exists(outputFilepath) == false) {
                    break;
                }
                replaceFilename = filename + " (" + i + ").mobi";
                i++;
            }

            resultFilepath = Files.move(mobiFile.toPath(), outputFilepath);
            logger.debug("Mobi file moved to: " + resultFilepath.toAbsolutePath().toString());
            return resultFilepath.toAbsolutePath();

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Saves the html document to a file with .html extension
     * @param document JDOM Document representing the HTML
     * @return written HTML File object
     */
    private File saveHtmlFile(Document document) {
        Path tempFilepath = null;


        try {
            Path tempDir = Files.createTempDirectory("latex2mobi");

            ClassLoader classLoader = getClass().getClassLoader();
            InputStream mainCssIs = classLoader.getResourceAsStream(MAIN_CSS_FILENAME);

            logger.debug("Copying main.css file to temp dir: " + tempDir.toAbsolutePath().toString());
            Files.copy(mainCssIs, tempDir.resolve(MAIN_CSS_FILENAME));
            tempFilepath = tempDir.resolve("latex2mobi.html");

            logger.debug("tempFile created at: " + tempFilepath.toAbsolutePath().toString());

            Files.write(tempFilepath, new XMLOutputter().outputString(document).getBytes(Charset.forName("UTF-8")));

            if (exportMarkup) {
                Path markupDir = workingDirectory.resolve(title + "-markup");
                try {
                    Files.createDirectory(markupDir);
                } catch (FileAlreadyExistsException e) {
                    // do nothing
                }
                Files.copy(mainCssIs, markupDir.resolve(MAIN_CSS_FILENAME), StandardCopyOption.REPLACE_EXISTING);

                Files.copy(tempFilepath, markupDir.resolve(tempFilepath.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            // TODO Exception handling
        }

        return tempFilepath.toFile();
    }

}
