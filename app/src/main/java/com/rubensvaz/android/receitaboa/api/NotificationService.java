package com.rubensvaz.android.receitaboa.api;

import com.rubensvaz.android.receitaboa.model.NotificacaoDados;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface NotificationService {

   @Headers({"Authorization:key=AAAATGcEDV4:APA91bHQyIyYhkCrHsePQKU8HKkKEQi9DWeOELXgMuqqTef33tusvJ2y9AX8GihRCSQr6wTnOIzXskbxBISw_0YPJYfGHBUGFmY7nPgrSblv9TWBeYVHkphpGSgV--UUh_rLiR8-9rbm",
            "Content-Type:application/json"})

   @POST("send")
   Call<NotificacaoDados> salvarNotificacao(@Body NotificacaoDados notificacaoDados);

}
