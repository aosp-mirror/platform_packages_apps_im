/*
 * Copyright (C) 2007 Esmertec AG.
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.im.imps;

import java.io.IOException;
import java.io.InputStream;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.XMLReaderFactory;

import com.android.im.engine.ImException;

public class XmlPrimitiveParser implements
        PrimitiveParser {
    private XMLReader mXmlReader;
    private PrimitiveContentHandler mContentHandler;

    public XmlPrimitiveParser() throws ImException {
        //FIXME: Now we don't have the SAXParser wrapped inside,
        //       use the Driver class temporarily.
        System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");

        try {
            mXmlReader = XMLReaderFactory.createXMLReader();
            mContentHandler = new PrimitiveContentHandler();
            mXmlReader.setContentHandler(mContentHandler);
        } catch (SAXException e) {
            throw new ImException(e);
        }
    }

    public Primitive parse(InputStream in) throws ParserException, IOException {
        mContentHandler.reset();

        try {
            mXmlReader.parse(new InputSource(in));
        } catch (SAXException e) {
            throw new ParserException(e);
        }

        return mContentHandler.getPrimitive();
    }
}
