package com.amiel.tls.ui.lotem;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.amiel.tls.Constants;
import com.amiel.tls.PreferencesManager;
import com.amiel.tls.R;
import com.amiel.tls.RoomsListAdapter;
import com.amiel.tls.db.DBHandler;
import com.amiel.tls.db.entities.Room;
import com.google.firebase.database.DatabaseError;

import java.util.Iterator;
import java.util.Map;

public class LotemBoysFragment extends Fragment {

    private ListView LotemBoysRooms;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        context = container.getContext();
        View root = inflater.inflate(R.layout.fragment_boys_lotem, container, false);

        LotemBoysRooms = (ListView) root.findViewById(R.id.lotem_boys_rooms_listView);
        LotemBoysRooms.setEmptyView(root.findViewById(R.id.lotem_boys_emptyElement));
        swipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.fragment_boys_lotem_swipe_refresh_layout);

        updateRooms();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateRooms();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        context = getActivity();
        updateRooms();
        super.onResume();
    }

    private void updateRooms()
    {
        DBHandler.getAllRoomsByFilter(Constants.ROOM_TYPE_LOTEM_INT, Constants.GENDER_BOYS_INT, new DBHandler.OnGetRoomDataListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(final Map<Integer, Room> data) {
                if(!PreferencesManager.getInstance().getIsAdminValue()) {
                    Iterator<Map.Entry<Integer, Room>> iterator = data.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<Integer, Room> currRoom = iterator.next();
                        if(currRoom.getKey() != PreferencesManager.getInstance().getRoomIDValue()) {
                            iterator.remove();
                        }
                    }
                }

                RoomsListAdapter roomsListAdapter = new RoomsListAdapter(context, R.layout.rooms_list_item, data);
                LotemBoysRooms.setAdapter(roomsListAdapter);
            }

            @Override
            public void onFailed(DatabaseError databaseError) {

            }
        });
    }
}