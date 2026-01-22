package com.kyad.traystorage.data;

import android.content.Context;

import androidx.annotation.NonNull;

import com.kyad.traystorage.data.local.LocalDataSourceFactory;
import com.kyad.traystorage.data.model.ModelAgreement;
import com.kyad.traystorage.data.model.ModelAsk;
import com.kyad.traystorage.data.model.ModelBase;
import com.kyad.traystorage.data.model.ModelCategory;
import com.kyad.traystorage.data.model.ModelCode;
import com.kyad.traystorage.data.model.ModelDocument;
import com.kyad.traystorage.data.model.ModelFaq;
import com.kyad.traystorage.data.model.ModelFaqItem;
import com.kyad.traystorage.data.model.ModelNotice;
import com.kyad.traystorage.data.model.ModelNoticeDetail;
import com.kyad.traystorage.data.model.ModelPopupInfo;
import com.kyad.traystorage.data.model.ModelUploadFile;
import com.kyad.traystorage.data.model.ModelUser;
import com.kyad.traystorage.data.model.ModelVersion;
import com.kyad.traystorage.data.remote.RemoteDataSourceFactory;
import com.kyad.traystorage.data.remote.RemoteDataSourceHelper;

import java.io.File;
import java.util.List;

import helper.Util;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.android.schedulers.AndroidSchedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class DataManager {
    private static DataManager instance = null;
    private final RemoteDataSource remote;
    private final LocalDataSource local;

    public static void inject(Context context) {
        instance = new DataManager(RemoteDataSourceFactory.getInstance(), LocalDataSourceFactory.getInstance(context));
    }

    public static DataManager get() {
        return instance;
    }

    private DataManager(@NonNull RemoteDataSource remoteDataSource,
                        @NonNull LocalDataSource localDataSource) {
        remote = remoteDataSource;
        local = localDataSource;
    }

    public <T> Flowable<ApiResponse<T>> callApi(Flowable<ApiResponse<T>> flowable) {
        return flowable.compose(RemoteDataSourceHelper.flowableSchedulers());
    }

    public <T> Flowable<T> callRawApi(Flowable<T> flowable) {
        return flowable.compose(RemoteDataSourceHelper.flowableSchedulers());
    }
    /********************************
     *  Remote
     ********************************/

    public Flowable<ApiResponse<ModelUser>> login(String login_id, String password) {
        return callApi(remote.login(login_id, password));
    }

    public Flowable<ApiResponse<ModelPopupInfo.ListModel>> getPopupInfoList() {
        return callApi(remote.popup_info(getModel(ModelUser.class).access_token, 0));
    }
    public Flowable<ApiResponse<ModelBase>> viewPopup(Integer popupId) {
        return callApi(remote.view_click_popup(getModel(ModelUser.class).access_token, popupId, 0));
    }

    public Flowable<ApiResponse<ModelBase>> clickPopup(Integer popupId) {
        return callApi(remote.view_click_popup(getModel(ModelUser.class).access_token, popupId, 1));
    }

    public Flowable<ApiResponse<ModelDocument.ListModel>> getDocumentList(String keyword) {
        if (isTestMode()) {
            return Flowable.fromCallable(() -> {
                ApiResponse<ModelDocument.ListModel> response = new ApiResponse<>();
                response.result = 0;
                response.msg = "";
                ModelDocument.ListModel data = new ModelDocument.ListModel();
                data.document_list = LocalStorageManager.get().getAllDocuments(keyword);
                response.data = data;
                return response;
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        }
        return callApi(remote.get_document_list(getModel(ModelUser.class).access_token, keyword, null));
    }

    public Flowable<ApiResponse<ModelDocument.ListModel>> getDocumentListByCategory(Integer categoryId, String keyword) {
        if (isTestMode()) {
            return Flowable.fromCallable(() -> {
                ApiResponse<ModelDocument.ListModel> response = new ApiResponse<>();
                response.result = 0;
                response.msg = "";
                ModelDocument.ListModel data = new ModelDocument.ListModel();
                data.document_list = LocalStorageManager.get().getDocumentsByCategory(categoryId, keyword);
                response.data = data;
                return response;
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        }
        return callApi(remote.get_document_list(getModel(ModelUser.class).access_token, keyword, categoryId));
    }

    public Flowable<ApiResponse<ModelBase>> uploadImage(String imageUrl) {
        MultipartBody.Part req_file;

        File imageFile = new File(imageUrl);
        req_file = MultipartBody.Part.createFormData("file", imageFile.getName(), RequestBody.create(MediaType.parse("image/*"), imageFile));

        return callApi(remote.upload_file(
                RequestBody.create(MultipartBody.FORM, getModel(ModelUser.class).access_token),
                req_file));
    }

    public Flowable<ApiResponse<List<ModelUploadFile>>> uploadImages(String[] images) {
        MultipartBody.Part[] req_file = new MultipartBody.Part[images.length];

        for (int i = 0; i < images.length; i++) {
            File imageFile = new File(images[i]);
            req_file[i] = MultipartBody.Part.createFormData("files[]", imageFile.getName(), RequestBody.create(MediaType.parse("image/*"), imageFile));
        }
        return callApi(remote.upload_files(
                RequestBody.create(MultipartBody.FORM, getModel(ModelUser.class).access_token),
                req_file));
    }

    public Flowable<ApiResponse<ModelDocument.DetailModel>> insertDocument(String title, String content, Integer label, String[] tags, String[] images, Integer categoryId, String ocrText) {
        if (isTestMode()) {
            return Flowable.fromCallable(() -> {
                ApiResponse<ModelDocument.DetailModel> response = new ApiResponse<>();
                response.result = 0;
                response.msg = "";
                ModelDocument.DetailModel data = new ModelDocument.DetailModel();
                data.document = LocalStorageManager.get().insertDocument(
                        title, content, label,
                        Util.arrayJoin(",", tags),
                        Util.arrayJoin(",", images),
                        categoryId, ocrText);
                response.data = data;
                return response;
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        }
        return callApi(remote.insert_document(
                getModel(ModelUser.class).access_token,
                title,
                content,
                label,
                Util.arrayJoin(",", tags),
                Util.arrayJoin(",", images),
                categoryId,
                ocrText));
    }

    public Flowable<ApiResponse<ModelDocument.DetailModel>> getDocumentDetail(Integer docId) {
        if (isTestMode()) {
            return Flowable.fromCallable(() -> {
                ApiResponse<ModelDocument.DetailModel> response = new ApiResponse<>();
                ModelDocument doc = LocalStorageManager.get().getDocumentDetail(docId);
                if (doc != null) {
                    response.result = 0;
                    response.msg = "";
                    ModelDocument.DetailModel data = new ModelDocument.DetailModel();
                    data.document = doc;
                    response.data = data;
                } else {
                    response.result = 401;
                    response.msg = "문서를 찾을 수 없습니다.";
                }
                return response;
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        }
        return callApi(remote.get_document_detail(getModel(ModelUser.class).access_token, docId));
    }

    public Flowable<ApiResponse<ModelBase>> deleteDocumentItem(Integer docId) {
        if (isTestMode()) {
            return Flowable.fromCallable(() -> {
                ApiResponse<ModelBase> response = new ApiResponse<>();
                LocalStorageManager.get().deleteDocument(docId);
                response.result = 0;
                response.msg = "";
                return response;
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        }
        return callApi(remote.delete_document_item(getModel(ModelUser.class).access_token, docId));
    }

    public Flowable<ApiResponse<ModelBase>> updateDocument(Integer docId, String title, String content, Integer label, String[] tags, String[] images, Integer categoryId, String ocrText) {
        if (isTestMode()) {
            return Flowable.fromCallable(() -> {
                ApiResponse<ModelBase> response = new ApiResponse<>();
                LocalStorageManager.get().updateDocument(docId, title, content, label,
                        Util.arrayJoin(",", tags),
                        Util.arrayJoin(",", images),
                        categoryId, ocrText);
                response.result = 0;
                response.msg = "";
                return response;
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        }
        return callApi(remote.update_document(
                getModel(ModelUser.class).access_token,
                docId,
                title,
                content,
                label,
                Util.arrayJoin(",", tags),
                Util.arrayJoin(",", images),
                categoryId,
                ocrText));
    }

    public Flowable<ApiResponse<ModelVersion>> getVersionInfo() {
        return callApi(remote.version_info(0));
    }

    public Flowable<ApiResponse<ModelCode>> requestCodeForFind(String login_id, String phoneNumber) {
        return callApi(remote.request_code_for_find(login_id, phoneNumber));
    }

    public Flowable<ApiResponse<ModelCode>> requestCodeForSignup(String phoneNumber) {
        return callApi(remote.request_code_for_signup(phoneNumber));
    }

    public Flowable<ApiResponse<ModelBase>> changePassword(String login_id,  String password) {
        return callApi(remote.change_pwd(login_id, password));
    }
    public Flowable<ApiResponse<ModelBase>> agreeTerms() {
        return callApi(remote.agree_terms(getModel(ModelUser.class).access_token));
    }
    public Flowable<ApiResponse<ModelAgreement.ListModel>> getAgreeList(String accesstoken) {
        return callApi(remote.get_agree_list(accesstoken));
    }
    public Flowable<ApiResponse<ModelAgreement>> getAgreeDetail(Integer id) {
        return callApi(remote.get_agree_detail(id));
    }

    public Flowable<ApiResponse<ModelBase>> checkLoginId(String loginId) {
        return callApi(remote.check_login_id(loginId));
    }

    public Flowable<ApiResponse<ModelUser>> signup(String loginId, String password, String phone, String name, String birthday, Integer gender, String email, String signup_type) {
        return callApi(remote.signup(loginId, password, phone, name, birthday, gender, email, signup_type));
    }

    public Flowable<ApiResponse<ModelUser>> updateProfile(String name, String birthday, Integer gender, String email, String profile_image) {
        return callApi(remote.profile_update(getModel(ModelUser.class).access_token, name, birthday, gender, email, profile_image));
    }

    public Flowable<ApiResponse<ModelUser>> cancelExit(String login_id) {
        return callApi(remote.cancel_exit(login_id));
    }
    public Flowable<ApiResponse<ModelBase>> requestExit() {
        return callApi(remote.request_exit(getModel(ModelUser.class).access_token));
    }

    public Flowable<ApiResponse<ModelAsk.ListModel>> getAskList() {
        return callApi(remote.get_ask_list(getModel(ModelUser.class).access_token));
    }

    public Flowable<ApiResponse<ModelBase>> insertAsk(String title, String content) {
        return callApi(remote.insert_ask(getModel(ModelUser.class).access_token, title, content));
    }

    public Flowable<ApiResponse<ModelFaqItem.ListModel>> getFaqItemList() {
        return callApi(remote.get_faq_item_list(getModel(ModelUser.class).access_token));
    }
    public Flowable<ApiResponse<ModelFaq.ListModel>> getFaqList(int itemid) {
        return callApi(remote.get_faq_list(getModel(ModelUser.class).access_token, itemid, 0));
    }
    public Flowable<ApiResponse<ModelNotice.ListModel>> getNoticeList() {
        return callApi(remote.get_notice_list(getModel(ModelUser.class).access_token, 0));
    }
    public Flowable<ApiResponse<ModelNoticeDetail>> getNoticeDetail(String id, int is_code) {
        return callApi(remote.get_notice_detail(getModel(ModelUser.class).access_token, id, is_code));
    }

    /********************************
     *  Local
     ********************************/
    public <T extends ModelBase> void setModel(T model) {
        local.setModel(model);
    }

    public <T extends ModelBase> T getModel(Class<T> type) {
        return local.getModel(type);
    }

    public <T extends ModelBase> void removeModel(Class<T> type) {
        local.removeModel(type);
    }


    /********************************
     *  Test Mode Helper
     ********************************/
    
    public boolean isTestMode() {
        ModelUser user = getModel(ModelUser.class);
        return user != null && user.id == 999;
    }

    /********************************
     *  Category APIs
     ********************************/

    public Flowable<ApiResponse<ModelCategory.ListModel>> getCategoryList() {
        if (isTestMode()) {
            return Flowable.fromCallable(() -> {
                ApiResponse<ModelCategory.ListModel> response = new ApiResponse<>();
                response.result = 0;
                response.msg = "";
                ModelCategory.ListModel data = new ModelCategory.ListModel();
                data.category_list = LocalStorageManager.get().getCategories();
                response.data = data;
                return response;
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        }
        return callApi(remote.get_category_list(getModel(ModelUser.class).access_token));
    }

    public Flowable<ApiResponse<ModelCategory.DetailModel>> insertCategory(String name, Integer color) {
        if (isTestMode()) {
            return Flowable.fromCallable(() -> {
                ApiResponse<ModelCategory.DetailModel> response = new ApiResponse<>();
                response.result = 0;
                response.msg = "";
                ModelCategory.DetailModel data = new ModelCategory.DetailModel();
                data.category = LocalStorageManager.get().insertCategory(name, color);
                response.data = data;
                return response;
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        }
        return callApi(remote.insert_category(getModel(ModelUser.class).access_token, name, color));
    }

    public Flowable<ApiResponse<ModelBase>> updateCategory(Integer categoryId, String name, Integer color) {
        if (isTestMode()) {
            return Flowable.fromCallable(() -> {
                ApiResponse<ModelBase> response = new ApiResponse<>();
                LocalStorageManager.get().updateCategory(categoryId, name, color);
                response.result = 0;
                response.msg = "";
                return response;
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        }
        return callApi(remote.update_category(getModel(ModelUser.class).access_token, categoryId, name, color));
    }

    public Flowable<ApiResponse<ModelBase>> deleteCategory(Integer categoryId) {
        if (isTestMode()) {
            return Flowable.fromCallable(() -> {
                ApiResponse<ModelBase> response = new ApiResponse<>();
                LocalStorageManager.get().deleteCategory(categoryId);
                response.result = 0;
                response.msg = "";
                return response;
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        }
        return callApi(remote.delete_category(getModel(ModelUser.class).access_token, categoryId));
    }

    /********************************
     *  global
     ********************************/

}
