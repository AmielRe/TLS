package com.amiel.tls;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.amiel.tls.db.DBHandler;
import com.amiel.tls.db.entities.Person;
import com.amiel.tls.db.entities.Room;
import com.google.firebase.database.DatabaseError;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class RoomManagementActivity extends AppCompatActivity {
    private static final String EXTRA_ROOM_KEY = "room";
    private static final String EXTRA_ROOM_ID_KEY = "roomID";

    private ListView roomPersons;
    private SwipeRefreshLayout swipeRefreshLayout;
    private static Integer roomID;
    private TextView roomCapacity;
    private Integer maxCapacity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_management);

        roomPersons = (ListView) findViewById(R.id.room_management_persons_listView);
        roomPersons.setEmptyView(findViewById(R.id.room_management_emptyElement));

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_room_management_swipe_refresh_layout);

        TextView roomName = (TextView) findViewById(R.id.room_management_column1_room_name);
        roomCapacity = (TextView) findViewById(R.id.room_management_column1_capacity);
        TextView roomType = (TextView) findViewById(R.id.room_management_column2_type);
        TextView roomGender = (TextView) findViewById(R.id.room_management_column2_gender);

        Room roomToView = (Room) Objects.requireNonNull(getIntent().getExtras()).getSerializable(EXTRA_ROOM_KEY);
        maxCapacity = roomToView.maxCapacity;
        roomID = getIntent().getExtras().getInt(EXTRA_ROOM_ID_KEY);

        roomName.setText(roomToView.roomName);
        roomCapacity.setText(String.format(Locale.getDefault(), "%d/%d", roomToView.currentCapacity, maxCapacity));
        roomGender.setText(CommonUtils.intToGender(this, roomToView.roomGender));
        roomType.setText(CommonUtils.intToRoomType(this, roomToView.roomType));

        updatePersons(roomID);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updatePersons(roomID);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void updatePersons(Integer roomID)
    {
        DBHandler.getAllPersonsInRoom(roomID, new DBHandler.OnGetPersonDataListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(Map<Integer, Person> data) {
                PersonsListAdapter personsListAdapter = new PersonsListAdapter(RoomManagementActivity.this, R.layout.persons_list_item, roomCapacity, maxCapacity);
                for(Map.Entry<Integer,Person> currPerson : data.entrySet())
                {
                    personsListAdapter.add(currPerson.getValue());
                }

                roomPersons.setAdapter(personsListAdapter);
            }

            @Override
            public void onFailed(DatabaseError databaseError) {

            }
        });
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.room_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.option_send_message_to_room) {
            sendBroadcastMessage();
        }

        return super.onOptionsItemSelected(item);
    }

    @AfterPermissionGranted(Constants.REQUEST_PERMISSIONS_SMS)
    private void sendBroadcastMessage() {
        if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.error_sms_permissions_required),
                    Constants.REQUEST_PERMISSIONS_SMS,
                    Manifest.permission.SEND_SMS);
        }

        final EditText txtMessage = new EditText(this);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(36, 0, 36, 0);
        txtMessage.setLayoutParams(lp);
        txtMessage.setGravity(android.view.Gravity.TOP| Gravity.START);
        txtMessage.setInputType(InputType.TYPE_CLASS_TEXT);
        txtMessage.setLines(1);
        txtMessage.setMaxLines(1);
        txtMessage.setTextDirection(View.TEXT_DIRECTION_RTL);
        txtMessage.addTextChangedListener(new TextValidator(txtMessage) {
            @Override public void validate(TextView textView, String text) {
                if(text.length() < 1) {
                    txtMessage.setError(getString(R.string.error_invalid_message_length));
                } else {
                    txtMessage.setError(null);
                }
            }
        });
        container.addView(txtMessage, lp);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.send_message_title))
                .setMessage(getString(R.string.send_message_to_room_message))
                .setView(container)
                .setPositiveButton(getString(R.string.send), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                DBHandler.getAllRoomLeaders(new DBHandler.OnGetPersonDataListener() {
                                    @Override
                                    public void onStart() {

                                    }

                                    @Override
                                    public void onSuccess(Map<Integer, Person> data) {
                                        if(checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                                            Toast.makeText(RoomManagementActivity.this, getString(R.string.error_no_send_sms_permissions), Toast.LENGTH_SHORT).show();
                                        } else {
                                            for (int i = 0; i < roomPersons.getAdapter().getCount(); i++) {
                                                Person currPerson = (Person)roomPersons.getAdapter().getItem(i);
                                                SmsManager sm = SmsManager.getDefault();
                                                String phoneNumber = currPerson.phoneNumber.startsWith(Constants.ISRAEL_LOCALE_PHONE_PREFIX) ? currPerson.phoneNumber : Constants.ISRAEL_LOCALE_PHONE_PREFIX + currPerson.phoneNumber;
                                                sm.sendTextMessage(phoneNumber, null, txtMessage.getText().toString(), null, null);
                                            }
                                            Toast.makeText(RoomManagementActivity.this, getString(R.string.success_sms_sent), Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailed(DatabaseError databaseError) {

                                    }
                                });

                            }
                        });
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
    }
}
