package com.amiel.tls;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.amiel.tls.db.DBHandler;
import com.amiel.tls.db.entities.Person;
import com.amiel.tls.db.entities.Room;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;


import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.amiel.tls.Constants.REQUEST_PICK_CONTACT_ADAPTER;

public class FaultListAdapter extends ArrayAdapter<FaultListItem> {

    private List<FaultListItem> faultsList = new ArrayList<>();
    private Context context;
    private GoogleAccountCredential mCredential;
    private TextView totalFaults;

    private FaultListAdapter.CardViewHolder lastSelectedItem;

    static class CardViewHolder {
        TextView roomName;
        TextView description;
    }

    FaultListAdapter(Context context, int textViewResourceId, GoogleAccountCredential credential, TextView totalFaults) {
        super(context, textViewResourceId);
        this.context = context;
        this.mCredential = credential;
        this.totalFaults = totalFaults;
    }

    @Override
    public void add(FaultListItem object) {
        faultsList.add(object);
        super.add(object);
    }

    @Override
    public int getCount() {
        return this.faultsList.size();
    }

    @Override
    public FaultListItem getItem(int index) {
        return this.faultsList.get(index);
    }

    @Override
    @NonNull
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View row = convertView;
        final FaultListAdapter.CardViewHolder viewHolder;
        final FaultListItem fault = getItem(position);

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = Objects.requireNonNull(inflater).inflate(R.layout.fault_list_item, parent, false);
            viewHolder = new FaultListAdapter.CardViewHolder();
            viewHolder.roomName = (TextView) row.findViewById(R.id.fault_list_item_room_name);
            viewHolder.description = (TextView) row.findViewById(R.id.fault_list_item_description);

