package at.ac.tuwien.ims.latex2mobiformulaconv.tests.unit;

import at.ac.tuwien.ims.latex2mobiformulaconv.converter.mathml2html.MathmlCharacterDictionary;
import at.ac.tuwien.ims.latex2mobiformulaconv.converter.mathml2html.elements.token.Mo;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

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
 * @author Michael Auß
 */
public class MoTest extends FormulaElementTest {
    private static final Logger logger = Logger.getLogger(MoTest.class);
    private Mo mo;
    private String operator;


    @Before
    public void setUp() throws Exception {
        mo = new Mo();
        formulaElement = mo;

        operator = new ArrayList<>(MathmlCharacterDictionary.operatorDictionary.keySet())
                .get(RandomUtils.nextInt(MathmlCharacterDictionary.operatorDictionary.size()));

        logger.debug("Operator: " + operator);
        mo.setValue(operator);

    }

    @Test
    public void testDetails() throws Exception {
        Element result = mo.render(possibleParent, null);
        assertEquals("span", result.getName());
        assertEquals("mo", result.getAttributeValue("class"));


        String entity = MathmlCharacterDictionary.decodeEntity(operator);
        if (entity != null) {
            operator = entity;
        }

        assertEquals(operator, result.getText());
    }
}