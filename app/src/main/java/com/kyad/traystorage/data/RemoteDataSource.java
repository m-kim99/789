package com.kyad.traystorage.data;

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

import java.util.List;

import io.reactivex.Flowable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface RemoteDataSource {
    //    String API_BASE_URL = "http://192.168.2.101:8005/api/"; //py server
//    String API_BASE_URL = "http://192.168.0.13:8205/api/"; //local server
//    String API_BASE_URL = "http://traystorage.kr/server/api/"; //real server
    String API_BASE_URL = "https://rtlikunlsieloeprkbiv.supabase.co/functions/v1/"; //supabase
    String API_TERM_URL = "http://traystorage.kr/server/api/app/term?type=term";
    String API_PRIVACY_URL = "http://traystorage.kr/server/api/app/term?type=privacy";
    String API_MARKETING_URL = "http://traystorage.kr/server/api/app/term?type=marketing";

    int API_RESULT_SUCCESS = 0;
    int API_RESULT_ERROR_SYSTEM = 101;
    int API_RESULT_ERROR_DB = 102;
    int API_RESULT_ERROR_PRIVILEGE = 103;
    int API_RESULT_ERROR_PARAM = 104;
    int API_RESULT_ERROR_UPLOAD = 105;
    int API_RESULT_ERROR_ACCESS_TOKEN = 106;

    @POST("user/login")
    @FormUrlEncoded
    Flowable<ApiResponse<ModelUser>> login(
            @Field("login_id") String login_id,
            @Field("password") String password
    );

    @POST("app/popup_info")
    @FormUrlEncoded
    Flowable<ApiResponse<ModelPopupInfo.ListModel>> popup_info(
            @Field("access_token") String access_token,
            @Field("platform") Integer platform
    );

    @POST("app/get_document_list")
    @FormUrlEncoded
    Flowable<ApiResponse<ModelDocument.ListModel>> get_document_list(
            @Field("access_token") String access_token,
            @Field("keyword") String keyword,
            @Field("category_id") Integer category_id
    );

    @POST("upload/upload_file")
    @Multipart
    Flowable<ApiResponse<ModelBase>> upload_file(
            @Part("access_token") RequestBody access_token,
            @Part MultipartBody.Part upload_file
    );

    @POST("upload/upload_files")
    @Multipart
    Flowable<ApiResponse<List<ModelUploadFile>>> upload_files(
            @Part("access_token") RequestBody access_token,
            @Part MultipartBody.Part[] upload_files
    );

    @POST("app/insert_document")
    @FormUrlEncoded
    Flowable<ApiResponse<ModelDocument.DetailModel>> insert_document(
            @Field("access_token") String access_token,
            @Field("title") String title,
            @Field("content") String content,
            @Field("label") Integer label,
            @Field("tags") String tags,
            @Field("images") String images,
            @Field("category_id") Integer category_id,
            @Field("ocr_text") String ocr_text
    );

    @POST("app/get_document_detail")
    @FormUrlEncoded
    Flowable<ApiResponse<ModelDocument.DetailModel>> get_document_detail(
            @Field("access_token") String access_token,
            @Field("id") Integer id
    );

    @POST("app/delete_document_item")
    @FormUrlEncoded
    Flowable<ApiResponse<ModelBase>> delete_document_item(
            @Field("access_token") String access_token,
            @Field("id") Integer id
    );

    @POST("app/update_document")
    @FormUrlEncoded
    Flowable<ApiResponse<ModelBase>> update_document(
            @Field("access_token") String access_token,
            @Field("id") Integer id,
            @Field("title") String title,
            @Field("content") String content,
            @Field("label") Integer label,
            @Field("tags") String tags,
            @Field("images") String images,
            @Field("category_id") Integer category_id,
            @Field("ocr_text") String ocr_text
    );

    @POST("app/version_info")
    @FormUrlEncoded
    Flowable<ApiResponse<ModelVersion>> version_info(
            @Field("platform") Integer platform
    );

    @POST("user/request_code_for_find")
    @FormUrlEncoded
    Flowable<ApiResponse<ModelCode>> request_code_for_find(
            @Field("login_id") String login_id,
            @Field("phone_number") String phone_number
    );

    @POST("user/request_code_for_signup")
    @FormUrlEncoded
    Flowable<ApiResponse<ModelCode>> request_code_for_signup(
            @Field("phone_number") String phone_number
    );

    @POST("user/change_pwd")
    @FormUrlEncoded
    Flowable<ApiResponse<ModelBase>> change_pwd(
            @Field("login_id") String login_id,
            @Field("password") String password
    );

    @POST("user/agree_terms")
    @FormUrlEncoded
    Flowable<ApiResponse<ModelBase>> agree_terms(
            @Field("access_token") String access_token
    );

    @POST("app/get_agree_list")
    @FormUrlEncoded
    Flowable<ApiResponse<ModelAgreement.ListModel>> get_agree_list(
            @Field("access_token") String access_token //if empty, get all
    );

    @POST("app/get_agree_detail")
    @FormUrlEncoded
    Flowable<ApiResponse<ModelAgreement>> get_agree_detail(
            @Field("id") Integer id
    );

    @POST("user/check_login_id")
    @FormUrlEncoded
    Flowable<ApiResponse<ModelBase>> check_login_id(
            @Field("login_id") String login_id
    );

    @POST("user/cancel_exit")
    @FormUrlEncoded
    Flowable<ApiResponse<ModelUser>> cancel_exit(
            @Field("login_id") String login_id
    );

    @POST("user/request_exit")
    @FormUrlEncoded
    Flowable<ApiResponse<ModelBase>> request_exit(
            @Field("access_token") String access_token
    );

    @POST("user/signup")
    @FormUrlEncoded
    Flowable<ApiResponse<ModelUser>> signup(
            @Field("login_id") String login_id,
            @Field("password") String password,
            @Field("phone_number") String phone_number,
            @Field("name") String name,
            @Field("birthday") String birthday,
            @Field("gender") Integer gender,
            @Field("email") String email,
            @Field("signup_type") String signup_type
    );

    @POST("user/profile_update")
    @FormUrlEncoded
    Flowable<ApiResponse<ModelUser>> profile_update(
            @Field("access_token") String access_token,
            @Field("name") String name,
            @Field("birthday") String birthday,
            @Field("gender") Integer gender,
            @Field("email") String email,
            @Field("profile_image") String profile_image
    );

    @POST("app/get_ask_list")
    @FormUrlEncoded
    Flowable<ApiResponse<ModelAsk.ListModel>> get_ask_list(
            @Field("access_token") String access_token
    );

    @POST("app/insert_ask")
    @FormUrlEncoded
    Flowable<ApiResponse<ModelBase>> insert_ask(
            @Field("access_token") String access_token,
            @Field("title") String title,
            @Field("content") String content
    );

    @POST("app/view_click_popup")
    @FormUrlEncoded
    Flowable<ApiResponse<ModelBase>> view_click_popup(
            @Field("access_token") String access_token,
            @Field("id") Integer id,
            @Field("type") Integer type
    );

    @POST("app/get_faq_item_list")
    @FormUrlEncoded
    Flowable<ApiResponse<ModelFaqItem.ListModel>> get_faq_item_list(
            @Field("access_token") String access_token
    );

    @POST("app/get_faq_list")
    @FormUrlEncoded
    Flowable<ApiResponse<ModelFaq.ListModel>> get_faq_list(
            @Field("access_token") String access_token,
            @Field("faq_item_id") Integer faq_item_id, // -1 ALL
            @Field("platform") Integer platform
    );

    @POST("app/get_notice_list")
    @FormUrlEncoded
    Flowable<ApiResponse<ModelNotice.ListModel>> get_notice_list(
            @Field("access_token") String access_token,
            @Field("platform") Integer platform
    );

    @POST("app/get_notice_detail")
    @FormUrlEncoded
    Flowable<ApiResponse<ModelNoticeDetail>> get_notice_detail(
            @Field("access_token") String access_token,
            @Field("id") String id,
            @Field("is_code") Integer is_code
    );

    // =====================================================
    // Category APIs
    // =====================================================

    @POST("app/get_category_list")
    @FormUrlEncoded
    Flowable<ApiResponse<ModelCategory.ListModel>> get_category_list(
            @Field("access_token") String access_token
    );

    @POST("app/insert_category")
    @FormUrlEncoded
    Flowable<ApiResponse<ModelCategory.DetailModel>> insert_category(
            @Field("access_token") String access_token,
            @Field("name") String name,
            @Field("color") Integer color
    );

    @POST("app/update_category")
    @FormUrlEncoded
    Flowable<ApiResponse<ModelBase>> update_category(
            @Field("access_token") String access_token,
            @Field("id") Integer id,
            @Field("name") String name,
            @Field("color") Integer color
    );

    @POST("app/delete_category")
    @FormUrlEncoded
    Flowable<ApiResponse<ModelBase>> delete_category(
            @Field("access_token") String access_token,
            @Field("id") Integer id
    );

}
