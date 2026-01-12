package com.kyad.traystorage.data.model;

import java.util.List;

public class ModelUser extends ModelBase {
    //public String login_id; ModelBase
    public int id;
    public String name;
    public String phone_number;
    public Integer signup_type;
    public Integer status = 0; //0-ok, -1-withdrawl
    public Integer gender;
    public String birthday;
    public String email;
    public String profile_image;
    public String create_time;
    public String exit_reg_time;
    public String stop_remark;
    public String access_token = "123123";
    public Integer is_agree;
    //auto login
    public String password;
    public boolean isAutoLogin = false;
}
