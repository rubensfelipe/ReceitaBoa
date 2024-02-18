package com.rubensvaz.android.receitaboa.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.rubensfelipe.android.receitaboa.R;
import com.rubensvaz.android.receitaboa.helper.ConfiguracaoFirebase;
import com.rubensvaz.android.receitaboa.helper.UsuarioFirebaseAuth;
import com.rubensvaz.android.receitaboa.model.Feed;
import com.rubensvaz.android.receitaboa.model.Receitas;
import com.google.firebase.database.DatabaseReference;

public class VisualizarReceitaActivity extends AppCompatActivity {

    private TextView textNomeReceita, textIngredientes, textModoPreparo;
    private ImageView quadroFotoReceita;

    private String nomeReceitaClicada;
    private String nomeReceitaUserClicada;
    private String ingredientesReceitaClicada;
    private String ingredientesReceitaUserClicada;
    private String modoPreparoReceitaClicada;
    private String modoPreparoReceitaUserClicada;
    private String qtdPessoasServidasReceitaClicada;
    private String receitaFoto;
    private String fotoReceitaUser;
    private Button buttonAcaoPerfil;
    private Toolbar toolbar;

    private String idChefLogado;
    private String idReceitaClicada;
    private String idPostagem;
    private String idReceitaUserClicada;
    private String idReceitaFeedClicada;

    private DatabaseReference firebaseDbRef;
    private DatabaseReference receitasRef;
    private DatabaseReference ultimaPostagemRef;
    private DatabaseReference receitasChefRef;

    public static Activity atividadeAberta;

    private Bundle bundle;

