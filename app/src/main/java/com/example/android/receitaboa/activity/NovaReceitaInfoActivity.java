/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.receitaboa.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android.receitaboa.R;
import com.example.android.receitaboa.helper.ConfiguracaoFirebase;
import com.example.android.receitaboa.helper.Permissao;
import com.example.android.receitaboa.helper.UsuarioFirebaseAuth;
import com.example.android.receitaboa.model.Receitas;
import com.google.firebase.storage.StorageReference;

/**
 * Allows user to create a new recipe or edit an existing one.
 */
public class NovaReceitaInfoActivity extends AppCompatActivity {

    private EditText editNomeReceita;
    private EditText editIngredientesReceita;
    private EditText editModoPreparo;
    private EditText qtdPessoasServidas;

    private StorageReference storageRef;
    private String identificadorChef;

    public static Activity addInfoReceita;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nova_receita_info);

        //referenciando a Activity
        addInfoReceita = this;

        //Configurações iniciais
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        identificadorChef = UsuarioFirebaseAuth.getIdentificadorChefAuth();

        //inicializar componentes
        editNomeReceita = findViewById(R.id.editNomeReceita);
        editIngredientesReceita = findViewById(R.id.editReceitaIngredientes);
        editModoPreparo = findViewById(R.id.editModoPreparo);
        qtdPessoasServidas = findViewById(R.id.editQtdPessoasServidas);

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void cadastrarMinhaReceita(final Receitas minhasReceitas){

        minhasReceitas.setIdChef(identificadorChef); //setando o id do Chef na Classe Receitas

        //salvar os dados da Receita no FirebaseDatabase
        minhasReceitas.salvarMinhaReceitaFirebaseDb();

        Intent i = new Intent(NovaReceitaInfoActivity.this, NovaReceitaFotoActivity.class);
        startActivity(i);

    }

    public void validarReceita(View view){ //metodo onClick (botão salvar)

        //Recuperar textos dos campos digitados
        String campoNomeReceita = editNomeReceita.getText().toString();
        String campoIngredientes = editIngredientesReceita.getText().toString();
        String campoModoPreparo = editModoPreparo.getText().toString();
        String campoQtdPessoasServidas = qtdPessoasServidas.getText().toString();


        if(!campoNomeReceita.isEmpty()){
            if(!campoIngredientes.isEmpty()){
                if(!campoModoPreparo.isEmpty()){

                    Receitas minhasReceitas = new Receitas();
                    minhasReceitas.setNome(campoNomeReceita);
                    minhasReceitas.setIngredientes(campoIngredientes);
                    minhasReceitas.setModoPreparo(campoModoPreparo);
                    minhasReceitas.setQtdPessoasServidas(campoQtdPessoasServidas);

                    cadastrarMinhaReceita(minhasReceitas);

                }else {
                    Toast.makeText(NovaReceitaInfoActivity.this,"Preencha o modo de preparo de sua receita!",Toast.LENGTH_SHORT).show();
                }

            }else {
                Toast.makeText(NovaReceitaInfoActivity.this,"Preencha os ingredientes de sua receita!",Toast.LENGTH_SHORT).show();
            }

        }else {
            Toast.makeText(NovaReceitaInfoActivity.this,"Preencha o nome de sua receita!",Toast.LENGTH_SHORT).show();
        }

    }

}