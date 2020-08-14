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
import android.widget.ProgressBar;

import com.example.android.receitaboa.R;
import com.example.android.receitaboa.activity.NovaReceitaInfoActivity;
import com.example.android.receitaboa.activity.VisualizarReceitaActivity;
import com.example.android.receitaboa.adapter.ReceitasAdapter;
import com.example.android.receitaboa.helper.ConfiguracaoFirebase;
import com.example.android.receitaboa.helper.RecyclerItemClickListener;
import com.example.android.receitaboa.helper.UsuarioFirebaseAuth;
import com.example.android.receitaboa.model.Receitas;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MinhasReceitasFragment extends Fragment {

    private static final long CODIGO_ONE_TIME = 101;
    private boolean botaoApertado = false;

    private RecyclerView recyclerReceitas;
    private ReceitasAdapter adapterMR;

    private ImageView fabMiniChef;
    private View emptyFridgeView;
    private ProgressBar progressBarCard;

    private String idChefLogado;
    private DatabaseReference firebaseDbRef;
    private DatabaseReference receitasRef;
    private DatabaseReference receitasChefRef;

    private RecyclerView.LayoutManager layoutManager;

    private List<Receitas> listaMR = new ArrayList<>();

    private ValueEventListener valueEventListenerMR;

    public boolean receitaClicada = false;

    ShowcaseView.Builder showCaseView;

    public MinhasReceitasFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_receitas, container, false);

        inicializarComponentes(view);

        configuracoesIniciais();

        configurarAdapterMaisRecyclerView();

        configurarEventoCliqueMinhaReceita();

        mostrarHolofoteFAB(view);

        return view;
    }

    @Override
    public void onStart() {
        recuperarMinhasReceitasFirebaseDb();
        super.onStart();
    }

    private void inicializarComponentes(View vista) {
        progressBarCard = vista.findViewById(R.id.progressBarCard);
        emptyFridgeView = vista.findViewById(R.id.emptyLayoutFridgeView); //Linear Layout contendo a imagem e as frases da geladeira
        recyclerReceitas = vista.findViewById(R.id.recyclerViewReceitas);
        fabMiniChef = vista.findViewById(R.id.fab);
    }

    private void configuracoesIniciais() {
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getActivity()));

        idChefLogado = UsuarioFirebaseAuth.getIdentificadorChefAuth(); //id do chef logado (emailAuth convertido em base64)

        firebaseDbRef = ConfiguracaoFirebase.getFirebaseDatabase();
        receitasRef = firebaseDbRef.child("receitas");

        //Configurar referência receitas do chef (pelo nó sabemos de qual usuário é a lista de receitas que devemos mostrar na tela)
        receitasChefRef = receitasRef
                .child(idChefLogado);

        configurarFloatActionButton();

    }

    //Abre o editor de uma nova receita
    private void configurarFloatActionButton() {
        fabMiniChef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent adicionarReceita = new Intent(getActivity(), NovaReceitaInfoActivity.class);
                startActivity(adicionarReceita);
            }
        });
    }

    private void configurarAdapterMaisRecyclerView() {
        adapterMR = new ReceitasAdapter(listaMR, getActivity() );

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerReceitas.setLayoutManager(layoutManager);
        recyclerReceitas.setHasFixedSize(true);
        recyclerReceitas.setAdapter(adapterMR);
    }

    //realça o botão de adicionar uma receita para que o usuário saiba onde clicar ao entrar pela primeira vez no app
    public void mostrarHolofoteFAB(View vista){

        showCaseView = new ShowcaseView.Builder(getActivity())
                .withMaterialShowcase()
                .setTarget(new ViewTarget(vista.findViewById(R.id.fab)))
                .setContentTitle("Adicionando uma receita")
                .setContentText("Clique no chef de cozinha para começar adicionando a sua receita")
                .singleShot(CODIGO_ONE_TIME) //só aparece quando o usuario instala o app e loga na conta (pelo primeiro cadastro ou login)
                .setStyle(R.style.ShowCaseViewStyle);

        showCaseView.build();
    }

    private void configurarEventoCliqueMinhaReceita() {

        recyclerReceitas.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerReceitas,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                receitaClicada = true;

                                List<Receitas> listaMRAtualizada = adapterMR.getListaReceitas(); //permite que a posição na lista da receitas não se altere msm qdo houve uma busca

                                Receitas minhaReceitaSelecionada = listaMRAtualizada.get(position); //recupera qual item foi clicado de acordo com a posição na lista no momento do click

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

    }

    //Recupera os dados das minhas receitas
    public void recuperarMinhasReceitasFirebaseDb(){

        listaMR.clear();

        valueEventListenerMR = receitasChefRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                progressBarCard.setVisibility(View.VISIBLE);

                //limpa a lista de receitas para evitar que haja repetição ao mudar de tela
                listaMR.clear();

                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    Receitas minhasReceitas = ds.getValue(Receitas.class);

                    listaMR.add(minhasReceitas);
                }

                //se o usuário já tiver adicionado ao menos uma receita na sua lista, o homem da geladeira desaparece
                if(listaMR.size() != 0){
                    emptyFridgeView.setVisibility(View.GONE);
                }

                progressBarCard.setVisibility(View.GONE);

                listaEmOrdemAlfabetica();
                adapterMR.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {  }
        });

    }

    private void listaEmOrdemAlfabetica() {
        Collections.sort(listaMR, new Comparator<Receitas>() {
            @Override
            public int compare(Receitas rec1, Receitas rec2) {
                return rec1.getNome().compareToIgnoreCase(rec2.getNome()); //ordem crescente por nome da receita
                //return rec2.getNome().compareToIgnoreCase(rec1.getNome()); //ordem descrescente
                //return Integer.valueOf(rec1.getQtdPessoasServidas()).compareTo(Integer.valueOf(rec2.getQtdPessoasServidas()); //orderna em ordem crescente para números inteiros
            }
        });
    }

    /*
    PESQUISA DE RECEITAS ABAIXO
     */
    public void pesquisarMinhasReceitas(String receitaNaPesquisa) { //chamada na MainActivity

        List<Receitas> listaMRBusca = new ArrayList<>();

        for (Receitas receita: listaMR){

            String nomeReceita  = receita.getNome().toLowerCase(); //recupera o nome das minhas receitas na minha lista de receitas
            if (nomeReceita.contains(receitaNaPesquisa)){
                listaMRBusca.add(receita);
            }
        }
        configuracoesAdaptador(listaMRBusca);
    }

    public void recarregarMinhasReceitas() {
        configuracoesAdaptador(listaMR);
    }

    private void configuracoesAdaptador(List<Receitas> listaEscolhida) {
        adapterMR = new ReceitasAdapter(listaEscolhida, getActivity());
        recyclerReceitas.setAdapter(adapterMR);
        adapterMR.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        receitasChefRef.removeEventListener(valueEventListenerMR);
    }

}
