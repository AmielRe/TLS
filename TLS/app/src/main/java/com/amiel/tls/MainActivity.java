package com.amiel.tls;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RadioGroup;

import com.amiel.tls.db.DBHandler;
import com.amiel.tls.db.entities.Person;
import com.amiel.tls.db.entities.Room;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    FloatingActionMenu mainFAB;
    FloatingActionButton addRoomFAB, addPersonFAB;
    private final Map<Integer, Room> availableRooms = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        FirebaseDatabase.getInstance().getReference().keepSynced(true);

        mainFAB = (FloatingActionMenu) findViewById(R.id.material_design_android_floating_action_menu);
        addRoomFAB = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_room);
        addPersonFAB = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_person);

        addPersonFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddPersonDialog();
            }
        });

        addRoomFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddRoomDialog();
            }
        });
    }

    private void openAddRoomDialog()
    {
        final NestedScrollView scrollViewLayout = (NestedScrollView) getLayoutInflater().inflate(R.layout.add_room_layout, null);

        final TextInputEditText roomName = scrollViewLayout.findViewById(R.id.add_room_edit_name);
        final RadioGroup typeRadioGroup = scrollViewLayout.findViewById(R.id.add_room_radio_group_type);
        final RadioGroup genderRadioGroup = scrollViewLayout.findViewById(R.id.add_room_radio_group_gender);
        final TextInputEditText maxCapacity = scrollViewLayout.findViewById(R.id.add_room_max_capacity);

        //Finally building an AlertDialog
        final AlertDialog builder = new AlertDialog.Builder(Objects.requireNonNull(this))
                .setPositiveButton(getResources().getString(R.string.add_dialog_accept), null)
                .setNegativeButton(getResources().getString(R.string.add_dialog_cancel), null)
                .setView(scrollViewLayout)
                .setCancelable(false)
                .create();
        builder.show();

        //Setting up OnClickListener on positive button of AlertDialog
        builder.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                   Insert and get data using Database Async way
                 */
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        Room newRoom = new Room();
                        newRoom.roomName = Objects.requireNonNull(roomName.getText()).toString();
                        newRoom.currentCapacity = 0;
                        newRoom.maxCapacity = Integer.parseInt(Objects.requireNonNull(maxCapacity.getText()).toString());

                        newRoom.roomType = typeRadioGroup.indexOfChild(typeRadioGroup.findViewById(typeRadioGroup.getCheckedRadioButtonId()));
                        newRoom.roomGender = genderRadioGroup.indexOfChild(genderRadioGroup.findViewById(genderRadioGroup.getCheckedRadioButtonId()));

                        // Insert Data
                        DBHandler.addRoom(newRoom);

                        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
                        Objects.requireNonNull(fragment).onResume();
                    }
                });
                builder.dismiss();
            }
        });
    }

    private void openAddPersonDialog()
    {
        final NestedScrollView scrollViewLayout = (NestedScrollView) getLayoutInflater().inflate(R.layout.add_person_layout, null);

        final TextInputEditText fullName = scrollViewLayout.findViewById(R.id.add_person_edit_name);
        final RadioGroup armyPeriodRadioGroup = scrollViewLayout.findViewById(R.id.add_person_radio_group_army_period);
        final TextInputEditText homeTown = scrollViewLayout.findViewById(R.id.add_person_home_town);
        final TextInputEditText mid = scrollViewLayout.findViewById(R.id.add_person_mid);
        final TextInputEditText branch = scrollViewLayout.findViewById(R.id.add_person_branch);
        final TextInputEditText phoneNumber = scrollViewLayout.findViewById(R.id.add_person_phone_number);
        final TextInputEditText releaseDate = scrollViewLayout.findViewById(R.id.add_person_release_date);
        final AppCompatSpinner roomsSpinner = scrollViewLayout.findViewById(R.id.add_person_room_spinner);

        DBHandler.getAllAvailableRooms(new DBHandler.OnGetRoomDataListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(Map<Integer, Room> data) {
                Room[] rooms = new Room[data.size()];
                availableRooms.putAll(data);
                data.values().toArray(rooms);

                CustomRoomSpinnerAdapter customAdapter = new CustomRoomSpinnerAdapter(getApplicationContext(),
                        rooms);

                roomsSpinner.setAdapter(customAdapter);
            }

            @Override
            public void onFailed(DatabaseError databaseError) {

            }
        });

        //Finally building an AlertDialog
        final AlertDialog builder = new AlertDialog.Builder(Objects.requireNonNull(this))
                .setPositiveButton(getResources().getString(R.string.add_dialog_accept), null)
                .setNegativeButton(getResources().getString(R.string.add_dialog_cancel), null)
                .setView(scrollViewLayout)
                .setCancelable(false)
                .create();
        builder.show();

        releaseDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    showDateDialog(v);
                }
            }
        });

        releaseDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateDialog(v);
            }
        });

        //Setting up OnClickListener on positive button of AlertDialog
        builder.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                   Insert and get data using Database Async way
                 */
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        Person newPerson = new Person();
                        newPerson.fullName = fullName.getText().toString();
                        newPerson.MID = mid.getText().toString();
                        newPerson.homeTown = homeTown.getText().toString();
                        newPerson.phoneNumber = phoneNumber.getText().toString();
                        newPerson.branch = branch.getText().toString();
                        newPerson.releaseDate = releaseDate.getText().toString();
                        newPerson.roomLeader = false;

                        newPerson.armyPeriod = armyPeriodRadioGroup.indexOfChild(armyPeriodRadioGroup.findViewById(armyPeriodRadioGroup.getCheckedRadioButtonId()));

                        for(Map.Entry<Integer, Room> currRoom : availableRooms.entrySet()){
                            if(currRoom.getValue().roomName.equals(((Room) roomsSpinner.getSelectedItem()).roomName)) {
                                newPerson.roomID = currRoom.getKey();
                            }
                        }

                        DBHandler.addPerson(newPerson);
                    }
                });
                builder.dismiss();
            }
        });
    }

    private void showDateDialog(View view) {
        final TextInputEditText picked = (TextInputEditText) view;   // Store the dialog to be picked

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener()
        {
            @SuppressLint("DefaultLocale")
            @Override
            public void onDateSet(DatePicker view, int changedYear, int changedMonth, int changedDay)
            {
                //sets date in EditText
                // If done picking date
                String date = changedDay + "/" + (changedMonth+1) + "/" + changedYear;
                picked.setText(date);
            }
        }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        //shows DatePickerDialog
        datePickerDialog.show();
    }

}
