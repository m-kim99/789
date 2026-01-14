package com.kyad.traystorage.app.main;

import androidx.databinding.ObservableArrayList;
import androidx.lifecycle.MutableLiveData;

import com.kyad.traystorage.App;
import com.kyad.traystorage.R;
import com.kyad.traystorage.data.DataManager;
import com.kyad.traystorage.data.model.ModelBase;
import com.kyad.traystorage.data.model.ModelCategory;
import com.kyad.traystorage.data.model.ModelDocument;
import com.kyad.traystorage.data.model.ModelPopupInfo;
import com.kyad.traystorage.data.model.ModelUploadFile;
import com.kyad.traystorage.data.remote.ResponseSubscriber;

import java.util.ArrayList;
import java.util.List;

import base.BaseViewModel;
import lombok.Getter;
import lombok.Setter;

public class MainViewModel extends BaseViewModel {
    public ObservableArrayList<ModelDocument> documentList = new ObservableArrayList<>();
    public List<ModelDocument> allList = new ArrayList<>();
    public List<ModelCategory> categoryList = new ArrayList<>();
    public MutableLiveData<List<ModelPopupInfo>> popupInfoList = new MutableLiveData<>();
    public MutableLiveData<ModelDocument> documentDetail = new MutableLiveData<>();

    public MutableLiveData<String> title = new MutableLiveData<>("");
    public MutableLiveData<String> content = new MutableLiveData<>("");


    @Getter
    @Setter
    MainApiListener apiListener;

    /************************************************************
     *  Networking
     ************************************************************/
    public void getDocuments(String keyword) {
        addDisposable(DataManager.get().getDocumentList(keyword).subscribeWith(new ResponseSubscriber<ModelDocument.ListModel>() {
            @Override
            public void onComplete() {
                super.onComplete();

                if (getResponse().result == 0) {
                    allList.clear();
                    documentList.clear();
                    allList.addAll(getResponse().data.document_list);
                    for (ModelDocument doc :
                            allList) {
                        doc.updateValues();
                    }
                    apiListener.onGetDocuments();
                } else if (getResponse().result == 1) {
                    apiListener.onLoginTokenError();
                } else if (!getResponse().msg.isEmpty()) {
                    apiListener.onError(getResponse().msg);
                } else {
                    apiListener.onError(App.get().getString(R.string.error_network_content));
                }
            }
        }));
    }

    public void getPopupInfos() {
        addDisposable(DataManager.get().getPopupInfoList().subscribeWith(new ResponseSubscriber<ModelPopupInfo.ListModel>() {
            @Override
            public void onComplete() {
                super.onComplete();

                if (getResponse().result == 0) {
                    popupInfoList.setValue(getResponse().data.popup_list);
                } else if (!getResponse().msg.isEmpty()) {
                    apiListener.onError(getResponse().msg);
                } else {
                    apiListener.onError(App.get().getString(R.string.error_network_content));
                }
            }
        }));
    }

    public void getDocumentDetail(Integer docId) {
        addDisposable(DataManager.get().getDocumentDetail(docId).subscribeWith(new ResponseSubscriber<ModelDocument.DetailModel>() {
            @Override
            public void onComplete() {
                super.onComplete();

                if (getResponse().result == 0) {
                    documentDetail.setValue(getResponse().data.document);
                } else if (getResponse().result == 401) {
                    apiListener.onNodocument();
                } else if (!getResponse().msg.isEmpty()) {
                    apiListener.onError(getResponse().msg);
                } else {
                    apiListener.onError(App.get().getString(R.string.error_network_content));
                }
            }
        }));
    }

    public void uploadImage(String url) {
        addDisposable(DataManager.get().uploadImage(url).subscribeWith(new ResponseSubscriber<ModelBase>() {
            @Override
            public void onComplete() {
                super.onComplete();

                if (getResponse().result == 0) {
                    apiListener.onImageUploaded(url, getResponse().data.file_name);
                } else if (!getResponse().msg.isEmpty()) {
                    apiListener.onError(getResponse().msg);
                } else {
                    apiListener.onError(App.get().getString(R.string.error_network_content));
                }
            }
        }));
    }

    public void uploadImages(List<String> urls) {
        addDisposable(DataManager.get().uploadImages(urls.toArray(new String[0])).subscribeWith(new ResponseSubscriber<List<ModelUploadFile>>() {
            @Override
            public void onComplete() {
                super.onComplete();

                if (getResponse().result == 0) {
                    apiListener.onImagesUploaded(getResponse().data);
                } else if (!getResponse().msg.isEmpty()) {
                    apiListener.onError(getResponse().msg);
                } else {
                    apiListener.onError(App.get().getString(R.string.error_network_content));
                }
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                apiListener.onError(App.get().getString(R.string.error_network_content));
            }
        }));
    }

