package com.placesearch.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.placesearch.fragments.FavoritesFragment;
import com.placesearch.fragments.SearchFragment;



public class Pager extends FragmentStatePagerAdapter {

    int tabCount;

    //Constructor to the class
    public Pager(FragmentManager fm, int tabCount) {
        super(fm);
        //Initializing tab count
        this.tabCount= tabCount;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                SearchFragment searchfragment = new SearchFragment();
                return searchfragment;
            case 1:
                FavoritesFragment favoritesfragment = new FavoritesFragment();
                return favoritesfragment;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return this.tabCount;
    }
}
