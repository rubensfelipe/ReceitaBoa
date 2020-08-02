package com.example.android.receitaboa.activity;

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
import com.example.android.receitaboa.R;
import com.example.android.receitaboa.helper.ConfiguracaoFirebase;
import com.example.android.receitaboa.helper.UsuarioFirebaseAuth;
import com.example.android.receitaboa.model.Feed;
import com.example.android.receitaboa.model.Receitas;
import com.google.firebase.database.DatabaseReference;

public class VisualizarReceitaActivity extends AppCompatActivity {

    private TextView textNomeReceita, textIngredientes, textModoPreparo;
    private ImageView displayImageReceitaFinal;

    private String nomeReceitaClicada;
    private String nomeReceitaAmigoClicada;
    private String ingredientesReceitaClicada;
    private String ingredientesReceitaAmigoClicada;
    private String modoPreparoReceitaClicada;
    private String modoPreparoReceitaAmigoClicada;
    private String qtdPessoasServidasReceitaClicada;
    private String receitaFoto;
    private String receitaAmigoFoto;
    private Button buttonAcaoPerfil;
    private Toolbar toolbar;

    private String idChefLogado;
    private String idReceitaClicada;
    private String idPostagem;
    private String idReceitaAmigoClicada;
    private String idReceitaFeedClicada;

    private DatabaseReference firebaseDbRef;
    private DatabaseReference receitasRef;
    private DatabaseReference receitasChefRef;

    public static Activity atividadeAberta;

    private Bundle bundle;

    private Receitas minhaReceitaClicada;
    private Receitas receitaAmigoClicada;
    private Feed receitaFeedClicada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizar_receita);

        //Inicializar componentes;
        inicializarComponentes();

        //Configurações iniciais
        configuracoesRef();

        //referencia que atividadeAberta = essa Activity
        atividadeAberta = this;

        configurarToolbar();

        recuperarExtras();

    }

    private void recuperarExtras() {
        //Recupera os dados da Receita selecionada
        bundle = getIntent().getExtras();
        if (bundle != null){

            if (bundle.containsKey("dadosMinhaReceitaClicada")){

                minhaReceitaClicada = (Receitas) bundle.getSerializable("dadosMinhaReceitaClicada");
                minhaReceitaDados(minhaReceitaClicada);

            } else if(bundle.containsKey("dadosReceitaAmigoClicada")){

                receitaAmigoClicada = (Receitas) bundle.getSerializable("dadosReceitaAmigoClicada");
                amigoReceitaDados(receitaAmigoClicada);

            }else if (bundle.containsKey("dadosReceitaFeedClicada")){

                receitaFeedClicada = (Feed) bundle.getSerializable("dadosReceitaFeedClicada");
                feedReceitaDados(receitaFeedClicada);

            }
        }
    }

    private void configuracoesRef() {
        firebaseDbRef = ConfiguracaoFirebase.getFirebaseDatabase();
        receitasRef = firebaseDbRef.child("receitas");
        idChefLogado = UsuarioFirebaseAuth.getIdentificadorChefAuth(); //id do chef logado (emailAuth convertido em base64)

        //Configurar referência receitas do chef logado
        receitasChefRef = receitasRef
                .child(idChefLogado);
    }

    private void configurarToolbar() {
        toolbar.setTitle("Visualizar Receita");
        setSupportActionBar( toolbar );
        //adiciona o botão voltar (na barra superior) para MainActivity (padrão) (PARTE 1)
        //OBS: deve-se adicionar android:parentActivityName=".activity.MainActivity" no Android Manifest na parte do EditarPerfilActivity, botão voltar retorna a parentActivity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp); //customiza o botão voltar para o ícone q vc desejar
    }

    private void minhaReceitaDados(Receitas receitaClicada) {

        nomeReceitaClicada = receitaClicada.getNome();
        textNomeReceita.setText(nomeReceitaClicada);

        ingredientesReceitaClicada = receitaClicada.getIngredientes();
        textIngredientes.setText(ingredientesReceitaClicada);

        modoPreparoReceitaClicada = receitaClicada.getModoPreparo();
        textModoPreparo.setText(modoPreparoReceitaClicada);

        qtdPessoasServidasReceitaClicada = receitaClicada.getQtdPessoasServidas();

        //recupera a idReceita que foi selecionada na lista
        idReceitaClicada = receitaClicada.getIdReceita();

        receitaFoto = receitaClicada.getUrlFotoReceita();
        if (receitaFoto != null){
            Uri url = Uri.parse(receitaFoto);
            Glide.with(VisualizarReceitaActivity.this)
                    .load(url)
                    .into(displayImageReceitaFinal);
        }else{
            displayImageReceitaFinal.setImageResource(R.drawable.turkey_roast_3);
        }

    }

    private void amigoReceitaDados(Receitas receitaAmigoClicada) {

        nomeReceitaAmigoClicada = receitaAmigoClicada.getNome();
        textNomeReceita.setText(nomeReceitaAmigoClicada);

        ingredientesReceitaAmigoClicada = receitaAmigoClicada.getIngredientes();
        textIngredientes.setText(ingredientesReceitaAmigoClicada);

        modoPreparoReceitaAmigoClicada = receitaAmigoClicada.getModoPreparo();
        textModoPreparo.setText(modoPreparoReceitaAmigoClicada);

        //recupera a idReceita que foi selecionada na lista
        idReceitaAmigoClicada = receitaAmigoClicada.getIdReceita();

        receitaAmigoFoto = receitaAmigoClicada.getUrlFotoReceita();
        if (receitaAmigoFoto != null){
            Uri url = Uri.parse(receitaAmigoFoto);
            Glide.with(VisualizarReceitaActivity.this)
                    .load(url)
                    .into(displayImageReceitaFinal);
        }else{
            displayImageReceitaFinal.setImageResource(R.drawable.turkey_roast_3);
        }

    }

    private void feedReceitaDados(Feed receitaFeedClicada) {

        idPostagem = receitaFeedClicada.getId();

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
                    .into(displayImageReceitaFinal);
        }else{
            displayImageReceitaFinal.setImageResource(R.drawable.turkey_roast_3);
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

            switch (item.getItemId()){
                case R.id.menuEdicao:
                    abrirEditor();
                    break;
                case R.id.menuApagar:
                    excluirReceita();
                    break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void excluirReceita() {
        receitasChefRef.child(idReceitaClicada).removeValue();
        //receitaFeedRef.child(idPostagem).removeValue();
        Toast.makeText(VisualizarReceitaActivity.this,"A receita " + nomeReceitaClicada + " foi excluída com sucesso", Toast.LENGTH_SHORT).show();
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
        displayImageReceitaFinal = findViewById(R.id.imageReceitaFinal);
    }


    @Override
    public boolean onSupportNavigateUp() { //ao clicar no botao x da visualizacao da receita, a tela fecha e volta para a tela anterior

        finish();
        return false;
    }

}
