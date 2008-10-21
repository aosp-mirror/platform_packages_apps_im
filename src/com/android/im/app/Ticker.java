/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
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

package com.android.im.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class Ticker extends FrameLayout {

    public Ticker(Context context) {
        super(context);
    }

    public Ticker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Ticker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    @Override
    protected void measureChild(View child, int parentWidthMeasureSpec, 
            int parentHeightMeasureSpec) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();

        int childWidthMeasureSpec;
        int childHeightMeasureSpec;

        // Let the child be as wide as it wants, regardless of our bounds
        childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec, mPaddingLeft
                + mPaddingRight, lp.width);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }
}
