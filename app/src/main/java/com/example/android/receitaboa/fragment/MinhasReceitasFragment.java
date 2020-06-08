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
import android.widget.ImageView;

import com.example.android.receitaboa.R;
import com.example.android.receitaboa.activity.NovaReceitaInfoActivity;
import com.example.android.receitaboa.activity.VisualizarReceitaActivity;
import com.example.android.receitaboa.adapter.MinhasReceitasAdapter;
import com.example.android.receitaboa.helper.ConfiguracaoFirebase;
import com.example.android.receitaboa.helper.RecyclerItemClickListener;
import com.example.android.receitaboa.helper.UsuarioFirebaseAuth;
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
public class MinhasReceitasFragment extends Fragment {

    private RecyclerView recyclerViewMinhasReceitas;
    private MinhasReceitasAdapter adapterMR;

    private ImageView fabMiniChef;

    private String idChefLogado;
    private View emptyFridgeView;

    private DatabaseReference firebaseDbRef;
    private DatabaseReference receitasRef;
    private DatabaseReference receitasChefRef;

    private RecyclerView.LayoutManager layoutManager;

    private List<Receitas> listaMinhasReceitas = new ArrayList<>();

    private ValueEventListener valueEventListenerMinhasReceitas;

    public boolean receitaClicada = false;

    public MinhasReceitasFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_minhas_receitas, container, false);

        //Configurações iniciais
        idChefLogado = UsuarioFirebaseAuth.getIdentificadorChefAuth(); //id do chef logado (emailAuth convertido em base64)
        firebaseDbRef = ConfiguracaoFirebase.getFirebaseDatabase();
        receitasRef = firebaseDbRef.child("receitas");

        //Configurar referência receitas do chef (pelo nó sabemos de qual usuário é a lista de receitas que devemos mostrar na tela)
        receitasChefRef = receitasRef
                .child(idChefLogado);

        //Inicializar componentes
        emptyFridgeView = view.findViewById(R.id.emptyLayoutFridgeView); //Linear Layout contendo a imagem e as frases da geladeira
        recyclerViewMinhasReceitas = view.findViewById(R.id.recyclerViewMinhasReceitas);
        fabMiniChef = view.findViewById(R.id.fab);

        //Abre o editor de uma nova receita
        fabMiniChef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent adicionarReceita = new Intent(getActivity(), NovaReceitaInfoActivity.class);
                startActivity(adicionarReceita);
            }
        });

        //Configura adapter
        adapterMR = new MinhasReceitasAdapter(listaMinhasReceitas, getActivity() );

        //Configura recyclerview
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewMinhasReceitas.setLayoutManager(layoutManager);
        recyclerViewMinhasReceitas.setHasFixedSize(true);
        recyclerViewMinhasReceitas.setAdapter(adapterMR);

        //Configura evento de click a lista
        recyclerViewMinhasReceitas.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerViewMinhasReceitas,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                receitaClicada = true;

                                List<Receitas> listaMinhasReceitasAtualizada = adapterMR.getListaMinhasReceitas(); //permite que a posição na lista da receitas não se altere msm qdo houve uma busca

                                Receitas minhaReceitaSelecionada = listaMinhasReceitasAtualizada.get(position); //recupera qual item foi clicado de acordo com a posição na lista no momento do click

                                Intent i = new Intent(getActivity(), VisualizarReceitaActivity.class);
                                i.putExtra("dadosMinhaReceitaClicada", minhaReceitaSelecionada);
                                startActivity(i);

                            }
                            @Override
                            public void onLongItemClick(View view, int position) { }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {   }
                        }
                )
        );

        return view;
    }

    //Recupera os dados das minhas receitas
    public void recuperarMinhasReceitasFirebaseDb(){

        listaMinhasReceitas.clear();

        valueEventListenerMinhasReceitas = receitasChefRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //limpa a lista de receitas para evitar que haja repetição ao mudar de tela
                listaMinhasReceitas.clear();

                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Receitas minhasReceitas = ds.getValue(Receitas.class);

                    //se o usuário já tiver adicionado ao menos uma receita na sua lista, o homem da geladeira desaparece
                    if(minhasReceitas != null){
                        emptyFridgeView.setVisibility(View.GONE);
                    }
                    listaMinhasReceitas.add(minhasReceitas);
                }
                adapterMR.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStart() {
        recuperarMinhasReceitasFirebaseDb();
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        receitasChefRef.removeEventListener(valueEventListenerMinhasReceitas);
    }

    public void pesquisarMinhasReceitas(String textoBuscaMR) {

        List<Receitas> listaMinhasReceitasBusca = new ArrayList<>();

        for (Receitas receita: listaMinhasReceitas){

            String nomeMinhaReceita  = receita.getNome().toLowerCase();
            if (nomeMinhaReceita.contains(textoBuscaMR)){
                listaMinhasReceitasBusca.add(receita);
            }
        }
        configuracoesAdapter(listaMinhasReceitasBusca);
    }


    public void recarregarMinhasReceitas() {
        configuracoesAdapter(listaMinhasReceitas);
    }



    private void configuracoesAdapter(List<Receitas> listas) {
        adapterMR = new MinhasReceitasAdapter(listas, getActivity());
        recyclerViewMinhasReceitas.setAdapter(adapterMR);
        adapterMR.notifyDataSetChanged();
    }

}
