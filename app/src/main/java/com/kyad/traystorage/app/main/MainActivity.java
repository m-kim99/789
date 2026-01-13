package com.kyad.traystorage.app.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableInt;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kyad.traystorage.App;
import com.kyad.traystorage.R;
import com.kyad.traystorage.app.Constants;
import com.kyad.traystorage.app.auth.LoginHomeActivity;
import com.kyad.traystorage.app.common.Common;
import com.kyad.traystorage.app.common.dialog.LoadingDialog;
import com.kyad.traystorage.app.common.util.Utils;
import com.kyad.traystorage.app.setting.InquiryActivity;
import com.kyad.traystorage.app.setting.InviteActivity;
import com.kyad.traystorage.app.setting.NoticeActivity;
import com.kyad.traystorage.app.setting.NoticeDetailActivity;
import com.kyad.traystorage.app.setting.ProfileManageActivity;
import com.kyad.traystorage.app.setting.SettingActivity;
import com.kyad.traystorage.data.DataManager;
import com.kyad.traystorage.data.model.ModelDocument;
import com.kyad.traystorage.data.model.ModelPopupInfo;
import com.kyad.traystorage.data.model.ModelUser;
import com.kyad.traystorage.databinding.ActivityMainBinding;
import com.kyad.traystorage.databinding.DialogPopupBinding;
import com.kyad.traystorage.databinding.ItemDocumentBinding;

import com.kyad.traystorage.app.chatbot.ChatbotFragment;

import base.BaseBindingActivity;
import helper.RecyclerViewHelper;

public class MainActivity extends BaseBindingActivity<ActivityMainBinding> {
    public MainViewModel viewModel;
    private DocumentListAdapter docListAdapter;
    private static DialogPopupBinding popupBinding;
    private AlertDialog popupDialog;
    public ObservableInt showType = new ObservableInt(0);
    private String lastSearchKey = "";
    private ChatbotFragment chatbotFragment;
    private boolean isChatbotVisible = false;

    @Override
    public int getLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected Dialog loadingDialog() {
        return new LoadingDialog(this);
    }

    /************************************************************
     *  ApiListener
     ************************************************************/
    private final MainApiListener apiListener() {
        return new MainApiListener() {
            @Override
            public void onError(String msg) {
                Utils.showCustomToast(MainActivity.this, msg, Toast.LENGTH_SHORT);
            }

            @Override
            public void onLoginTokenError() {
                Utils.showCustomToast(MainActivity.this, R.string.login_token_error);
                Intent intent = new Intent(MainActivity.this, LoginHomeActivity.class);
                startActivity(intent);
                App.get().finishAllActivity();
            }

            public void onGetDocuments() {
                binding.docTittle.setText(binding.backLayout.getVisibility() == View.VISIBLE ? R.string.search_result : R.string.document1);
                binding.docCount.setText("" + viewModel.allList.size() + "건");
                if (lastSearchKey.isEmpty())
                    showType.set(viewModel.allList.size() > 0 ? 2 : 1);
                else
                    showType.set(2);

                if (viewModel.allList.size() == 0) {
                    binding.docList.setVisibility(View.GONE);
                    binding.layoutNotResult.setVisibility(View.VISIBLE);
                    binding.textNoResult.setText("'" + lastSearchKey + "'" + getResources().getString(R.string.search_not_result));
                } else {
                    binding.docList.setVisibility(View.VISIBLE);
                    binding.layoutNotResult.setVisibility(View.GONE);
                }
                updateCurrentList();
            }
        };

    }

    int currentCount = 0;

    public void updateCurrentList() {
        viewModel.documentList.clear();
        for (int i = 0; i < currentCount + 20; i++) {
            if (i < viewModel.allList.size())
                viewModel.documentList.add(viewModel.allList.get(i));
        }
        currentCount += 20;
    }

    // 테스트 모드 체크 헬퍼
    private boolean isTestMode() {
        ModelUser user = DataManager.get().getModel(ModelUser.class);
        return (user != null && user.id == 999);
    }

