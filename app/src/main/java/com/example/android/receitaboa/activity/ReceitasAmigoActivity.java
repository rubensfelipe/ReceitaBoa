package com.example.android.receitaboa.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.receitaboa.R;
import com.example.android.receitaboa.adapter.PerfilAmigoAdapterGrid;
import com.example.android.receitaboa.helper.ConfiguracaoFirebase;
import com.example.android.receitaboa.helper.UsuarioFirebaseAuth;
import com.example.android.receitaboa.model.Chef;
import com.example.android.receitaboa.model.Receitas;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReceitasAmigoActivity extends AppCompatActivity {

    private Button buttonAcaoPerfil;
    private CircleImageView imagePerfil;
    private TextView textPostagens, textSeguidores, textSeguindo;
    private GridView gridViewPerfil;

    private String idChefLogado;
    private String idChefAmigoSelecionado;

    private DatabaseReference firebaseRef;
    private DatabaseReference chefsRef;
    private DatabaseReference seguidoresRef;
    private DatabaseReference receitasChefAmigoRef;

    private List<Receitas> listaReceitasAmigo = new ArrayList<>();

    private PerfilAmigoAdapterGrid perfilAmigoAdapter;
    private Chef chefAmigoSelecionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receitas_amigo);

        inicializarComponentes();

        //Configuracoes iniciais
        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        chefsRef = firebaseRef.child("chefs"); //cria o nó usuários (se não existir)
        seguidoresRef = firebaseRef.child("seguidores"); //cria o nó seguidores (se não existir)
        idChefLogado = UsuarioFirebaseAuth.getIdentificadorChefAuth(); //getUid do usuario logado (id do FirebaseUser)

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            chefAmigoSelecionado = (Chef) bundle.getSerializable("chefSelecionado");
            idChefAmigoSelecionado = bundle.getString("idChefSeleciondado");

            //Configurar referência de receitas do chef selecionado
            receitasChefAmigoRef = ConfiguracaoFirebase.getFirebaseDatabase()
                    .child("receitas")
                    .child(idChefAmigoSelecionado);

            //Recuperar a foto do usuário
            String caminhoFoto = chefAmigoSelecionado.getUrlFotoChef();
            if(caminhoFoto != null){
                Uri url = Uri.parse(caminhoFoto); //String->Uri
                Glide.with(ReceitasAmigoActivity.this)
                        .load(url) //carrega a foto do FirebaseStorage através do caminho da foto
                        .into(imagePerfil); //carrega a foto no perfil do usuário no app
            }

            //Carrega as fotos mais rápido
            inicializarImageLoader();

            carregarReceitasAmigo();

            //Abre a foto que foi clicada no perfil usuario
            gridViewPerfil.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                    Receitas receitaAmigoSelecionada = listaReceitasAmigo.get(position);

                    Intent i = new Intent(getApplicationContext(), VisualizarReceitaActivity.class);
                    i.putExtra("dadosReceitaAmigoClicada", receitaAmigoSelecionada); //envia as informações da receita
                    startActivity(i);

                }
            });

        }

    }

    //Instancia a UniversalImagemLoader (biblioteca de carregamento de fotos)
    public void inicializarImageLoader(){

        //Configurando o ImageLoader (tamanhos, salvar imagens publicadas em uma memoria cache para serem carregadas mais rapidas no app)
        ImageLoaderConfiguration config = new ImageLoaderConfiguration
                .Builder(this)
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .build();
        ImageLoader.getInstance().init(config);

    }

    private void carregarReceitasAmigo() {

        //Recuperar as receitas criados pelo amigo
        receitasChefAmigoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                configurarTamanhoGrid();

                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Receitas receita = ds.getValue(Receitas.class);
                    listaReceitasAmigo.add( receita );
                }
                configurarAdapter(listaReceitasAmigo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {  }
        });

    }

    private void configurarAdapter(List<Receitas> lista) {
        perfilAmigoAdapter = new PerfilAmigoAdapterGrid(getApplicationContext(), R.layout.grid_postagem, lista);
        gridViewPerfil.setAdapter(perfilAmigoAdapter);
    }

    //configura o tamanho das imagens e das grades
    private void configurarTamanhoGrid() {

        int tamanhoGrid = getResources().getDisplayMetrics().widthPixels; //recupera a largura da gridView para cada aparelho de usuario
        int tamanhoImagem = tamanhoGrid/3; //pois são 3 colunas
        gridViewPerfil.setColumnWidth(tamanhoImagem); //seta o tamanho de cada view da grid

    }

    private void inicializarComponentes(){
        imagePerfil = findViewById(R.id.imagePerfil);
        gridViewPerfil = findViewById(R.id.gridViewPerfil);
        buttonAcaoPerfil = findViewById(R.id.buttonAcaoPerfil);
        textPostagens = findViewById(R.id.textPostagens);
        textSeguidores = findViewById(R.id.textSeguidores);
        textSeguindo = findViewById(R.id.textSeguindo);
        buttonAcaoPerfil.setText("Receitinhas"); //muda o texto do botão que inicialmente era editar perfil no fragment_pesquisa
    }

}
