package com.kyad.traystorage.app.common.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.kyad.traystorage.R;
import com.kyad.traystorage.databinding.DialogAlertBinding;

import org.jetbrains.annotations.NotNull;

public class AlertDialog extends Dialog {

    private static DialogAlertBinding binding;
    private ActionListener listener;

    public interface ActionListener {
        void onConfirm();
    }

    AlertDialog(Context context) {
        super(context, R.style.DialogCustomTheme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.dialog_alert, null, false);
        setContentView(binding.getRoot());
        initView();
    }

    private void initView() {
        binding.setDialog(this);
    }

    /************************************************************
     *  Public
     ************************************************************/

    public static AlertDialog show(Context context) {
        AlertDialog dialog = new AlertDialog(context);
        dialog.show();
        return dialog;
    }

    public AlertDialog setText(String title, String message, String confirm) {
        return setText(title, message, confirm, null);
    }
    public AlertDialog setText(String title, String message, String confirm, String cancel) {
        if(title==null || title.isEmpty())
            binding.title.setVisibility(View.GONE);
        else
            binding.title.setText(title);
        if(message==null || message.isEmpty())
            binding.message.setVisibility(View.GONE);
        else
            binding.message.setText(message);
        binding.btnConfirm.setText(confirm);
        if(cancel==null || cancel.isEmpty())
            binding.btnCancel.setVisibility(View.GONE);
        else
            binding.btnCancel.setText(cancel);
        return this;
    }

    public AlertDialog setListener(@NotNull ActionListener listener) {
        this.listener = listener;
        return this;
    }

    public void onClickConfirm() {
        listener.onConfirm();
        dismiss();
    }
    public void onClickCancel() {
        cancel();
    }
}
