package com.kyad.traystorage.app.main;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableList;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kyad.traystorage.R;
import com.kyad.traystorage.app.common.dialog.AlertDialog;
import com.kyad.traystorage.databinding.ItemDocImageBinding;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import helper.RecyclerViewHelper;

public class DocumentImageListAdapter extends RecyclerView.Adapter {
    public ObservableList<String> imageUrlList = new ObservableArrayList<>();
    public ObservableBoolean isItemClose = new ObservableBoolean(false);
    private Listener listener;

    public interface Listener {
        void onClickImage(String url);
    }

    DocumentImageListAdapter(Boolean isItemClose) {
        RecyclerViewHelper.linkAdapterAndObserable(this, imageUrlList);
        this.isItemClose.set(isItemClose);
    }

    public DocumentImageListAdapter setListener(@NotNull DocumentImageListAdapter.Listener listener) {
        this.listener = listener;
        return this;
    }

    public void addImageList(List<String> imageList) {
        this.imageUrlList.addAll(imageList);
    }

    public void setImageList(List<String> imageList) {
        this.imageUrlList.clear();
        this.imageUrlList.addAll(imageList);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_doc_image, viewGroup, false);
        RecyclerView.ViewHolder viewHolder = new ListItemViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ((ListItemViewHolder) viewHolder).bindItem(imageUrlList.get(i), i);
    }

    @Override
    public int getItemCount() {
        return imageUrlList.size();
    }

    public class ListItemViewHolder extends RecyclerView.ViewHolder {
        ItemDocImageBinding binding;
        String itemValue;
        public ListItemViewHolder(View view) {
            super(view);
            binding = DataBindingUtil.bind(view);
            binding.setHolder(this);
        }
        public void onClick(){
            if (listener != null)
                listener.onClickImage(itemValue);
        }
        public void onCloseClick(){
            imageUrlList.remove(itemValue);
        }
        public void bindItem(String imageUrl, int i) {
            itemValue = imageUrl;
            binding.deleteButton.setVisibility(isItemClose.get() ? View.VISIBLE : View.GONE);
            Glide.with(binding.imageView).load(imageUrl).centerCrop().into(binding.imageView);
        }
    }
}