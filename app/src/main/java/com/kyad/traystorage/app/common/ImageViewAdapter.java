package com.kyad.traystorage.app.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.kyad.traystorage.R;

import java.util.ArrayList;
import java.util.List;

public class ImageViewAdapter extends PagerAdapter {

    public ActionListener listener;
    public List<Integer> imageIdList = new ArrayList<>();
    public List<String> imageUrlList = new ArrayList<>();
    public List<String> linkUrlList = new ArrayList<>();

    @Override
    public int getCount() {
        return imageIdList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Context context = container.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_image_view, null);
        ImageView imageView = view.findViewById(R.id.image);

        // use if the ad image is from the url of api response.

//        Glide.with(context)
//                .load(imageList.get(position))
//                .centerCrop()
//                .into(imageView);

        imageView.setImageResource(imageIdList.get(position));

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (linkUrlList.size() > 0)
                    listener.onItem(imageView, position, linkUrlList.get(position));
                else
                    listener.onItem(imageView, position, "");
            }
        });

        container.addView(view);
        return view;
    }

    public interface ActionListener {
        void onItem(ImageView imageView, Integer pos, String url);
    }

    public ImageViewAdapter(ActionListener listener) {
        this.listener = listener;
    }

    public void setImageListData(List<Integer> idList, List<String> urlList) {
        if (idList != null) {
            imageIdList.clear();
            imageIdList.addAll(idList);
        }

        if (urlList != null) {
            linkUrlList.clear();
            linkUrlList.addAll(urlList);

        }

        notifyDataSetChanged();
    }


}
