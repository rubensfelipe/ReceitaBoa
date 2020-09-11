package com.rubensvaz.android.receitaboa.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.rubensfelipe.android.receitaboa.R;
import com.rubensvaz.android.receitaboa.adapter.AdapterFeed;
import com.rubensvaz.android.receitaboa.helper.ConfiguracaoFirebase;
import com.rubensvaz.android.receitaboa.helper.UsuarioFirebaseAuth;
import com.rubensvaz.android.receitaboa.model.Feed;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.rubensvaz.android.receitaboa.model.Postagem;

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
    private DatabaseReference postagensRef;
    private AdapterFeed adapterFeed;

    private ProgressBar progressBarFeed;

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

        //configurarEventoCliqueReceita(); (agora evento de clique é tratado no adapter)

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        listandoFeed();
    }

    private void inicializarComponentes(View visto) {
        progressBarFeed = visto.findViewById(R.id.progressBarFeed);
        recyclerFeed = visto.findViewById(R.id.recyclerFeed);
    }

    private void configuracoesIniciais() {

        idUsuarioLogado = UsuarioFirebaseAuth.getIdentificadorChefAuth();
        DatabaseReference firebase = ConfiguracaoFirebase.getFirebaseDatabase();

        feedRef = firebase
                .child("feed")
                .child(idUsuarioLogado);

        postagensRef = firebase
                .child("postagens")
                .child(idUsuarioLogado);

    }

    private void configurarAdapterMaisRecyclerView() {
        adapterFeed = new AdapterFeed(listaFeed, getActivity());
        recyclerFeed.setHasFixedSize(true);
        recyclerFeed.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerFeed.setAdapter(adapterFeed);
    }

    /*
    private void configurarEventoCliqueReceita() {


        recyclerFeed.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerFeed,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                List<Feed> listaFeedAtualizada = adapterFeed.getListaFeed(); //permite que a posição na lista da receitas não se altere msm qdo houve uma busca

                                Feed postagemSelecionada = listaFeedAtualizada.get(position); //recupera qual item foi clicado de acordo com a posição na lista no momento do click

                                Intent i = new Intent(getActivity(), VisualizarReceitaActivity.class);
                                i.putExtra("dadosReceitaFeedClicada", postagemSelecionada);
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
    */

    //listando Posts do Usuário Logado no Feed dele (necessário para visualizar os comentários nas receitas dele)
    /*private void recuperandoIdPostagensUsuarioLogado(){

        valueEventListenerMinhasPostagens = postagensRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                progressBarFeed.setVisibility(View.VISIBLE);

                for (DataSnapshot ds : snapshot.getChildren()){

                    listaIdPostagens.add(ds.getKey());

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }*/

    private void listandoFeed() {

        //limpar lista (evita repetição ao sair da tela de feed)
        listaFeed.clear();

        valueEventListenerFeed = feedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                progressBarFeed.setVisibility(View.VISIBLE);

                for (DataSnapshot ds : dataSnapshot.getChildren()){

                    listaFeed.add(ds.getValue(Feed.class)); //recuperado os dados de publicacao dos nossos seguidores que estão gravados na tree do feed no FirebaseDatabase

                }

                progressBarFeed.setVisibility(View.GONE);

                Collections.reverse(listaFeed); //reverte a ordem da lista (para que q sempre apareça primeiro a ultima postagem)
                adapterFeed.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {       }
        });

    }

    /*
    PESQUISA das postagens dos Amigos no Feed
     */
    public void pesquisarFeed(String feedNaPesquisa) { //chamada na MainActivity

        List<Feed> listaFeedBusca = new ArrayList<>();

        for (Feed post: listaFeed){
            String nomeChef  = post.getNomeChef().toLowerCase();
            //String nomeReceita  = post.getNomeReceita().toLowerCase(); //recupera o nome das minhas receitas na minha lista de receitas
            if (nomeChef.contains(feedNaPesquisa)){
                listaFeedBusca.add(post);
            }
        }
        configuracoesAdaptador(listaFeedBusca);
    }

    public void recarregarFeed() {
        configuracoesAdaptador(listaFeed);
    }

    private void configuracoesAdaptador(List<Feed> listaEscolhida) {
        adapterFeed = new AdapterFeed(listaEscolhida, getActivity());
        recyclerFeed.setAdapter(adapterFeed);
        adapterFeed.notifyDataSetChanged();
    }


    @Override
    public void onStop() {
        super.onStop();
        feedRef.removeEventListener(valueEventListenerFeed);
        //((AppCompatActivity)getActivity()).getSupportActionBar().show();
    }

}
