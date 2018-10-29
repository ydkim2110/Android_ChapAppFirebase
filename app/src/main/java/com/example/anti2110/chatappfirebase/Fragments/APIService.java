package com.example.anti2110.chatappfirebase.Fragments;

import com.example.anti2110.chatappfirebase.Notifications.MyResponse;
import com.example.anti2110.chatappfirebase.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
            "Authorization:key=AAAAUEmbfJI:APA91bHzfK5FRR1o3IPJDcXlqhhS3ybEGIOR-G5Q4P79IZXb02i1WotKbHpdNUFO_p6xIkxDCZd4q_5BqTkG7vEEzWVuxEx6pXK3IpROHkgY42djb7ova7mj1AErEEyXJVW2FWjGiyRt",
            "Content-Type:application/json"
    })

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);

}
