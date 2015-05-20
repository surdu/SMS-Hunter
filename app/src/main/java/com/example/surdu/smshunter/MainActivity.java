package com.example.surdu.smshunter;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;


public class MainActivity extends AppCompatActivity {

    final static private String APP_KEY = "wdshtp32ustrk8j";
    final static private String APP_SECRET = "SECRET";

    private DropboxAPI<AndroidAuthSession> mDBApi;

    public void toast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Store.context = this;

        EditText senderInput = (EditText) findViewById(R.id.senderInput);
        senderInput.setText(Store.get("SENDERS"));
        senderInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView input, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Store.set("SENDERS", input.getText().toString());

                    //dismiss keyboard
                    InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(input.getWindowToken(), 0);

                    input.clearFocus();

                    return true;
                }

                return false;
            }
        });

        AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys);
        loadAuth(session);

        mDBApi = new DropboxAPI<AndroidAuthSession>(session);

        if (!mDBApi.getSession().isLinked()) {
            mDBApi.getSession().startOAuth2Authentication(MainActivity.this);
        }
    }

    public void dumpExistingSMS(View btn) {
        final String SMS_URI_INBOX = "content://sms/inbox";
        String allSMS = "";
        try {
            Uri uri = Uri.parse(SMS_URI_INBOX);

            String[] senders = Store.get("SENDERS").split(",");
            String sendersQuery = "address in (";
            for (int f = 0; f < senders.length; f++) {
                sendersQuery += "'" + senders[f].trim() + "'";
            }
            sendersQuery += ")";

            String[] projection = new String[] { "_id", "address", "person", "body", "date", "type" };
            Cursor cur = getContentResolver().query(uri, projection, sendersQuery, null, "date ASC");
            if (cur.moveToFirst()) {
                int index_Body = cur.getColumnIndex("body");
                do {
                    allSMS += cur.getString(index_Body) + "\n";
                } while (cur.moveToNext());

                if (!cur.isClosed()) {
                    cur.close();
                    cur = null;
                }

                new UpdateDropboxTask().execute(mDBApi, allSMS);
                toast("SMS dumped to Dropbox");
            }
            else {
                toast("No SMS found from the above senders");
            }
        } catch (SQLiteException ex) {
            Log.d("SQLiteException", ex.getMessage());
        }
}

    public void testDropboxLink(View btn) {
        String number = "to be added";
        new UpdateDropboxTask().execute(mDBApi, "Watching for SMS from these numbers: " + Store.get("SENDERS"));
        toast("Check your Dropbox");
    }

    protected void onResume() {
        super.onResume();

        AndroidAuthSession session = mDBApi.getSession();

        if (session.authenticationSuccessful()) {
            try {
                session.finishAuthentication();
                storeAuth(session);
            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        }
    }

    private void storeAuth(AndroidAuthSession session) {
        String oauth2AccessToken = session.getOAuth2AccessToken();
        if (oauth2AccessToken != null) {
            Store.set("TOKEN", oauth2AccessToken);
        }
    }

    private void loadAuth(AndroidAuthSession session) {
        String token = Store.get("TOKEN");
        if (token == null || token.length() == 0) {
            return;
        }

        session.setOAuth2AccessToken(token);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
