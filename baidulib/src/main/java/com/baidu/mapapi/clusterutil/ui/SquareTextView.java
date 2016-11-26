/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */

package com.baidu.mapapi.clusterutil.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

public class SquareTextView extends TextView {
    private int mOffsetTop = 0;
    private int mOffsetLeft = 0;
    private int newHeight;
    private int width;

    public SquareTextView(Context context) {
        super(context);
    }

    public SquareTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        int height = getMeasuredHeight();
        //this.height = height;
        //int dimension = Math.max(width, height);
        /*if (width > height) {
            mOffsetTop = width - height;
            mOffsetLeft = 0;
        } else {
            mOffsetTop = 0;
            mOffsetLeft = height - width;
        }*/
        newHeight = width * 136 /  83;

        setMeasuredDimension(width, newHeight);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.translate(width / 2, 0);
        super.draw(canvas);
    }
}
