package com.kyad.traystorage.app.common;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableChar;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.databinding.ObservableList;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

//import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
//import com.kyad.traystorage.BuildConfig;
import com.kyad.traystorage.R;
import com.kyad.traystorage.app.common.dialog.LoadingDialog;
import com.kyad.traystorage.app.common.util.Utils;
import com.kyad.traystorage.app.main.DocumentEditActivity;
import com.kyad.traystorage.databinding.ActivityPhotoSelectBinding;
import com.kyad.traystorage.databinding.ItemLabelColorBinding;
import com.kyad.traystorage.databinding.ItemPhotoSelectBinding;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import base.BaseBindingActivity;
import base.BaseEvent;
import helper.RecyclerViewHelper;

public class PhotoSelectActivity extends BaseBindingActivity<ActivityPhotoSelectBinding> {
    public ObservableList<PhotoSelectModel> photoModelList = new ObservableArrayList<>();
    List<PhotoSelectModel> selectedImages = new ArrayList<>();

    @Override
    public int getLayout() {
        return R.layout.activity_photo_select;
    }
    @Override
    protected Dialog loadingDialog() {
        return new LoadingDialog(this);
    }

    public boolean isSingle = false;
    @Override
    public void init() {
        initView();

        isSingle = getIntent().getIntExtra("is_single", 0) == 1;
        getAllPhotos();
    }

    private void initView() {
        binding.setActivity(this);
        ImageListAdapter imageListAdapter = new ImageListAdapter();
        RecyclerViewHelper.linkAdapterAndObserable(imageListAdapter, photoModelList);
        binding.imageList.setAdapter(imageListAdapter);
    }

    private final String[] projection = new String[]{
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
    };
    private static File makeSafeFile(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        try {
            return new File(path);
        } catch (Exception ignored) {
            return null;
        }
    }
    private void getAllPhotos() {

        photoModelList.clear();
        List<PhotoSelectModel> photoList = new ArrayList<>();

        Cursor cursor;
       {
            cursor = this.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                    null, null, MediaStore.Images.Media.DATE_ADDED);
        }

        if (cursor == null) {
            return;
        }

        if (cursor.moveToLast()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndex(projection[0]));
                String name = cursor.getString(cursor.getColumnIndex(projection[1]));
                String path = cursor.getString(cursor.getColumnIndex(projection[2]));
                String bucket = cursor.getString(cursor.getColumnIndex(projection[3]));

                File file = makeSafeFile(path);
                if (file != null) {
                    String url = file.getAbsolutePath();
                    if(url.toLowerCase().endsWith(".gif"))
                        continue;
                    PhotoSelectModel model = new PhotoSelectModel(url);
                    model.isSingle.set(isSingle);
                    photoList.add(model);
                }

            } while (cursor.moveToPrevious());
        }
        cursor.close();

        photoModelList.addAll(photoList);
    }

    public void onCompleteClick() {
        List<String> photoUrlList = new ArrayList<>();
        for (PhotoSelectModel model : selectedImages) {

               photoUrlList.add(model.url);
        }
        EventBus.getDefault().post(new BaseEvent.PhotoSelectCompleteEvent(photoUrlList));
        finish();
    }

    public class PhotoSelectModel{
        public ObservableBoolean isSelected = new ObservableBoolean(false);
        public String url;
        public ObservableBoolean isSingle = new ObservableBoolean(false);
        public ObservableField<String> selectedIndex = new ObservableField<>("");

        PhotoSelectModel(String url) {
            this.url = url;
        }
    }

    int selectCount = 0;
    MediaManager mmanager;
    public class ImageListAdapter extends RecyclerView.Adapter {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            ViewDataBinding itemBinding;
            itemBinding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.item_photo_select, viewGroup, false);
            RecyclerView.ViewHolder viewHolder = new ListItemViewHolder((ItemPhotoSelectBinding) itemBinding);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            ((ListItemViewHolder) viewHolder).bindItem(i);
        }

        @Override
        public int getItemCount() {
            return photoModelList.size() + 1;
        }

        public class ListItemViewHolder extends RecyclerView.ViewHolder {
            ItemPhotoSelectBinding itemBinding;
            public ListItemViewHolder(ItemPhotoSelectBinding itemBinding) {
                super(itemBinding.getRoot());
                this.itemBinding = itemBinding;
                this.itemBinding.setHolder(this);
            }

            public void onCameraClick() {
                mmanager = new MediaManager(PhotoSelectActivity.this);
                mmanager.setCropEnable(false);
                mmanager.setMediaCallback(new MediaManager.MediaCallback() {
                    @Override
                    public void onImage(Uri uri, Bitmap bitmap) {
                        String fpath = mmanager.getFileFromUri(uri).getAbsolutePath();
                        if(fpath!=null) {
                            List<String> photoUrlList = new ArrayList<>();
                            photoUrlList.add(fpath);
                            EventBus.getDefault().post(new BaseEvent.PhotoSelectCompleteEvent(photoUrlList));
                            finish();
                        }
                    }

                    @Override
                    public void onVideo(Uri video, Uri thumb, Bitmap thumbBitmap) {

                    }

                    @Override
                    public void onFailed(int code, String err) {

                    }

                    @Override
                    public void onDelete() {

                    }
                });
                mmanager.openCamera();
            }

            public void onClick() {
                if(itemBinding.getData().isSelected.get()){
                    selectCount--;
                }else
                    selectCount++;
                if(selectCount>0 && isSingle) {
                    selectedImages.add(itemBinding.getData());
                    onCompleteClick();
                    return;
                }
                if(selectCount>30){
                    Utils.showCustomToast(PhotoSelectActivity.this, R.string.image_30);
                    return;
                }

                if(itemBinding.getData().isSelected.get()){
                    selectedImages.remove(itemBinding.getData());
                    itemBinding.getData().isSelected.set(false);
                }else {
                    selectedImages.add(itemBinding.getData());
                    itemBinding.getData().isSelected.set(true);
                }
                if(selectCount>0){
                    binding.btnComplete.setVisibility(View.VISIBLE);
                }else
                    binding.btnComplete.setVisibility(View.GONE);

                int nSelIndex = 1;
                for(PhotoSelectModel c:photoModelList){
                    c.selectedIndex.set("");
                }
                for(PhotoSelectModel c:selectedImages){
                    c.selectedIndex.set("" + nSelIndex);
                    nSelIndex++;
                }
            }

            public void bindItem(int i) {
                itemBinding.btnCamera.setVisibility((i == 0) ? View.VISIBLE : View.GONE);
                itemBinding.viewPhoto.setVisibility((i == 0) ? View.GONE : View.VISIBLE);
                if (i > 0) {
                    itemBinding.setData(photoModelList.get(i - 1));
                    Glide.with(PhotoSelectActivity.this)
                            .load(photoModelList.get(i - 1).url)
                            .centerCrop()
                            .into(itemBinding.imageView);
                }
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(mmanager!=null){
            mmanager.onActivityResult(requestCode, resultCode, data);

        }
    }
}
