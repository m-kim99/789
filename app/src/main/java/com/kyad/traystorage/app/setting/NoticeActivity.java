package com.kyad.traystorage.app.setting;

import android.app.Dialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import com.kyad.traystorage.App;
import com.kyad.traystorage.R;
import com.kyad.traystorage.app.common.dialog.LoadingDialog;
import com.kyad.traystorage.data.DataManager;
import com.kyad.traystorage.data.model.ModelAsk;
import com.kyad.traystorage.data.model.ModelBase;
import com.kyad.traystorage.data.model.ModelNotice;
import com.kyad.traystorage.data.remote.ResponseSubscriber;
import com.kyad.traystorage.databinding.ActivityNoticeBinding;
import com.kyad.traystorage.databinding.ItemInquireBinding;
import com.kyad.traystorage.databinding.ItemNoticeBinding;

import base.BaseBindingActivity;
import base.BaseViewModel;
import helper.RecyclerViewHelper;

public class NoticeActivity extends BaseBindingActivity<ActivityNoticeBinding> {
    @Override
    public int getLayout() {
        return R.layout.activity_notice;
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
    public ObservableArrayList<ModelNotice> noticeList = new ObservableArrayList<>();
    public class ViewModel extends BaseViewModel {
        public void getNotices() {
            addDisposable(DataManager.get().getNoticeList().subscribeWith(new ResponseSubscriber<ModelNotice.ListModel>() {
                @Override
                public void onComplete() {
                    super.onComplete();

                    if (getResponse().result == 0) {
                        noticeList.clear();
                        noticeList.addAll(getResponse().data.list);
                    } else if (!getResponse().msg.isEmpty()) {
                        onApiError(getResponse().msg);
                    } else {
                        onApiError(App.get().getString(R.string.error_network_content));
                    }
                }
            }));
        }

    }
    private void initViewModel() {
        viewModel = new ViewModel();
    }
    private void initView() {
        binding.setActivity(this);
        binding.listView.setAdapter(new ListAdapter());
        RecyclerViewHelper.linkAdapterAndObserable(binding.listView.getAdapter(), noticeList);
    }
    public void onResume(){
        super.onResume();
        viewModel.getNotices();
    }

    public void onConfirm(){}

    public void goDetail(int id){
        Intent intent = new Intent(this, NoticeDetailActivity.class);
        intent.putExtra("id", id);
        startActivity(intent);
    }

    public class ListAdapter extends RecyclerView.Adapter {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int i) {
            ViewDataBinding itemBinding;
            itemBinding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.item_notice, viewGroup, false);
            RecyclerView.ViewHolder viewHolder = new ListItemViewHolder((ItemNoticeBinding) itemBinding);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            ((ListItemViewHolder) viewHolder).bindItem(i);

        }

        @Override
        public int getItemCount() {
            return noticeList.size();
        }

        public class ListItemViewHolder extends RecyclerView.ViewHolder {
            ItemNoticeBinding itemBinding;
            ModelNotice notice;

            public ListItemViewHolder(ItemNoticeBinding itemBinding) {
                super(itemBinding.getRoot());
                this.itemBinding = itemBinding;
                itemBinding.setHolder(this);
            }
            public void onClick(){
                goDetail(notice.id);
            }

            public void bindItem(int i) {
                this.notice = noticeList.get(i);
                itemBinding.setData(notice);
            }
        }
    }
}
