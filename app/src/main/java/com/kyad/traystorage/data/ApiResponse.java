package com.kyad.traystorage.data;

import java.io.Serializable;

public class ApiResponse<T> implements Serializable {
    public int result;
    public String msg;
    public String reason;
    public T data;

    public static final int API_RESULT_SUCCESS=0;
    public static final int API_RESULT_ERROR_SERVER=101;
    public static final int API_RESULT_ERROR_DB=102;
    public static final int API_RESULT_ERROR_PARAM=103;
    public static final int API_RESULT_ERROR_ACCESS_TOKEN=104;
    public static final int API_RESULT_ERROR_VERIFY_CODE=105;
    public static final int API_RESULT_ERROR_USER_NO_EXIST=202;
    public static final int API_RESULT_ERROR_ID_DUPLICATED=203;
    public static final int API_RESULT_ERROR_NICKNAME_DUPLICATED=204;
    public static final int API_RESULT_ERROR_PHONE_DUPLICATED=206;
    public static final int API_RESULT_ERROR_USER_CHECKING=207;
    public static final int API_RESULT_ERROR_UPLOAD=208;
    public static final int API_RESULT_ERROR_USER_PAUSED=209;
    public static final int API_RESULT_ERROR_USER_EXIT=210;
    public static final int API_RESULT_ERROR_WRONG_PWD=211;
    public static final int API_RESULT_ERROR_EMAIL_DUPLICATED=212;
    public static final int API_RESULT_ERROR_USER_DUPLICATED=214;
    public static final int API_RESULT_ERROR_ALREADY_CHECKED=215;
    public static final int API_RESULT_ALREADY_USER_FREE_PAY=300;
}
