package com.amiel.tls.ui.topaz;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.amiel.tls.Constants;
import com.amiel.tls.R;
import com.amiel.tls.RoomsListAdapter;
import com.amiel.tls.db.DBHandler;
import com.amiel.tls.db.entities.Room;
import com.google.firebase.database.DatabaseError;

import java.util.Map;

public class TopazGirlsFragment extends Fragment {

    private ListView TopazGirlsRooms;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = container.getContext();
        View root = inflater.inflate(R.layout.fragment_girls_topaz, container, false);

        TopazGirlsRooms = (ListView) root.findViewById(R.id.topaz_girls_rooms_listView);
        TopazGirlsRooms.setEmptyView(root.findViewById(R.id.topaz_girls_emptyElement));
        swipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.fragment_girls_topaz_swipe_refresh_layout);

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
        DBHandler.getAllRoomsByFilter(Constants.ROOM_TYPE_TOPAZ_INT, Constants.GENDER_GIRLS_INT, new DBHandler.OnGetRoomDataListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(final Map<Integer, Room> data) {
                RoomsListAdapter roomsListAdapter = new RoomsListAdapter(context, R.layout.rooms_list_item, data);
                TopazGirlsRooms.setAdapter(roomsListAdapter);
            }

            @Override
            public void onFailed(DatabaseError databaseError) {

            }
        });
    }
}