    @Override
    public void init() {
        initViewModel();
        initView();
        setupKeyboard(binding.drawerLayout);

        // 테스트 모드: API 호출 건너뛰기
        if (isTestMode()) {
            showType.set(2); // 문서 목록 UI 표시
            binding.setUser(DataManager.get().getModel(ModelUser.class));
            binding.docCount.setText("0건");
            return;
        }

        viewModel.getPopupInfos();

        if (!Common.gDocumentID.equals("")) {
            Integer docId = Integer.parseInt(Common.gDocumentID);
            Common.gDocumentID = "";
            //goDetail(docId);
            Intent intent = new Intent(this, DocumentDetailActivity.class);
            intent.putExtra("doc_id", docId);
            startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        
        binding.setUser(DataManager.get().getModel(ModelUser.class));
        
        // 테스트 모드: API 호출 건너뛰기
        if (isTestMode()) {
            showType.set(2); // 문서 목록 UI 표시
            binding.docCount.setText("0건");
            return;
        }
        
        viewModel.getDocuments(lastSearchKey);
        Glide.with(this).load(binding.getUser().profile_image).placeholder(R.drawable.icon_c_user_60).into(binding.imgAvatar);
    }

    private void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.apiListener = apiListener();

        //Boolean isNeverPopup = new PrefManager(MainActivity.this).getBoolean("never_popup", false);
        //if (!isNeverPopup)
        {
            viewModel.popupInfoList.observe(this, modelPopupInfos -> {
                for (ModelPopupInfo popupInfo : modelPopupInfos) {
                    showPopupInfo(popupInfo);
                    break;
                }
            });
        }
    }

    private void initView() {
        binding.setActivity(this);

        binding.docCount.setText("0");
        docListAdapter = new DocumentListAdapter();
        binding.docList.setAdapter(docListAdapter);

        popupBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_popup, null, false);
        popupBinding.setPresenter(new PopupInfoPresenter());

