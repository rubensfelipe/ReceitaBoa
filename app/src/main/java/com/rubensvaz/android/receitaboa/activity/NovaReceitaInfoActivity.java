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
package com.rubensvaz.android.receitaboa.activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.rubensfelipe.android.receitaboa.R;
import com.rubensvaz.android.receitaboa.api.NotificationService;
import com.rubensvaz.android.receitaboa.helper.ConfiguracaoFirebase;
import com.rubensvaz.android.receitaboa.helper.Permissao;
import com.rubensvaz.android.receitaboa.helper.UsuarioFirebaseAuth;
import com.rubensvaz.android.receitaboa.model.Chef;
import com.rubensvaz.android.receitaboa.model.Notificacao;
import com.rubensvaz.android.receitaboa.model.NotificacaoDados;
import com.rubensvaz.android.receitaboa.model.Receitas;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.rubensvaz.android.receitaboa.helper.UsuarioFirebaseAuth.getChefAtualAuth;

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

    private Chef dadosChef;
    private FirebaseUser chefAuth;

    private DatabaseReference firebase;
    private DatabaseReference chefsRef;

    private List<String> listaTokens = new ArrayList<>();

    private Retrofit retrofit;
    private String baseUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nova_receita_info);

        //referenciando a Activity
        atividadeAberta = this;

        inicializarComponentes();

        configuracoesIniciais();

        configuracaoNotificacoes();

    }

    private void configuracaoNotificacoes() {

        baseUrl = "https://fcm.googleapis.com/fcm/";

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarTokens();
    }

    private void recuperarTokens() {

        chefsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds: snapshot.getChildren()){

                    Chef chefToken = ds.getValue(Chef.class);

                    String tokenCel = chefToken.getTokenCel();

                    if (tokenCel != null)
                    listaTokens.add(tokenCel);

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void configuracoesIniciais() {
        identificadorChef = UsuarioFirebaseAuth.getIdentificadorChefAuth();
        dadosChef = UsuarioFirebaseAuth.getDadosChefLogadoAuth();

        chefAuth = getChefAtualAuth();

        firebase = ConfiguracaoFirebase.getFirebaseDatabase();
        chefsRef = firebase.child("chefs");
    }

    private void inicializarComponentes() {
        editNomeReceita = findViewById(R.id.editNomeReceita);
        editIngredientesReceita = findViewById(R.id.editReceitaIngredientes);
        editModoPreparo = findViewById(R.id.editModoPreparo);
        qtdPessoasServidas = findViewById(R.id.editQtdPessoasServidas);
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

                    minhasReceitas.setNomeChef(chefAuth.getDisplayName());

                    cadastrarMinhaReceita(minhasReceitas);

                }else {
                    Toast.makeText(NovaReceitaInfoActivity.this,
                            getString(R.string.ponha_modoPreparo),
                            Toast.LENGTH_SHORT).show();
                }

            }else {
                Toast.makeText(NovaReceitaInfoActivity.this,
                        getString(R.string.ponha_ingredientes),
                        Toast.LENGTH_SHORT).show();
            }

        }else {
            Toast.makeText(NovaReceitaInfoActivity.this,
                    getString(R.string.ponha_nome_receita),
                    Toast.LENGTH_SHORT).show();
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

        dialog.setPositiveButton(R.string.sim, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                enviarNotificacao();

                dialog.cancel();

                //Validar permissões (para acesso da camera e da galeria de fotos do usuário)
                Permissao.validarPermissoes(permissoesNecessarias,NovaReceitaInfoActivity.this,1);

                Intent i = new Intent(NovaReceitaInfoActivity.this, NovaReceitaFotoActivity.class);
                i.putExtra("idReceita", minhasReceitas.getIdReceita());
                i.putExtra("nome", minhasReceitas.getNome());

                i.putExtra("ingredientes", minhasReceitas.getIngredientes());
                i.putExtra("modoPreparo", minhasReceitas.getModoPreparo());
                i.putExtra("qtdPessoasServidas", minhasReceitas.getQtdPessoasServidas());

                startActivity(i);

            }
        });

    }

    private void acaoSeNegativa(AlertDialog.Builder dialog) {

        dialog.setNegativeButton(R.string.nao, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                enviarNotificacao();

                Toast.makeText(getApplicationContext(),
                        getApplicationContext().getString(R.string.nova_receita_adicionada),
                        Toast.LENGTH_SHORT).show();

                finish();
            }
        });

    }


    public void enviarNotificacao() { //envia notificacao para o firebase

        for (String tokenCel : listaTokens) {

                String to = tokenCel;

                //Monta objeto notificação
                Notificacao notificacao = new Notificacao( getString(R.string.notification_title), chefAuth.getDisplayName() + getString(R.string.notification_body) );
                NotificacaoDados notificacaoDados = new NotificacaoDados(to, notificacao);

                NotificationService service = retrofit.create(NotificationService.class);
                Call<NotificacaoDados> call = service.salvarNotificacao(notificacaoDados);

            call.enqueue(new Callback<NotificacaoDados>() {
                @Override
                public void onResponse(Call<NotificacaoDados> call, Response<NotificacaoDados> response) {

                    if (response.isSuccessful()) {

                        /*Intent i = new Intent(getApplicationContext(), ReceitasUsuariosFragment.class);
                        startActivity(i);*/

                        //Toast.makeText(NovaReceitaInfoActivity.this, "Notificação enviada", Toast.LENGTH_SHORT).show();

                    }
                }

                @Override
                public void onFailure(Call<NotificacaoDados> call, Throwable t) {

                }
            });
        }
    }


}