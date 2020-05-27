package com.example.android.receitaboa.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.receitaboa.R;
import com.example.android.receitaboa.model.Receitas;

import de.hdodenhof.circleimageview.CircleImageView;

public class VisualizarReceitaActivity extends AppCompatActivity {

    private TextView textNomeReceita, textIngredientes, textModoPreparo;
    private ImageView displayImageReceitaFinal;

    private Receitas receitaClicada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizar_receita);

        //Inicializar componentes;
        inicializarComponentes();

        /*
        //Configura toolbar
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Visualizar Receita");
        setSupportActionBar( toolbar );
        //adiciona o botão voltar (na barra superior) para MainActivity (padrão) (PARTE 1)
        //OBS: deve-se adicionar android:parentActivityName=".activity.MainActivity" no Android Manifest na parte do EditarPerfilActivity, botão voltar retorna a parentActivity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp); //customiza o botão voltar para o ícone q vc desejar
         */

        //Recupera os dados da Receita selecionada
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){

            receitaClicada = (Receitas) bundle.getSerializable("dadosReceitaClicada");
            textNomeReceita.setText(receitaClicada.getNome());
            textIngredientes.setText(receitaClicada.getIngredientes());
            textModoPreparo.setText(receitaClicada.getModoPreparo());

            String fotoReceita = receitaClicada.getUrlFotoReceita();
            if (fotoReceita != null){
                Uri url = Uri.parse(fotoReceita);
                Glide.with(VisualizarReceitaActivity.this)
                        .load(url)
                        .into(displayImageReceitaFinal);
            }else{
                displayImageReceitaFinal.setImageResource(R.drawable.cloche_tableware);
            }

        }


    }

    private void inicializarComponentes() {
        textNomeReceita = findViewById(R.id.textNomeReceita);
        textIngredientes = findViewById(R.id.textIngredientes);
        textModoPreparo = findViewById(R.id.textModoPreparo);
        displayImageReceitaFinal = findViewById(R.id.imageReceitaFinal);
    }

    /*
    @Override
    public boolean onSupportNavigateUp() { //ao clicar no botao x da visualizacao da receita, a tela fecha e volta para a tela anterior
        finish();
        return false;
    }
     */


}
