package com.example.android.receitaboa.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.receitaboa.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PesquisaReceitasFragment extends Fragment {

    public PesquisaReceitasFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pesquisa_receitas, container, false);
    }
}
