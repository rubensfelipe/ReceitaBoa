package com.example.android.receitaboa.activity;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.receitaboa.R;
import com.example.android.receitaboa.helper.ConfiguracaoFirebase;
import com.example.android.receitaboa.model.Chef;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText campoEmail, campoSenha;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        campoEmail = findViewById(R.id.editLoginEmail);
        campoSenha = findViewById(R.id.editLoginSenha);

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
                        excecao = "Usuário não cadastrado!";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        excecao = "Digite um e-mail válido";
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
                Toast.makeText(LoginActivity.this,"Preencha o e-mail!",Toast.LENGTH_SHORT).show();
            }

        }else {
            Toast.makeText(LoginActivity.this,"Preencha a senha!",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser usuarioAtual = autenticacao.getCurrentUser();
        if (usuarioAtual != null){
            abrirTelaPrincipal();
        }
    }

    public void abrirCadastro(View view){
        Intent i = new Intent(LoginActivity.this,CadastroActivity.class);
        startActivity(i);
    }

    public void abrirTelaPrincipal(){
        Intent i = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(i);
    }

}
