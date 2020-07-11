package com.amiel.tls.ui.topaz;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.amiel.tls.R;
import com.amiel.tls.TabAdapter;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.material.tabs.TabLayout;

public class TopazFragment extends Fragment {

    FloatingActionMenu topazMainFAB;
    FloatingActionButton topazAddRoomFAB, topazAddPersonFAB;

    private TopazViewModel topazViewModel;
    private TabAdapter TopazAdapter;
    private TabLayout TopazTabLayout;
    private ViewPager TopazViewPager;

    private int[] tabIcons = {
            R.drawable.ic_boy,
            R.drawable.ic_girl
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        topazViewModel =
                ViewModelProviders.of(this).get(TopazViewModel.class);
        View root = inflater.inflate(R.layout.fragment_topaz, container, false);
        /*final TextView textView = root.findViewById(R.id.text_dashboard);
        topazViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/

        TopazViewPager = (ViewPager) root.findViewById(R.id.viewPager_topaz);
        TopazTabLayout = (TabLayout) root.findViewById(R.id.tabLayout_topaz);
        TopazAdapter = new TabAdapter(getActivity().getSupportFragmentManager());
        TopazAdapter.addFragment(new TopazBoysFragment(), getResources().getString(R.string.title_boys));
        TopazAdapter.addFragment(new TopazGirlsFragment(), getResources().getString(R.string.title_girls));
        TopazViewPager.setAdapter(TopazAdapter);
        TopazTabLayout.setupWithViewPager(TopazViewPager);

        TopazTabLayout.getTabAt(0).setIcon(tabIcons[0]);
        TopazTabLayout.getTabAt(1).setIcon(tabIcons[1]);

        topazMainFAB = (FloatingActionMenu) root.findViewById(R.id.material_design_android_floating_action_menu_topaz);
        topazAddRoomFAB = (FloatingActionButton) root.findViewById(R.id.material_design_floating_action_menu_room_topaz);
        topazAddPersonFAB = (FloatingActionButton) root.findViewById(R.id.material_design_floating_action_menu_person_topaz);

        return root;
    }
}