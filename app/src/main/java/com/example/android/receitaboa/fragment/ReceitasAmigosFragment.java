package com.example.android.receitaboa.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import com.example.android.receitaboa.R;
import com.example.android.receitaboa.activity.VisualizarReceitaActivity;
import com.example.android.receitaboa.adapter.ReceitasAdapter;
import com.example.android.receitaboa.helper.ConfiguracaoFirebase;
import com.example.android.receitaboa.helper.RecyclerItemClickListener;
import com.example.android.receitaboa.helper.UsuarioFirebaseAuth;
import com.example.android.receitaboa.model.Chef;
import com.example.android.receitaboa.model.Receitas;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReceitasAmigosFragment extends Fragment {

    private ProgressBar progressBarReceitas;
    private RecyclerView recyclerReceitasAmigos;

    private RecyclerView.LayoutManager layoutManager;

    private List<Receitas> listaRA = new ArrayList<>();
    private List<String> listaIdAmigos = new ArrayList<>();
    private List<Chef> listaAmigos = new ArrayList<>();

    private ReceitasAdapter adapterRA;

    private DatabaseReference firebaseDbRef;
    private DatabaseReference amigosRef;
    private DatabaseReference receitasRef;
    private DatabaseReference receitasAmigoRef;
    private String idChefLogado;

    private ValueEventListener valueEventListenerRA;
    private ValueEventListener valueEventListenerAmigos;

    public boolean receitaClicada = false;

    public ReceitasAmigosFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_amigos_receitas, container, false);

        inicializarComponentes(view);

        configuracoesIniciais();

        configurarAdapterMaisRecyclerView();

        configurarEventoCliqueReceita();

        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        recuperarReceitasAmigos();
    }

    private void inicializarComponentes(View vista) {
        recyclerReceitasAmigos = vista.findViewById(R.id.recyclerReceitasAmigos);
        progressBarReceitas = vista.findViewById(R.id.progressBarReceitas);
    }

    private void configuracoesIniciais() {

        idChefLogado = UsuarioFirebaseAuth.getIdentificadorChefAuth();

        firebaseDbRef = ConfiguracaoFirebase.getFirebaseDatabase();

        amigosRef = firebaseDbRef
                .child("amigos")
                .child(idChefLogado);

        receitasRef = firebaseDbRef
                .child("receitas");

    }

    private void configurarAdapterMaisRecyclerView() {
        adapterRA = new ReceitasAdapter(listaRA, getActivity() );

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerReceitasAmigos.setLayoutManager(layoutManager);
        recyclerReceitasAmigos.setHasFixedSize(true);
        recyclerReceitasAmigos.setAdapter(adapterRA);
    }

    private void configurarEventoCliqueReceita() {

        recyclerReceitasAmigos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerReceitasAmigos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                receitaClicada = true;
                                List<Receitas> listaRAAtualizada = adapterRA.getListaReceitas(); //permite que a posição na lista da receitas não se altere msm qdo houver uma busca

                                Receitas receitaAmigoSelecionada = listaRAAtualizada.get(position); //recupera qual item foi clicado de acordo com a posição na lista no momento do click

                                Intent i = new Intent(getActivity(), VisualizarReceitaActivity.class);
                                i.putExtra("dadosReceitaAmigoClicada", receitaAmigoSelecionada);
                                startActivity(i);

                            }
                            @Override
                            public void onLongItemClick(View view, int position) { }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {   }
                        }
                )
        );

    }

    private void recuperarAmigos(){

        valueEventListenerAmigos = amigosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                listaAmigos.clear();
                listaIdAmigos.clear();

                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    String idAmigos = ds.getKey();

                    listaIdAmigos.add(idAmigos);
                }
                adapterRA.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {     }
        });

    }

    private void recuperarReceitasAmigos() {

        recuperarAmigos();

       listaRA.clear(); //nenhuma alteração

        for (String idAmigo : listaIdAmigos) { //percorrendo id Amigo do chef logado dentro da lista

            //Percorrer apenas as receitas dos Amigos do Chef logado
            receitasAmigoRef = receitasRef.child(idAmigo);

            //receitasAmigoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            valueEventListenerRA = receitasAmigoRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot ds: dataSnapshot.getChildren()){

                        Receitas receitaAmigo = ds.getValue(Receitas.class);
                        listaRA.add(receitaAmigo);

                        //se o amigo já tiver adicionado ao menos uma receita no app, a progressBar desaparece
                        if(receitaAmigo != null){
                            progressBarReceitas.setVisibility(View.GONE);
                        }

                    }
                    adapterRA.notifyDataSetChanged();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {   }
            });

        }
    }

    public void pesquisarReceitasAmigos(String receitaNaPesquisa){

        List<Receitas> listaRABusca = new ArrayList<>();

        for (Receitas receita: listaRA){

            String nomeReceita = receita.getNome().toLowerCase(); //pega o nome da receita na listaReceitasAmigos e deixa tudo em minúsculo
            if (nomeReceita.contains(receitaNaPesquisa)){
                listaRABusca.add(receita);
            }
        }
        configuracaoAdaptador(listaRABusca);
    }

    public void recarregarReceitasAmigos(){
        recuperarReceitasAmigos();
        configuracaoAdaptador(listaRA);
    }

    private void configuracaoAdaptador(List<Receitas> listaEscolhida) {
        adapterRA = new ReceitasAdapter(listaEscolhida, getActivity());
        recyclerReceitasAmigos.setAdapter(adapterRA);
        adapterRA.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        //amigosRef.removeEventListener(valueEventListenerAmigos);
        //receitasAmigoRef.removeEventListener(valueEventListenerRA);
    }
}
