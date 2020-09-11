package com.rubensvaz.android.receitaboa.helper;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.rubensvaz.android.receitaboa.model.Chef;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

//Classe para recuperar os dados do usuário salvos no FirebaseAuth
public class UsuarioFirebaseAuth {

    public static String getIdentificadorChefAuth(){

        FirebaseAuth chefAuth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String emailAuth = chefAuth.getCurrentUser().getEmail();
        String identificarChefAuth = Base64Custom.codificarBase64(emailAuth); //id do chef logado (emailAuth convertido em base64)

        return identificarChefAuth;

    }

    //retorna os dados email,senha, etc que foram salvos no FirebaseAuth
    public static FirebaseUser getChefAtualAuth(){
        FirebaseAuth chefAuth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        return chefAuth.getCurrentUser();
    }

    public static boolean atualizarNomeChefAuth(String nome){

        try {
            //salvando o caminho da foto do FirebaseAuth
            FirebaseUser chefAuth = getChefAtualAuth();
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(nome) //seta o o nome do usuario na classe FirebaseUser
                    .build();

            chefAuth.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()){
                        Log.d("Perfil","Erro ao atualizar o nome de perfil!");
                    }
                }
            });
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static boolean atualizarFotoChefAuth(Uri url){

        try {
            //salvando o caminho da foto do FirebaseAuth
            FirebaseUser user = getChefAtualAuth();
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(url) //seta o caminho da foto do usuario na classe FirebaseUser
                    .build();

            user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()){
                        Log.d("Perfil","Erro ao atualizar a foto de perfil!");
                    }
                }
            });
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //recupera os dados (nome, email e caminho da Foto) do FirebaseAuth e Seta eles na Classe Chef
    public static Chef getDadosChefLogadoAuth(){

        FirebaseUser chefAuth = getChefAtualAuth();
        Chef chef = new Chef();
        chef.setEmail(chefAuth.getEmail());
        chef.setNome(chefAuth.getDisplayName());

        if(chefAuth.getPhotoUrl() == null){
            chef.setUrlFotoChefAuth("https://firebasestorage.googleapis.com/v0/b/receita-boa-b409e.appspot.com/o/imagens%2Fperfil%2Favatar.jpg?alt=media&token=13e9ff79-8869-496f-985e-fd30e4d091b0");  //caso o chef não tenha selecionado uma foto de perfil, seta o caminho da foto como vazio
        }else {
            chef.setUrlFotoChefAuth(chefAuth.getPhotoUrl().toString()); //recupera o caminho da foto do chef, foto de perfil, do FirebaseAuth e seta este caminho da foto na Classe Chef
        }
        return chef;
    }

}
