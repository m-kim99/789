package com.kyad.traystorage.app.main;

import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.kyad.traystorage.R;
import com.kyad.traystorage.app.common.PhotoSelectActivity;
import com.kyad.traystorage.app.common.dialog.LoadingDialog;
import com.kyad.traystorage.app.common.util.PrefMgr;
import com.kyad.traystorage.app.splash.LoadingActivity;
import com.kyad.traystorage.app.splash.PermissionActivity;
import com.kyad.traystorage.databinding.ActivityDocumentImageviewBinding;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import base.BaseBindingActivity;
import base.BaseEvent;
import widgets.viewpager.BasePagerAdapter;

public class DocumentImageViewActivity extends BaseBindingActivity<ActivityDocumentImageviewBinding> {
    Integer firstImageIndex = 0;
    @Override
    public int getLayout() {
        return R.layout.activity_document_imageview;
    }

    @Override
    protected Dialog loadingDialog() {
        return new LoadingDialog(this);
    }

    BasePagerAdapter adapter;

    @Override
    public void init() {
        binding.setActivity(this);

        Intent intent = getIntent();
        firstImageIndex = intent.getIntExtra("index", 0);
        String[] imageUrls = intent.getStringArrayExtra("urls");

        adapter = new BasePagerAdapter(getSupportFragmentManager());
        binding.viewPager.setAdapter(adapter);

        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                binding.labelPage.setText("" + (position + 1) + "/" + adapter.getCount());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        List<Fragment> fragments = new ArrayList<>();
        for (String url : imageUrls) {
            DocumentImageFragment frag = new DocumentImageFragment(R.layout.image_page, url);
            fragments.add(frag);
        }
        adapter.setFragments(fragments);

        if (firstImageIndex > 0)
            binding.viewPager.setCurrentItem(firstImageIndex);
        else
            binding.labelPage.setText("1/" + adapter.getCount());

    }

    public void onCloseClick()
    {
        finish();
    }
}
