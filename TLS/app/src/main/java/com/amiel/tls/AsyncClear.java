package com.amiel.tls;

import android.content.Context;
import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;

public class AsyncClear extends AsyncTask<String, Void, Void> {

    AsyncClear(Context context, GoogleAccountCredential credential) {
        this.context = context;

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                transport, jsonFactory, credential)
                .setApplicationName(Constants.GOOGLE_SHEET_APP_NAME)
                .build();
    }

    private Context context;
    private com.google.api.services.sheets.v4.Sheets mService;

    @Override
    protected Void doInBackground(String... params) {
        try {
            String spreadSheetId = params[0];
            String range = params[1];

            ClearValuesRequest requestBody = new ClearValuesRequest();

            this.mService.spreadsheets().values()
                    .clear(spreadSheetId, range, requestBody)
                    .execute();

            return null;
        } catch (Exception e) {
            cancel(true);
            return null;
        }
    }
}
