package com.amiel.tls.ui.topaz;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.amiel.tls.R;
import com.amiel.tls.RoomsListAdapter;
import com.amiel.tls.TabAdapter;
import com.amiel.tls.db.DBHandler;
import com.amiel.tls.db.entities.Person;
import com.amiel.tls.db.entities.Room;
import com.amiel.tls.CustomRoomSpinnerAdapter;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TopazFragment extends Fragment {

    private TabAdapter TopazAdapter;
    private TabLayout TopazTabLayout;
    private ViewPager TopazViewPager;

    private final Map<Integer, Room> availableRooms = new HashMap<>();

    private int[] tabIcons = {
            R.drawable.ic_boy,
            R.drawable.ic_girl
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_topaz, container, false);

        TopazViewPager = (ViewPager) root.findViewById(R.id.viewPager_topaz);
        TopazTabLayout = (TabLayout) root.findViewById(R.id.tabLayout_topaz);
        TopazAdapter = new TabAdapter(getActivity().getSupportFragmentManager());
        TopazAdapter.addFragment(new TopazBoysFragment(), getResources().getString(R.string.title_boys));
        TopazAdapter.addFragment(new TopazGirlsFragment(), getResources().getString(R.string.title_girls));
        TopazViewPager.setAdapter(TopazAdapter);
        TopazTabLayout.setupWithViewPager(TopazViewPager);

        TopazTabLayout.getTabAt(0).setIcon(tabIcons[0]);
        TopazTabLayout.getTabAt(1).setIcon(tabIcons[1]);

        return root;
    }
}