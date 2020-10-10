package com.amiel.tls;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.amiel.tls.db.DBHandler;
import com.amiel.tls.db.entities.Person;
import com.amiel.tls.db.entities.Room;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.amiel.tls.Constants.APP_PERMISSIONS_CODE;
import static com.amiel.tls.Constants.MID_LENGTH;
import static com.amiel.tls.Constants.PHONE_NUMBER_LENGTH;
import static com.amiel.tls.Constants.REQUEST_PICK_CONTACT;

public class MainActivity extends AppCompatActivity {

    FloatingActionMenu mainFAB;
    FloatingActionButton addRoomFAB, addPersonFAB;
    private final Map<Integer, Room> availableRooms = new HashMap<>();
    private TextInputEditText selectedPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);

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

        requestAppPermission();
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id){
            case R.id.option_send_message:
                sendBroadcastMessage();
                break;

            case R.id.option_send_form:
                sendForm();
                break;

            case R.id.option_display_all_requests:
                startActivity(new Intent(this, WaitingListActivity.class));
                break;

            case R.id.option_display_all_faults:
                startActivity(new Intent(this, FaultListActivity.class));
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openAddRoomDialog()
    {
        final NestedScrollView scrollViewLayout = (NestedScrollView) getLayoutInflater().inflate(R.layout.add_room_layout, null);

        final TextInputEditText roomName = scrollViewLayout.findViewById(R.id.add_room_edit_name);
        final RadioGroup typeRadioGroup = scrollViewLayout.findViewById(R.id.add_room_radio_group_type);
        final RadioGroup genderRadioGroup = scrollViewLayout.findViewById(R.id.add_room_radio_group_gender);
        final TextInputEditText maxCapacity = scrollViewLayout.findViewById(R.id.add_room_max_capacity);

        final TextInputLayout roomNameLayout = scrollViewLayout.findViewById(R.id.add_room_edit_name_layout);
        final TextInputLayout maxCapacityLayout = scrollViewLayout.findViewById(R.id.add_room_max_capacity_layout);

        roomName.setError(getString(R.string.error_invalid_room_name));
        maxCapacity.setError(getString(R.string.error_invalid_max_capacity));

        roomName.addTextChangedListener(new TextValidator(roomName) {
            @Override public void validate(TextView textView, String text) {
                if(text.length() <= 0) {
                    roomNameLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
                    roomName.setError(getString(R.string.error_invalid_room_name));
                } else {
                    roomName.setError(null);
                    roomNameLayout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
                    roomNameLayout.setEndIconDrawable(R.drawable.ic_check_circle_black_24dp);
                }
            }
        });

        maxCapacity.addTextChangedListener(new TextValidator(maxCapacity) {
            @Override public void validate(TextView textView, String text) {
                if (text.length() <= 0) {
                    maxCapacityLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
                    maxCapacity.setError(getString(R.string.error_invalid_max_capacity));
                } else {
                    maxCapacity.setError(null);
                    maxCapacityLayout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
                    maxCapacityLayout.setEndIconDrawable(R.drawable.ic_check_circle_black_24dp);
                }
            }
        });

        //Finally building an AlertDialog
        final AlertDialog builder = new AlertDialog.Builder(this)
                .setPositiveButton(getString(R.string.insert), null)
                .setNegativeButton(getString(R.string.cancel), null)
                .setView(scrollViewLayout)
                .setCancelable(false)
                .create();
        builder.show();

        //Setting up OnClickListener on positive button of AlertDialog
        builder.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(roomName.getError() == null && maxCapacity.getError() == null)
                {
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
                } else {
                    Toast.makeText(getApplicationContext(),getString(R.string.error_fill_missing_fields),Toast.LENGTH_SHORT).show();
                }
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

        final TextInputLayout fullNameLayout = scrollViewLayout.findViewById(R.id.add_person_edit_name_layout);
        final TextInputLayout homeTownLayout = scrollViewLayout.findViewById(R.id.add_person_home_town_layout);
        final TextInputLayout midLayout = scrollViewLayout.findViewById(R.id.add_person_mid_layout);
        final TextInputLayout branchLayout = scrollViewLayout.findViewById(R.id.add_person_branch_layout);
        final TextInputLayout phoneNumberLayout = scrollViewLayout.findViewById(R.id.add_person_phone_number_layout);
        final TextInputLayout releaseDateLayout = scrollViewLayout.findViewById(R.id.add_person_release_date_layout);

        fullName.setError(getString(R.string.error_invalid_full_name));
        homeTown.setError(getString(R.string.error_invalid_home_town));
        branch.setError(getString(R.string.error_invalid_branch));
        releaseDate.setError(getString(R.string.error_invalid_release_date));
        mid.setError(getString(R.string.error_invalid_mid));
        phoneNumber.setError(getString(R.string.error_invalid_phone_number));

        fullName.addTextChangedListener(new TextValidator(fullName) {
            @Override public void validate(TextView textView, String text) {
                if (text.length() <= 0) {
                    fullNameLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
                    fullName.setError(getString(R.string.error_invalid_full_name));
                } else {
                    fullName.setError(null);
                    fullNameLayout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
                    fullNameLayout.setEndIconDrawable(R.drawable.ic_check_circle_black_24dp);
                }
            }
        });

        homeTown.addTextChangedListener(new TextValidator(homeTown) {
            @Override public void validate(TextView textView, String text) {
                if (text.length() <= 0) {
                    homeTownLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
                    homeTown.setError(getString(R.string.error_invalid_home_town));
                } else {
                    homeTown.setError(null);
                    homeTownLayout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
                    homeTownLayout.setEndIconDrawable(R.drawable.ic_check_circle_black_24dp);
                }
            }
        });

        branch.addTextChangedListener(new TextValidator(branch) {
            @Override public void validate(TextView textView, String text) {
                if (text.length() <= 0) {
                    branchLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
                    branch.setError(getString(R.string.error_invalid_branch));
                } else {
                    branch.setError(null);
                    branchLayout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
                    branchLayout.setEndIconDrawable(R.drawable.ic_check_circle_black_24dp);
                }
            }
        });

        releaseDate.addTextChangedListener(new TextValidator(releaseDate) {
            @Override public void validate(TextView textView, String text) {
                if (text.length() <= 0) {
                    releaseDateLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
                    releaseDate.setError(getString(R.string.error_invalid_release_date));
                } else {
                    releaseDate.setError(null);
                    releaseDateLayout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
                    releaseDateLayout.setEndIconDrawable(R.drawable.ic_check_circle_black_24dp);
                }
            }
        });

        mid.addTextChangedListener(new TextValidator(mid) {
            @Override public void validate(TextView textView, String text) {
                if (text.length() != MID_LENGTH) {
                    midLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
                    mid.setError(getString(R.string.error_invalid_mid));
                } else {
                    mid.setError(null);
                    midLayout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
                    midLayout.setEndIconDrawable(R.drawable.ic_check_circle_black_24dp);
                }
            }
        });

        phoneNumber.addTextChangedListener(new TextValidator(phoneNumber) {
            @Override public void validate(TextView textView, String text) {
                if (text.length() != PHONE_NUMBER_LENGTH) {
                    phoneNumberLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
                    phoneNumber.setError(getString(R.string.error_invalid_phone_number));
                } else {
                    phoneNumber.setError(null);
                    phoneNumberLayout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
                    phoneNumberLayout.setEndIconDrawable(R.drawable.ic_check_circle_black_24dp);
                }
            }
        });

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
        final AlertDialog builder = new AlertDialog.Builder(this)
                .setPositiveButton(getString(R.string.insert), null)
                .setNegativeButton(getString(R.string.cancel), null)
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
                if(phoneNumber.getError() == null && mid.getError() == null
                && homeTown.getError() == null && branch.getError() == null
                && fullName.getError() == null && releaseDate.getError() == null) {
                /*
                   Insert and get data using Database Async way
                 */
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            Person newPerson = new Person();
                            newPerson.fullName = Objects.requireNonNull(fullName.getText()).toString();
                            newPerson.MID = Objects.requireNonNull(mid.getText()).toString();
                            newPerson.homeTown = Objects.requireNonNull(homeTown.getText()).toString();
                            newPerson.phoneNumber = Objects.requireNonNull(phoneNumber.getText()).toString();
                            newPerson.branch = Objects.requireNonNull(branch.getText()).toString();
                            newPerson.releaseDate = Objects.requireNonNull(releaseDate.getText()).toString();
                            newPerson.roomLeader = false;

                            newPerson.armyPeriod = armyPeriodRadioGroup.indexOfChild(armyPeriodRadioGroup.findViewById(armyPeriodRadioGroup.getCheckedRadioButtonId()));

                            for (Map.Entry<Integer, Room> currRoom : availableRooms.entrySet()) {
                                if (currRoom.getValue().roomName.equals(((Room) roomsSpinner.getSelectedItem()).roomName)
                                        && currRoom.getValue().roomType.equals(((Room) roomsSpinner.getSelectedItem()).roomType)
                                        && currRoom.getValue().roomGender.equals(((Room) roomsSpinner.getSelectedItem()).roomGender)) {
                                    newPerson.roomID = currRoom.getKey();
                                }
                            }

                            DBHandler.addPerson(newPerson);
                        }
                    });
                    builder.dismiss();
                } else {
                    Toast.makeText(getApplicationContext(),getString(R.string.error_fill_missing_fields),Toast.LENGTH_SHORT).show();
                }
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
                String date = changedDay + Constants.backSlash + (changedMonth+1) + Constants.backSlash + changedYear;
                picked.setText(date);
            }
        }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        //shows DatePickerDialog
        datePickerDialog.show();
    }

    public void pickContact(View v)
    {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(contactPickerIntent, REQUEST_PICK_CONTACT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check whether the result is ok
        if (resultCode == RESULT_OK) {
            // Check for the request code, we might be using multiple startActivityForResult
            switch (requestCode) {
                case REQUEST_PICK_CONTACT:
                    contactPicked(data);
                    break;
            }
        } else {
            Log.e("ContactView", "Failed to pick contact");
        }
    }

    /**
     * Query the Uri and read contact details. Handle the picked contact data.
     * @param data
     */
    @SuppressLint("Recycle")
    private void contactPicked(Intent data) {
        Cursor cursor = null;
        try {
            String phoneNo = null ;
            // getData() method will have the Content Uri of the selected contact
            Uri uri = data.getData();
            //Query the content uri
            cursor = getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            // column index of the phone number
            int  phoneIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            // column index of the contact name
            phoneNo = cursor.getString(phoneIndex);
            // Set the value to the text view
            selectedPhoneNumber.setText(phoneNo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestAppPermission() {
        List<String> missingPermissions = new ArrayList<>();
        if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
            missingPermissions.add(Manifest.permission.SEND_SMS);
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
            missingPermissions.add(Manifest.permission.CALL_PHONE);
        if (checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
            missingPermissions.add(Manifest.permission.INTERNET);
        if (checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED)
            missingPermissions.add(Manifest.permission.ACCESS_NETWORK_STATE);
        if (checkSelfPermission(Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED)
            missingPermissions.add(Manifest.permission.GET_ACCOUNTS);

        if(!missingPermissions.isEmpty()) {
            requestPermissions(new String[]
                    {
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.CALL_PHONE,
                            Manifest.permission.INTERNET,
                            Manifest.permission.ACCESS_NETWORK_STATE,
                            Manifest.permission.GET_ACCOUNTS,
                    }, APP_PERMISSIONS_CODE);
        }
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

        // Set the default text
        txtMessage.setHint(getString(R.string.send_message_hint));

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.send_message_title))
                .setMessage(getString(R.string.send_message_message))
                .setView(txtMessage)
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
                                            Toast.makeText(MainActivity.this, getString(R.string.error_no_send_sms_permissions), Toast.LENGTH_SHORT).show();
                                        } else {
                                            for (Map.Entry<Integer, Person> currPerson : data.entrySet()) {
                                                SmsManager sm = SmsManager.getDefault();
                                                sm.sendTextMessage(Constants.ISRAEL_LOCALE_PHONE_PREFIX + currPerson.getValue().phoneNumber.substring(1), null, txtMessage.getText().toString(), null, null);
                                            }
                                            Toast.makeText(MainActivity.this, getString(R.string.success_sms_sent), Toast.LENGTH_SHORT).show();
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

    private void sendForm() {
        // Set the default text
        View dialogView = getLayoutInflater().inflate(R.layout.send_form_dialog, null);
        final AppCompatSpinner formTypeSpinner = (AppCompatSpinner) dialogView.findViewById(R.id.send_form_type_spinner);
        selectedPhoneNumber = (TextInputEditText) dialogView.findViewById(R.id.send_form_phone_number_edittext);
        final MaterialButton chooseContact = (MaterialButton) dialogView.findViewById(R.id.send_form_pick_contact_button);
        chooseContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickContact(v);
            }
        });

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.send_form_title))
                .setMessage(getString(R.string.send_form_message))
                .setView(dialogView)
                .setPositiveButton(getString(R.string.send), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String formToAdd = Constants.REQUEST_FORM;
                                    if(formTypeSpinner.getSelectedItemPosition() == 1) {
                                        formToAdd = Constants.FAULT_FORM;
                                    }

                                    String phoneNumberWithoutPrefix = Objects.requireNonNull(selectedPhoneNumber.getText()).toString().substring(1);
                                    String url = Constants.SEND_API_PREFIX + Constants.SEND_API_PHONE_PARAM + Constants.ISRAEL_LOCALE_PHONE_PREFIX +  phoneNumberWithoutPrefix + Constants.SEND_API_MESSAGE_PARAM + formToAdd;
                                    Intent waIntent = new Intent(Intent.ACTION_VIEW);
                                    waIntent.setPackage(Constants.WHATSAPP_PACKAGE);
                                    waIntent.setData(Uri.parse(url));

                                    if (waIntent.resolveActivity(getPackageManager()) != null) {
                                        startActivity(waIntent);
                                    }
                                    else {
                                        throw new PackageManager.NameNotFoundException();
                                    }

                                } catch (PackageManager.NameNotFoundException e) {
                                    Toast.makeText(getBaseContext(), getString(R.string.error_whatsapp_not_installed), Toast.LENGTH_SHORT)
                                            .show();
                                }

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
