package com.kyad.traystorage.app.common.font;

import android.content.Context;
import android.graphics.Typeface;

import androidx.core.content.res.ResourcesCompat;

import com.kyad.traystorage.R;

public class FontWidget {
    public static Typeface getTypeface(Context ctx, String fontName) {
        int defaultFont = R.font.notosanscjkkr_regular;
        if (fontName == null) {
            fontName = "";
        }

        switch (fontName) {
            case "notosanscjkkr_bold.otf":
                defaultFont = R.font.notosanscjkkr_bold;
                break;
            case "notosanscjkkr_medium.otf":
                defaultFont = R.font.notosanscjkkr_medium;
                break;
            case "notosanscjkkr_regular.otf":
                defaultFont = R.font.notosanscjkkr_regular;
                break;
        }

        return ResourcesCompat.getFont(ctx, defaultFont);
    }
}
