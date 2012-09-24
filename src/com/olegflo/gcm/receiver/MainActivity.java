package com.olegflo.gcm.receiver;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.gcm.GCMRegistrar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends Activity {

    Context context;

    String TAG = "GCM Android";

    TextView registrationId;

    private ArrayList<String> messagesList = new ArrayList<String>();

    ArrayAdapter<String> messagesAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        context = this;

        fillTextViews();

        GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);

        final String regId = GCMRegistrar.getRegistrationId(this);
        Log.v(TAG, "Run GCMRegistrar.getRegistrationId(), regId=" + regId);
        if (regId.equals("")) {
            register();
        } else {
            Log.v(TAG, "Already registered");
            registrationId.setText(regId);
        }

    }

    private void fillTextViews() {
        TextView projectId = (TextView) findViewById(R.id.projId);
        projectId.setText(Config.SENDER_ID);

        TextView apiKey = (TextView) findViewById(R.id.apiKey);
        apiKey.setText(Config.API_KEY);

        registrationId = (TextView) findViewById(R.id.regId);

        // Receive registration event
        GCMIntentService.setOnRegistrationIdReceived(new GCMIntentService.OnRegistrationIdReceived() {
            @Override
            public void onReceived(final String regId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        registrationId.setText(regId);
                    }
                });
            }
        });

        Button share = (Button) findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String shareString = String.format("proj_id:\n%s\n\nreg_id:\n%s\n\napp_key:\n%s",
                        Config.SENDER_ID, registrationId.getText().toString(), Config.API_KEY);

                Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setType("plain/text");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "GCM Push stuff");
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareString);
                startActivity(Intent.createChooser(shareIntent, "Share via"));
            }
        });

        // Receive messages
        GCMIntentService.setOnMessageReceived(new GCMIntentService.OnMessageReceived() {
            @Override
            public void onReceived(final String message) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                messagesList.add(String.format("%s : %s", sdf.format(new Date()), message));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        messagesAdapter.notifyDataSetChanged();
                        showNotification(message);
                    }
                });
                System.out.println(messagesList);
            }
        });

        messagesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, messagesList);
        ListView messages = (ListView) findViewById(R.id.messages);
        messages.setAdapter(messagesAdapter);
    }

    private void register() {
        Log.v(TAG, "Run GCMRegistrar.register(), SENDER_ID=" + Config.SENDER_ID);
        GCMRegistrar.register(this, Config.SENDER_ID);
    }

    private void showNotification(String message) {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(NOTIFICATION_SERVICE);

        Notification pushReceived = new Notification();
        pushReceived.icon = android.R.drawable.stat_notify_sync;
        pushReceived.when = System.currentTimeMillis();
        pushReceived.defaults |= Notification.DEFAULT_ALL;
        pushReceived.flags |= Notification.FLAG_AUTO_CANCEL;

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);

        String contentTitle = "Push received";
        String contentText = message;
        pushReceived.setLatestEventInfo(context, contentTitle,
                contentText, contentIntent);

        notificationManager.notify(0, pushReceived);
    }
}