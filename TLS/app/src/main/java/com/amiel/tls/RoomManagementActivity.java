package com.amiel.tls;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.amiel.tls.db.DBHandler;
import com.amiel.tls.db.entities.Person;
import com.amiel.tls.db.entities.Room;
import com.google.firebase.database.DatabaseError;

import java.util.Map;

public class RoomManagementActivity extends AppCompatActivity {

    private ListView roomPersons;
    private SwipeRefreshLayout swipeRefreshLayout;
    private static Integer roomID;
    //private static

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_management);

        roomPersons = (ListView) findViewById(R.id.room_management_persons_listView);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_room_management_swipe_refresh_layout);

        TextView roomName = (TextView) findViewById(R.id.room_management_column1_room_name);
        TextView roomCapacity = (TextView) findViewById(R.id.room_management_column1_capacity);
        TextView roomType = (TextView) findViewById(R.id.room_management_column2_type);
        TextView roomGender = (TextView) findViewById(R.id.room_management_column2_gender);

        Room roomToView = (Room)getIntent().getExtras().getSerializable("room");
        roomID = getIntent().getExtras().getInt("roomID");

        roomName.setText(roomToView.roomName);
        roomCapacity.setText(String.format("%d/%d", roomToView.currentCapacity, roomToView.maxCapacity));
        roomGender.setText(CommonUtils.intToGender(roomToView.roomGender));
        roomType.setText(CommonUtils.intToRoomType(roomToView.roomType));

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
                PersonsListAdapter personsListAdapter = new PersonsListAdapter(RoomManagementActivity.this, R.layout.persons_list_item);
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
}
