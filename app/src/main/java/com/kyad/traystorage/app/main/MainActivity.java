package com.kyad.traystorage.app.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;
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
import com.kyad.traystorage.app.Constants;
import com.kyad.traystorage.data.remote.ResponseSubscriber;
import io.reactivex.disposables.CompositeDisposable;
import java.util.ArrayList;
import java.util.List;

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
    private ViewTreeObserver.OnGlobalLayoutListener chatbotKeyboardListener;
    private List<ModelDocument> documentList = new ArrayList<>();
    private List<ModelDocument> allList = new ArrayList<>();
    private int currentCount = 0;
    private CompositeDisposable disposables = new CompositeDisposable();

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

        };

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
        loadDocuments("");
        if (!isTestMode()) {
            Glide.with(this).load(binding.getUser().profile_image).placeholder(R.drawable.icon_c_user_60).into(binding.imgAvatar);
        }
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

        binding.docCount.setText("0건");
        docListAdapter = new DocumentListAdapter();
        binding.docList.setAdapter(docListAdapter);

        binding.docList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (binding.docList.getAdapter().getItemCount() != 0) {
                    int lastPos = layoutManager.findLastCompletelyVisibleItemPosition();
                    if ((lastPos != RecyclerView.NO_POSITION) &&
                            (lastPos == binding.docList.getAdapter().getItemCount() - 1) &&
                            (currentCount < allList.size())) {
                        updateCurrentList();
                    }
                }
            }
        });

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
            loadDocuments("");
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
        lastSearchKey = binding.textSearch.getPlanText();
        if (lastSearchKey.isEmpty()) {
            Utils.showCustomToast(this, R.string.search_input);
            return;
        }
        currentCount = 0;
        documentList.clear();
        loadDocuments(lastSearchKey);
    }

    private void loadDocuments(String keyword) {
        disposables.add(DataManager.get().getDocumentList(keyword)
                .subscribeWith(new ResponseSubscriber<ModelDocument.ListModel>() {
                    @Override
                    public void onComplete() {
                        super.onComplete();

                        if (getResponse().result == 0) {
                            allList.clear();
                            documentList.clear();
                            currentCount = 0;
                            allList.addAll(getResponse().data.document_list);
                            for (ModelDocument doc : allList) {
                                doc.updateValues();
                            }
                            updateCurrentList();
                            updateUI();
                        } else if (!getResponse().msg.isEmpty()) {
                            Utils.showCustomToast(MainActivity.this, getResponse().msg, Toast.LENGTH_SHORT);
                        } else {
                            Utils.showCustomToast(MainActivity.this, R.string.error_network_content);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        Utils.showCustomToast(MainActivity.this, R.string.error_network_content);
                    }
                }));
    }

    private void updateCurrentList() {
        for (int i = currentCount; i < currentCount + 20 && i < allList.size(); i++) {
            documentList.add(allList.get(i));
        }
        currentCount = documentList.size();
        docListAdapter.notifyDataSetChanged();
    }

    private void updateUI() {
        binding.docCount.setText(allList.size() + "건");
        showType.set(allList.size() > 0 ? 2 : 1);
        if (allList.size() == 0 && !lastSearchKey.isEmpty()) {
            binding.docList.setVisibility(View.GONE);
            binding.layoutNotResult.setVisibility(View.VISIBLE);
            binding.textNoResult.setText("'" + lastSearchKey + "'" + getResources().getString(R.string.search_not_result));
        } else {
            binding.docList.setVisibility(View.VISIBLE);
            binding.layoutNotResult.setVisibility(View.GONE);
        }
    }

    void goDetail(Integer docId) {
        Intent intent = new Intent(this, DocumentDetailActivity.class);
        intent.putExtra("doc_id", docId);
        startActivity(intent);
    }

    public class DocumentListAdapter extends RecyclerView.Adapter<DocumentListAdapter.ListItemViewHolder> {

        @NonNull
        @Override
        public ListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_document, parent, false);
            return new ListItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ListItemViewHolder holder, int position) {
            holder.bindItem(documentList.get(position));
        }

        @Override
        public int getItemCount() {
            return documentList.size();
        }

        public class ListItemViewHolder extends RecyclerView.ViewHolder {
            ItemDocumentBinding binding;

            public ListItemViewHolder(View view) {
                super(view);
                binding = DataBindingUtil.bind(view);
                binding.setHolder(null);
            }

            public void bindItem(ModelDocument document) {
                binding.setData(document);
                binding.imgLabel.setImageResource(Constants.LabelColorArray.valueOf("color" + document.label).value);
                if (document.image_list != null && document.image_list.size() > 0)
                    Glide.with(MainActivity.this).load(document.image_list.get(0)).into(binding.imageView);
                else
                    Glide.with(MainActivity.this).load("").into(binding.imageView);

                binding.itemContainer.setOnClickListener(v -> goDetail(document.id));
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

    public void onAddDocumentClick() {
        if (!binding.textSearch.isEnabled())
            return;
        Intent intent = new Intent(this, DocumentEditActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
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
                        setupChatbotKeyboardListener();
                    }
                })
                .start();

        binding.fabContainer.setVisibility(View.GONE);
    }

    private void setupChatbotKeyboardListener() {
        if (chatbotKeyboardListener != null) {
            binding.mainLayout.getViewTreeObserver().removeOnGlobalLayoutListener(chatbotKeyboardListener);
        }
        
        chatbotKeyboardListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            private boolean isKeyboardVisible = false;
            
            @Override
            public void onGlobalLayout() {
                if (binding == null || !isChatbotVisible) return;
                
                Rect r = new Rect();
                binding.mainLayout.getWindowVisibleDisplayFrame(r);
                int screenHeight = binding.mainLayout.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;
                
                boolean keyboardNowVisible = keypadHeight > screenHeight * 0.15;
                
                if (keyboardNowVisible != isKeyboardVisible) {
                    isKeyboardVisible = keyboardNowVisible;
                    
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) binding.chatbotContainer.getLayoutParams();
                    
                    if (isKeyboardVisible) {
                        // 키보드가 열리면 bottom margin을 키보드 높이로 설정
                        params.bottomMargin = keypadHeight;
                        // 로고 아래부터 시작하도록 top margin 설정
                        params.topMargin = (int) (70 * getResources().getDisplayMetrics().density);
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                        params.height = RelativeLayout.LayoutParams.MATCH_PARENT;
                    } else {
                        // 키보드가 닫히면 원래대로
                        params.bottomMargin = 0;
                        params.topMargin = 0;
                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                    }
                    binding.chatbotContainer.setLayoutParams(params);
                }
            }
        };
        binding.mainLayout.getViewTreeObserver().addOnGlobalLayoutListener(chatbotKeyboardListener);
    }

    private void hideChatbot() {
        // 키보드 리스너 제거
        if (chatbotKeyboardListener != null) {
            binding.mainLayout.getViewTreeObserver().removeOnGlobalLayoutListener(chatbotKeyboardListener);
            chatbotKeyboardListener = null;
        }
        
        // 레이아웃 원래대로 복원
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) binding.chatbotContainer.getLayoutParams();
        params.bottomMargin = 0;
        params.topMargin = 0;
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        binding.chatbotContainer.setLayoutParams(params);
        
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
