package com.example.surdu.smshunter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;

import java.util.Arrays;

public class SMSListener extends BroadcastReceiver {

    final static private String APP_KEY = "wdshtp32ustrk8j";
    final static private String APP_SECRET = "SECRET";

    private void loadAuth(AndroidAuthSession session, Context context) {
        SharedPreferences prefs = context.getSharedPreferences("prefs", 0);
        String token = prefs.getString("TOKEN", null);
        if (token == null || token.length() == 0) {
            return;
        }

        session.setOAuth2AccessToken(token);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys);
        loadAuth(session, context);

        DropboxAPI<AndroidAuthSession> mDBApi = new DropboxAPI<AndroidAuthSession>(session);


        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                String message = smsMessage.getMessageBody();
                String sender = smsMessage.getOriginatingAddress();

                Store.context = context;
                String[] senders = Store.get("SENDERS").split(",");

                for (int f = 0; f < senders.length; f++) {
                    senders[f] = senders[f].trim();
                }

                if (Arrays.asList(senders).contains(sender)) {
                    new UpdateDropboxTask().execute(mDBApi, message);
                }
            }
        }
    }
}
