package com.kyad.traystorage.app.common;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.kyad.traystorage.App;
import com.kyad.traystorage.R;
import com.kyad.traystorage.app.common.font.FontTextView;

import helper.Util;

public class bindAdapter {
    @BindingAdapter("image")
    public static void setImage(ImageView view, String image) {
        String imagePath = image;
        if (imagePath == null) return;
        if (!imagePath.contains("http")) {
            imagePath = "http://" + imagePath;
        }
        Glide.with(view).load(imagePath).into(view);
    }

    @BindingAdapter("isSelect")
    public static void setSelect(
            View view,
            Boolean isSelect
    ) {
        if (view == null || isSelect == null) return;
        view.setSelected(isSelect);
    }

    @BindingAdapter("team_text_color")
    public static void setTeamTextColor(
            FontTextView view,
            Integer team_text_color
    ) {
        if (view == null || team_text_color == null) return;

        switch (team_text_color) {
            case 1:
                view.setTextColor(Util.getColor(App.get(), R.color.focus_color));
                break;
            case 2:
                view.setTextColor(Util.getColor(App.get(), R.color.C39d0ff));
                break;
            case 3:
                view.setTextColor(Util.getColor(App.get(), R.color.Cceec17));
                break;
            case 4:
                view.setTextColor(Util.getColor(App.get(), R.color.C0047d2));
                break;
        }
    }
}

