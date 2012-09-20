package com.olegflo.gcm.generator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gcm.GCMRegistrar;

public class MainActivity extends Activity {

    String TAG = "GCM Android";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        fillTextViews();

        GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);

        final String regId = GCMRegistrar.getRegistrationId(this);
        Log.v(TAG, "Run GCMRegistrar.getRegistrationId(), regId=" + regId);
        if (regId.equals("")) {
            register();
        } else {
            Log.v(TAG, "Already registered, try to unregister");
            GCMRegistrar.unregister(this);
            register();
        }

    }

    private void fillTextViews() {
        TextView projectId = (TextView) findViewById(R.id.projId);
        projectId.setText(Config.SENDER_ID);

        TextView apiKey = (TextView) findViewById(R.id.apiKey);
        apiKey.setText(Config.API_KEY);

        final TextView registrationId = (TextView) findViewById(R.id.regId);
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
    }

    private void register() {
        Log.v(TAG, "Run GCMRegistrar.register(), SENDER_ID=" + Config.SENDER_ID);
        GCMRegistrar.register(this, Config.SENDER_ID);
    }

}