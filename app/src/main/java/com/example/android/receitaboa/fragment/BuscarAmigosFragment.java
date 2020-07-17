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
import com.example.android.receitaboa.activity.PerfilAmigoActivity;
import com.example.android.receitaboa.adapter.AmigosAdapter;
import com.example.android.receitaboa.helper.ConfiguracaoFirebase;
import com.example.android.receitaboa.helper.RecyclerItemClickListener;
import com.example.android.receitaboa.helper.UsuarioFirebaseAuth;
import com.example.android.receitaboa.model.Chef;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class BuscarAmigosFragment extends Fragment {

    private RecyclerView recyclerListaUsuarios;
    private AmigosAdapter adapter;
    private ArrayList<Chef> listaUsuarios = new ArrayList<>(); //instanciando a Lista de Amigos que serão recuperados pela Classe Chef
    private DatabaseReference chefsRef;
    private ValueEventListener valueEventListenerAmigos;
    private FirebaseUser chefAtualAuth;
    private ProgressBar progressBar;


    public BuscarAmigosFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pesquisa_amigos, container, false);

        inicializarComponentes(view);

        configuracoesIniciais();

        configuracoesRecyclerMaisAdapter();

        configuracaoEventoCliqueAmigos();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarUsuariosFirebaseDb();
    }

    private void inicializarComponentes(View vista) {
        progressBar = vista.findViewById(R.id.progressBarAmigos);
        recyclerListaUsuarios = vista.findViewById(R.id.recyclerViewListaAmigos);
    }

    private void configuracoesIniciais() {
        chefsRef = ConfiguracaoFirebase.getFirebaseDatabase().child("chefs");
        chefAtualAuth = UsuarioFirebaseAuth.getChefAtualAuth(); //recupera os dados do chef que está logado
    }

    private void configuracoesRecyclerMaisAdapter() {
        //Configurar adapter
        adapter = new AmigosAdapter(listaUsuarios, getActivity()); //AmigosAdapter(Lista [tipo: ArrayList<>], contexto) //PRIMEIRO: Criar construtor na classe AmigosAdapter, AmigosAdapter(Lista [tipo: List<>], contexto)

        //Configurar recyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerListaUsuarios.setLayoutManager(layoutManager);
        recyclerListaUsuarios.setHasFixedSize(true);
        recyclerListaUsuarios.setAdapter(adapter);
    }

    private void configuracaoEventoCliqueAmigos() {

        //Configurar evento de clique no recyclerView (na lista de contatos)
        recyclerListaUsuarios.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerListaUsuarios,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                //recupera a lista que está sendo utilizada pelo adapter, assim quando um amigo é pesquisado, esse amigo não mudará a sua posição na lista completa
                                List<Chef> listaAmigosAtualizada = adapter.getListAmigos();

                                Chef chefAmigoSelecionado = listaAmigosAtualizada.get(position);

                                Intent i = new Intent(getActivity(), PerfilAmigoActivity.class);
                                i.putExtra("chefSelecionado", chefAmigoSelecionado);
                                startActivity(i);

                            }

                            @Override
                            public void onLongItemClick(View view, int position) {  }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {    }
                        }
                )
        );

    }

    //recupera os dados dos amigos a partir do FirebaseDatabase e adiciona a lista de Amigos
    public void recuperarUsuariosFirebaseDb(){

        limparListaAmigos();

        valueEventListenerAmigos = chefsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //limpar lista para evitar repetição de amigos
                limparListaAmigos();

                for (DataSnapshot dados : dataSnapshot.getChildren()){

                    Chef amigo = dados.getValue(Chef.class); //recupera os dados salvo nos nós do FirebaseDatabase

                    if (amigo != null){
                        progressBar.setVisibility(View.GONE);
                    }

                    String emailChefLogado = chefAtualAuth.getEmail();
                    if ( !emailChefLogado.equals( amigo.getEmail() ) ){ //verifica se o amigo adicionado na lista não é o próprio usuario logado
                        listaUsuarios.add(amigo); //adiciona um novo amigo (os dados dos amigos do chef) a lista de amigos
                    }
                }
                adapter.notifyDataSetChanged(); //a lista de amigos só é atualizada quando há uma alteração na lista de amigos (só adicionaremos mais amigos a listaDeAmigos, quando houverem novos amigos)
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {    }
        });

    }

    private void limparListaAmigos() {
        listaUsuarios.clear();
    } //evitar repetir chefs na lista


    /*
    TRECHO DE PESQUISA DE AMIGOS (utilizado na MainActivity)
     */
    public void pesquisarAmigos(String nomeAmigo) {

        List<Chef> listaChefsBusca = new ArrayList<>();

        for (Chef chefAmigo : listaUsuarios){

            String nome = chefAmigo.getNome().toLowerCase(); //recupera os nomes e grava eles em lowerCase
            if (nome.contains(nomeAmigo)){ //se o começo do nome for correspondente a um amigo da lista
                listaChefsBusca.add(chefAmigo); //adiciona a lista de busca
            }
        }
        configuracoesAdapter(listaChefsBusca);
    }

    //Recarrega a lista de amigos completa ao fechar a caixa de pesquisa de usuários
    public void recarregarListaUsuarios() { //(localizado no MainActivity)
        configuracoesAdapter(listaUsuarios);
    }

    //seta a lista em um adapter
    private void configuracoesAdapter(List<Chef> listaEscolhida) {
        adapter = new AmigosAdapter(listaEscolhida, getActivity());
        recyclerListaUsuarios.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        chefsRef.removeEventListener(valueEventListenerAmigos); //fecha o listener quando fechar a activity de contatos
    }

}
