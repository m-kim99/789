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
import com.kyad.traystorage.data.remote.ResponseSubscriber;
import com.kyad.traystorage.databinding.ActivityInquiryBindingImpl;
import com.kyad.traystorage.databinding.ItemInquireBinding;

import base.BaseBindingActivity;
import base.BaseViewModel;
import helper.RecyclerViewHelper;

public class InquiryActivity extends BaseBindingActivity<ActivityInquiryBindingImpl> {
    @Override
    public int getLayout() {
        return R.layout.activity_inquiry;
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

    public InquiryViewModel viewModel;

    public class InquiryViewModel extends BaseViewModel {
        public void getAsks() {
            addDisposable(DataManager.get().getAskList().subscribeWith(new ResponseSubscriber<ModelAsk.ListModel>() {
                @Override
                public void onComplete() {
                    super.onComplete();

                    if (getResponse().result == 0) {
                        askList.clear();
                        askList.addAll(getResponse().data.list);
                        binding.layoutNot.setVisibility(askList.size() == 0 ? View.VISIBLE : View.GONE);
                        binding.countText.setText(getString(R.string.inquiry) + " " + askList.size() + "건");

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
        viewModel = new InquiryViewModel();//ViewModelProviders.of(this).get(InquiryViewModel.class);
    }

    public void onResume() {
        super.onResume();
        viewModel.getAsks();
    }

    ListAdapter listAdapter;
    public ObservableArrayList<ModelAsk> askList = new ObservableArrayList<>();

    private void initView() {
        binding.setActivity(this);
        binding.countText.setText("");
        listAdapter = new ListAdapter();
        binding.listView.setAdapter(listAdapter);
        RecyclerViewHelper.linkAdapterAndObserable(listAdapter, askList);
    }


    public class ListAdapter extends RecyclerView.Adapter {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int i) {
            ViewDataBinding itemBinding;
            itemBinding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.item_inquire, viewGroup, false);
            RecyclerView.ViewHolder viewHolder = new ListItemViewHolder((ItemInquireBinding) itemBinding);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            ((ListItemViewHolder) viewHolder).bindItem(i);

        }

        @Override
        public int getItemCount() {
            return askList.size();
        }

        public class ListItemViewHolder extends RecyclerView.ViewHolder {
            ItemInquireBinding itemBinding;
            ModelAsk ask;

            public ListItemViewHolder(ItemInquireBinding itemBinding) {
                super(itemBinding.getRoot());
                this.itemBinding = itemBinding;
                itemBinding.setHolder(this);
            }

            public void onExpand() {
                //if(ask.status==1)
                ask.isExpanded.set(!ask.isExpanded.get());
                ask.replyVisible.set(ask.isExpanded.get() && ask.status == 1);
            }

            public void bindItem(int i) {
                this.ask = askList.get(i);
                itemBinding.setData(ask);
            }
        }

    }

    public void onAdd() {
        Intent intent = new Intent(this, ContactusActivity.class);
        startActivity(intent);
    }
}