    public void registerDocument(String title, String content, Integer label, List<String> tags, List<String> uploadFileNames, Integer categoryId) {
        addDisposable(DataManager.get().insertDocument(title, content, label, tags.toArray(new String[0]), uploadFileNames.toArray(new String[0]), categoryId).subscribeWith(new ResponseSubscriber<ModelDocument.DetailModel>() {
            @Override
            public void onComplete() {
                super.onComplete();

                if (getResponse().result == 0) {
                    apiListener.onRegisterDocumentSuccess(getResponse().data.document.id);
                } else if (!getResponse().msg.isEmpty()) {
                    apiListener.onError(getResponse().msg);
                } else {
                    apiListener.onError(App.get().getString(R.string.error_network_content));
                }
            }
        }));
    }

    public void updateDocument(Integer docId, String title, String content, Integer label, List<String> tags, List<String> uploadFileNames, Integer categoryId) {
        addDisposable(DataManager.get().updateDocument(docId, title, content, label, tags.toArray(new String[0]), uploadFileNames.toArray(new String[0]), categoryId).subscribeWith(new ResponseSubscriber<ModelBase>() {
            @Override
            public void onComplete() {
                super.onComplete();

                if (getResponse().result == 0) {
                    apiListener.onRegisterDocumentSuccess(docId);
                } else if (!getResponse().msg.isEmpty()) {
                    apiListener.onError(getResponse().msg);
                } else {
                    apiListener.onError(App.get().getString(R.string.error_network_content));
                }
            }
        }));
    }

    public void viewPopup(Integer popupId) {
        addDisposable(DataManager.get().viewPopup(popupId).subscribeWith(new ResponseSubscriber<ModelBase>() {
            @Override
            public void onComplete() {
                super.onComplete();

                if (getResponse().result == 0) {

                } else if (!getResponse().msg.isEmpty()) {
                    apiListener.onError(getResponse().msg);
                } else {
                    apiListener.onError(App.get().getString(R.string.error_network_content));
                }
            }
        }));
    }

    public void clickPopup(Integer popupId) {
        addDisposable(DataManager.get().clickPopup(popupId).subscribeWith(new ResponseSubscriber<ModelBase>() {
            @Override
            public void onComplete() {
                super.onComplete();

                if (getResponse().result == 0) {

                } else if (!getResponse().msg.isEmpty()) {
                    apiListener.onError(getResponse().msg);
                } else {
                    apiListener.onError(App.get().getString(R.string.error_network_content));
                }
            }
        }));
    }

    public void deleteDocument(Integer docId) {
        addDisposable(DataManager.get().deleteDocumentItem(docId).subscribeWith(new ResponseSubscriber<ModelBase>() {
            @Override
            public void onComplete() {
                super.onComplete();

                if (getResponse().result == 0) {
                    apiListener.onDeleteDocumentSuccess();
                } else if (!getResponse().msg.isEmpty()) {
                    apiListener.onError(getResponse().msg);
                } else {
                    apiListener.onError(App.get().getString(R.string.error_network_content));
                }
            }
        }));
    }

    /************************************************************
     *  Category APIs
     ************************************************************/
    public void getCategories() {
        addDisposable(DataManager.get().getCategoryList().subscribeWith(new ResponseSubscriber<ModelCategory.ListModel>() {
            @Override
            public void onComplete() {
                super.onComplete();

                if (getResponse().result == 0) {
                    categoryList.clear();
                    categoryList.addAll(getResponse().data.category_list);
                    apiListener.onGetCategories();
                } else if (getResponse().result == 1) {
                    apiListener.onLoginTokenError();
                } else if (!getResponse().msg.isEmpty()) {
                    apiListener.onError(getResponse().msg);
                } else {
                    apiListener.onError(App.get().getString(R.string.error_network_content));
                }
            }
        }));
    }

    public void insertCategory(String name, Integer color) {
        addDisposable(DataManager.get().insertCategory(name, color).subscribeWith(new ResponseSubscriber<ModelCategory.DetailModel>() {
            @Override
            public void onComplete() {
                super.onComplete();

                if (getResponse().result == 0) {
                    getCategories();
                } else if (!getResponse().msg.isEmpty()) {
                    apiListener.onError(getResponse().msg);
                } else {
                    apiListener.onError(App.get().getString(R.string.error_network_content));
                }
            }
        }));
    }

    public void updateCategory(Integer categoryId, String name, Integer color) {
        addDisposable(DataManager.get().updateCategory(categoryId, name, color).subscribeWith(new ResponseSubscriber<ModelBase>() {
            @Override
            public void onComplete() {
                super.onComplete();

                if (getResponse().result == 0) {
                    getCategories();
                } else if (!getResponse().msg.isEmpty()) {
                    apiListener.onError(getResponse().msg);
                } else {
                    apiListener.onError(App.get().getString(R.string.error_network_content));
                }
            }
        }));
    }

    public void deleteCategory(Integer categoryId) {
        addDisposable(DataManager.get().deleteCategory(categoryId).subscribeWith(new ResponseSubscriber<ModelBase>() {
            @Override
            public void onComplete() {
                super.onComplete();

                if (getResponse().result == 0) {
                    getCategories();
                } else if (!getResponse().msg.isEmpty()) {
                    apiListener.onError(getResponse().msg);
                } else {
                    apiListener.onError(App.get().getString(R.string.error_network_content));
                }
            }
        }));
    }
}