        binding.textSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    onSearchClick();
                }
                return false;
            }
        });

        binding.drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                binding.textSearch.setEnabled(false);
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                binding.textSearch.setEnabled(true);
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });


        //scroll list (view more)
        binding.docList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (binding.docList.getAdapter().getItemCount() != 0) {
                    int lastPos = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                    if ((lastPos != RecyclerView.NO_POSITION) && (lastPos == binding.docList.getAdapter().getItemCount() - 1) && (currentCount < viewModel.allList.size())) {
                        updateCurrentList();
                    }
                }
            }
        });
    }

    private boolean bFinish = false;

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(binding.navMenu)) {
            binding.drawerLayout.closeDrawer(binding.navMenu);
            return;
        }
        if (isChatbotVisible) {
            hideChatbot();
            return;
        }
        if (binding.backLayout.getVisibility() == View.VISIBLE) {
            binding.backLayout.setVisibility(View.GONE);
            binding.titleLayout.setVisibility(View.VISIBLE);
            binding.fabContainer.setVisibility(View.VISIBLE);
            binding.textSearch.setText("");
            lastSearchKey = "";
            viewModel.getDocuments("");
            return;
        }
        if (!bFinish) {
            bFinish = true;
            Utils.showCustomToast(this, (R.string.app_finish_message));
            new Handler().postDelayed(() -> bFinish = false, 2000);
        } else {
            finish();
        }
    }

    public void onSearchTextChanged() {
        binding.deleteText.setVisibility(binding.textSearch.getPlanText().isEmpty() ? View.GONE : View.VISIBLE);
    }

    public void onClearSearchClick() {
        if (!binding.textSearch.isEnabled())
            return;
        lastSearchKey = "";
        binding.deleteText.setVisibility(View.GONE);
        binding.textSearch.setText("");
    }

    public void onSearchClick() {
        if (!binding.textSearch.isEnabled())
            return;
        currentCount = 0;
        lastSearchKey = binding.textSearch.getPlanText();
        if (lastSearchKey.isEmpty()) {
            Utils.showCustomToast(this, R.string.search_input);
            return;
        }
        viewModel.getDocuments(lastSearchKey);
        binding.backLayout.setVisibility(View.VISIBLE);
        binding.titleLayout.setVisibility(View.GONE);
        binding.fabContainer.setVisibility(View.GONE);
    }

    void goDetail(Integer docId) {
        if (!binding.textSearch.isEnabled())
            return;
        Intent intent = new Intent(this, DocumentDetailActivity.class);
        intent.putExtra("doc_id", docId);
        startActivity(intent);
    }

    public class DocumentListAdapter extends RecyclerView.Adapter {

        public DocumentListAdapter() {
            RecyclerViewHelper.linkAdapterAndObserable(this, viewModel.documentList);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_document, viewGroup, false);
            RecyclerView.ViewHolder viewHolder = new DocumentListAdapter.ListItemViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            ((ListItemViewHolder) viewHolder).bindItem(viewModel.documentList.get(i), i);
        }

        @Override
        public int getItemCount() {
            return viewModel.documentList.size();
        }

        public class ListItemViewHolder extends RecyclerView.ViewHolder {
            ItemDocumentBinding binding;

            public ListItemViewHolder(View view) {
                super(view);
                binding = DataBindingUtil.bind(view);
                binding.setHolder(this);
            }

            public void onClick(Integer docId) {
                goDetail(docId);
            }

            public void bindItem(ModelDocument document, int position) {
                binding.setData(document);
                binding.imgLabel.setImageResource(Constants.LabelColorArray.valueOf("color" + document.label).value);
                if (document.image_list.size() > 0)
                    Glide.with(MainActivity.this).load(document.image_list.get(0)).into(binding.imageView);
                else
                    Glide.with(MainActivity.this).load("").into(binding.imageView);
            }
        }
    }

    /*
     * Popup
     */

    public void showPopupInfo(ModelPopupInfo popupInfo) {
        popupBinding.getPresenter().setPopupInfo(popupInfo);
        popupDialog = new AlertDialog.Builder(this).setView(popupBinding.getRoot()).show();
    }

    public class PopupInfoPresenter {
        public ModelPopupInfo popupInfo;

        public void setPopupInfo(ModelPopupInfo info) {
            popupInfo = info;
            if (popupInfo.content_type == 0)
                Glide.with(popupBinding.imageView).load(popupInfo.content_image).into(popupBinding.imageView);
            else
                popupBinding.webView.loadData(popupInfo.content, "text/html", "UTF8");
            if (info.close_method == 0) {
                popupBinding.btnNever.setVisibility(View.GONE);
            }
        }

        public void onClickImage() {
            viewModel.clickPopup(popupInfo.id);
            //popupInfo.move_type;
            //popupInfo.move_path
            popupDialog.dismiss();
            if (popupInfo.move_type == 0) {//outerlink
                try {
                    startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse(popupInfo.move_path))
                    );
                } catch (Exception e) {
                    Utils.showCustomToast(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
                }
            } else if (popupInfo.move_type == 1) {//notice
                Intent intent = new Intent(MainActivity.this, NoticeDetailActivity.class);
                intent.putExtra("code", popupInfo.move_path);
                startActivity(intent);
            } else if (popupInfo.move_type == 2) {//faq
                /*Intent intent = new Intent(MainActivity.this, FaqDetailActivity.class);
                intent.putExtra("code", popupInfo.move_path);
                startActivity(intent);*/
            }
        }

        public void onClickNever() {
            viewModel.viewPopup(popupInfo.id);
            //new PrefManager(MainActivity.this).put("never_popup", true);
            popupDialog.dismiss();
        }

        public void onClickClose() {
            if (popupInfo.close_method == 0)
                viewModel.viewPopup(popupInfo.id);
            popupDialog.dismiss();
        }
    }

    /*
     * Sliding Menu
     */

    public void onMenu() {
        binding.drawerLayout.openDrawer(binding.navMenu);
    }

    public void onProfile() {
        Intent intent = new Intent(this, ProfileManageActivity.class);
        startActivity(intent);
        onBackPressed();
    }

    public void onInvite() {
        Intent intent = new Intent(this, InviteActivity.class);
        startActivity(intent);
        onBackPressed();
    }

    public void onContactus() {
        Intent intent = new Intent(this, InquiryActivity.class);
        startActivity(intent);
        onBackPressed();
    }

    public void onNotice() {
        Intent intent = new Intent(this, NoticeActivity.class);
        startActivity(intent);
        onBackPressed();
    }

    public void onSetting() {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
        onBackPressed();
    }

    public void onRegisterClick() {
        if (!binding.textSearch.isEnabled())
            return;
        Intent intent = new Intent(this, DocumentEditActivity.class);
        startActivity(intent);
    }

    /*
     * Chatbot
     */

    public void onChatbotClick() {
        if (!binding.textSearch.isEnabled())
            return;
        if (isChatbotVisible) {
            hideChatbot();
        } else {
            showChatbot();
        }
    }

    private void showChatbot() {
        if (chatbotFragment == null) {
            chatbotFragment = new ChatbotFragment();
            chatbotFragment.setOnCloseListener(() -> hideChatbot());
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.chatbot_container, chatbotFragment)
                .commit();

        binding.chatbotContainer.setVisibility(View.VISIBLE);
        binding.chatbotContainer.setTranslationY(binding.chatbotContainer.getHeight() > 0 ? binding.chatbotContainer.getHeight() : 600);
        binding.chatbotContainer.animate()
                .translationY(0)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isChatbotVisible = true;
                    }
                })
                .start();

        binding.fabContainer.setVisibility(View.GONE);
    }

    private void hideChatbot() {
        binding.chatbotContainer.animate()
                .translationY(binding.chatbotContainer.getHeight())
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        binding.chatbotContainer.setVisibility(View.GONE);
                        isChatbotVisible = false;
                        if (showType.get() != 1) {
                            binding.fabContainer.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .start();
    }
}