            row.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //Creating the instance of PopupMenu
                    PopupMenu popup = new PopupMenu(context, v);
                    //Inflating the Popup using xml file
                    popup.getMenuInflater().inflate(R.menu.fault_list_fault_action_menu, popup.getMenu());

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {

                            FaultListItem toRemove = new FaultListItem();
                            for (FaultListItem currFault : faultsList) {

                                String roomName = Objects.requireNonNull(currFault).roomName + " (" + currFault.roomType + " " + currFault.gender + ")";
                                String fullDescription = "תקלה ב" + currFault.faultIn;
                                if(!currFault.airConditionNumber.isEmpty()) {
                                    fullDescription += " מספר " + currFault.airConditionNumber;
                                }
                                fullDescription += ".\n" + currFault.description;

                                if (roomName.equalsIgnoreCase(viewHolder.roomName.getText().toString())
                                        && fullDescription.equalsIgnoreCase(viewHolder.description.getText().toString())) {
                                    toRemove = currFault;
                                    break;
                                }
                            }

                            final FaultListItem toRemoveFinal = toRemove;
                            switch (item.getItemId()) {
                                case R.id.complete_action:
                                    Integer roomType = toRemoveFinal.roomType.equalsIgnoreCase(context.getString(R.string.topaz)) ? Constants.ROOM_TYPE_TOPAZ_INT : Constants.ROOM_TYPE_LOTEM_INT;
                                    Integer gender = toRemoveFinal.gender.equalsIgnoreCase(context.getString(R.string.boys)) ? Constants.GENDER_BOYS_INT : Constants.GENDER_GIRLS_INT;

                                    DBHandler.getAllRoomsByFilter(roomType, gender, new DBHandler.OnGetRoomDataListener() {
                                        @Override
                                        public void onStart() {

                                        }

                                        @Override
                                        public void onSuccess(Map<Integer, Room> data) {
                                            Integer roomID = -1;
                                            for(Map.Entry<Integer,Room> currEntry : data.entrySet())
                                            {
                                                if(currEntry.getValue().roomName.equalsIgnoreCase(toRemoveFinal.roomName))
                                                {
                                                    roomID = currEntry.getKey();
                                                }
                                            }

                                            if(roomID != -1)
                                            {
                                                DBHandler.getLeaderInRoom(roomID, new DBHandler.OnGetPersonDataListener() {
                                                    @Override
                                                    public void onStart() {

                                                    }

                                                    @Override
                                                    public void onSuccess(Map<Integer, Person> data) {
                                                        if(data.size() > 0)
                                                        {
                                                            Map.Entry<Integer,Person> entry = data.entrySet().iterator().next();
                                                            if (context.checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                                                                Toast.makeText(context, context.getString(R.string.error_no_send_sms_permissions), Toast.LENGTH_LONG).show();
                                                            } else {
                                                                SmsManager sm = SmsManager.getDefault();
                                                                sm.sendTextMessage(Constants.ISRAEL_LOCALE_PHONE_PREFIX + entry.getValue().phoneNumber.substring(1), null, context.getString(R.string.fault_fix_message), null, null);
                                                                Toast.makeText(context, context.getString(R.string.success_sms_sent), Toast.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailed(DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onFailed(DatabaseError databaseError) {

                                        }
                                    });

                                    faultsList.remove(toRemove);
                                    ((Activity)context).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            totalFaults.setText(String.format(Locale.getDefault(), "%d", faultsList.size()));
                                            notifyDataSetChanged();
                                        }
                                    });

                                    String range = Constants.FAULTS_SHEET_NAME + "!A" + toRemove.rowID + ":I" + toRemove.rowID;
                                    AsyncClear faultInfoClear = new AsyncClear(context, mCredential);
                                    faultInfoClear.execute(Constants.FAULTS_SPREAD_SHEET_ID, range);
                                    break;

                                case R.id.send_to_action:
                                    lastSelectedItem = viewHolder;

                                    Activity origin = (Activity)context;
                                    Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                                    origin.startActivityForResult(contactPickerIntent, REQUEST_PICK_CONTACT_ADAPTER);
                                    break;

                                case R.id.delete_action:
                                    faultsList.remove(toRemove);
                                    ((Activity)context).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            totalFaults.setText(String.format(Locale.getDefault(), "%d", faultsList.size()));
                                            notifyDataSetChanged();
                                        }
                                    });

                                    String rangeToDelete = Constants.FAULTS_SHEET_NAME + "!A" + toRemove.rowID + ":I" + toRemove.rowID;
                                    AsyncClear faultsInfoClear = new AsyncClear(context, mCredential);
                                    faultsInfoClear.execute(Constants.FAULTS_SPREAD_SHEET_ID, rangeToDelete);
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
            viewHolder = (FaultListAdapter.CardViewHolder) row.getTag();
        }

        viewHolder.roomName.setText(Objects.requireNonNull(fault).roomName + " (" + fault.roomType + " " + fault.gender + ")");
        viewHolder.roomName.setPaintFlags(viewHolder.roomName.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);

        String fullDescription = "תקלה ב" + fault.faultIn;
        if(!fault.airConditionNumber.isEmpty()) {
            fullDescription += " מספר " + fault.airConditionNumber;
        }
        fullDescription += ".\n" + fault.description;

        viewHolder.description.setText(fullDescription);

        return row;
    }

    public  void onActivityResult(int requestCode, int resultCode, Intent data) {
        contactPicked(data);
    }

    private void contactPicked(Intent data) {
        Cursor cursor = null;
        try {
            String phoneNo = null ;
            // getData() method will have the Content Uri of the selected contact
            Uri uri = data.getData();
            //Query the content uri
            cursor = context.getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            // column index of the phone number
            int  phoneIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            // column index of the contact name
            phoneNo = cursor.getString(phoneIndex);

            String phoneNumberWithoutPrefix = phoneNo.substring(1);
            String fullMessage = lastSelectedItem.roomName.getText().toString() + ".\n" + lastSelectedItem.description.getText().toString();

            String url = Constants.SEND_API_PREFIX + Constants.SEND_API_PHONE_PARAM + Constants.ISRAEL_LOCALE_PHONE_PREFIX +  phoneNumberWithoutPrefix + Constants.SEND_API_MESSAGE_PARAM + fullMessage;
            Intent waIntent = new Intent(Intent.ACTION_VIEW);
            waIntent.setPackage(Constants.WHATSAPP_PACKAGE);
            waIntent.setData(Uri.parse(url));

            if (waIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(waIntent);
            }
            else {
                throw new PackageManager.NameNotFoundException();
            }
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(context, context.getString(R.string.error_whatsapp_not_installed), Toast.LENGTH_SHORT)
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
