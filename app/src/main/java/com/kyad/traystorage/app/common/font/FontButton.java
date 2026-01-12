package com.kyad.traystorage.app.common.font;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.kyad.traystorage.R;

import androidx.appcompat.widget.AppCompatButton;

public class FontButton extends AppCompatButton {
    public FontButton(Context context) {
        super(context);
    }

    public FontButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(context, attrs);
    }

    public FontButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCustomFont(context, attrs);
    }

    private void setCustomFont(Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, com.kyad.traystorage.R.styleable.CustomFont);
        String customFont = a.getString(R.styleable.CustomFont_customFont);
        setCustomFont(ctx, customFont);
        a.recycle();
    }

    private boolean setCustomFont(Context ctx, String fontName) {
        Typeface typeface = FontWidget.getTypeface(ctx, fontName);
        if (typeface != null) {
            setTypeface(typeface);
        }
        return true;
    }
}
