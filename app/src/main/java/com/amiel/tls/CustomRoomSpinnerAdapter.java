package com.amiel.tls;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.amiel.tls.db.entities.Room;

import java.util.Locale;

public class CustomRoomSpinnerAdapter extends BaseAdapter {

    private Context context;
    private Room[] rooms;
    private LayoutInflater inflater;

    CustomRoomSpinnerAdapter(Context applicationContext, Room[] rooms) {
        this.context = applicationContext;
        this.rooms = rooms;
        inflater = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return rooms.length;
    }

    @Override
    public Object getItem(int i) {
        return rooms[i];
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.custom_room_spinner_row, null);
        TextView roomName = (TextView) view.findViewById(R.id.custom_room_spinner_row_room_name);
        TextView roomCapacity = (TextView) view.findViewById(R.id.custom_room_spinner_row_capacity);
        TextView roomGenderType = (TextView) view.findViewById(R.id.custom_room_spinner_row__gender_type);

        roomName.setText(rooms[i].roomName);
        roomCapacity.setText(String.format(Locale.getDefault(), "%d/%d", rooms[i].currentCapacity, rooms[i].maxCapacity));
        roomGenderType.setText(String.format("%s (%s): ", CommonUtils.intToRoomType(context, rooms[i].roomType), CommonUtils.intToGender(context, rooms[i].roomGender)));
        return view;
    }
}
