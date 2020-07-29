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
import android.widget.GridView;

import com.example.android.receitaboa.R;
import com.example.android.receitaboa.activity.PerfilAmigoActivity;
import com.example.android.receitaboa.adapter.AmigosAdapter;
import com.example.android.receitaboa.adapter.BuscaAmigosAdapter;
import com.example.android.receitaboa.helper.ConfiguracaoFirebase;
import com.example.android.receitaboa.helper.RecyclerItemClickListener;
import com.example.android.receitaboa.helper.UsuarioFirebaseAuth;
import com.example.android.receitaboa.model.Chef;
import com.example.android.receitaboa.model.Receitas;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.List;

public class AmigosFragment extends Fragment {

    private GridView gridViewAmigos;

    private AmigosAdapter amigosAdapter;
    private ArrayList<Chef> listaAmigos = new ArrayList<>(); //instanciando a Lista de Amigos que serão recuperados pela Classe Chef

    private ValueEventListener valueEventListenerAmigos;

    private String idChefLogado;
    private DatabaseReference amigosChefRef;
    private DatabaseReference amigosRef;
    private FirebaseUser chefAtualAuth;

    public AmigosFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getActivity()));
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_amigos, container, false);

        inicializarComponentes(view);

        configuracoesIniciais();

        configuracaoEventoCliqueAmigos();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarAmigos();
    }

    private void inicializarComponentes(View vista) {
        gridViewAmigos = vista.findViewById(R.id.gridViewAmigos);
    }

    private void configuracoesIniciais() {
        idChefLogado = UsuarioFirebaseAuth.getIdentificadorChefAuth();
        chefAtualAuth = UsuarioFirebaseAuth.getChefAtualAuth(); //recupera os dados do chef que está logado

        amigosRef = ConfiguracaoFirebase.getFirebaseDatabase().child("amigos");
        amigosChefRef = amigosRef.child(idChefLogado);
    }

    private void configuracaoEventoCliqueAmigos() {

        gridViewAmigos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                Chef amigoSelecionado = listaAmigos.get(position);

                Intent i = new Intent(getActivity(), PerfilAmigoActivity.class);
                i.putExtra("chefSelecionado", amigoSelecionado); //envia as informações da receita
                startActivity(i);

            }
        });

    }

    private void recuperarAmigos(){
        amigosChefRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                limparListaAmigos();

                configurarTamanhoGrid();

                for (DataSnapshot dados: dataSnapshot.getChildren()){

                    Chef amigo = dados.getValue(Chef.class);

                    listaAmigos.add(amigo);

                }
                configurarAdapter(listaAmigos);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void limparListaAmigos() {
        listaAmigos.clear();
    } //evitar repetir chefs na lista



    //configura o tamanho das imagens e das grades
    private void configurarTamanhoGrid() {

        int tamanhoGrid = getResources().getDisplayMetrics().widthPixels; //recupera a largura da gridView para cada aparelho de usuario
        int tamanhoImagem = tamanhoGrid/3; //pois são 3 colunas
        gridViewAmigos.setColumnWidth(tamanhoImagem); //seta o tamanho de cada view da grid

    }

    private void configurarAdapter(List<Chef> lista) {
        amigosAdapter = new AmigosAdapter(getContext(), R.layout.grid_postagem, lista);
        gridViewAmigos.setAdapter(amigosAdapter);
    }

    /*
    TRECHO DE PESQUISA DE AMIGOS (utilizado na MainActivity)
     */

    /*
    public void pesquisarAmigos(String nomeAmigo) {

        List<Chef> listaChefsBusca = new ArrayList<>();

        for (Chef chefAmigo : listaAmigos){

            String nome = chefAmigo.getNome().toLowerCase(); //recupera os nomes e grava eles em lowerCase
            if (nome.contains(nomeAmigo)){ //se o começo do nome for correspondente a um amigo da lista
                listaChefsBusca.add(chefAmigo); //adiciona a lista de busca
            }
        }
        configuracoesAdapter(listaChefsBusca);
    }

    //Recarrega a lista de amigos completa ao fechar a caixa de pesquisa de usuários
    public void recarregarListaUsuarios() { //(localizado no MainActivity)
        configuracoesAdapter(listaAmigos);
    }

    //seta a lista em um adapter
    private void configuracoesAdapter(List<Chef> listaEscolhida) {
        amigosAdapter = new AmigosAdapter(listaEscolhida, ,getActivity());
        recyclerListaAmigos.setAdapter(amigosAdapter);
        amigosAdapter.notifyDataSetChanged();
    }

     */

    @Override
    public void onStop() {
        super.onStop();
        //amigosChefRef.removeEventListener(valueEventListenerAmigos); //fecha o listener quando fechar a activity de contatos
    }
}