package com.kyad.traystorage.app.splash;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import base.BaseFragment;

public class IntroFragment extends BaseFragment {

    int rid;
    public IntroFragment(int rid){
        super();
        this.rid = rid;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(rid, container, false);

    }
}
