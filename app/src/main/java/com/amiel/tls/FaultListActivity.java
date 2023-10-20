package com.amiel.tls;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class FaultListActivity extends AppCompatActivity implements AsyncFetch.onResponse, EasyPermissions.PermissionCallbacks{

    private ListView faultListItems;
    private TextView totalFaults;
    private SwipeRefreshLayout swipeRefreshLayout;
    GoogleAccountCredential mCredential;
    private static final String[] SCOPES = { SheetsScopes.SPREADSHEETS };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fault_list);

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        faultListItems = (ListView) findViewById(R.id.fault_list_faults_listView);
        faultListItems.setEmptyView(findViewById(R.id.fault_list_emptyElement));

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_fault_list_swipe_refresh_layout);

        totalFaults = (TextView) findViewById(R.id.fault_list_total);

        getResultsFromApi();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getResultsFromApi();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void updateList() {
        AsyncFetch faultsInfoFetch = new AsyncFetch(this, mCredential);
        faultsInfoFetch.setOnResponse(this);
        faultsInfoFetch.execute(Constants.FAULTS_SPREAD_SHEET_ID, Constants.FAULTS_SHEET_NAME + Constants.FAULTS_SHEET_FETCH_RANGE);
    }

    @Override
    public void onResponse(List<List<Object>> values, Exception ex) {
        if(ex instanceof UserRecoverableAuthIOException)
        {
            startActivityForResult(((UserRecoverableAuthIOException) ex).getIntent(), Constants.REQUEST_AUTHORIZATION);
        }
        final FaultListAdapter faultsListAdapter = new FaultListAdapter(FaultListActivity.this, R.layout.fault_list_item, mCredential, totalFaults);

        List<FaultListItem> results = new ArrayList<>();
        if (values != null) {
            for (List row : values) {
                if(values.indexOf(row) == 0 || row.size() == 0) {
                    continue;
                }

                FaultListItem currentItem = new FaultListItem();

                currentItem.rowID = values.indexOf(row) + Constants.SHEET_START_ROW_INDEX;

                if(!((String)row.get(2)).isEmpty()) {
                    currentItem.roomType = getString(R.string.topaz);
                    currentItem.gender = getString(R.string.boys);
                    currentItem.roomName = (String)row.get(2);
                } else if(!((String)row.get(3)).isEmpty()) {
                    currentItem.roomType = getString(R.string.topaz);
                    currentItem.gender = getString(R.string.girls);
                    currentItem.roomName = (String)row.get(3);
                } else if(!((String)row.get(4)).isEmpty()) {
                    currentItem.roomType = getString(R.string.lotem);
                    currentItem.gender = getString(R.string.boys);
                    currentItem.roomName = (String)row.get(4);
                } else if(!((String)row.get(5)).isEmpty()) {
                    currentItem.roomType = getString(R.string.lotem);
                    currentItem.gender = getString(R.string.girls);
                    currentItem.roomName = (String)row.get(5);
                }

                currentItem.faultIn = (String)row.get(6);
                currentItem.airConditionNumber = (String)row.get(7);
                currentItem.description = (String)row.get(8);

                results.add(currentItem);
            }
        }

        for(FaultListItem currFault : results) {
            faultsListAdapter.add(currFault);
        }
        faultListItems.setAdapter(faultsListAdapter);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                totalFaults.setText(String.format(Locale.getDefault(), "%d", faultsListAdapter.getCount()));
            }
        });
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            Toast.makeText(this, getString(R.string.error_message_internet_connection), Toast.LENGTH_SHORT).show();
        } else {
            updateList();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(Constants.REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(Constants.PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        Constants.REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.error_message_google_account_access),
                    Constants.REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case Constants.REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(this, getString(R.string.error_message_google_play_services), Toast.LENGTH_LONG).show();
                } else {
                    getResultsFromApi();
                }
                break;
            case Constants.REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(Constants.PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case Constants.REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
            case Constants.REQUEST_PICK_CONTACT_ADAPTER:
                if(resultCode == RESULT_OK)
                {
                    ((FaultListAdapter)faultListItems.getAdapter()).onActivityResult(requestCode, resultCode, data);
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                FaultListActivity.this,
                connectionStatusCode,
                Constants.REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }
}
