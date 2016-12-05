package com.joongsoo.strider.client.Fragment;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;

/**
 * Created by joongsoo on 2016-10-21.
 */
public class BthFragment extends Fragment {
    public interface OnFragmentClickListener{
        public void onClickInFragment(View view);
    }

    private OnFragmentClickListener onClickToActivityListener;

    public View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
}