    private Receitas minhaReceitaClicada;
    private Receitas receitaUsuarioClicada;
    private Feed receitaFeedClicada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizar_receita);

        inicializarComponentes();

        configuracoesIniciais();

        configurarToolbar();

        recuperarDadosReceitaClicada();

    }

    private void recuperarDadosReceitaClicada() {
        //Recupera os dados da Receita selecionada
        bundle = getIntent().getExtras();
        if (bundle != null){

            if (bundle.containsKey("dadosMinhaReceitaClicada")){

                minhaReceitaClicada = (Receitas) bundle.getSerializable("dadosMinhaReceitaClicada");
                minhaReceitaDados(minhaReceitaClicada);

            } else if(bundle.containsKey("dadosReceitaClicada")){

                receitaUsuarioClicada = (Receitas) bundle.getSerializable("dadosReceitaClicada");
                usuarioReceitaDados(receitaUsuarioClicada);

            }else if (bundle.containsKey("dadosReceitaFeedClicada")){

                receitaFeedClicada = (Feed) bundle.getSerializable("dadosReceitaFeedClicada");
                feedReceitaDados(receitaFeedClicada);

            }
        }
    }

    private void configuracoesIniciais() {
        //referencia que atividadeAberta = essa Activity
        atividadeAberta = this;

        firebaseDbRef = ConfiguracaoFirebase.getFirebaseDatabase();
        receitasRef = firebaseDbRef.child("receitas");
        ultimaPostagemRef = firebaseDbRef.child("ultimasPostagens");
        idChefLogado = UsuarioFirebaseAuth.getIdentificadorChefAuth(); //id do chef logado (emailAuth convertido em base64)

        //Configurar referência receitas do chef logado
        receitasChefRef = receitasRef
                .child(idChefLogado);
    }

    private void configurarToolbar() {
        toolbar.setTitle(R.string.visualizar_receita);
        setSupportActionBar( toolbar );
        //adiciona o botão voltar (na barra superior) para MainActivity (padrão) (PARTE 1)
        //OBS: deve-se adicionar android:parentActivityName=".activity.MainActivity" no Android Manifest na parte do EditarPerfilActivity, botão voltar retorna a parentActivity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp); //customiza o botão voltar para o ícone q vc desejar
    }

    private void minhaReceitaDados(Receitas receitaSelecionada) {

        //recupera a idReceita que foi selecionada na lista
        idReceitaClicada = receitaSelecionada.getIdReceita();

        nomeReceitaClicada = receitaSelecionada.getNome();
        textNomeReceita.setText(nomeReceitaClicada);

        ingredientesReceitaClicada = receitaSelecionada.getIngredientes();
        textIngredientes.setText(ingredientesReceitaClicada);

        modoPreparoReceitaClicada = receitaSelecionada.getModoPreparo();
        textModoPreparo.setText(modoPreparoReceitaClicada);

        qtdPessoasServidasReceitaClicada = receitaSelecionada.getQtdPessoasServidas();

        receitaFoto = receitaSelecionada.getUrlFotoReceita();
        if (receitaFoto != null){
            Uri url = Uri.parse(receitaFoto);
            Glide.with(VisualizarReceitaActivity.this)
                    .load(url)
                    .into(quadroFotoReceita);
        }else{
            quadroFotoReceita.setImageResource(R.drawable.turkey_roast_3);
        }

    }

    private void usuarioReceitaDados(Receitas receitaUserClicada) {

        nomeReceitaUserClicada = receitaUserClicada.getNome();
        textNomeReceita.setText(nomeReceitaUserClicada);

        ingredientesReceitaUserClicada = receitaUserClicada.getIngredientes();
        textIngredientes.setText(ingredientesReceitaUserClicada);

        modoPreparoReceitaUserClicada = receitaUserClicada.getModoPreparo();
        textModoPreparo.setText(modoPreparoReceitaUserClicada);

        //recupera a idReceita que foi selecionada na lista
        idReceitaUserClicada = receitaUserClicada.getIdReceita();

        fotoReceitaUser = receitaUserClicada.getUrlFotoReceita();
        if (fotoReceitaUser != null){
            Uri url = Uri.parse(fotoReceitaUser);
            Glide.with(VisualizarReceitaActivity.this)
                    .load(url)
                    .into(quadroFotoReceita);
        }else{
            quadroFotoReceita.setImageResource(R.drawable.turkey_roast_3);
        }

    }

    private void feedReceitaDados(Feed receitaFeedClicada) {

        idPostagem = receitaFeedClicada.getIdPostagem();

        nomeReceitaClicada = receitaFeedClicada.getNomeReceita();
        textNomeReceita.setText(nomeReceitaClicada);

        ingredientesReceitaClicada = receitaFeedClicada.getIngredientes();
        textIngredientes.setText(ingredientesReceitaClicada);

        modoPreparoReceitaClicada = receitaFeedClicada.getModoPreparo();
        textModoPreparo.setText(modoPreparoReceitaClicada);

        qtdPessoasServidasReceitaClicada = receitaFeedClicada.getQtdPessoasServidas();

        String postagemFotoFeed = receitaFeedClicada.getFotoPostagem();
        if (postagemFotoFeed != null){
            Uri url = Uri.parse(postagemFotoFeed);
            Glide.with(VisualizarReceitaActivity.this)
                    .load(url)
                    .into(quadroFotoReceita);
        }else{
            quadroFotoReceita.setImageResource(R.drawable.turkey_roast_3);
        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (bundle.containsKey("dadosMinhaReceitaClicada")){

            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_view_receita, menu);

        }
        return super.onCreateOptionsMenu(menu);


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (bundle.containsKey("dadosMinhaReceitaClicada")){

            int itemId = item.getItemId();

            if (itemId == R.id.menuEdicao){
                abrirEditor();
            } else if (itemId == R.id.menuApagar) {
                excluirReceita();
            } else if (itemId == com.miguelcatalan.materialsearchview.R.id.homeAsUp){
                finish();
            } else {
                return super.onOptionsItemSelected(item);
            }

        }

        return true;
    }

    private void excluirReceita() {

        receitasChefRef.child(idReceitaClicada).removeValue();
        ultimaPostagemRef.child(idReceitaClicada).removeValue();

        Toast.makeText(VisualizarReceitaActivity.this,
                "A " + getString(R.string.receita) + nomeReceitaClicada + getString(R.string.excluida),
                Toast.LENGTH_SHORT).show();

        finish();
    }

    private void abrirEditor() {

        Intent i = new Intent(VisualizarReceitaActivity.this, EditarReceitaActivity.class);
        i.putExtra("nome", nomeReceitaClicada);
        i.putExtra("ingredientes", ingredientesReceitaClicada);
        i.putExtra("modoPreparo", modoPreparoReceitaClicada);
        i.putExtra("qtdPessoasServidas", qtdPessoasServidasReceitaClicada);
        i.putExtra("urlFoto", receitaFoto);
        i.putExtra("idR", idReceitaClicada);

        startActivity(i);

    }

    private void inicializarComponentes() {
        toolbar = findViewById(R.id.toolbarPrincipal);
        textNomeReceita = findViewById(R.id.textNomeReceita);
        textIngredientes = findViewById(R.id.textIngredientes);
        textModoPreparo = findViewById(R.id.textModoPreparo);
        quadroFotoReceita = findViewById(R.id.quadroFotoReceita);
    }


    @Override
    public boolean onSupportNavigateUp() { //ao clicar no botao x da visualizacao da receita, a tela fecha e volta para a tela anterior
        finish();
        return true;
    }

}
