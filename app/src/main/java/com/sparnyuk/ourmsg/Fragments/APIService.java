package com.sparnyuk.ourmsg.Fragments;

import com.sparnyuk.ourmsg.Notification.MyResponse;
import com.sparnyuk.ourmsg.Notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA0d2zC3g:APA91bHbJnuCuNy1KsoNadY0D2nn8camHuaFgtvZQJsHj8PG1Ev-YHsxb7tsyOMMRUXih5AXcgBQ8rt-vN4vcgvH6ApokYnrMfOcK4KUzwBp5pS8_uGZ1KGYO5R-jUrWSXTYr_L_wo2c"
            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
