package com.lucataddeo.datacollectionapp.app_sections.single_connection.tests;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lucataddeo.datacollectionapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class EcgPortraitFragment extends Fragment {


    public EcgPortraitFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ecg_portrait, container, false);
    }

}
