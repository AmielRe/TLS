package com.amiel.tls;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.amiel.tls.db.DBHandler;
import com.amiel.tls.db.entities.Person;
import com.amiel.tls.db.entities.Room;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoomsListAdapter extends ArrayAdapter<Room> {
    private List<Room> roomsList = new ArrayList<>();
    private Map<Integer, Room> rooms;
    private Context context;

    static class CardViewHolder {
        TextView roomName;
        TextView roomCapacity;
        Button removeRoom;
    }

    public RoomsListAdapter(Context context, int textViewResourceId, Map<Integer, Room> rooms) {
        super(context, textViewResourceId);
        this.context = context;
        this.rooms = rooms;
    }

    @Override
    public void add(Room object) {
        roomsList.add(object);
        super.add(object);
    }

    @Override
    public int getCount() {
        return this.roomsList.size();
    }

    @Override
    public Room getItem(int index) {
        return this.roomsList.get(index);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        final CardViewHolder viewHolder;
        final Room room = getItem(position);

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.rooms_list_item, parent, false);
            viewHolder = new CardViewHolder();
            viewHolder.roomName = (TextView) row.findViewById(R.id.rooms_list_item_room_name);
            viewHolder.roomCapacity = (TextView) row.findViewById(R.id.rooms_list_item_room_capacity);
            viewHolder.removeRoom = (Button) row.findViewById(R.id.rooms_list_item_room_remove);

            viewHolder.removeRoom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                    alertDialog.setTitle("שים לב");
                    alertDialog.setMessage("האם אתה בטוח שברצונך למחוק שורה זו?");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "כן",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Room toRemove = new Room();
                                    for(Room currRoom : roomsList) {
                                        if(currRoom.roomName.equalsIgnoreCase(viewHolder.roomName.getText().toString())) {
                                            toRemove = currRoom;
                                            break;
                                        }
                                    }
                                    DBHandler.removeRoom(toRemove);
                                    roomsList.remove(toRemove);
                                    notifyDataSetChanged();
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "בטל",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            });

            row.setTag(viewHolder);
        } else {
            viewHolder = (CardViewHolder)row.getTag();
        }

        viewHolder.roomName.setText(room.roomName);
        viewHolder.roomCapacity.setText(String.format("%d/%d", room.currentCapacity, room.maxCapacity));

        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer roomID = 0;
                Room toDisplay = new Room();
                for(Map.Entry<Integer, Room> currRoom : rooms.entrySet()) {
                    if(currRoom.getValue().roomName == viewHolder.roomName.getText().toString()) {
                        toDisplay = currRoom.getValue();
                        roomID = currRoom.getKey();
                        break;
                    }
                }

                getContext().startActivity(new Intent(getContext(), RoomManagementActivity.class).putExtra("room", toDisplay).putExtra("roomID", roomID));
            }
        });
        return row;
    }
}