package com.kyad.traystorage.app.setting;

import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kyad.traystorage.R;
import com.kyad.traystorage.app.common.dialog.LoadingDialog;
import com.kyad.traystorage.databinding.ActivityLicenseBinding;

import base.BaseBindingActivity;

public class LicenseActivity extends BaseBindingActivity<ActivityLicenseBinding> {
    @Override
    public int getLayout() {
        return R.layout.activity_license;
    }
    @Override
    protected Dialog loadingDialog() {
        return new LoadingDialog(this);
    }

    @Override
    public void init() {
        initView();
    }

    private void initView() {
        binding.setActivity(this);
        binding.listView.setAdapter(new ListAdapter());
        binding.webView.loadUrl("http://traystorage.kr/server/api/App/term?type=opensource");
    }

    public void onConfirm(){}


    private class ListAdapter extends RecyclerView.Adapter {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_license, viewGroup, false);
            RecyclerView.ViewHolder viewHolder = new ListAdapter.ListItemViewHolder(view);
            //view.setOnClickListener(v -> goDetail(i));
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            //((ListItemViewHolder) viewHolder).bindItem(noticeList.get(i), i);
        }

        @Override
        public int getItemCount() {
            return 20;
        }

        private class ListItemViewHolder extends RecyclerView.ViewHolder {
            public ListItemViewHolder(View view) {
                super(view);
            }
        }
    }
}
