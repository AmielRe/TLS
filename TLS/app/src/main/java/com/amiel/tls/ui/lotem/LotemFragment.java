package com.amiel.tls.ui.lotem;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.amiel.tls.R;
import com.amiel.tls.TabAdapter;
import com.google.android.material.tabs.TabLayout;

public class LotemFragment extends Fragment {

    private TabAdapter LotemAdapter;
    private TabLayout LotemTabLayout;
    private ViewPager LotemViewPager;

    private int[] tabIcons = {
            R.drawable.ic_boy,
            R.drawable.ic_girl
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_lotem, container, false);

        LotemViewPager = (ViewPager) root.findViewById(R.id.viewPager_lotem);
        LotemTabLayout = (TabLayout) getActivity().findViewById(R.id.tabLayout_main);
        LotemAdapter = new TabAdapter(getActivity().getSupportFragmentManager());
        LotemAdapter.addFragment(new LotemBoysFragment(), getResources().getString(R.string.boys));
        LotemAdapter.addFragment(new LotemGirlsFragment(), getResources().getString(R.string.girls));
        LotemViewPager.setAdapter(LotemAdapter);
        LotemTabLayout.setupWithViewPager(LotemViewPager);

        LotemTabLayout.getTabAt(0).setIcon(tabIcons[0]);
        LotemTabLayout.getTabAt(1).setIcon(tabIcons[1]);

        return root;
    }
}