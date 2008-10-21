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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a XML element of Primitive. Note that this class is not
 * thread-safe.
 */
final public class PrimitiveElement {
    private String mTagName;
    private HashMap<String, String> mAttributes;
    private ArrayList<PrimitiveElement> mChildren;
    private String mContents;

    public PrimitiveElement(String tagName) {
        mTagName = tagName;
    }

    public String getTagName() {
        return mTagName;
    }

    public void setTagName(String tagName) {
        this.mTagName = tagName;
    }

    public Map<String, String> getAttributes() {
        if (mAttributes == null) {
            return null;
        }
        return Collections.unmodifiableMap(mAttributes);
    }

    public void setAttribute(String key, String value) {
        if (key != null && value != null) {
            if (mAttributes == null) {
                mAttributes = new HashMap<String, String>();
            }
            mAttributes.put(key, value);
        }
    }

    public ArrayList<PrimitiveElement> getChildren() {
        if (mChildren == null) {
            mChildren = new ArrayList<PrimitiveElement>();
        }
        return mChildren;
    }

    public ArrayList<PrimitiveElement> getChildren(String tagName) {
        ArrayList<PrimitiveElement> children = new ArrayList<PrimitiveElement>();

        for (PrimitiveElement child : getChildren()) {
            if (tagName.equals(child.getTagName())) {
                children.add(child);
            }
        }

        return children;
    }

    public PrimitiveElement getChild(String tagName) {
        for (PrimitiveElement child : getChildren()) {
            if (tagName.equals(child.getTagName())) {
                return child;
            }
        }
        return null;
    }

    public String getChildContents(String tagName) {
        PrimitiveElement child = getChild(tagName);
        return child == null ? null : child.getContents();
    }

    public int getChildCount() {
        if (mChildren == null || mChildren.isEmpty()) {
            return 0;
        } else {
            return mChildren.size();
        }
    }

    public PrimitiveElement getFirstChild() {
        if ((mChildren == null) || mChildren.isEmpty()) {
            return null;
        }
        return mChildren.get(0);
    }

    public PrimitiveElement addChild(PrimitiveElement child) {
        if (child != null) {
            getChildren().add(child);
        }

        return child;
    }

    public PrimitiveElement addChild(String tagName) {
        if (null == tagName) {
            return null;
        }
        PrimitiveElement element = new PrimitiveElement(tagName);
        getChildren().add(element);
        return element;
    }

    public void addChild(String tagName, String contents) {
        PrimitiveElement element = addChild(tagName);
        if (null != contents) {
            element.setContents(contents);
        }
    }

    public void addChild(String tagName, boolean value) {
        addChild(tagName).setContents(value ?
                ImpsConstants.TRUE : ImpsConstants.FALSE);
    }

    public void addPropertyChild(String name, String value)
    {
        PrimitiveElement ret = addChild(ImpsTags.Property);
        ret.addChild(ImpsTags.Name, name);
        ret.addChild(ImpsTags.Value, value);
    }

    public void addPropertyChild(String name, boolean value)
    {
        PrimitiveElement ret = addChild(ImpsTags.Property);
        ret.addChild(ImpsTags.Name, name);
        ret.addChild(ImpsTags.Value, value);
    }

    public String getContents() {
        return mContents;
    }

    public void setContents(String contents) {
        mContents = contents;
    }
}
