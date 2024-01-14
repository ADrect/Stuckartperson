package com.example.stuckart;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.example.stuckart.databinding.FragmentMyAdsBinding;

public class MyAdsFragment extends Fragment {

    //View Binding
    private static final String TAG = "MY_ADS_TAG";
    //TAG to show logs in logcat
    private FragmentMyAdsBinding binding;

    //Context for this fragment class
    private Context mContext;

    private MyTabsViewPagerAdapter myTabsViewPagerAdapter;

    public MyAdsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        //get and init the context for this fragment class Context context;
        mContext = context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            // Inflate/bind the layout (fragment_my.ods.xml) for this fragment
            binding = FragmentMyAdsBinding.inflate(inflater, container, false);

            return binding.getRoot();
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            //Add the tabs to the TobLayout
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Ads"));
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Favorites"));

            //Fragment manage, initializing using getChildFragmentManager() because we are using tabs in fragment not activity
            FragmentManager fragmentManager = getChildFragmentManager();
            myTabsViewPagerAdapter = new MyTabsViewPagerAdapter(fragmentManager, getLifecycle());
            binding.viewPager.setAdapter(myTabsViewPagerAdapter);

            //tob selected listener to set current item on view page
            binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    binding.viewPager.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });

            //Change Tab when swiping
            binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position));
                }
            });
        }

    public class MyTabsViewPagerAdapter extends FragmentStateAdapter {

        public MyTabsViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }


        @NonNull
        @Override
        public Fragment createFragment(int position) {
            //tob position starts from 0. if 0 set/show MyAdsAdsFragment otherwise it is definitely 1 so shaw RyAds FavFrageer

            if (position == 0) {

                return new MyAdsAdsFragment();
            } else {

                return new MyAdsFavFragment();
            }
        }

        @Override
        public int getItemCount() {
            //return List of items/tabs
            return 2; //setting static size 2 because we have two tabs/fragments
        }
    }
}