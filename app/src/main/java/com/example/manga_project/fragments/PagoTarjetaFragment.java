package com.example.manga_project.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.manga_project.R;

public class PagoTarjetaFragment extends Fragment {

    public PagoTarjetaFragment() {
        // Required empty public constructor
    }

    public static PagoTarjetaFragment newInstance() {
        return new PagoTarjetaFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pago_tarjeta, container, false);
    }
}

