package com.rubensvaz.android.receitaboa.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.rubensfelipe.android.receitaboa.R;
import com.rubensvaz.android.receitaboa.helper.Base64Custom;
import com.rubensvaz.android.receitaboa.helper.ConfiguracaoFirebase;
import com.rubensvaz.android.receitaboa.helper.UsuarioFirebaseAuth;
import com.rubensvaz.android.receitaboa.model.Chef;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroActivity extends AppCompatActivity {

    private EditText campoNome, campoEmail, campoSenha;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        inicializarComponentes();

    }

    private void inicializarComponentes() {
        campoNome = findViewById(R.id.editCadastroNome);
        campoEmail = findViewById(R.id.editCadastroEmail);
        campoSenha = findViewById(R.id.editCadastroSenha);
    }

    public void validarCadastroChef(View view){ //metodo onClick activity_cadastro (botaoCadastrar)

        //Recuperar textos dos campos digitados
        String textoNome = campoNome.getText().toString();
        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();

        if(!textoNome.isEmpty()){
            if(!textoEmail.isEmpty()){
                if(!textoSenha.isEmpty()){

                    Chef chef = new Chef();
                    chef.setNome(textoNome);
                    chef.setEmail(textoEmail);
                    chef.setSenha(textoSenha);

                    cadastrarChefDbAuth(chef);

                }else {
                    Toast.makeText(CadastroActivity.this, R.string.ponha_senha, Toast.LENGTH_SHORT).show();
                }

            }else {
                Toast.makeText(CadastroActivity.this, R.string.ponha_email, Toast.LENGTH_SHORT).show();
            }

        }else {
            Toast.makeText(CadastroActivity.this, R.string.ponha_nome, Toast.LENGTH_SHORT).show();
        }

    }


    public void cadastrarChefDbAuth(final Chef chef){

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                chef.getEmail(), chef.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){ //conseguiu cadastrar no FirebaseAuth
                    Toast.makeText(CadastroActivity.this,
                            R.string.bem_vindo + chef.getNome() + R.string.cadastro_finalizado,
                            Toast.LENGTH_LONG).show();
                    UsuarioFirebaseAuth.atualizarNomeChefAuth(chef.getNome()); //atualiza o nome no FirebaseAuth


                    finish();

                    try {
                        String idChef64 = Base64Custom.codificarBase64(chef.getEmail());
                        chef.setId(idChef64); //setando o id do Chef cadastrado na classe Chef

                        //salvar os dados do Chef no FirebaseDatabase
                        chef.salvarFirebaseDatabase();

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }else {

                    String excecao = "";
                    try{
                        throw task.getException();
                    }catch ( FirebaseAuthWeakPasswordException e){
                        excecao =  getString(R.string.senha_fraca);
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        excecao = getString(R.string.email_invalido);
                    }catch (FirebaseAuthUserCollisionException e){
                        excecao = getString(R.string.conta_jacadastrada);
                    }catch (Exception e){
                        excecao = getString(R.string.erro_cadastro) + e.getMessage();
                        e.printStackTrace();
                    }
                    Toast.makeText(CadastroActivity.this,
                            excecao,
                            Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

}
