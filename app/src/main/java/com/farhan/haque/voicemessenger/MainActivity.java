package com.farhan.haque.voicemessenger;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class MainActivity extends ActionBarActivity {
    TextView titleTextView;
    private static final int SPEECH_REQUEST_CODE = 0;
    public static final String ACTION_SMS_SENT = "com.farhan.haque.voicemessenger";
    String phoneNumber,spokenText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        titleTextView= (TextView) findViewById(R.id.titleTextView);
        if(savedInstanceState!=null){
            savedInstanceState.getString("spokenText","");
        }
        // Register broadcast receivers for SMS sent and delivered intents
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = null;
                boolean error = true;
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        message = "Message sent!";
                        error = false;
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        message = "Error.";
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        message = "Error: No service.";
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        message = "Error: Null PDU.";
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        message = "Error: Radio off.";
                        break;
                }
                titleTextView.setText(message);
                titleTextView.setTextColor(error ? Color.RED : Color.GREEN);

            }
        }, new IntentFilter(ACTION_SMS_SENT));
    }

    // This callback is invoked when the Speech Recognizer returns.
// This is where you process the intent and extract the speech text from the intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            spokenText = results.get(0);
            // Do something with spokenText
            Toast.makeText(this,spokenText,Toast.LENGTH_LONG).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    // Create an intent that can start the Speech Recognizer activity
    public void displaySpeechRecognizer(View v) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
// Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }
    //  Find contact based on name.
 public void findNumbers(View v){
     String NAME= "Farhan";
     ContentResolver cr = getContentResolver();
     Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
             "DISPLAY_NAME = '" + NAME + "'", null, null);
     if (cursor.moveToFirst()) {
         String contactId =
                 cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
         //
         //  Get all phone numbers.
         //
         Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                 ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
         while (phones.moveToNext()) {
             String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
             int type = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
          /*   switch (type) {
                 case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                     // do something with the Home number here...
                     break;
                 case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                     // do something with the Mobile number here...
                     break;
                 case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                     // do something with the Work number here...
                     break;
             }*/
             if(type==ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                 Toast.makeText(this, number, Toast.LENGTH_LONG).show();
                 phoneNumber = number;
             } else{
                 Toast.makeText(this,"please enter a mobile phone number",Toast.LENGTH_SHORT).show();
             }
         }
         phones.close();
     }
     cursor.close();
 }
// sends the sms
    public void onClickSend(View v)
    {
        //sms body coming from user input
       // String strSMSBody = "This is a test";

        SmsManager sms = SmsManager.getDefault();
        List<String> messages = sms.divideMessage(spokenText);
        for (String message : messages) {
            sms.sendTextMessage(phoneNumber, null, message, PendingIntent.getBroadcast(
                    this, 0, new Intent(ACTION_SMS_SENT), 0), null);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("spokenText",spokenText);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        savedInstanceState.getString("spokenText","");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
