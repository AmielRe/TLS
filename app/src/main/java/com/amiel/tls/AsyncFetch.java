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
import java.util.List;

public class AsyncFetch extends AsyncTask<String, Void, List<List<Object>>> {

    private Context context;
    private onResponse onResponse;
    private ProgressDialog dialog;
    private Exception ex;

    void setOnResponse(onResponse onResponse) {
        this.onResponse = onResponse;
    }

    private com.google.api.services.sheets.v4.Sheets mService;

    AsyncFetch(Context context, GoogleAccountCredential credential) {
        this.context = context;
        this.dialog = new ProgressDialog(context);

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                transport, jsonFactory, credential)
                .setApplicationName(Constants.GOOGLE_SHEET_APP_NAME)
                .build();
    }

    /**
     * Background task to call Google Sheets API.
     * @param params no parameters needed for this task.
     */
    @Override
    protected List<List<Object>> doInBackground(String... params) {
        try {
            String spreadSheetId = params[0];
            String range = params[1];

            return getDataFromApi(spreadSheetId, range);
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
        return response.getValues();
    }



    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setMessage(context.getString(R.string.loading));
        dialog.show();
    }

    @Override
    protected void onPostExecute(List<List<Object>> output) {
        dialog.dismiss();
        super.onPostExecute(output);
        this.onResponse.onResponse(output, ex);
    }

    public interface onResponse {
        void onResponse(List<List<Object>> values, Exception ex);
    }
}
