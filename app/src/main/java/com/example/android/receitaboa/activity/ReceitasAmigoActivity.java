package com.example.android.receitaboa.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.receitaboa.R;
import com.example.android.receitaboa.adapter.PerfilAmigoAdapterGrid;
import com.example.android.receitaboa.helper.Base64Custom;
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
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReceitasAmigoActivity extends AppCompatActivity {

    private Button buttonAcaoPerfil;
    private CircleImageView imagePerfil;
    private TextView textPostagens, textSeguidores, textSeguindo;
    private GridView gridViewPerfil;

    private String idAmigoSelecionado;
    private String idChefLogado;

    private DatabaseReference firebaseRef;
    private DatabaseReference chefsRef;
    private DatabaseReference chefLogadoRef;
    private DatabaseReference amigoRef;
    private DatabaseReference seguidoresRef;
    private DatabaseReference amigosRef;
    private DatabaseReference receitasAmigoRef;

    private List<Receitas> listaReceitasAmigo = new ArrayList<>();

    private PerfilAmigoAdapterGrid perfilAmigoAdapter;
    private Chef amigoSelecionado;
    private Chef chefLogado;
    private String emailAmigoSelecionado;

    private ValueEventListener valueEventListenerPerfilAmigo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receitas_amigo);

        inicializarComponentes();

        configuracoesIniciais();

        recuperarExtras();

    }

    private void recuperarExtras() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){

            recuperarIdAmigoSelecionado(bundle);

            configurarReceitasAmigoRef(recuperarIdAmigoSelecionado(bundle));

            recuperarFotoAmigo();

            //Carrega as fotos mais rápido
            inicializarImageLoader();

            carregarReceitasAmigo();

            //Abre a foto da receita que foi clicada no perfil usuario
            eventoClickFotoReceita();

        }
    }

    private void eventoClickFotoReceita() {

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

    private String recuperarIdAmigoSelecionado(Bundle bund) {
        amigoSelecionado = (Chef) bund.getSerializable("chefSelecionado");

        //Recuperar id do Chef Amigo selecionado em pesquisaAmigos
        emailAmigoSelecionado = amigoSelecionado.getEmail();
        idAmigoSelecionado = Base64Custom.codificarBase64(emailAmigoSelecionado);

        return idAmigoSelecionado;

    }

    private void configurarReceitasAmigoRef(String idAmigoClicado) {
        receitasAmigoRef = ConfiguracaoFirebase.getFirebaseDatabase()
                .child("receitas")
                .child(idAmigoClicado);
    }

    private void recuperarFotoAmigo() {
        String caminhoFoto = amigoSelecionado.getUrlFotoChef();
        if(caminhoFoto != null){
            Uri url = Uri.parse(caminhoFoto); //String->Uri
            Glide.with(ReceitasAmigoActivity.this)
                    .load(url) //carrega a foto do FirebaseStorage através do caminho da foto
                    .into(imagePerfil); //carrega a foto no perfil do usuário no app
        }
    }

    private void configuracoesIniciais() {
        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        chefsRef = firebaseRef.child("chefs"); //cria o nó usuários (se não existir)
        seguidoresRef = firebaseRef.child("seguidores"); //cria o nó seguidores (se não existir)
        amigosRef = firebaseRef.child("amigos");
        idChefLogado = UsuarioFirebaseAuth.getIdentificadorChefAuth(); //id do chef logado (emailAuth convertido em base64)
        chefLogadoRef = chefsRef.child(idChefLogado);
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
        receitasAmigoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                configurarTamanhoGrid();

                for (DataSnapshot ds: dataSnapshot.getChildren()){


                    Receitas receita = ds.getValue(Receitas.class);

                    listaReceitasAmigo.add(receita);
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
        buttonAcaoPerfil.setText("Carregando"); //muda o texto do botão que inicialmente era editar perfil no fragment_pesquisa
    }

    private void recuperarDadosChefLogado() {

        chefLogadoRef = chefsRef.child(idChefLogado);
        chefLogadoRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        chefLogado = dataSnapshot.getValue(Chef.class);

                        //Verifica se usuário já está seguindo o amigo selecionado
                        verficaSeSegueAmigo();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );
    }

    private void verficaSeSegueAmigo() {

        DatabaseReference seguidorRef = seguidoresRef
                .child(idAmigoSelecionado)
                .child(idChefLogado);

        seguidorRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){ //se houver dados dentro do nó id do usuárioSelecionado (então quer dizer que o usuário já está seguindo seu amigo)
                            Log.i("dadosUsuário","Seguindo");
                            habilitarBotaoSeguir(true);
                        }else {
                            Log.i("dadosUsuário","Seguir");
                            habilitarBotaoSeguir(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );

    }

    private void habilitarBotaoSeguir(boolean seguindoAmigo) {

        if (seguindoAmigo){
            buttonAcaoPerfil.setText("Seguindo");
        }else {
            buttonAcaoPerfil.setText("Seguir");

            //Adicionar evento de seguir amigo
            buttonAcaoPerfil.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    salvarSeguidor(chefLogado, amigoSelecionado);
                    salvarAmigo();
                }
            });
        }

    }

    /* Configuração (criação) do nó no FirebaseDatabase
        seguidores->id_do_usario_qualquer->id_usuario_logado(comecou a seguir um usuario qualquer)->dados do usuario logado (tree que mostra os meus seguidores)
     */
    private void salvarSeguidor(Chef chefLogado, Chef amigo) {

        HashMap<String, Object> dadosChefLogado = new HashMap<>();
        dadosChefLogado.put("nome", chefLogado.getNome());
        dadosChefLogado.put("caminhoFoto", chefLogado.getUrlFotoChef());

        DatabaseReference seguidorRef = seguidoresRef
                .child(idAmigoSelecionado)
                .child(idChefLogado);
        seguidorRef.setValue(dadosChefLogado); //atualiza a contagem de seguidores do usuário logado

        //Alterar botão para seguindo
        buttonAcaoPerfil.setText("Seguindo");
        buttonAcaoPerfil.setOnClickListener(null); //bloqueia o click no botão acao após o texto do botão setar para seguindo

        //Incrementar o contador Seguindo do chef logado
        int seguindo = chefLogado.getSeguindo() + 1;
        HashMap<String, Object> dadosSeguindo = new HashMap<>();
        dadosSeguindo.put("seguindo", seguindo);

        DatabaseReference chefSeguindo = chefsRef
                .child(idChefLogado);
        chefSeguindo.updateChildren(dadosSeguindo); //atualiza a contagem de seguindo do usuário logado

        //Incrementar o contador Seguidores do seu amigo
        int seguidores = amigo.getSeguidores() + 1;
        HashMap<String, Object> dadosSeguidores = new HashMap<>();
        dadosSeguidores.put("seguidores", seguidores); //atualiza a contagem de seguidores do perfil do amigo


        DatabaseReference chefSeguidores = chefsRef
                .child(idAmigoSelecionado);
        chefSeguidores.updateChildren(dadosSeguidores); //atualiza a contagem de seguindo no perfil do amigo

    }

    private void salvarAmigo(){

        HashMap<String, Object> dadosAmigoLogado = new HashMap<>();
        dadosAmigoLogado.put("nome", amigoSelecionado.getNome());
        dadosAmigoLogado.put("caminhoFoto", amigoSelecionado.getUrlFotoChef());

        DatabaseReference amigoRef = amigosRef
                .child(idChefLogado)
                .child(idAmigoSelecionado);
        amigoRef.setValue(dadosAmigoLogado);

    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarDadosChefLogado();

        recuperarDadosSeguidoresAmigo();
    }

    private void recuperarDadosSeguidoresAmigo() {

        amigoRef = chefsRef.child(idAmigoSelecionado);
        valueEventListenerPerfilAmigo = amigoRef.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        Chef amigo = dataSnapshot.getValue(Chef.class);

                        String postagens = String.valueOf(amigo.getPostagens());
                        String seguindo = String.valueOf(amigo.getSeguindo());
                        String seguidores = String.valueOf(amigo.getSeguidores());

                        textPostagens.setText(postagens);
                        textSeguidores.setText(seguidores);
                        textSeguindo.setText(seguindo);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        amigoRef.removeEventListener(valueEventListenerPerfilAmigo);
    }
}
