package com.kyad.traystorage.app.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.kyad.traystorage.R;
import com.kyad.traystorage.app.common.PhotoSelectActivity;

import base.BaseFragment;

public class DocumentImageFragment extends BaseFragment {

    int rid;
    String url;
    public DocumentImageFragment(int rid, String url){
        super();
        this.rid = rid;
        this.url = url;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View theView =  inflater.inflate(rid, container, false);

        ImageView imgView = theView.findViewById(R.id.image_view);

        Glide.with(getContext())
                .load(url)
               // .centerCrop()
                .into(imgView);

        return theView;
    }
}
