package com.amiel.tls;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;

import com.amiel.tls.db.DBHandler;
import com.amiel.tls.db.entities.Person;
import com.amiel.tls.db.entities.Room;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class WaitingListAdapter extends ArrayAdapter<WaitingListPerson> {
    private List<WaitingListPerson> waitingList = new ArrayList<>();
    private Context context;
    private GoogleAccountCredential mCredential;
    private TextView totalWaiting;
    private final Map<Integer, Room> availableRooms = new HashMap<>();

    static class CardViewHolder {
        TextView personName;
        TextView personMID;
        TextView personPhone;
        TextView personArmyPeriod;
        TextView personAddress;
        TextView personReleaseDate;
        TextView personBranch;
        TextView personGender;
    }

    WaitingListAdapter(Context context, int textViewResourceId, GoogleAccountCredential credential, TextView totalWaiting) {
        super(context, textViewResourceId);
        this.context = context;
        this.mCredential = credential;
        this.totalWaiting = totalWaiting;
    }

    @Override
    public void add(WaitingListPerson object) {
        waitingList.add(object);
        super.add(object);
    }

    @Override
    public int getCount() {
        return this.waitingList.size();
    }

    @Override
    public WaitingListPerson getItem(int index) {
        return this.waitingList.get(index);
    }

    @Override
    @NonNull
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View row = convertView;
        final WaitingListAdapter.CardViewHolder viewHolder;
        final WaitingListPerson person = getItem(position);

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = Objects.requireNonNull(inflater).inflate(R.layout.waiting_list_item, parent, false);
            viewHolder = new WaitingListAdapter.CardViewHolder();
            viewHolder.personName = (TextView) row.findViewById(R.id.waiting_list_item_person_name);
            viewHolder.personAddress = (TextView) row.findViewById(R.id.waiting_list_item_person_address);
            viewHolder.personMID = (TextView) row.findViewById(R.id.waiting_list_item_person_mid);
            viewHolder.personArmyPeriod = (TextView) row.findViewById(R.id.waiting_list_item_person_army_period);
            viewHolder.personGender = (TextView) row.findViewById(R.id.waiting_list_item_person_gender);
            viewHolder.personBranch = (TextView) row.findViewById(R.id.waiting_list_item_person_branch);
            viewHolder.personPhone = (TextView) row.findViewById(R.id.waiting_list_item_person_phone);
            viewHolder.personReleaseDate = (TextView) row.findViewById(R.id.waiting_list_item_person_release_date);

            row.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //Creating the instance of PopupMenu
                    PopupMenu popup = new PopupMenu(context, v);
                    //Inflating the Popup using xml file
                    popup.getMenuInflater().inflate(R.menu.wait_list_person_actions_menu, popup.getMenu());

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {

                            switch (item.getItemId()) {
                                case R.id.insert_to_room_action:
                                    final AppCompatSpinner roomsSpinner = new AppCompatSpinner(context);
                                    DBHandler.getAllAvailableRooms(new DBHandler.OnGetRoomDataListener() {
                                        @Override
                                        public void onStart() {

                                        }

                                        @Override
                                        public void onSuccess(Map<Integer, Room> data) {
                                            Room[] rooms = new Room[data.size()];
                                            data.values().toArray(rooms);
                                            availableRooms.putAll(data);

                                            CustomRoomSpinnerAdapter customAdapter = new CustomRoomSpinnerAdapter(context, rooms);

                                            roomsSpinner.setAdapter(customAdapter);

                                            new AlertDialog.Builder(context)
                                                    .setTitle(context.getString(R.string.insert_to_room_title))
                                                    .setMessage(context.getString(R.string.insert_to_room_message))
                                                    .setView(roomsSpinner)
                                                    .setPositiveButton(context.getString(R.string.accept), new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                            AsyncTask.execute(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    Person newPerson = new Person();
                                                                    newPerson.fullName = person.fullName;
                                                                    newPerson.MID = person.MID;
                                                                    newPerson.homeTown = person.homeTown;
                                                                    newPerson.phoneNumber = person.phoneNumber;
                                                                    newPerson.branch = person.branch;
                                                                    newPerson.releaseDate = person.releaseDate;
                                                                    newPerson.roomLeader = false;
                                                                    newPerson.armyPeriod = person.armyPeriod.equals(context.getString(R.string.must)) ? Constants.ARMY_PERIOD_MUST_INT : Constants.ARMY_PERIOD_SIGN_INT;

                                                                    for (Map.Entry<Integer, Room> currRoom : availableRooms.entrySet()) {
                                                                        if (currRoom.getValue().roomName.equals(((Room) roomsSpinner.getSelectedItem()).roomName)
                                                                                && currRoom.getValue().roomType.equals(((Room) roomsSpinner.getSelectedItem()).roomType)
                                                                                && currRoom.getValue().roomGender.equals(((Room) roomsSpinner.getSelectedItem()).roomGender)) {
                                                                            newPerson.roomID = currRoom.getKey();
                                                                        }
                                                                    }

                                                                    DBHandler.addPerson(newPerson);

                                                                    WaitingListPerson toRemove = new WaitingListPerson();
                                                                    for (WaitingListPerson currPerson : waitingList) {
                                                                        if (currPerson.MID.equalsIgnoreCase(viewHolder.personMID.getText().toString())) {
                                                                            toRemove = currPerson;
                                                                            break;
                                                                        }
                                                                    }

                                                                    String range = Constants.REQUESTS_SHEET_NAME + "!A" + toRemove.rowID + ":I" + toRemove.rowID;
                                                                    AsyncClear requestsInfoClear = new AsyncClear(context, mCredential);
                                                                    requestsInfoClear.execute(Constants.REQUESTS_SPREAD_SHEET_ID, range);

                                                                    waitingList.remove(toRemove);

                                                                    ((Activity)context).runOnUiThread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            totalWaiting.setText(String.format(Locale.getDefault(), "%d", waitingList.size()));
                                                                            notifyDataSetChanged();
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                        }
                                                    })
                                                    .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                        }
                                                    })
                                                    .show();
                                        }

                                        @Override
                                        public void onFailed(DatabaseError databaseError) {

                                        }
                                    });
                                    break;
                                case R.id.delete_action:
                                    WaitingListPerson toRemove = new WaitingListPerson();
                                    for (WaitingListPerson currPerson : waitingList) {
                                        if (currPerson.MID.equalsIgnoreCase(viewHolder.personMID.getText().toString())) {
                                            toRemove = currPerson;
                                            break;
                                        }
                                    }

                                    String range = Constants.REQUESTS_SHEET_NAME + "!A" + toRemove.rowID + ":I" + toRemove.rowID;
                                    AsyncClear requestsInfoClear = new AsyncClear(context, mCredential);
                                    requestsInfoClear.execute(Constants.REQUESTS_SPREAD_SHEET_ID, range);

                                    waitingList.remove(toRemove);

                                    ((Activity)context).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            totalWaiting.setText(String.format(Locale.getDefault(), "%d", waitingList.size()));
                                            notifyDataSetChanged();
                                        }
                                    });
                                    break;
                            }
                            return true;
                        }
                    });
                    popup.show();//showing popup menu

                    return true;
                }
            });
            row.setTag(viewHolder);
        } else {
            viewHolder = (WaitingListAdapter.CardViewHolder) row.getTag();
        }

        viewHolder.personName.setText(Objects.requireNonNull(person).fullName);
        viewHolder.personReleaseDate.setText(person.releaseDate);
        viewHolder.personPhone.setText(person.phoneNumber);
        viewHolder.personBranch.setText(person.branch);
        viewHolder.personMID.setText(person.MID);
        viewHolder.personAddress.setText(person.homeTown);
        viewHolder.personArmyPeriod.setText(person.armyPeriod);
        viewHolder.personGender.setText(person.gender);

        return row;
    }
}