package com.kyad.traystorage.app.splash;

import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.fragment.app.Fragment;

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
        fragments.add(new IntroFragment(R.layout.fragment_intro1));
        fragments.add(new IntroFragment(R.layout.fragment_intro2));
        fragments.add(new IntroFragment(R.layout.fragment_intro3));
        fragments.add(new IntroFragment(R.layout.fragment_intro4));
        fragments.add(new IntroFragment(R.layout.fragment_intro5));
        adapter.setFragments(fragments);
        
        // 자동 넘김 제거
        // new Handler(Looper.getMainLooper()).postDelayed(this::nextPage, 2000);
    }

    public void nextPage() {
        int currentItem = binding.viewPager.getCurrentItem();
        if(currentItem < 4) { // 0~4 (5개 페이지)
            binding.viewPager.setCurrentItem(currentItem + 1);
        } else {
            // 마지막 페이지에서 "확인" 버튼 클릭 시
            goLoading();
        }
    }

    public void prevPage() {
        int currentItem = binding.viewPager.getCurrentItem();
        if(currentItem > 0) {
            binding.viewPager.setCurrentItem(currentItem - 1);
        }
    }

    public void goNext() {
        // 건너뛰기 (닫기 버튼)
        PrefMgr prefMgr = new PrefMgr(getSharedPreferences(PrefMgr.traystorage_PREFS, MODE_PRIVATE));
        prefMgr.put(PrefMgr.FIRST_START, false);
        goLoading();
    }

    public void goLoading() {
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
