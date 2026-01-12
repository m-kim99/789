package com.kyad.traystorage.app.common.font;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;

import com.kyad.traystorage.R;

public class FontEditText extends AppCompatEditText {
    public FontEditText(Context context) {
        super(context);
    }

    public FontEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(context, attrs);
    }

    public FontEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCustomFont(context, attrs);
    }

    public String getPlanText() {
        return getText().toString();
    }

    private void setCustomFont(Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.CustomFont);
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
