package com.rubensvaz.android.receitaboa.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.rubensfelipe.android.receitaboa.R;
import com.rubensvaz.android.receitaboa.fragment.ReceitasUsuariosFragment;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
    }

    public void enviarNotificacao(String titulo, String corpo){

        //Configurar notificação
        String canal = getString(R.string.default_notification_channel_id);
        Uri uriSom = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent i = new Intent(this, ReceitasUsuariosFragment.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0, i, PendingIntent.FLAG_ONE_SHOT);

        //Criar notificação
        NotificationCompat.Builder notificacao = new NotificationCompat.Builder(this, canal)
                .setContentTitle(titulo)
                .setContentText(corpo)
                .setSmallIcon(R.drawable.ic_chef_24dp)
                .setSound(uriSom)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        //Recupera notificationManager
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //Verifica versão do Android a partir do versão Oreo para configurar canal de notificação
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){ //caso esteja utilizando uma versão >= android oreo (26)
            NotificationChannel channel = new NotificationChannel(canal,"canal", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        //Envia notificação
        notificationManager.notify(0, notificacao.build());

    }

    @Override
    public void onNewToken(@NonNull String s) { //qdo o app é instalado em um telefone, um token será gerado para aquele telefone
        super.onNewToken(s);

        Log.i("onNewToken", "onNewToken: " + s);
    }
}
