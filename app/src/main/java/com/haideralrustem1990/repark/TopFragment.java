package com.haideralrustem1990.repark;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;



/**
 * A simple {@link Fragment} subclass.
 */
public class TopFragment extends Fragment {

    Connector mainActivity;

    // A very important attribute that serves as a pointer to the corresponding data element
    // in the array (occurrence)
    private int fragmentPosition;

    public int getFragmentPosition() {
        return fragmentPosition;
    }

    public void setFragmentPosition(int fragmentPosition) {
        this.fragmentPosition = fragmentPosition;
    }



    public TopFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout =  inflater.inflate(R.layout.fragment_top, container, false);
        if(savedInstanceState != null){
            fragmentPosition = savedInstanceState.getInt("position");
        }

        return layout;
    }
    @Override
    public void onActivityCreated(Bundle bundle){
        super.onActivityCreated(bundle);

    }

    @Override
    public void onStart(){
        super.onStart();
        this.mainActivity = (Connector) getContext();

        /*  The two methods below are basically responsible for the rendering of each
        of the fragments contents
         */

        // This is responsible for rendering the view of the fragment
        mainActivity.adjustTextView(R.id.topTextView, fragmentPosition, this);
        //R.id.topTextView
        mainActivity.adjustImageView(R.id.imageView, fragmentPosition, this);

    }
    @Override
    public void onSaveInstanceState(Bundle bundle){
        bundle.putInt("position", fragmentPosition);
    }


}
