/*
 * Copyright 2016 Ahmad Muzakki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.muzakki.ahmad.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * JustifyLayout will arrange child elements horizontally with the same margin between them.
 * If there is not enough space for next view new line will be added.
 *
 * User: Ahmad Muzakki
 * Date: 2/8/16
 */
public class JustifyLayout extends ViewGroup {
    int horizontalSpacing = 20;
    int verticalSpacing = 20;
    ArrayList<ArrayList<View>> lines = new ArrayList<>();
    ArrayList<Integer> linesHeight = new ArrayList<>();
    ArrayList<Integer> linesGap = new ArrayList<>();

    public JustifyLayout(Context context) {
        this(context,null);
    }

    public JustifyLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public JustifyLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.JustifyLayout,
                0, 0);

        try {
            horizontalSpacing = a.getDimensionPixelSize(R.styleable.JustifyLayout_horizontalSpacing, dpToPx(horizontalSpacing));
            verticalSpacing = a.getDimensionPixelSize(R.styleable.JustifyLayout_verticalSpacing, dpToPx(verticalSpacing));
        } finally {
            a.recycle();
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);

        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        int width = 0;
        int height = getPaddingTop() + getPaddingBottom();

        int lineWidth = 0;
        int lineHeight = 0;
        int excess = 0;

        int childCount = getChildCount();

        for(int i = 0; i < childCount; i++) {
            View child = getChildAt(i);


            measureChildWithMargins(child, widthMeasureSpec, lineWidth, heightMeasureSpec, height);

            LayoutParams lp = (LayoutParams) child.getLayoutParams();

            int childWidthMode = MeasureSpec.AT_MOST;
            int childWidthSize = sizeWidth;

            int childHeightMode = MeasureSpec.AT_MOST;
            int childHeightSize = sizeHeight;

            if(lp.width >= 0) {
                childWidthMode = MeasureSpec.EXACTLY;
                childWidthSize = lp.width;
            }

            if(lp.height >= 0) {
                childHeightMode = MeasureSpec.EXACTLY;
                childHeightSize = lp.height;
            } else if (modeHeight == MeasureSpec.UNSPECIFIED) {
                childHeightMode = MeasureSpec.UNSPECIFIED;
                childHeightSize = 0;
            }

            child.measure(
                    MeasureSpec.makeMeasureSpec(childWidthSize, childWidthMode),
                    MeasureSpec.makeMeasureSpec(childHeightSize, childHeightMode)
            );

            int childWidth = child.getMeasuredWidth();

            if((2*horizontalSpacing) + excess+ lineWidth + childWidth > sizeWidth) {
                width = Math.max(width, lineWidth);
                lineWidth = childWidth;
                excess = 0;
                height += lineHeight+verticalSpacing;
                lineHeight = child.getMeasuredHeight();

            } else {
                excess= horizontalSpacing;
                lineWidth += childWidth;
                lineHeight = Math.max(lineHeight, child.getMeasuredHeight());
            }

        }

        height += lineHeight;
        width += getPaddingLeft() + getPaddingRight();
        setMeasuredDimension(
                (modeWidth == MeasureSpec.EXACTLY) ? sizeWidth : width,
                (modeHeight == MeasureSpec.EXACTLY) ? sizeHeight : height);

    }

    @Override
    protected void onLayout(boolean changed, int t, int l, int b, int r) {
            lines.clear();
            linesHeight.clear();
            linesGap.clear();

        int count = getChildCount();
        int width = getMeasuredWidth();
        int lineWidth = 0;
        int lineHeight = 0;
        int excess = 0;
        ArrayList<View> line = new ArrayList<>();
        for(int i=0;i<count;i++){
            View child = getChildAt(i);


            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            if((horizontalSpacing *2)+excess+lineWidth+childWidth>width){
                lines.add(line);
                linesGap.add((width-lineWidth)/(line.size()+1));
                linesHeight.add(lineHeight);
                line = new ArrayList<>();
                lineWidth= 0;
                lineHeight = 0;
                excess=0;
            }else {
                excess = horizontalSpacing;
            }
            line.add(child);
            lineWidth += childWidth;
            lineHeight = Math.max(lineHeight, childHeight);
        }
        // append the last line
        lines.add(line);
        linesGap.add((width-lineWidth)/(line.size()+1));
        linesHeight.add(lineHeight);

        //draw the child
        int y = getPaddingTop();
        for(int i=0;i<lines.size();i++){
            ArrayList<View> lineViews = lines.get(i);
            lineHeight = linesHeight.get(i);
            int gap = linesGap.get(i);

            int x = gap;
            for(View child:lineViews){
                int childHeight = child.getMeasuredHeight();
                int childWidth = child.getMeasuredWidth();
                int verticalMargin = 0;//(lineHeight - childHeight) / 2;

                child.layout(x,
                        y+verticalMargin,
                        x+childWidth,
                        y+verticalMargin+childHeight
                );
                x+=childWidth+gap;
            }
            y+=lineHeight+ verticalSpacing;
        }
    }


    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return super.checkLayoutParams(p) && p instanceof LayoutParams;
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return (int)((dp * displayMetrics.density) + 0.5);
    }

    public static class LayoutParams extends MarginLayoutParams {
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

    }
}
