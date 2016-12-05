package com.joongsoo.strider.client.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.joongsoo.strider.client.R;

/**
 * Created by joongsoo on 2016-10-21.
 */
public class Frag_main_3 extends TutorialFragment{

    ViewMapper viewMapper;

    @Override
    public View onCreateView( LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState ) {
        View view =  View.inflate(getActivity(), R.layout.main_frag3,null);
        viewMapper = new ViewMapper(view);
        return view;
    }
    private class ViewMapper{
        private ViewMapper(View view) {
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }
    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
