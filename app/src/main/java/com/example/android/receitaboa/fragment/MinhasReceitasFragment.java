package com.example.android.receitaboa.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.android.receitaboa.R;
import com.example.android.receitaboa.activity.NovaReceitaInfoActivity;
import com.example.android.receitaboa.adapter.MinhasReceitasAdapterGrid;
import com.example.android.receitaboa.helper.ConfiguracaoFirebase;
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

    public GridView gridViewMinhasReceitas;
    private ImageView fabMiniChef;

    private String idChefLogado;
    private MinhasReceitasAdapterGrid adapterMinhasReceitas;
    private View emptyFridgeView;

    private DatabaseReference firebaseDbRef;
    private DatabaseReference receitasRef;
    private DatabaseReference receitasChefRef;

    private List<Receitas> minhaListaReceitas = new ArrayList<>();

    private ValueEventListener valueEventListenerMinhasReceitas;

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
        receitasChefRef = receitasRef.child(idChefLogado);

        //Inicializar componentes
        emptyFridgeView = view.findViewById(R.id.emptyLayoutFridgeView); //Linear Layout contendo a imagem e as frases da geladeira
        gridViewMinhasReceitas = view.findViewById(R.id.gridViewMinhasReceitas);
        fabMiniChef = view.findViewById(R.id.fab);

        //Configurar adapterGrid
        adapterMinhasReceitas = new MinhasReceitasAdapterGrid(getActivity(), R.layout.adapter_minha_receita, minhaListaReceitas);
        gridViewMinhasReceitas.setAdapter(adapterMinhasReceitas);
        gridViewMinhasReceitas.setEmptyView(emptyFridgeView); //a imagem da geladeira e os textos abaixo dela só aparecem quando a lista está vazia

        //Abre o editor de uma nova receita
        fabMiniChef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent adicionarReceita = new Intent(getActivity(), NovaReceitaInfoActivity.class);
                startActivity(adicionarReceita);
            }
        });

        return view;
    }


    //Recupera as fotos das receitas
    private void recuperarMinhasReceitasFirebaseDb(){

        valueEventListenerMinhasReceitas = receitasChefRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Configurar o tamanho do grid

                int tamanhoGrid = getResources().getDisplayMetrics().widthPixels;
                int tamanhoImagem = tamanhoGrid / 2; //dividi pelo numero de colunas
                gridViewMinhasReceitas.setColumnWidth( tamanhoImagem );


                //limpa a lista de receitas para evitar que haja repetição ao mudar de tela
                minhaListaReceitas.clear();

                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    minhaListaReceitas.add(ds.getValue(Receitas.class));

                }
                adapterMinhasReceitas.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarMinhasReceitasFirebaseDb();
    }

    @Override
    public void onStop() {
        super.onStop();
        receitasChefRef.removeEventListener(valueEventListenerMinhasReceitas);
    }
}
