package com.amiel.tls;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.widget.NestedScrollView;

import com.amiel.tls.db.DBHandler;
import com.amiel.tls.db.entities.Person;
import com.amiel.tls.db.entities.Room;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseError;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.amiel.tls.Constants.MID_LENGTH;
import static com.amiel.tls.Constants.PHONE_NUMBER_LENGTH;

public class PersonsListAdapter extends ArrayAdapter<Person> {
    private List<Person> personsList = new ArrayList<>();
    private Context context;
    private TextView capacity;
    private Integer maxCapacity;

    static class CardViewHolder {
        TextView personName;
        TextView personMID;
        TextView personPhone;
        TextView personArmyPeriod;
        ImageView personLeader;
        TextView personAddress;
        TextView personReleaseDate;
        TextView personBranch;
        MaterialButton removePerson;
    }

    PersonsListAdapter(Context context, int textViewResourceId, TextView capacity, Integer maxCapacity) {
        super(context, textViewResourceId);
        this.context = context;
        this.capacity = capacity;
        this.maxCapacity = maxCapacity;
    }

    @Override
    public void add(Person object) {
        personsList.add(object);
        super.add(object);
    }

    @Override
    public int getCount() {
        return this.personsList.size();
    }

    @Override
    public Person getItem(int index) {
        return this.personsList.get(index);
    }

    @Override
    @NonNull
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View row = convertView;
        final PersonsListAdapter.CardViewHolder viewHolder;
        final Person person = getItem(position);

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = Objects.requireNonNull(inflater).inflate(R.layout.persons_list_item, parent, false);
            viewHolder = new PersonsListAdapter.CardViewHolder();
            viewHolder.personName = (TextView) row.findViewById(R.id.persons_list_item_person_name);
            viewHolder.personAddress = (TextView) row.findViewById(R.id.persons_list_item_person_address);
            viewHolder.personMID = (TextView) row.findViewById(R.id.persons_list_item_person_mid);
            viewHolder.personArmyPeriod = (TextView) row.findViewById(R.id.persons_list_item_person_army_period);
            viewHolder.personLeader = (ImageView) row.findViewById(R.id.persons_list_item_person_room_leader);
            viewHolder.personBranch = (TextView) row.findViewById(R.id.persons_list_item_person_branch);
            viewHolder.personPhone = (TextView) row.findViewById(R.id.persons_list_item_person_phone);
            viewHolder.personReleaseDate = (TextView) row.findViewById(R.id.persons_list_item_person_release_date);
            viewHolder.removePerson = (MaterialButton) row.findViewById(R.id.persons_list_item_person_remove);

            if(!PreferencesManager.getInstance().getIsAdminValue()) {
                viewHolder.removePerson.setVisibility(View.GONE);
                viewHolder.removePerson.setEnabled(false);
                row.setEnabled(false);
            }

