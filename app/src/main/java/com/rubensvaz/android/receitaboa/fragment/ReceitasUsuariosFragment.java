package com.rubensvaz.android.receitaboa.fragment;

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

import com.rubensfelipe.android.receitaboa.R;
import com.rubensvaz.android.receitaboa.activity.VisualizarReceitaActivity;
import com.rubensvaz.android.receitaboa.adapter.ReceitasUsuariosAdapter;
import com.rubensvaz.android.receitaboa.helper.ConfiguracaoFirebase;
import com.rubensvaz.android.receitaboa.helper.RecyclerItemClickListener;
import com.rubensvaz.android.receitaboa.helper.UsuarioFirebaseAuth;
import com.rubensvaz.android.receitaboa.model.Receitas;
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
    private DatabaseReference ultimasPostagensRef;
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
        progressBarReceitas = vista.findViewById(R.id.progressBarReceitas);
    }

    private void configuracoesIniciais() {

        idChefLogado = UsuarioFirebaseAuth.getIdentificadorChefAuth();

        firebaseDbRef = ConfiguracaoFirebase.getFirebaseDatabase();

        amigosRef = firebaseDbRef
                .child("amigos")
                .child(idChefLogado);

        ultimasPostagensRef = firebaseDbRef
                .child("ultimasPostagens");

        receitasRef = firebaseDbRef
                .child("receitas");

    }

    private void configurarAdapterMaisRecyclerView() {

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

            //valueEventListenerRA = receitasRef.addValueEventListener(new ValueEventListener() {
            valueEventListenerRA = ultimasPostagensRef.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                    progressBarReceitas.setVisibility(View.VISIBLE);

                    for (DataSnapshot dadosReceita: dataSnapshot.getChildren()){

                        Receitas receitas = dadosReceita.getValue(Receitas.class);
                        listaReceitas.add(receitas);

                    }


                    //METODO UTILIZADO QUANDO AS RECEITAS ERAM RECUPERADAS DO NÓ RECEITAS
                    /*for (DataSnapshot ds: dataSnapshot.getChildren()){
                        for (DataSnapshot dadosReceita: ds.getChildren()){

                                Receitas receitas = dadosReceita.getValue(Receitas.class);

                                listaReceitas.add(receitas);

                                //copiar e colar as receitas do nó receitas para o nó ultimas postagens
                                //tree: ultimasPostagens->idReceita(dados Receita)
                                *//*DatabaseReference ultimosPostsRef = firebaseDbRef.child("ultimasPostagens").child(dadosReceita.getKey());
                                ultimosPostsRef.setValue(receitas);*//*

                        }
                    }*/

                    progressBarReceitas.setVisibility(View.GONE);

                    //listaEmOrdemAlfabetica();

                    Collections.reverse(listaReceitas); //reverte a ordem da lista (para que q sempre apareça primeiro a ultima postagem)
                    adapterReceitas.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {   }
            });
    }

    private void listaEmOrdemAlfabetica() { //receitas em ordem alfabetica
        Collections.sort(listaReceitas, new Comparator<Receitas>() {
            @Override
            public int compare(Receitas rec1, Receitas rec2) {
                if (rec1 == null || rec2 == null){
                    return 0;
                }
                if (rec1.getNome() == null || rec2.getNome() == null){
                    return 0;
                }
                return rec1.getNome().compareToIgnoreCase(rec2.getNome()); //ordem crescente por nome da receita
            }
        });
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
