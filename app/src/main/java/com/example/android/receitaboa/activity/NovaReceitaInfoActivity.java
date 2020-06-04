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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android.receitaboa.R;
import com.example.android.receitaboa.helper.Permissao;
import com.example.android.receitaboa.helper.UsuarioFirebaseAuth;
import com.example.android.receitaboa.model.Receitas;

/**
 * Allows user to create a new recipe or edit an existing one.
 */
public class NovaReceitaInfoActivity extends AppCompatActivity {

    private EditText editNomeReceita;
    private EditText editIngredientesReceita;
    private EditText editModoPreparo;
    private EditText qtdPessoasServidas;

    private String identificadorChef;

    public static Activity atividadeAberta;

    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nova_receita_info);

        //referenciando a Activity
        atividadeAberta = this;

        //Configurações iniciais
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

        //apos usuário clicar em salvar, o teclado é encerrado
        fecharTeclado();

        minhasReceitas.setIdChef(identificadorChef); //setando o id do Chef na Classe Receitas

        //salvar os dados da Receita no FirebaseDatabase
        minhasReceitas.salvarMinhaReceitaFirebaseDb();

        abrirDialog(minhasReceitas);

    }

    private void fecharTeclado() {
        editNomeReceita.onEditorAction(EditorInfo.IME_ACTION_DONE);
        editIngredientesReceita.onEditorAction(EditorInfo.IME_ACTION_DONE);
        editModoPreparo.onEditorAction(EditorInfo.IME_ACTION_DONE);
        qtdPessoasServidas.onEditorAction(EditorInfo.IME_ACTION_DONE);
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

    public void abrirDialog(final Receitas minhasReceitas){

        //Instancia AlertDialog
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        //Configura titulo e mensagem
        configurarTituloMensagem(dialog);

        //Configura cancelamento
        dialog.setCancelable(false); //false: se clicar fora do alerta do dialog, o alerta não é fechado e o usuário precisa clicar em sim ou não

        //Configura ações para o sim e o não
        acaoSePositiva(dialog, minhasReceitas);

        acaoSeNegativa(dialog);

        //Criar e exibir AlertDialog
        exibirAlerta(dialog);

    }

    private void exibirAlerta(AlertDialog.Builder dialog) {
        dialog.create();
        dialog.show();
    }

    private void configurarTituloMensagem(AlertDialog.Builder dialog) {

        dialog.setTitle(getApplicationContext().getString(R.string.dialog_titulo_devo_adicionar_foto_receita));
        dialog.setMessage(getApplicationContext().getString(R.string.dialog_msg_devo_adicionar_foto_receita));

    }

    private void acaoSePositiva(AlertDialog.Builder dialog, final Receitas minhasReceitas) {

        dialog.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();

                //Validar permissões (para acesso da camera e da galeria de fotos do usuário)
                Permissao.validarPermissoes(permissoesNecessarias,NovaReceitaInfoActivity.this,1);

                Intent i = new Intent(NovaReceitaInfoActivity.this, NovaReceitaFotoActivity.class);
                i.putExtra("idReceita",minhasReceitas.getIdReceita());
                startActivity(i);

            }
        });

    }

    private void acaoSeNegativa(AlertDialog.Builder dialog) {

        dialog.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.nova_receita_adicionada), Toast.LENGTH_SHORT).show();

                finish();
            }
        });

    }

}