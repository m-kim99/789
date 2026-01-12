package com.kyad.traystorage.app.splash;

import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;

import com.kyad.traystorage.R;
import com.kyad.traystorage.app.common.dialog.LoadingDialog;
import com.kyad.traystorage.app.common.util.PrefMgr;
import com.kyad.traystorage.databinding.ActivityIntroBinding;

import java.util.ArrayList;
import java.util.List;

import base.BaseBindingActivity;
import widgets.viewpager.BasePagerAdapter;

import static android.content.Context.MODE_PRIVATE;

public class IntroActivity extends BaseBindingActivity<ActivityIntroBinding> {

    @Override
    public int getLayout() {
        return R.layout.activity_intro;
    }

    @Override
    protected Dialog loadingDialog() {
        return new LoadingDialog(this);
    }

    BasePagerAdapter adapter;
    @Override
    public void init() {

        binding.setActivity(this);

        adapter = new BasePagerAdapter(getSupportFragmentManager());
        binding.viewPager.setAdapter(adapter);
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new IntroFragment(R.layout.intro_page1));
        fragments.add(new IntroFragment(R.layout.intro_page2));
        adapter.setFragments(fragments);

        new Handler(Looper.getMainLooper()).postDelayed(this::nextPage, 2000);
    }

    public void nextPage(){
        if(binding.viewPager.getCurrentItem() < 1){
            binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem()+1);
        }else{
            goLoading();
            return;
        }
        new Handler(Looper.getMainLooper()).postDelayed(this::nextPage, 2000);
    }

    public void goNext() {
        PrefMgr prefMgr = new PrefMgr(getSharedPreferences(PrefMgr.traystorage_PREFS, MODE_PRIVATE));
        prefMgr.put(PrefMgr.FIRST_START, false);
        goLoading();
    }

    void goLoading() {
        if (LoadingActivity.getInstance() != null){
            return;
        }
        PrefMgr prefMgr = new PrefMgr(getSharedPreferences(PrefMgr.traystorage_PREFS, MODE_PRIVATE));
        prefMgr.put(PrefMgr.FIRST_START, false);
        Intent intent = new Intent(this, LoadingActivity.class);
        startActivity(intent);
        finish();
    }

}
