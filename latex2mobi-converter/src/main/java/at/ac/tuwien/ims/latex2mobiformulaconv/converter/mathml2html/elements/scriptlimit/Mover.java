package at.ac.tuwien.ims.latex2mobiformulaconv.converter.mathml2html.elements.scriptlimit;

import at.ac.tuwien.ims.latex2mobiformulaconv.converter.mathml2html.elements.FormulaElement;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.List;

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
 *         Date: 15.09.14
 *         Time: 22:35
 */
public class Mover implements FormulaElement {
    // TODO Mover
    private FormulaElement base;
    private FormulaElement overscript;

    public FormulaElement getOverscript() {
        return overscript;
    }

    public void setOverscript(FormulaElement overscript) {
        this.overscript = overscript;
    }

    public FormulaElement getBase() {
        return base;
    }

    public void setBase(FormulaElement base) {
        this.base = base;
    }

    @Override
    public Element render(FormulaElement parent, List<FormulaElement> siblings) {
        Element mainDiv = new Element("div");
        mainDiv.setAttribute("class", "mover");

        // Siblings
        List<FormulaElement> content = new ArrayList<>();
        content.add(base);
        content.add(overscript);

        Element baseDiv = new Element("div");
        baseDiv.addContent(base.render(this, content));
        baseDiv.setAttribute("class", "base");

        Element underscriptDiv = new Element("div");
        underscriptDiv.setAttribute("class", "overscript");
        underscriptDiv.addContent(overscript.render(this, content));

        mainDiv.addContent(underscriptDiv);
        mainDiv.addContent(baseDiv);

        // TODO munder
        return mainDiv;
    }
}