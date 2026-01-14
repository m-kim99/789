package com.kyad.traystorage.app.main;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableList;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.kyad.traystorage.R;
import com.kyad.traystorage.app.Constants;
import com.kyad.traystorage.app.common.PhotoSelectActivity;
import com.kyad.traystorage.app.common.dialog.AlertDialog;
import com.kyad.traystorage.app.common.dialog.LoadingDialog;
import com.kyad.traystorage.app.common.util.PermissionHelper;
import com.kyad.traystorage.app.common.util.PrefMgr;
import com.kyad.traystorage.app.common.util.Utils;
import com.kyad.traystorage.app.splash.IntroActivity;
import com.kyad.traystorage.data.DataManager;
import com.kyad.traystorage.data.model.ModelDocument;
import com.kyad.traystorage.data.model.ModelUploadFile;
import com.kyad.traystorage.databinding.ActivityDocumentEditBinding;
import com.kyad.traystorage.databinding.ItemLabelColorBinding;
import com.kyad.traystorage.databinding.ItemTagBinding;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import base.BaseBindingActivity;
import base.BaseEvent;
import helper.RecyclerViewHelper;
import helper.Validation;
import android.os.Build;
import com.kyad.traystorage.data.model.ModelUser;

public class DocumentEditActivity extends BaseBindingActivity<ActivityDocumentEditBinding> {
    public MainViewModel viewModel;

