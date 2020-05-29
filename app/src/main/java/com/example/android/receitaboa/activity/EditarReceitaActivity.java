package com.example.android.receitaboa.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.android.receitaboa.R;
import com.example.android.receitaboa.helper.Permissao;
import com.example.android.receitaboa.model.Receitas;

public class EditarReceitaActivity extends AppCompatActivity {

    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA = 200;

    private EditText atualizarNome;
    private EditText atualizarIngredientes;
    private EditText atualizarModoPreparo;
    private ImageView atualizarCamera;
    private ImageView atualizarGaleria;
    private ImageView atualizarDisplayFotoReceita;

    private Receitas receitaNome;
    private Receitas receitaIngredientes;
    private Receitas receitaModoPreparo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_receita);

        inicializarComponentes();

        //Validar permissões de Câmera e Galeria de Imagens
        Permissao.validarPermissoes(permissoesNecessarias, this, 1);

        //Recupera os dados da Receita selecionada
        /*
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){

            receitaNome = (Receitas) bundle.getSerializable("nome");
            atualizarNome.setText(receitaNome.getNome());

            /*

            receitaIngredientes = (Receitas) bundle.getSerializable("ingredientes");
            atualizarNome.setText(receitaIngredientes.getIngredientes());

            receitaModoPreparo = (Receitas) bundle.getSerializable("modoPreparo");
            atualizarNome.setText(receitaModoPreparo.getModoPreparo());
             */

            /*
                        String fotoReceita = receitaClicada.getUrlFotoReceita();
            if (fotoReceita != null){
                Uri url = Uri.parse(fotoReceita);
                Glide.with(EditarReceitaActivity.this)
                        .load(url)
                        .into(atualizarDisplayFotoReceita);
            }else{
                atualizarDisplayFotoReceita.setImageResource(R.drawable.cloche_tableware);
            }
             */

    }





    private void inicializarComponentes() {
        atualizarNome = findViewById(R.id.atualizarNomeReceita);
        atualizarIngredientes = findViewById(R.id.atualizarIngredientes);
        atualizarModoPreparo = findViewById(R.id.atualizarModoPreparo);
        atualizarCamera = findViewById(R.id.cameraAtualizarReceita);
        atualizarGaleria = findViewById(R.id.galeriaAtualizarReceita);
        atualizarDisplayFotoReceita = findViewById(R.id.displayAtualizarFoto);
    }
}
