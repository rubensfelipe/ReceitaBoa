package com.example.android.receitaboa.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.example.android.receitaboa.R;
import com.example.android.receitaboa.activity.VisualizarReceitaActivity;
import com.example.android.receitaboa.adapter.AdapterFeed;
import com.example.android.receitaboa.helper.ConfiguracaoFirebase;
import com.example.android.receitaboa.helper.RecyclerItemClickListener;
import com.example.android.receitaboa.helper.UsuarioFirebaseAuth;
import com.example.android.receitaboa.model.Feed;
import com.example.android.receitaboa.model.Receitas;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FeedFragment extends Fragment {

    private List<Feed> listaFeed = new ArrayList<>();

    private RecyclerView recyclerFeed;
    private String idUsuarioLogado;
    private DatabaseReference feedRef;
    private AdapterFeed adapterFeed;

    private ValueEventListener valueEventListenerFeed;

    public FeedFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_feed, container,false);

        configuracoesIniciais();

        inicializarComponentes(view);

        configurarAdapterMaisRecyclerView();

        configurarEventoCliqueReceita();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        listandoFeed();
    }

    private void configuracoesIniciais() {
        idUsuarioLogado = UsuarioFirebaseAuth.getIdentificadorChefAuth();
        feedRef = ConfiguracaoFirebase.getFirebaseDatabase()
                .child("feed")
                .child(idUsuarioLogado);
    }

    private void inicializarComponentes(View visto) {
        recyclerFeed = visto.findViewById(R.id.recyclerFeed);
    }

    private void configurarAdapterMaisRecyclerView() {
        adapterFeed = new AdapterFeed(listaFeed, getActivity());
        recyclerFeed.setHasFixedSize(true);
        recyclerFeed.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerFeed.setAdapter(adapterFeed);
    }

    private void configurarEventoCliqueReceita() {

        recyclerFeed.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerFeed,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                Feed receitaSelecionadaFeed = listaFeed.get(position);

                                Intent i = new Intent(getActivity(), VisualizarReceitaActivity.class);
                                i.putExtra("dadosReceitaFeedClicada", receitaSelecionadaFeed);
                                startActivity(i);
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {        }

                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {        }
                        }
                )
        );
    }

    private void listandoFeed() {

        //limpar lista (evita repetição ao sair da tela de feed)
        listaFeed.clear();

        valueEventListenerFeed = feedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    listaFeed.add(ds.getValue(Feed.class)); //recuperado os dados de publicacao dos nossos seguidores que estão gravados na tree do feed no FirebaseDatabase
                }
                Collections.reverse(listaFeed); //reverte a ordem da lista (para que q sempre apareça primeiro a ultima postagem)
                adapterFeed.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {       }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        //((AppCompatActivity)getActivity()).getSupportActionBar().hide();
    }

    @Override
    public void onStop() {
        super.onStop();
        feedRef.removeEventListener(valueEventListenerFeed);
        //((AppCompatActivity)getActivity()).getSupportActionBar().show();
    }

}