            viewHolder.removePerson.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                    alertDialog.setTitle(context.getString(R.string.warning_title));
                    alertDialog.setMessage(context.getString(R.string.warning_message));
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Person toRemove = new Person();
                                    for(Person currPerson : personsList) {
                                        if(currPerson.MID.equalsIgnoreCase(viewHolder.personMID.getText().toString())) {
                                            toRemove = currPerson;
                                            break;
                                        }
                                    }
                                    DBHandler.removePerson(toRemove);
                                    personsList.remove(toRemove);
                                    notifyDataSetChanged();
                                    capacity.setText(String.format(Locale.getDefault(), "%d/%d", personsList.size(), maxCapacity));
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.no),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            });

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openEditPersonDialog(viewHolder.personMID.getText().toString(), viewHolder.personName.getText().toString());
                }
            });

            row.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //Creating the instance of PopupMenu
                    PopupMenu popup = new PopupMenu(context, v);
                    //Inflating the Popup using xml file
                    popup.getMenuInflater().inflate(R.menu.person_actions_menu, popup.getMenu());

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.make_leader_action:
                                    for (Person currPerson : personsList) {
                                        currPerson.roomLeader = currPerson.MID.equalsIgnoreCase(viewHolder.personMID.getText().toString());
                                    }

                                    for (int i = parent.getChildCount() - 1; i >= 0; i--) {
                                        parent.getChildAt(i).findViewById(R.id.persons_list_item_person_room_leader).setVisibility(View.GONE);
                                    }

                                    viewHolder.personLeader.setVisibility(View.VISIBLE);
                                    DBHandler.setNewRoomLeader(person.roomID, viewHolder.personMID.getText().toString());
                                    break;
                                case R.id.make_phone_call_action:
                                    if (context.checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                        Toast.makeText(context, context.getString(R.string.error_no_call_permissions), Toast.LENGTH_LONG).show();
                                        return true;
                                    }
                                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                                    callIntent.setData(Uri.parse(Constants.CALL_PHONE_NUMBER_PREFIX + viewHolder.personPhone.getText().toString()));
                                    callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(callIntent);
                                    break;
                                case R.id.send_message_action:
                                    PackageManager pm=context.getPackageManager();
                                    try {
                                        String phoneNumber = viewHolder.personPhone.getText().toString();
                                        String url = Constants.SEND_API_PREFIX + Constants.SEND_API_PHONE_PARAM;
                                        if(!phoneNumber.startsWith(Constants.ISRAEL_LOCALE_PHONE_PREFIX)) {
                                            url += Constants.ISRAEL_LOCALE_PHONE_PREFIX;
                                        }
                                        url = url + phoneNumber;
                                        Intent waIntent = new Intent(Intent.ACTION_VIEW);
                                        waIntent.setPackage(Constants.WHATSAPP_PACKAGE);
                                        waIntent.setData(Uri.parse(url));

                                        if (waIntent.resolveActivity(pm) != null) {
                                            context.startActivity(waIntent);
                                        }
                                        else {
                                            throw new PackageManager.NameNotFoundException();
                                        }

                                    } catch (PackageManager.NameNotFoundException e) {
                                        Toast.makeText(context, context.getString(R.string.error_whatsapp_not_installed), Toast.LENGTH_SHORT)
                                                .show();
                                    }
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
            viewHolder = (PersonsListAdapter.CardViewHolder)row.getTag();
        }

        viewHolder.personName.setText(Objects.requireNonNull(person).fullName);
        viewHolder.personReleaseDate.setText(person.releaseDate);
        viewHolder.personPhone.setText(person.phoneNumber);
        viewHolder.personBranch.setText(person.branch);
        viewHolder.personMID.setText(person.MID);
        viewHolder.personAddress.setText(person.homeTown);
        viewHolder.personArmyPeriod.setText(CommonUtils.intToArmyPeriod(context, person.armyPeriod));

        if(person.roomLeader) {
            viewHolder.personLeader.setVisibility(View.VISIBLE);
        } else {
            viewHolder.personLeader.setVisibility(View.GONE);
        }

        return row;
    }

    private void openEditPersonDialog(String personMID, String personName)
    {
        final NestedScrollView scrollViewLayout = (NestedScrollView) ((RoomManagementActivity)context).getLayoutInflater().inflate(R.layout.add_person_layout, null);

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

        fullName.setError(context.getString(R.string.error_invalid_full_name));
        homeTown.setError(context.getString(R.string.error_invalid_home_town));
        branch.setError(context.getString(R.string.error_invalid_branch));
        releaseDate.setError(context.getString(R.string.error_invalid_release_date));
        mid.setError(context.getString(R.string.error_invalid_mid));
        phoneNumber.setError(context.getString(R.string.error_invalid_phone_number));

        fullName.addTextChangedListener(new TextValidator(fullName) {
            @Override public void validate(TextView textView, String text) {
                if (text.length() <= 0) {
                    fullNameLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
                    fullName.setError(context.getString(R.string.error_invalid_full_name));
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
                    homeTown.setError(context.getString(R.string.error_invalid_home_town));
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
                    branch.setError(context.getString(R.string.error_invalid_branch));
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
                    releaseDate.setError(context.getString(R.string.error_invalid_release_date));
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
                    mid.setError(context.getString(R.string.error_invalid_mid));
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
                    phoneNumber.setError(context.getString(R.string.error_invalid_phone_number));
                } else {
                    phoneNumber.setError(null);
                    phoneNumberLayout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
                    phoneNumberLayout.setEndIconDrawable(R.drawable.ic_check_circle_black_24dp);
                }
            }
        });

        roomsSpinner.setVisibility(View.GONE);
        mid.setEnabled(false);

        //Finally building an AlertDialog
        final androidx.appcompat.app.AlertDialog builder = new androidx.appcompat.app.AlertDialog.Builder(context)
                .setPositiveButton(context.getString(R.string.update), null)
                .setNegativeButton(context.getString(R.string.cancel), null)
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

        DBHandler.getPerson(personMID, personName, new DBHandler.OnGetPersonDataListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(Map<Integer, Person> data) {
                if(data.size() > 0)
                {
                    final Map.Entry<Integer,Person> entry = data.entrySet().iterator().next();
                    fullName.setText(entry.getValue().fullName);
                    homeTown.setText(entry.getValue().homeTown);
                    branch.setText(entry.getValue().branch);
                    releaseDate.setText(entry.getValue().releaseDate);
                    mid.setText(entry.getValue().MID);
                    phoneNumber.setText(entry.getValue().phoneNumber);
                    ((RadioButton)armyPeriodRadioGroup.getChildAt(entry.getValue().armyPeriod)).setChecked(true);

                    //Setting up OnClickListener on positive button of AlertDialog
                    builder.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(phoneNumber.getError() == null && mid.getError() == null
                                    && homeTown.getError() == null && branch.getError() == null
                                    && fullName.getError() == null && releaseDate.getError() == null) {
                /*
                   Update and get data using Database Async way
                 */
                                AsyncTask.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        Person updatedPerson = new Person();
                                        updatedPerson.fullName = Objects.requireNonNull(fullName.getText()).toString();
                                        updatedPerson.MID = Objects.requireNonNull(mid.getText()).toString();
                                        updatedPerson.homeTown = Objects.requireNonNull(homeTown.getText()).toString();
                                        updatedPerson.phoneNumber = Objects.requireNonNull(phoneNumber.getText()).toString();
                                        updatedPerson.branch = Objects.requireNonNull(branch.getText()).toString();
                                        updatedPerson.releaseDate = Objects.requireNonNull(releaseDate.getText()).toString();
                                        updatedPerson.roomLeader = entry.getValue().roomLeader;
                                        updatedPerson.roomID = entry.getValue().roomID;
                                        updatedPerson.armyPeriod = armyPeriodRadioGroup.indexOfChild(armyPeriodRadioGroup.findViewById(armyPeriodRadioGroup.getCheckedRadioButtonId()));

                                        for(Person currPerson : personsList) {
                                            if(currPerson.MID.equals(entry.getValue().MID)) {
                                                personsList.set(personsList.indexOf(currPerson), updatedPerson);
                                                ((RoomManagementActivity)context).runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        notifyDataSetChanged();
                                                    }
                                                });
                                            }
                                        }

                                        DBHandler.updatePerson(updatedPerson);
                                    }
                                });
                                builder.dismiss();
                            } else {
                                Toast.makeText(context, context.getString(R.string.error_fill_missing_fields),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailed(DatabaseError databaseError) {

            }
        });
    }

    private void showDateDialog(View view) {
        final TextInputEditText picked = (TextInputEditText) view;   // Store the dialog to be picked

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener()
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
}
