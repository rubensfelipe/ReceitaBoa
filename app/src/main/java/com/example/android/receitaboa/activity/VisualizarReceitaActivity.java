package com.example.android.receitaboa.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.receitaboa.R;
import com.example.android.receitaboa.helper.ConfiguracaoFirebase;
import com.example.android.receitaboa.helper.UsuarioFirebaseAuth;
import com.example.android.receitaboa.model.Receitas;
import com.google.firebase.database.DatabaseReference;

public class VisualizarReceitaActivity extends AppCompatActivity {

    private TextView textNomeReceita, textIngredientes, textModoPreparo;
    private ImageView displayImageReceitaFinal;

    private String nomeReceitaClicada;
    private String ingredientesReceitaClicada;
    private String modoPreparoReceitaClicada;
    private String qtdPessoasServidasReceitaClicada;
    private String receitaFoto;

    private String idChefLogado;
    private String idReceitaClicada;
    private DatabaseReference firebaseDbRef;
    private DatabaseReference receitasRef;
    private DatabaseReference receitasChefRef;


    private Receitas receitaClicada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizar_receita);

        //Inicializar componentes;
        inicializarComponentes();

        //Configurações iniciais
        idChefLogado = UsuarioFirebaseAuth.getIdentificadorChefAuth(); //id do chef logado (emailAuth convertido em base64)
        firebaseDbRef = ConfiguracaoFirebase.getFirebaseDatabase();
        receitasRef = firebaseDbRef.child("receitas");

        //Configurar referência receitas do chef logado
        receitasChefRef = receitasRef
                .child(idChefLogado);


        //Configura toolbar
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Visualizar Receita");
        setSupportActionBar( toolbar );
        //adiciona o botão voltar (na barra superior) para MainActivity (padrão) (PARTE 1)
        //OBS: deve-se adicionar android:parentActivityName=".activity.MainActivity" no Android Manifest na parte do EditarPerfilActivity, botão voltar retorna a parentActivity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp); //customiza o botão voltar para o ícone q vc desejar

        //Recupera os dados da Receita selecionada
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){

            receitaClicada = (Receitas) bundle.getSerializable("dadosReceitaClicada");

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
                displayImageReceitaFinal.setImageResource(R.drawable.cloche_tableware);
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_view_receita,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menuEdicao:
                abrirEditor();
                break;
            case R.id.menuApagar:
                excluirReceita();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void excluirReceita() {
        receitasChefRef.child(idReceitaClicada).removeValue();
        Toast.makeText(VisualizarReceitaActivity.this,"A receita " + nomeReceitaClicada + " foi excluída com sucesso", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void abrirEditor() {

        Intent i = new Intent(VisualizarReceitaActivity.this, EditarReceitaActivity.class);
        i.putExtra("nome", nomeReceitaClicada);
        i.putExtra("ingredientes", ingredientesReceitaClicada);
        i.putExtra("modoPreparo", modoPreparoReceitaClicada);
        i.putExtra("qtdPessoasServidas", qtdPessoasServidasReceitaClicada);
        i.putExtra("urlFoto",receitaFoto);
        startActivity(i);

    }

    private void inicializarComponentes() {
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
