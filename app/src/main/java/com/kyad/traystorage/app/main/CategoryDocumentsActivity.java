package com.kyad.traystorage.app.main;

import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kyad.traystorage.App;
import com.kyad.traystorage.R;
import com.kyad.traystorage.app.Constants;
import com.kyad.traystorage.app.common.dialog.LoadingDialog;
import com.kyad.traystorage.app.common.util.Utils;
import com.kyad.traystorage.data.DataManager;
import com.kyad.traystorage.data.model.ModelDocument;
import com.kyad.traystorage.data.model.ModelUser;
import com.kyad.traystorage.data.remote.ResponseSubscriber;
import com.kyad.traystorage.databinding.ActivityCategoryDocumentsBinding;
import com.kyad.traystorage.databinding.ItemDocumentBinding;

import java.util.ArrayList;
import java.util.List;

import base.BaseBindingActivity;
import helper.RecyclerViewHelper;
import io.reactivex.disposables.CompositeDisposable;
import org.greenrobot.eventbus.EventBus;
import base.BaseEvent;

public class CategoryDocumentsActivity extends BaseBindingActivity<ActivityCategoryDocumentsBinding> {

    private int categoryId;
    public ObservableField<String> categoryName = new ObservableField<>("");
    private DocumentListAdapter docListAdapter;
    private List<ModelDocument> documentList = new ArrayList<>();
    private List<ModelDocument> allList = new ArrayList<>();
    private String lastSearchKey = "";
    private int currentCount = 0;
    private CompositeDisposable disposables = new CompositeDisposable();

    @Override
    public int getLayout() {
        return R.layout.activity_category_documents;
    }

    @Override
    protected Dialog loadingDialog() {
        return new LoadingDialog(this);
    }

    @Override
    public void init() {
        categoryId = getIntent().getIntExtra("category_id", -1);
        categoryName.set(getIntent().getStringExtra("category_name"));

        binding.setActivity(this);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDocuments("");
    }

    private void initView() {
        docListAdapter = new DocumentListAdapter();
        binding.docList.setAdapter(docListAdapter);

        binding.textSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                onSearchClick();
                return true;
            }
            return false;
        });

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
    }

    private void loadDocuments(String keyword) {
        EventBus.getDefault().post(new BaseEvent.LoadingEvent(true));
        disposables.add(DataManager.get().getDocumentListByCategory(categoryId, keyword)
                .subscribeWith(new ResponseSubscriber<ModelDocument.ListModel>() {
                    @Override
                    public void onComplete() {
                        super.onComplete();
                        EventBus.getDefault().post(new BaseEvent.LoadingEvent(false));

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
                            Utils.showCustomToast(CategoryDocumentsActivity.this, getResponse().msg, Toast.LENGTH_SHORT);
                        } else {
                            Utils.showCustomToast(CategoryDocumentsActivity.this, R.string.error_network_content);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        EventBus.getDefault().post(new BaseEvent.LoadingEvent(false));
                        Utils.showCustomToast(CategoryDocumentsActivity.this, R.string.error_network_content);
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
        if (allList.size() == 0 && !lastSearchKey.isEmpty()) {
            binding.docList.setVisibility(View.GONE);
            binding.layoutNotResult.setVisibility(View.VISIBLE);
            binding.textNoResult.setText("'" + lastSearchKey + "'" + getResources().getString(R.string.search_not_result));
        } else {
            binding.docList.setVisibility(View.VISIBLE);
            binding.layoutNotResult.setVisibility(View.GONE);
        }
    }

    public void onSearchTextChanged() {
        binding.deleteText.setVisibility(binding.textSearch.getPlanText().isEmpty() ? View.GONE : View.VISIBLE);
    }

    public void onClearSearchClick() {
        lastSearchKey = "";
        binding.deleteText.setVisibility(View.GONE);
        binding.textSearch.setText("");
        loadDocuments("");
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

    public void onRegisterClick() {
        Intent intent = new Intent(this, DocumentEditActivity.class);
        intent.putExtra("category_id", categoryId);
        intent.putExtra("category_name", categoryName.get());
        startActivity(intent);
    }

    void goDetail(Integer docId) {
        Intent intent = new Intent(this, DocumentDetailActivity.class);
        intent.putExtra("doc_id", docId);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
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

            public void onClick(Integer docId) {
                goDetail(docId);
            }

            public void bindItem(ModelDocument document) {
                binding.setData(document);
                binding.imgLabel.setImageResource(Constants.LabelColorArray.valueOf("color" + document.label).value);
                if (document.image_list != null && document.image_list.size() > 0)
                    Glide.with(CategoryDocumentsActivity.this).load(document.image_list.get(0)).into(binding.imageView);
                else
                    Glide.with(CategoryDocumentsActivity.this).load("").into(binding.imageView);

                binding.getRoot().setOnClickListener(v -> goDetail(document.id));
            }
        }
    }
}
