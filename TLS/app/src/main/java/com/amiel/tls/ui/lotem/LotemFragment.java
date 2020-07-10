package com.amiel.tls.ui.lotem;

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
import com.google.android.material.tabs.TabLayout;

public class LotemFragment extends Fragment {

    private LotemViewModel lotemViewModel;
    private TabAdapter LotemAdapter;
    private TabLayout LotemTabLayout;
    private ViewPager LotemViewPager;

    private int[] tabIcons = {
            R.drawable.ic_boy,
            R.drawable.ic_girl
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        lotemViewModel =
                ViewModelProviders.of(this).get(LotemViewModel.class);
        View root = inflater.inflate(R.layout.fragment_lotem, container, false);
        /*final TextView textView = root.findViewById(R.id.text_home);
        lotemViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/

        LotemViewPager = (ViewPager) root.findViewById(R.id.viewPager_lotem);
        LotemTabLayout = (TabLayout) root.findViewById(R.id.tabLayout_lotem);
        LotemAdapter = new TabAdapter(getActivity().getSupportFragmentManager());
        LotemAdapter.addFragment(new LotemBoysFragment(), getResources().getString(R.string.title_boys));
        LotemAdapter.addFragment(new LotemGirlsFragment(), getResources().getString(R.string.title_girls));
        LotemViewPager.setAdapter(LotemAdapter);
        LotemTabLayout.setupWithViewPager(LotemViewPager);

        LotemTabLayout.getTabAt(0).setIcon(tabIcons[0]);
        LotemTabLayout.getTabAt(1).setIcon(tabIcons[1]);

        return root;
    }
}