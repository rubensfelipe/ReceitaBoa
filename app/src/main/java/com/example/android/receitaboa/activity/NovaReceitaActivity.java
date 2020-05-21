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
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android.receitaboa.R;
import com.example.android.receitaboa.helper.ConfiguracaoFirebase;
import com.example.android.receitaboa.helper.Permissao;
import com.example.android.receitaboa.helper.UsuarioFirebaseAuth;
import com.example.android.receitaboa.model.Chef;
import com.example.android.receitaboa.model.Receitas;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

/**
 * Allows user to create a new recipe or edit an existing one.
 */
public class NovaReceitaActivity extends AppCompatActivity {

    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA = 200;

    private EditText editNomeReceita;
    private EditText editIngredientesReceita;
    private EditText editModoPreparo;
    private EditText qtdPessoasServidas;
    private ImageView displayFotoReceita;
    private ImageView cameraMinhaReceita;
    private ImageView galeriaMinhaReceita;

    private StorageReference storageRef;
    private String identificadorChef;

    private Chef chef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nova_receita);

        //Validar permissões
        Permissao.validarPermissoes(permissoesNecessarias,this,1);

        //Configurações iniciais
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        identificadorChef = UsuarioFirebaseAuth.getIdentificadorChefAuth();

        //inicializar componentes
        editNomeReceita = findViewById(R.id.editNomeReceita);
        displayFotoReceita = findViewById(R.id.displayFotoReceita);
        cameraMinhaReceita = findViewById(R.id.cameraMinhaReceita);
        galeriaMinhaReceita = findViewById(R.id.galeriaMinhaReceita);
        editIngredientesReceita = findViewById(R.id.editReceitaIngredientes);
        editModoPreparo = findViewById(R.id.editModoPreparo);
        qtdPessoasServidas = findViewById(R.id.editQtdPessoasServidas);

        cameraMinhaReceita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if ( i.resolveActivity(getPackageManager()) != null ){ //se foi possivel acessar a camera
                    startActivityForResult(i, SELECAO_CAMERA); //starta a activity (camera do usuário) e captura o resultado da ação do usuario (resultado: capturar a foto tirada)
                }

            }
        });

        galeriaMinhaReceita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); //escolher a foto da galeria no local onde se encontra a galeria de um celular

                if ( i.resolveActivity(getPackageManager()) != null ){ //se foi possivel acessar a galeria
                    startActivityForResult(i, SELECAO_GALERIA); //starta a activity (galeria de imagens) e capta o resultado da ação do usuario (resultado: selecionada a imagem da galeria)
                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_OK){
            Bitmap fotoReceita = null;

            try {

                switch (requestCode){
                    case SELECAO_GALERIA:
                        Uri localImagemSelecionada = data.getData();
                        ImageDecoder.Source imgSource = ImageDecoder.createSource(getContentResolver(), localImagemSelecionada); //Primeiro cria a source
                        fotoReceita = ImageDecoder.decodeBitmap(imgSource); //SEGUNDO: converte ImageDecoder.Source -> Bitmap
                        break;
                    case SELECAO_CAMERA:
                        fotoReceita = (Bitmap) data.getExtras().get("data"); //dados internos da imagem, 0010011001
                        break;
                }

                //Se a foto foi selecionada ou da camera ou da galeria, temos uma imagem
                if (fotoReceita != null){

                    //Seta a imagem na tela
                    displayFotoReceita.setImageBitmap(fotoReceita);

                    //Tratamento da Imagem e Conversão (Bitmap -> ByteArray)
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    fotoReceita.compress(Bitmap.CompressFormat.JPEG,75,baos);
                    byte[] dadosImagemReceita = baos.toByteArray();

                    final Receitas minhaReceita = new Receitas();

                    //Salvar imagem firebaseStorage
                    StorageReference fotoReceitaRef = storageRef
                            .child("imagens")
                            .child("receitas")
                            .child(identificadorChef) //idChefAuth
                            .child(minhaReceita.getIdReceita() + ".jpg");

                    UploadTask uploadTask = fotoReceitaRef.putBytes(dadosImagemReceita);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(NovaReceitaActivity.this,
                                    "Erro ao salvar a imagem, tente novamente!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            //Recupera a foto da receita do FirebaseStorage
                            if (taskSnapshot.getMetadata() != null) {
                                if (taskSnapshot.getMetadata().getReference() != null) {
                                    Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl(); //recupera a foto do firebaseStorage

                                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {

                                            Uri urlImageReceita = uri;


                                            /*
                                            Receitas minhasReceitas = new Receitas();

                                            minhasReceitas.setIdChef(identificadorChef); //setando o id do Chef na Classe Receitas

                                            //Ao baixa a foto do FirebaseStorage, salvar o caminho da foto no FirebaseStorage (Salva apenas o caminho da foto e não salva ainda os outros dados)
                                            DatabaseReference firebaseDbRef = ConfiguracaoFirebase.getFirebaseDatabase();
                                            DatabaseReference chefRef = firebaseDbRef.child("receitas").child(minhasReceitas.getIdChef());
                                            DatabaseReference receitaRef = chefRef.child(minhasReceitas.getIdReceita()).child("urlFotoReceita");
                                            receitaRef.setValue(urlImageReceita.toString());

                                             */

                                            //Seta o caminho da foto na Classe Receitas
                                            setarUrlEmReceitas(urlImageReceita);

                                        }
                                    });
                                }
                            }

                        }


                    });

                }



                }catch (Exception e){
                e.printStackTrace();
            }


        }

    }



    public void setarUrlEmReceitas(Uri uri){
        Receitas minhasReceitas = new Receitas();
        minhasReceitas.setUrlFotoReceita(uri.toString());
        minhasReceitas.guardarUrlFotoReceita();
    }


    public void cadastrarMinhaReceita(final Receitas minhasReceitas){

        minhasReceitas.setIdChef(identificadorChef); //setando o id do Chef na Classe Receitas

        //salvar os dados da Receita no FirebaseDatabase
        minhasReceitas.salvarMinhaReceitaFirebaseDatabase();


        Toast.makeText(NovaReceitaActivity.this,
                "Sua Receita Boa foi salva com sucesso!",
                Toast.LENGTH_SHORT).show();

        finish();

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
                    minhasReceitas.setNomeReceita(campoNomeReceita);
                    minhasReceitas.setIngredientes(campoIngredientes);
                    minhasReceitas.setModoPreparo(campoModoPreparo);
                    minhasReceitas.setQtdPessoasServidas(campoQtdPessoasServidas);

                    cadastrarMinhaReceita(minhasReceitas);

                }else {
                    Toast.makeText(NovaReceitaActivity.this,"Preencha o modo de preparo de sua receita!",Toast.LENGTH_SHORT).show();
                }

            }else {
                Toast.makeText(NovaReceitaActivity.this,"Preencha os ingredientes de sua receita!",Toast.LENGTH_SHORT).show();
            }

        }else {
            Toast.makeText(NovaReceitaActivity.this,"Preencha o nome de sua receita!",Toast.LENGTH_SHORT).show();
        }

    }

}