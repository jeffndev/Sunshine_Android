package com.example.jnewel200.sunshine.app;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by jeffreynewell1 on 4/17/15.
 */
public class JeffView extends View {

    public JeffView(Context context){
        super(context);
    }
    public JeffView(Context context, AttributeSet attrs){
        super(context,attrs);
    }
    public JeffView(Context context, AttributeSet attrs, int defaultStyle){
        super(context,attrs, defaultStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int wMySpec=0, hMySpec=0;
        int wSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSizeGiven = MeasureSpec.getSize(widthMeasureSpec);
        int hSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int hSizeGiven = MeasureSpec.getSize(heightMeasureSpec);

        if(wSpecMode == MeasureSpec.EXACTLY){
            wMySpec = wSizeGiven;
        }else if(wSpecMode == MeasureSpec.AT_MOST){
            wMySpec = wSizeGiven;
        }
        if(hSpecMode == MeasureSpec.EXACTLY){
            hMySpec = hSizeGiven;
        }else if(hSpecMode == MeasureSpec.AT_MOST){
            hMySpec = hSizeGiven;
        }

        setMeasuredDimension(wMySpec, hMySpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
