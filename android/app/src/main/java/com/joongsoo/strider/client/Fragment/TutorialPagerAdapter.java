package com.joongsoo.strider.client.Fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


import java.util.ArrayList;

public class TutorialPagerAdapter extends FragmentPagerAdapter {

    ArrayList<TutorialFragment> fragmentsList;

    public TutorialPagerAdapter(FragmentManager fm, ArrayList<TutorialFragment> fragmentsList) {
        super(fm);
        this.fragmentsList = fragmentsList;
    }

    @Override
    public int getCount() {
        return fragmentsList.size();
    }

    @Override
    public Fragment getItem(int position) {
        if (position < fragmentsList.size() & fragmentsList.get(position) != null) {
            return fragmentsList.get(position);
        }
        return null;
    }

    @Override
    public float getPageWidth(int position) {
        return (1.0f);
    }
}
