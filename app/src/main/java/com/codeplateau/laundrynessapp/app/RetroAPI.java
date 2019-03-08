package com.codeplateau.laundrynessapp.app;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

public interface RetroAPI {

    @FormUrlEncoded
    @POST("/userdetails/login")
    public void url_login(@Field("email") String UEId,
                          @Field("password") String UPwd,
                          Callback<retrofit.client.Response> response);

    @FormUrlEncoded
    @POST("/user_login")
    public void url_loginuser_email(@Field("email") String email,
                                    @Field("password") String password, Callback<Response> response);

    @FormUrlEncoded
    @POST("/user_login")
    public void url_loginuser_mobileno(@Field("mobileno") String mobileno,
                                       @Field("password") String password, Callback<Response> response);

    @FormUrlEncoded
    @POST("/user_register")
    public void url_signup(@Field("email") String email,
                           @Field("password") String password,
                           @Field("username") String username,
                           @Field("mobileno") String mobileno, Callback<Response> response);

    @FormUrlEncoded
    @POST("/otp_send")
    public void sendOTP(@Field("email") String email,
                        @Field("mobile") String mobile, Callback<Response> response);

    @FormUrlEncoded
    @POST("/get_subcategory_data")
    public void url_get_subcategory(@Field("category_id") String category_id,
                                    Callback<retrofit.client.Response> response);

    @FormUrlEncoded
    @POST("/cart_add")
    public void url_card(@Field("service_id") String service_id,
                         @Field("category_id") String category_id,
                         @Field("subcategory_id") String subcategory_id,
                         @Field("price") String price,
                         @Field("qty") String qty,
                         @Field("user_id") String user_id,
                         @Field("cart_id") String cart_id,
                         Callback<retrofit.client.Response> response);

    @FormUrlEncoded
    @POST("/cart_list")
    public void url_get_cartlist(@Field("user_id") String user_id,
                                 @Field("cart_id") String cart_data,
                                 Callback<retrofit.client.Response> response);
    @FormUrlEncoded
    @POST("/get_service_data_with_category_data")
    public void url_categorylist(@Field("service_id") String service_id, Callback<Response> response);

    @FormUrlEncoded
    @POST("/get_user_data1")
    public void getprofiledrawer(@Field("id") String id,Callback<Response> response);
}
