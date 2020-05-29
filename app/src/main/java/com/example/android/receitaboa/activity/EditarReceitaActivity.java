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
    private EditText atualizarQtdPessoasServidas;
    private ImageView atualizarCamera;
    private ImageView atualizarGaleria;
    private ImageView atualizarDisplayFotoReceita;

    private String receitaNome;
    private String receitaIngredientes;
    private String receitaModoPreparo;
    private String receitaQtdPessoasServidas;
    private String receitaFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_receita);

        inicializarComponentes();

        //Validar permissões de Câmera e Galeria de Imagens
        Permissao.validarPermissoes(permissoesNecessarias, this, 1);

        //Recupera os dados da Receita visualizada

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            receitaNome = (String) bundle.getSerializable("nome");
            atualizarNome.setText(receitaNome);

            receitaIngredientes = (String) bundle.getSerializable("ingredientes");
            atualizarIngredientes.setText(receitaIngredientes);

            receitaModoPreparo = (String) bundle.getSerializable("modoPreparo");
            atualizarModoPreparo.setText(receitaModoPreparo);

            receitaQtdPessoasServidas = (String) bundle.getSerializable("qtdPessoasServidas");
            atualizarQtdPessoasServidas.setText(receitaQtdPessoasServidas);

            receitaFoto = (String) bundle.getSerializable("urlFoto");

            if (receitaFoto != null){
                Uri url = Uri.parse(receitaFoto);
                Glide.with(EditarReceitaActivity.this)
                        .load(url)
                        .into(atualizarDisplayFotoReceita);
            }else{
                atualizarDisplayFotoReceita.setImageResource(R.drawable.cloche_tableware);
            }

        }
    }


    private void inicializarComponentes() {
        atualizarNome = findViewById(R.id.atualizarNomeReceita);
        atualizarIngredientes = findViewById(R.id.atualizarIngredientes);
        atualizarModoPreparo = findViewById(R.id.atualizarModoPreparo);
        atualizarQtdPessoasServidas = findViewById(R.id.atualizarQtdPessoasServidas);
        atualizarCamera = findViewById(R.id.cameraAtualizarReceita);
        atualizarGaleria = findViewById(R.id.galeriaAtualizarReceita);
        atualizarDisplayFotoReceita = findViewById(R.id.displayAtualizarFoto);
    }
}
