package com.kyad.traystorage.app.main;

import android.app.Dialog;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModelProviders;

import com.kyad.traystorage.R;
import com.kyad.traystorage.app.Constants;
import com.kyad.traystorage.app.common.dialog.AlertDialog;
import com.kyad.traystorage.app.common.dialog.LoadingDialog;
import com.kyad.traystorage.app.common.util.Utils;
import com.kyad.traystorage.data.DataManager;
import com.kyad.traystorage.data.model.ModelDocument;
import com.kyad.traystorage.data.model.ModelUser;
import com.kyad.traystorage.databinding.ActivityDocumentDetailBinding;

import base.BaseBindingActivity;

public class DocumentDetailActivity extends BaseBindingActivity<ActivityDocumentDetailBinding> {
    public MainViewModel viewModel;
    public ObservableField<ModelDocument> document = new ObservableField<>();
    public ObservableBoolean isMine = new ObservableBoolean(true);
    public DocumentImageListAdapter imageListAdapter;

    public Integer documentId = 0;

    @Override
    public int getLayout() {
        return R.layout.activity_document_detail;
    }

    @Override
    protected Dialog loadingDialog() {
        return new LoadingDialog(this);
    }

    @Override
    public void init() {
        Intent i = getIntent();
        documentId = i.getIntExtra("doc_id", 0);
        initViewModel();
        initView();
    }


    @Override
    protected void onResume() {
        super.onResume();
        //after edit document
        viewModel.getDocumentDetail(documentId);
    }

    public void onDeleteClick() {
        AlertDialog.show(this).setText(getString(R.string.doc_delete), "", getString(R.string.confirm), getString(R.string.cancel))
                .setListener(() -> {
                    viewModel.deleteDocument(document.get().id);
                });
    }

    /************************************************************
     *  ApiListener
     ************************************************************/
    private final MainApiListener apiListener() {
        return new MainApiListener() {
            @Override
            public void onError(String msg) {
                Utils.showCustomToast(DocumentDetailActivity.this, msg, Toast.LENGTH_SHORT);
            }

            @Override
            public void onDeleteDocumentSuccess() {
                Utils.showCustomToast(DocumentDetailActivity.this, R.string.doc_delete_ok);
                finish();
            }

            @Override
            public void onNodocument() {
                //문서가 존재하지 않음
                binding.rlyEmptyContent.setVisibility(View.VISIBLE);
                isMine.set(false);;
            }
        };
    }

    private void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.apiListener = apiListener();

        viewModel.documentDetail.observe(this, document -> {
            document.updateValues();
            ModelUser user = DataManager.get().getModel(ModelUser.class);
            if (user == null) {
                isMine.set(false);
            } else {
                isMine.set(user.id == document.user_id);
            }
            this.document.set(document);
            imageListAdapter.setImageList(document.image_list);
            binding.labelImage.setImageResource(Constants.LabelColorArray.valueOf("color" + document.label).value);
        });
    }

    private void initView() {
        binding.setActivity(this);

        imageListAdapter = new DocumentImageListAdapter(false).setListener((url) -> {
            Intent intent = new Intent(DocumentDetailActivity.this, DocumentImageViewActivity.class);
            intent.putExtra("index", imageListAdapter.imageUrlList.indexOf(url));
            intent.putExtra("urls", imageListAdapter.imageUrlList.toArray(new String[0]));
            startActivity(intent);
        });
        binding.imageList.setAdapter(imageListAdapter);
    }

    public void onEditClick() {
        DataManager.get().setModel(this.document.get());
        Intent intent = new Intent(this, DocumentEditActivity.class);
        intent.putExtra("is_edit", true);
        startActivity(intent);
        finish();
    }

    public void onNFCRegisterClick() {
        Intent intent = new Intent(this, NFCRegisterActivity.class);
        intent.putExtra("code", viewModel.documentDetail.getValue().code);
        intent.putExtra("doc_id", documentId);
        startActivity(intent);
    }

    public void onGoMainClick() {
        finish();
    }
}
