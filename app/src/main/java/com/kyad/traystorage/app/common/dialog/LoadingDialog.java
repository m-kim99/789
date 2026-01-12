package com.kyad.traystorage.app.common.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;

import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.kyad.traystorage.R;
import com.kyad.traystorage.databinding.DialogLoadingBinding;

public class LoadingDialog extends Dialog {

    private DialogLoadingBinding binding;

    public LoadingDialog(Context context) {
        super(context, R.style.DialogCustomTheme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    public void onBackPressed() {
    }

    private void init() {
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.dialog_loading, null, false);
        setContentView(binding.getRoot());
        initView();
    }

    private void initView() {

    }

    boolean closed = false;
    void doAnimation(){
        if(this.closed)
            return;
        new Handler(Looper.getMainLooper()).postDelayed(this::changeIcons, 100);
    }
    int iconIndex = 0;
    void changeIcons(){
        int rids[] = new int[]{R.drawable.loading1, R.drawable.loading2,R.drawable.loading3};
        binding.loading1.setImageResource(rids[iconIndex%3]);
        binding.loading2.setImageResource(rids[(iconIndex+1)%3]);
        binding.loading3.setImageResource(rids[(iconIndex+2)%3]);
        iconIndex++;
        doAnimation();
    }

    @Override
    public void show() {
        try {
            if (!isShowing()) {
                super.show();
                doAnimation();
            }
        }catch (Exception e){}
    }

    @Override
    public void hide() {
        try {
            closed = true;
            dismiss();
        }catch (Exception e){}
    }
}
