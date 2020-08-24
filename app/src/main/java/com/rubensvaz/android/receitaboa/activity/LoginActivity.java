package com.rubensvaz.android.receitaboa.activity;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.rubensfelipe.android.receitaboa.R;
import com.rubensvaz.android.receitaboa.helper.ConfiguracaoFirebase;
import com.rubensvaz.android.receitaboa.model.Chef;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static final int MEU_CODIGO = 99;
    private EditText campoEmail, campoSenha;
    private FirebaseAuth autenticacao;
    private List<AuthUI.IdpConfig> servidores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inicializarComponentes();

        configuracoesIniciais();

    }


    private void inicializarComponentes() {
        campoEmail = findViewById(R.id.editLoginEmail);
        campoSenha = findViewById(R.id.editLoginSenha);
    }

    private void configuracoesIniciais() {
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    }

    public void logarUsuario(Chef chef){

        autenticacao.signInWithEmailAndPassword( //loga o usuario após digitar um email e senha já cadastrados
                chef.getEmail(), chef.getSenha() //SEGUNDO: recupera as informações do usuario, que foram setadas na Classe Chef, para fazer o login
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){
                    abrirTelaPrincipal();
                }else {
                    String excecao = "";
                    try{
                        throw task.getException();
                    }catch ( FirebaseAuthInvalidUserException e){
                        excecao = "Usuário não existe!";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        excecao = "E-mail ou senha incorreto";
                    }catch (Exception e){
                        excecao ="Erro ao logar o usuário: " + e.getMessage();
                        e.printStackTrace();
                    }
                    Toast.makeText(LoginActivity.this,excecao,Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    public void validarAutenticacaoChef(View view){

        //Recuperar os textos digitados pelo usuário na tela de login
        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();

        //Validar se e-mail e senha foram digitados nos seus respectivos campos
        if(!textoEmail.isEmpty()){
            if(!textoSenha.isEmpty()){

                Chef chef = new Chef();
                chef.setEmail(textoEmail); //primeiro seta as informações, digitadas pelo usuario, na Classe Chef
                chef.setSenha(textoSenha);

                logarUsuario(chef);

            }else {
                Toast.makeText(LoginActivity.this,"Preencha a sua senha!",Toast.LENGTH_SHORT).show();
            }

        }else {
            Toast.makeText(LoginActivity.this,"Preencha o seu e-mail!",Toast.LENGTH_SHORT).show();
        }
    }

    public void abrirCadastro(View view){
        Intent i = new Intent(LoginActivity.this,CadastroActivity.class);
        startActivity(i);
    }

    public void abrirTelaPrincipal(){
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser usuarioAtual = autenticacao.getCurrentUser();
        if (usuarioAtual != null){
            abrirTelaPrincipal();
        }
    }

}
