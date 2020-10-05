package com.amiel.tls;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AsyncFetch extends AsyncTask<String, Void, List<List<Object>>> {

    private Context context;
    private onResponse onResponse;
    private ProgressDialog dialog;
    private Exception ex;

    public onResponse getOnResponse() {
        return onResponse;
    }

    public void setOnResponse(onResponse onResponse) {
        this.onResponse = onResponse;
    }

    private com.google.api.services.sheets.v4.Sheets mService = null;

    AsyncFetch(Context context, GoogleAccountCredential credential) {
        this.context = context;
        this.dialog = new ProgressDialog(context);

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("TLSApp")
                .build();
    }

    /**
     * Background task to call Google Sheets API.
     * @param params no parameters needed for this task.
     */
    @Override
    protected List<List<Object>> doInBackground(String... params) {
        try {
            return getDataFromApi(params[0], params[1]);
        } catch(UserRecoverableAuthIOException e) {
            ex = e;
            return null;
        }
        catch (Exception e) {
            dialog.dismiss();
            ex = e;
            cancel(true);
            return null;
        }
    }

    private List<List<Object>> getDataFromApi(String spreadsheetId, String range) throws IOException {
        ValueRange response = this.mService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();

        return values;
    }



    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setMessage("טוען נתונים...");
        dialog.show();
    }

    @Override
    protected void onPostExecute(List<List<Object>> output) {
        dialog.dismiss();
        super.onPostExecute(output);
        this.onResponse.onResponse(output, ex);
    }

    public interface onResponse {
        public void onResponse(List<List<Object>> values, Exception ex);
    }
}
