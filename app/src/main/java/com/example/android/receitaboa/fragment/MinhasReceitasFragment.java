package com.example.android.receitaboa.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.receitaboa.R;
import com.example.android.receitaboa.activity.NovaReceitaInfoActivity;
import com.example.android.receitaboa.adapter.MinhasReceitasAdapter;
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

    private RecyclerView recyclerMinhasReceitas;
    private MinhasReceitasAdapter adapterMR;

    //public GridView gridViewMinhasReceitas;
    private ImageView fabMiniChef;

    private String idChefLogado;
    private View emptyFridgeView;

    private DatabaseReference firebaseDbRef;
    private DatabaseReference receitasRef;
    private DatabaseReference receitasChefRef;

    private RecyclerView.LayoutManager layoutManager;

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
        receitasChefRef = receitasRef
                .child(idChefLogado);

        //Inicializar componentes
        emptyFridgeView = view.findViewById(R.id.emptyLayoutFridgeView); //Linear Layout contendo a imagem e as frases da geladeira
        recyclerMinhasReceitas = view.findViewById(R.id.recyclerViewMinhasReceitas);
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
        adapterMR = new MinhasReceitasAdapter(minhaListaReceitas, getActivity() );

        //Configura recyclerview
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerMinhasReceitas.setLayoutManager(layoutManager);
        recyclerMinhasReceitas.setHasFixedSize(true);
        recyclerMinhasReceitas.setAdapter(adapterMR);
        //recyclerMinhasReceitas.setEmptyView(emptyFridgeView); //a imagem da geladeira e os textos abaixo dela só aparecem quando a lista está vazia (PROCURAR UMA ALTERNATIVA)

        //Configura evento de click a lista
        //(A FAZER)

        return view;
    }

    //Recupera as fotos das receitas
    private void recuperarMinhasReceitasFirebaseDb(){

        valueEventListenerMinhasReceitas = receitasChefRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //limpa a lista de receitas para evitar que haja repetição ao mudar de tela
                minhaListaReceitas.clear();

                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Receitas minhasReceitas = ds.getValue(Receitas.class);
                    minhaListaReceitas.add(minhasReceitas);
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
}
