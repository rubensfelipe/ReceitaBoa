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
import com.example.android.receitaboa.adapter.MinhasReceitasAdapter;
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

    private List<Receitas> listaReceitasAmigos = new ArrayList<>();
    private List<String> listaIdAmigos = new ArrayList<>();
    private List<Chef> listaAmigos = new ArrayList<>();

    private MinhasReceitasAdapter adapterReceitasAmigos;

    private DatabaseReference firebaseDbRef;
    private DatabaseReference amigosRef;
    private DatabaseReference receitasRef;
    private String idChefLogado;

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
        adapterReceitasAmigos = new MinhasReceitasAdapter(listaReceitasAmigos, getActivity() );

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerReceitasAmigos.setLayoutManager(layoutManager);
        recyclerReceitasAmigos.setHasFixedSize(true);
        recyclerReceitasAmigos.setAdapter(adapterReceitasAmigos);
    }

    private void configurarEventoCliqueReceita() {

        recyclerReceitasAmigos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerReceitasAmigos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                //ADICIONAR APÓS RESOLVER O PROBLEMA DA BUSCA DE RECEITAS
                                //receitaClicada = true;
                                //List<Receitas> listaMinhasReceitasAtualizada = adapterReceitasAmigos.getListaMinhasReceitas(); //permite que a posição na lista da receitas não se altere msm qdo houve uma busca

                                Receitas receitaAmigoSelecionada = listaReceitasAmigos.get(position); //recupera qual item foi clicado de acordo com a posição na lista no momento do click

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

        //listaAmigos.clear();
        //listaIdAmigos.clear();

        amigosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                listaAmigos.clear();
                listaIdAmigos.clear();

                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    String idAmigos = ds.getKey();

                    listaIdAmigos.add(idAmigos);

                    listaAmigos.add(ds.getValue(Chef.class));
                }
                adapterReceitasAmigos.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {     }
        });

    }

    private void listarReceitasAmigos() {

        recuperarAmigos();

       listaReceitasAmigos.clear(); //nenhuma alteração

        for (String idAmigo : listaIdAmigos) { //percorrendo id Amigo do chef logado dentro da lista

            //listaReceitasAmigos.clear(); //nenhuma alteração

            //Percorrer apenas as receitas dos Amigos do Chef logado
            DatabaseReference receitasAmigoRef = receitasRef.child(idAmigo);

            receitasAmigoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    //listaReceitasAmigos.clear(); //some a primeira receita da lista

                    for (DataSnapshot ds: dataSnapshot.getChildren()){

                        //listaReceitasAmigos.clear(); //deixa apenas o último item da lista

                        Receitas receitaAmigo = ds.getValue(Receitas.class);
                        listaReceitasAmigos.add(receitaAmigo);

                        //se o amigo já tiver adicionado ao menos uma receita no app, a progressBar desaparece
                        if(receitaAmigo != null){
                            progressBarReceitas.setVisibility(View.GONE);
                        }

                    }
                    adapterReceitasAmigos.notifyDataSetChanged();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {   }
            });

        }
    }

    @Override
    public void onStart(){
        listarReceitasAmigos();
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

}
