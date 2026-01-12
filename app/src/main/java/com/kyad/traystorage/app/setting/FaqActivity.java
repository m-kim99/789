package com.kyad.traystorage.app.setting;

import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kyad.traystorage.App;
import com.kyad.traystorage.R;
import com.kyad.traystorage.app.common.dialog.LoadingDialog;
import com.kyad.traystorage.data.DataManager;
import com.kyad.traystorage.data.model.ModelAsk;
import com.kyad.traystorage.data.model.ModelFaq;
import com.kyad.traystorage.data.model.ModelFaqItem;
import com.kyad.traystorage.data.remote.ResponseSubscriber;
import com.kyad.traystorage.databinding.ActivityFaqBinding;
import com.kyad.traystorage.databinding.ItemFaqBinding;
import com.kyad.traystorage.databinding.ItemFaqCategoryBinding;

import java.util.ArrayList;
import java.util.List;

import base.BaseBindingActivity;
import base.BaseViewModel;
import helper.RecyclerViewHelper;

public class FaqActivity extends BaseBindingActivity<ActivityFaqBinding> {
    @Override
    public int getLayout() {
        return R.layout.activity_faq;
    }
    @Override
    protected Dialog loadingDialog() {
        return new LoadingDialog(this);
    }

    @Override
    public void init() {
        initViewModel();
        initView();

    }

    public ViewModel viewModel;
    public class ViewModel extends BaseViewModel {
        public void getItems() {
            addDisposable(DataManager.get().getFaqItemList().subscribeWith(new ResponseSubscriber<ModelFaqItem.ListModel>() {
                @Override
                public void onComplete() {
                    super.onComplete();

                    if (getResponse().result == 0) {
                        itemList.clear();
                        ModelFaqItem all = new ModelFaqItem();
                        all.id  = -1;
                        all.name = getString(R.string.all);
                        all.isSelected.set(true);
                        itemList.add(all);
                        itemList.addAll(getResponse().data.list);
                    } else if (!getResponse().msg.isEmpty()) {
                        onApiError(getResponse().msg);
                    } else {
                        onApiError(App.get().getString(R.string.error_network_content));
                    }
                }
            }));
        }

        public void getFaqs(int id) {
            addDisposable(DataManager.get().getFaqList(id).subscribeWith(new ResponseSubscriber<ModelFaq.ListModel>() {
                @Override
                public void onComplete() {
                    super.onComplete();

                    if (getResponse().result == 0) {
                        allList.clear();
                        currentList.clear();
                        currentCount = 0;
                        allList.addAll(getResponse().data.list);
                        updateCurrentList();
                    } else if (!getResponse().msg.isEmpty()) {
                        onApiError(getResponse().msg);
                    } else {
                        onApiError(App.get().getString(R.string.error_network_content));
                    }
                }
            }));
        }
    }

    private void updateCurrentList() {
        for(int i=currentCount;i<currentCount+20;i++){
            if(i<allList.size())
                currentList.add(allList.get(i));
        }
        currentCount+=20;
    }

    private void initViewModel() {
        viewModel = new ViewModel();
        viewModel.getItems();
        viewModel.getFaqs(-1);
    }

    ListAdapter listAdapter;
    CategoryListAdapter categoryAdapter;
    public ObservableArrayList<ModelFaqItem> itemList = new ObservableArrayList<>();
    public List<ModelFaq> allList = new ArrayList<>();
    int currentCount=0;
    public ObservableArrayList<ModelFaq> currentList = new ObservableArrayList<>();
    private void initView() {
        binding.setActivity(this);
        binding.listView.setAdapter(listAdapter = new ListAdapter());
        binding.categoryList.setAdapter(categoryAdapter = new CategoryListAdapter());
        RecyclerViewHelper.linkAdapterAndObserable(listAdapter, currentList);
        RecyclerViewHelper.linkAdapterAndObserable(categoryAdapter, itemList);

        //scroll list (view more)
        binding.listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (binding.listView.getAdapter().getItemCount() != 0) {
                    int lastPos = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                    if ((lastPos != RecyclerView.NO_POSITION) && (lastPos == binding.listView.getAdapter().getItemCount() - 1) && (currentCount<allList.size())) {
                        updateCurrentList();
                    }
                }
            }
        });

        //pull to refresh
        /*binding.pull.setRefreshStyle(0);
        binding.pull.setOnRefreshListener(() -> {
            mPage = 0;
            isPullRefreshing = true;
            updateVideoList();
            updateChallengeInfo();
        });*/
    }

    public void onConfirm(){}


    public class CategoryListAdapter extends RecyclerView.Adapter {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int i) {
            ViewDataBinding itemBinding;
            itemBinding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.item_faq_category, viewGroup, false);
            RecyclerView.ViewHolder viewHolder = new ListItemViewHolder((ItemFaqCategoryBinding) itemBinding);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            ((ListItemViewHolder) viewHolder).bindItem(i);

        }

        @Override
        public int getItemCount() {
            return itemList.size();
        }

        public class ListItemViewHolder extends RecyclerView.ViewHolder {
            ItemFaqCategoryBinding itemBinding;
            ModelFaqItem item;

            public ListItemViewHolder(ItemFaqCategoryBinding itemBinding) {
                super(itemBinding.getRoot());
                this.itemBinding = itemBinding;
                itemBinding.setHolder(this);
            }
            public void onClick(){
                for(ModelFaqItem item:itemList){
                    item.isSelected.set(false);
                }
                item.isSelected.set(true);
                viewModel.getFaqs(item.id);
            }

            public void bindItem(int i) {
                this.item = itemList.get(i);
                itemBinding.setData(item);
            }
        }
    }

    public class ListAdapter extends RecyclerView.Adapter {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int i) {
            ViewDataBinding itemBinding;
            itemBinding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.item_faq, viewGroup, false);
            RecyclerView.ViewHolder viewHolder = new ListAdapter.ListItemViewHolder((ItemFaqBinding) itemBinding);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            ((ListItemViewHolder) viewHolder).bindItem(i);

        }

        @Override
        public int getItemCount() {
            return currentList.size();
        }

        public class ListItemViewHolder extends RecyclerView.ViewHolder {
            ItemFaqBinding itemBinding;
            ModelFaq faq;

            public ListItemViewHolder(ItemFaqBinding itemBinding) {
                super(itemBinding.getRoot());
                this.itemBinding = itemBinding;
                itemBinding.setHolder(this);
            }
            public void onExpand(){
                faq.isExpanded.set(!faq.isExpanded.get());
            }

            public void bindItem(int i) {
                this.faq = currentList.get(i);
                itemBinding.setData(faq);
            }
        }

    }
}
