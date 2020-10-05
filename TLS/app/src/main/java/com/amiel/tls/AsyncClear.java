package com.amiel.tls;

import android.content.Context;
import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.ClearValuesResponse;

public class AsyncClear extends AsyncTask<String, Void, Void> {

    public AsyncClear(Context context, GoogleAccountCredential credential) {
        this.context = context;

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("TLSApp")
                .build();
    }

    private Context context;
    private com.google.api.services.sheets.v4.Sheets mService = null;

    @Override
    protected Void doInBackground(String... params) {
        try {
            ClearValuesRequest requestBody = new ClearValuesRequest();

            ClearValuesResponse response = this.mService.spreadsheets().values()
                    .clear(params[0], params[1], requestBody)
                    .execute();
            return null;
        } catch (Exception e) {
            cancel(true);
            return null;
        }
    }
}