    private String[] requiredPermissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
    };
    private static final int RC_PERMISSION = 2001;
    private PermissionHelper permissionHelper;

    public DocumentImageListAdapter imageListAdapter = new DocumentImageListAdapter(true);
    public ObservableList<String> tagList = new ObservableArrayList<>();
    public List<LabelColorModel> colorModels = new ArrayList<>();

    ModelDocument editDocument = null;
    private List<String> docImageFileList = new ArrayList<>();
    private Integer categoryId = null;
    private String categoryName = "";

    @Override
    public int getLayout() {
        return R.layout.activity_document_edit;
    }

    @Override
    protected Dialog loadingDialog() {
        return new LoadingDialog(this);
    }

    @Override
    public void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            requiredPermissions = new String[]{
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                Manifest.permission.CAMERA,
            };
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions = new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.CAMERA,
            };
        }

        Boolean isDocEdit = getIntent().getBooleanExtra("is_edit", false);
        categoryId = getIntent().hasExtra("category_id") ? getIntent().getIntExtra("category_id", -1) : null;
        categoryName = getIntent().getStringExtra("category_name");
        initViewModel();
        initView();
        binding.tagAdd.setTag(new Object());
        setupKeyboard(binding.bg);

        if (isDocEdit) {
            binding.title.setText(R.string.document_edit);
            binding.btnRegister.setText(R.string.save);
            editDocument = DataManager.get().getModel(ModelDocument.class);
        }
        if (editDocument != null) {
            imageListAdapter.setImageList(editDocument.image_list);
            //binding.textSubject.setText(editDocument.title);
            viewModel.title.setValue(editDocument.title);
            //binding.textContent.setText(editDocument.content);
            viewModel.content.setValue(editDocument.content);
            if (editDocument.label < colorModels.size()) {
                colorModels.get(0).isSelected.set(false);
                colorModels.get(editDocument.label).isSelected.set(true);
            }
            tagList.clear();
            tagList.addAll(editDocument.tag_list);
        }
    }

    boolean isChanged;

    @Override
    public void onBackPressed() {
        if (editDocument == null) {
            isChanged = isChanged || binding.textSubject.length() > 0 || binding.textContent.length() > 0;
        } else {
            isChanged = isChanged || !viewModel.title.getValue().equals(editDocument.title) || !viewModel.content.getValue().equals(editDocument.content);
        }
        if (isChanged) {
            AlertDialog.show(this).setText(getString(R.string.doc_reg_back), "", getString(R.string.confirm), getString(R.string.cancel))
                    .setListener(() -> {
                        super.onBackPressed();
                    });
        } else {
            super.onBackPressed();
        }
    }

    /************************************************************
     *  ApiListener
     ************************************************************/
    private final MainApiListener apiListener() {
        return new MainApiListener() {
            @Override
            public void onError(String msg) {
                Utils.showCustomToast(DocumentEditActivity.this, msg, Toast.LENGTH_SHORT);
            }

            @Override
            public void onImagesUploaded(List<ModelUploadFile> uploadFileNames) {
                for (ModelUploadFile upload :
                        uploadFileNames) {
                    docImageFileList.add(upload.file_name);
                }
                registerDocument(docImageFileList);
            }

            @Override
            public void onRegisterDocumentSuccess(Integer docId) {
                Utils.showCustomToast(DocumentEditActivity.this, R.string.register_ok);

                if (editDocument == null) {
                    Intent intent = new Intent(DocumentEditActivity.this, DocumentDetailActivity.class);
                    intent.putExtra("doc_id", docId);
                    startActivity(intent);
                }
                finish();
            }
        };
    }

    private void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.apiListener = apiListener();

        viewModel.title.observe(this, title -> {
            checkValidData();
        });
        viewModel.content.observe(this, content -> {
            checkValidData();
        });

        ObservableList.OnListChangedCallback callback = new ObservableList.OnListChangedCallback<ObservableList<String>>() {
            @Override
            public void onChanged(ObservableList<String> sender) {
                checkValidData();
            }

            @Override
            public void onItemRangeChanged(ObservableList<String> sender, int positionStart, int itemCount) {
                checkValidData();
            }

            @Override
            public void onItemRangeInserted(ObservableList<String> sender, int positionStart, int itemCount) {
                checkValidData();
            }

            @Override
            public void onItemRangeMoved(ObservableList<String> sender, int fromPosition, int toPosition, int itemCount) {
                checkValidData();
                isChanged = true;
            }

            @Override
            public void onItemRangeRemoved(ObservableList<String> sender, int positionStart, int itemCount) {
                checkValidData();
                isChanged = true;
            }
        };
        imageListAdapter.imageUrlList.addOnListChangedCallback(callback);
        tagList.addOnListChangedCallback(callback);
    }

    private void initView() {
        binding.setActivity(this);
        binding.setViewModel(this.viewModel);

        binding.imageList.setAdapter(imageListAdapter);

        TagListAdapter tagListAdapter = new TagListAdapter();
        RecyclerViewHelper.linkAdapterAndObserable(tagListAdapter, tagList);
        binding.tagList.setAdapter(tagListAdapter);

        for (Constants.LabelColorArray c : Constants.LabelColorArray.values()) {
            LabelColorModel m = new LabelColorModel();
            m.colorId = c.value;
            colorModels.add(m);
        }
        colorModels.get(0).isSelected.set(true);
        colorModels.get(0).isWhite.set(true);
        binding.labelColorList.setAdapter(new LabelColorListAdapter());
    }

    public void checkValidData() {
        boolean isEnabled = !viewModel.title.getValue().isEmpty() && !viewModel.content.getValue().isEmpty()
                && imageListAdapter.imageUrlList.size() > 0 && tagList.size() > 0;
        binding.btnRegister.setEnabled(isEnabled);
    }

    public void onAddPhotoClick() {
        permissionHelper = new PermissionHelper(this);
        checkPermissions();
    }

    public void onRegisterClick() {
        AlertDialog.show(DocumentEditActivity.this).setText(getString(R.string.register_confirm), "", getString(R.string.yes), getString(R.string.no))
                .setListener(() -> {
                    // 테스트 모드: 바로 성공 처리
                    if (isTestMode()) {
                        Utils.showCustomToast(DocumentEditActivity.this, R.string.register_ok);
                        finish();
                        return;
                    }
                    
                    docImageFileList.clear();
                    List<String> localUrlList = new ArrayList<>();
                    if (editDocument != null) {
                        for (String imgUrl : imageListAdapter.imageUrlList) {
                            if (!Validation.isUrl(imgUrl)) {
                                localUrlList.add(imgUrl);
                            } else {
                                String fileName = URLUtil.guessFileName(imgUrl, null, null);
                                if (fileName == null || fileName.isEmpty()) {
                                    int index = imgUrl.lastIndexOf("/");
                                    fileName = imgUrl.substring(index + 1);
                                }
                                docImageFileList.add(fileName);
                            }
                        }
                    } else {
                        localUrlList = imageListAdapter.imageUrlList;
                    }
                    if (!localUrlList.isEmpty())
                        viewModel.uploadImages(localUrlList);
                    else
                        registerDocument(docImageFileList);
                });
    }

    // 테스트 모드 체크 헬퍼
    private boolean isTestMode() {
        ModelUser user = DataManager.get().getModel(ModelUser.class);
        return (user != null && user.id == 999);
    }

    private void registerDocument(List<String> uploadFiles) {
        int label = 0;
        for (int i = 0; i < colorModels.size(); i++) {
            if (colorModels.get(i).isSelected.get()) {
                label = i;
                break;
            }
        }
        int finalLabel = label;
        
        // 테스트 모드: API 호출 없이 성공 처리
        if (isTestMode()) {
            Utils.showCustomToast(DocumentEditActivity.this, R.string.register_ok);
            finish();
            return;
        }
        
        if (editDocument == null) {
            viewModel.registerDocument(viewModel.title.getValue(), viewModel.content.getValue(), finalLabel, tagList, uploadFiles, categoryId);
        } else {
            viewModel.updateDocument(editDocument.id, viewModel.title.getValue(), viewModel.content.getValue(), finalLabel, tagList, uploadFiles, categoryId);
        }
    }

    /************************************************************
     *  Event Bus
     ************************************************************/
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void PhotoSelectCompleteEvent(BaseEvent.PhotoSelectCompleteEvent e) {
        imageListAdapter.addImageList(e.photoUrlList);
        isChanged = true;
    }

    public void onAddTagClick() {
        if (binding.textTag.length() < 1) {
            Utils.showCustomToast(this, R.string.input_tag, Toast.LENGTH_SHORT);
            return;
        }
        if (tagList.size() >= 5) {
            Utils.showCustomToast(this, R.string.tag_limit, Toast.LENGTH_SHORT);
            return;
        }
        String tagString = binding.textTag.getPlanText();
        tagString.replace(" ", "");
        if (tagList.contains(tagString)) {
            Utils.showCustomToast(this, R.string.tag_exist, Toast.LENGTH_SHORT);
            return;
        }
        isChanged = true;
        tagList.add(0, tagString);
        binding.textTag.setText("");
        new Handler(Looper.getMainLooper()).postDelayed(this::scrollToEnd, 100);

    }

    public void scrollToEnd() {
        binding.scrollView.scrollTo(0, 100000);
        binding.tagList.scrollToPosition(0);
    }

    public class LabelColorModel {
        public ObservableBoolean isWhite = new ObservableBoolean(false);
        public ObservableBoolean isSelected = new ObservableBoolean(false);
        public int colorId;
    }

    public class LabelColorListAdapter extends RecyclerView.Adapter {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            ViewDataBinding itemBinding;
            itemBinding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.item_label_color, viewGroup, false);
            RecyclerView.ViewHolder viewHolder = new ListItemViewHolder((ItemLabelColorBinding) itemBinding);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            ((ListItemViewHolder) viewHolder).bindItem(i);
        }

        @Override
        public int getItemCount() {
            return colorModels.size();
        }

        public class ListItemViewHolder extends RecyclerView.ViewHolder {
            ItemLabelColorBinding itemBinding;

            public ListItemViewHolder(ItemLabelColorBinding itemBinding) {
                super(itemBinding.getRoot());
                this.itemBinding = itemBinding;
                this.itemBinding.setHolder(this);
            }

            public void onClick() {
                for (LabelColorModel c : colorModels) {
                    c.isSelected.set(false);
                }
                isChanged = true;
                itemBinding.getData().isSelected.set(true);
            }

            public void bindItem(int i) {
                itemBinding.setData(colorModels.get(i)); //.labelImage.setImageResource();
                itemBinding.labelImage.setImageResource(colorModels.get(i).colorId);
            }
        }
    }

    public class TagListAdapter extends RecyclerView.Adapter {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_tag, viewGroup, false);
            RecyclerView.ViewHolder viewHolder = new TagListAdapter.ListItemViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            ((ListItemViewHolder) viewHolder).bindItem(tagList.get(i), i);
        }

        @Override
        public int getItemCount() {
            return tagList.size();
        }

        public class ListItemViewHolder extends RecyclerView.ViewHolder {
            ItemTagBinding binding;
            String tagValue;

            public ListItemViewHolder(View view) {
                super(view);
                binding = DataBindingUtil.bind(view);
                binding.setHolder(this);
            }

            public void onCloseClick() {
                tagList.remove(tagValue);
                Utils.showCustomToast(DocumentEditActivity.this, R.string.delete_tag);
            }

            public void bindItem(String tag, int i) {
                tagValue = tag;
                binding.textTag.setText(tag);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PERMISSION) {
            if (permissionHelper.isPermisionsRevoked(requiredPermissions)) {
                showPermissionGuide(requestCode);
            } else {
                checkPermissions();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == RC_PERMISSION) {
            if (permissionHelper.isPermisionsRevoked(requiredPermissions)) {
                showPermissionGuide(requestCode);
            } else {
                checkPermissions();
            }
        }
    }

    private void showPermissionGuide(final int RC) {
        Utils.showCustomToast(this, (R.string.permission_not_allow));
    }

    private void checkPermissions() {
        if (permissionHelper.hasPermission(requiredPermissions)) {
            Intent intent = new Intent(this, PhotoSelectActivity.class);
            intent.putExtra("is_single", 0);
            startActivity(intent);
        } else {
            permissionHelper.requestPermission(DocumentEditActivity.this, requiredPermissions, RC_PERMISSION);
        }
    }
}
