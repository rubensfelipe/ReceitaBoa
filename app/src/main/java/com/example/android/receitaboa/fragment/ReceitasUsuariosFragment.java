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
import com.example.android.receitaboa.adapter.ReceitasUsuariosAdapter;
import com.example.android.receitaboa.helper.ConfiguracaoFirebase;
import com.example.android.receitaboa.helper.RecyclerItemClickListener;
import com.example.android.receitaboa.helper.UsuarioFirebaseAuth;
import com.example.android.receitaboa.model.Receitas;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReceitasUsuariosFragment extends Fragment {

    private ProgressBar progressBarReceitas;
    private RecyclerView recyclerReceitas;

    private RecyclerView.LayoutManager layoutManager;

    private List<Receitas> listaReceitas = new ArrayList<>();

    //private ReceitasAdapter adapterRA;
    private ReceitasUsuariosAdapter adapterReceitas;

    private DatabaseReference firebaseDbRef;
    private DatabaseReference amigosRef;
    private DatabaseReference receitasRef;
    private String idChefLogado;

    private ValueEventListener valueEventListenerRA;

    public boolean receitaClicada = false;

    public ReceitasUsuariosFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_receitas_usuarios, container, false);

        inicializarComponentes(view);

        configuracoesIniciais();

        configurarAdapterMaisRecyclerView();

        configurarEventoCliqueReceita();

        return view;
    }

    @Override
    public void onStart(){
        recuperarReceitasUsuarios();
        super.onStart();
    }

    private void inicializarComponentes(View vista) {
        recyclerReceitas = vista.findViewById(R.id.recyclerReceitasAmigos);
        progressBarReceitas = vista.findViewById(R.id.progressBarRA);
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
        //adapterRA = new ReceitasAdapter(listaRA, getActivity() );
        adapterReceitas = new ReceitasUsuariosAdapter(listaReceitas, getActivity() );

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerReceitas.setLayoutManager(layoutManager);
        recyclerReceitas.setHasFixedSize(true);
        recyclerReceitas.setAdapter(adapterReceitas);
    }

    private void configurarEventoCliqueReceita() {

        recyclerReceitas.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerReceitas,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                receitaClicada = true;
                                List<Receitas> listaReceitasAtualizada = adapterReceitas.getListaReceitasUsuarios(); //permite que a posição na lista da receitas não se altere msm qdo houver uma busca

                                Receitas receitaSelecionada = listaReceitasAtualizada.get(position); //recupera qual item foi clicado de acordo com a posição na lista no momento do click

                                Intent i = new Intent(getActivity(), VisualizarReceitaActivity.class);
                                i.putExtra("dadosReceitaClicada", receitaSelecionada);
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

    public void recuperarReceitasUsuarios() {

        listaReceitas.clear(); //nenhuma alteração

            valueEventListenerRA = receitasRef.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                    for (DataSnapshot ds: dataSnapshot.getChildren()){
                        for (DataSnapshot dadosReceita: ds.getChildren()){

                                Receitas receitas = dadosReceita.getValue(Receitas.class);

                                listaReceitas.add(receitas);

                                sumirProgressBar(receitas);

                        }
                    }
                    listaEmOrdemAlfabetica();
                    adapterReceitas.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {   }
            });
    }

    private void listaEmOrdemAlfabetica() {
        Collections.sort(listaReceitas, new Comparator<Receitas>() {
            @Override
            public int compare(Receitas rec1, Receitas rec2) {
                return rec1.getNome().compareToIgnoreCase(rec2.getNome()); //ordem crescente por nome da receita
                //return rec2.getNome().compareToIgnoreCase(rec1.getNome()); //ordem descrescente
                //return Integer.valueOf(rec1.getQtdPessoasServidas()).compareTo(Integer.valueOf(rec2.getQtdPessoasServidas()); //orderna em ordem crescente para números inteiros
            }
        });
    }

    private void sumirProgressBar(Receitas rAmigo) {
        //se o amigo já tiver adicionado ao menos uma receita no app, a progressBar desaparece
        if(rAmigo != null){
            progressBarReceitas.setVisibility(View.GONE);
        }
    }

    public void pesquisarReceitas(String receitaNaPesquisa){

        List<Receitas> listaRABusca = new ArrayList<>();

        for (Receitas receita: listaReceitas){

            String nomeReceita = receita.getNome().toLowerCase(); //pega o nome da receita na listaReceitasAmigos e deixa tudo em minúsculo
            if (nomeReceita.contains(receitaNaPesquisa)){
                listaRABusca.add(receita);
            }
        }
        configuracaoAdaptador(listaRABusca);
    }

    public void recarregarReceitasUsuarios(){
        configuracaoAdaptador(listaReceitas);
    }

    private void configuracaoAdaptador(List<Receitas> listaEscolhida) {
        adapterReceitas = new ReceitasUsuariosAdapter(listaEscolhida, getActivity());
        recyclerReceitas.setAdapter(adapterReceitas);
        adapterReceitas.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
//        amigosRef.removeEventListener(valueEventListenerAmigos);
    }
}
