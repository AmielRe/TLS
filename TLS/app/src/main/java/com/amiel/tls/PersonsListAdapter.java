package com.amiel.tls;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.amiel.tls.db.DBHandler;
import com.amiel.tls.db.entities.Person;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PersonsListAdapter extends ArrayAdapter<Person> {
    private List<Person> personsList = new ArrayList<>();
    private Context context;

    static class CardViewHolder {
        TextView personName;
        TextView personMID;
        TextView personPhone;
        TextView personArmyPeriod;
        ImageView personLeader;
        TextView personAddress;
        TextView personReleaseDate;
        TextView personBranch;
        Button removePerson;
    }

    PersonsListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = context;
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
            viewHolder.removePerson = (Button) row.findViewById(R.id.persons_list_item_person_remove);

            viewHolder.removePerson.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                                        Toast.makeText(context, "לאפליקציה אין הרשאות למצלמה", Toast.LENGTH_LONG).show();
                                        return true;
                                    }
                                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                                    callIntent.setData(Uri.parse(String.format("tel:%s",viewHolder.personPhone.getText().toString())));
                                    callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(callIntent);
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
        viewHolder.personArmyPeriod.setText(CommonUtils.intToArmyPeriod(person.armyPeriod));

        if(person.roomLeader) {
            viewHolder.personLeader.setVisibility(View.VISIBLE);
        } else {
            viewHolder.personLeader.setVisibility(View.GONE);
        }

        return row;
    }
}
