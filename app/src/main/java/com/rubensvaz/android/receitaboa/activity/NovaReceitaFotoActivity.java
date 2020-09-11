package com.rubensvaz.android.receitaboa.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.rubensfelipe.android.receitaboa.R;
import com.rubensvaz.android.receitaboa.helper.ConfiguracaoFirebase;
import com.rubensvaz.android.receitaboa.helper.DateCustom;
import com.rubensvaz.android.receitaboa.helper.UsuarioFirebaseAuth;
import com.rubensvaz.android.receitaboa.model.Postagem;
import com.rubensvaz.android.receitaboa.model.Receitas;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class NovaReceitaFotoActivity extends AppCompatActivity {

    private ImageView displayFotoReceita;
    private ImageView cameraMinhaReceita;
    private ImageView galeriaMinhaReceita;
    private ProgressBar progressBarFotoReceita;

    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA = 200;

    private StorageReference storageRef;
    private String identificadorChef;
    private String idReceita;
    private String nomeReceita;

    private String idChefLogado;
    private DatabaseReference firebaseRef;
    private DatabaseReference chefsRef;
    private DatabaseReference chefLogadoRef;
    private DataSnapshot seguidoresSnapshot;

    private String ingredientes;
    private String modoPreparo;
    private String qtdPessoasServidas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nova_receita_foto);

        //Recupera o id da receita gerada na tela anterior
        regastarDadosReceita();

        inicializarComponentes();

        configuracoesIniciais();

        recuperarDadosSeguidores();

        abrirCamera();

        abrirGaleriaFotos();

    }

    private void regastarDadosReceita() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            idReceita = (String) bundle.getSerializable("idReceita");
            nomeReceita = (String) bundle.getSerializable("nome");
            ingredientes = (String) bundle.getSerializable("ingredientes");
            modoPreparo = (String) bundle.getSerializable("modoPreparo");
            qtdPessoasServidas = (String) bundle.getSerializable("qtdPessoasServidas");

        }
    }

    private void inicializarComponentes() {
        displayFotoReceita = findViewById(R.id.displayFotoReceita);
        cameraMinhaReceita = findViewById(R.id.cameraMinhaReceita);
        galeriaMinhaReceita = findViewById(R.id.galeriaMinhaReceita);
        progressBarFotoReceita = findViewById(R.id.progressBarNF);
    }

    private void configuracoesIniciais() {
        progressBarFotoReceita.setVisibility(View.GONE);
        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        identificadorChef = UsuarioFirebaseAuth.getIdentificadorChefAuth();
        idChefLogado = UsuarioFirebaseAuth.getIdentificadorChefAuth();
        chefsRef = firebaseRef.child("chefs");
    }

    private void abrirCamera() {

        cameraMinhaReceita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if ( i.resolveActivity(getPackageManager()) != null ){ //se foi possivel acessar a camera
                    startActivityForResult(i, SELECAO_CAMERA); //starta a activity (camera do usuário) e captura o resultado da ação do usuario (resultado: capturar a foto tirada)
                }

            }
        });

    }

    private void abrirGaleriaFotos() {

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

    public void carregarProgressBar(){
        progressBarFotoReceita.setVisibility(View.VISIBLE);
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

                        fotoReceita = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada);

                        break;
                    case SELECAO_CAMERA:
                        fotoReceita = (Bitmap) data.getExtras().get("data"); //dados internos da imagem, 0010011001
                        break;

                }

                //Se a foto foi selecionada ou da camera ou da galeria, temos uma imagem
                if (fotoReceita != null){

                    //A progressBar fica visível após o usuário ter selecionado uma foto
                    carregarProgressBar();

                    //Seta a imagem na tela
                    displayFotoReceita.setImageBitmap(fotoReceita);

                    //Tratamento da Imagem e Conversão (Bitmap -> ByteArray)
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    fotoReceita.compress(Bitmap.CompressFormat.JPEG,75, baos);
                    byte[] dadosImagemReceita = baos.toByteArray();

                    //Salvar imagem firebaseStorage
                    StorageReference fotoReceitaRef = storageRef
                            .child("imagens")
                            .child("receitas")
                            .child(identificadorChef) //idChefAuth
                            .child(idReceita + ".jpg");

                    UploadTask uploadTask = fotoReceitaRef.putBytes(dadosImagemReceita);

                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(NovaReceitaFotoActivity.this,
                                    getApplicationContext().getString(R.string.erro_upload_img),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            recuperarUrlFirebaseStorage(taskSnapshot);

                        }


                    });
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void recuperarUrlFirebaseStorage(UploadTask.TaskSnapshot taskSnapshot) {

        //Recupera a foto da receita do FirebaseStorage
        if (taskSnapshot.getMetadata() != null) {
            if (taskSnapshot.getMetadata().getReference() != null) {
                Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl(); //recupera a foto do firebaseStorage

                result.addOnSuccessListener(new OnSuccessListener<Uri>() {

                    @Override
                    public void onSuccess(Uri uri) {

                        Uri urlImageReceita = uri;

                        adicionarFotoReceitaFirebaseDb(urlImageReceita);

                        publicarPostagem(urlImageReceita);

                        finish();

                    }
                });
            }
        }

    }

    //atualiza o dado Url ao nó de receitas do usuário
    public void adicionarFotoReceitaFirebaseDb(final Uri url) {

        Receitas minhaReceita = new Receitas();

        //recupera o caminho da foto do FirebaseStorage e seta-o na classe Receitas
        minhaReceita.setUrlFotoReceita(url.toString());

        //SALVAR URL NO FIREBASEDB
        //recupera todos os dados não alterados e os reescreve no FirebaseDatabase e também recupera o caminho da foto alterada e a atualiza no FirebaseDatabase
        minhaReceita.adicionarUrlFotoFirebaseDb(idReceita);

        mensagemFotoAdicionada();

        fecharAtividadeAtualAnterior(NovaReceitaInfoActivity.atividadeAberta);

    }

    private void fecharAtividadeAtualAnterior(Activity ativadadeAnterior) {
        //encerra a activity anterior ao clicar no botão atualizar
        ativadadeAnterior.finish();
        //encerra essa activity
        finish();
    }

    private void mensagemFotoAdicionada() {

        Toast.makeText(NovaReceitaFotoActivity.this,
                getApplicationContext().getString(R.string.foto_adicionada),
                Toast.LENGTH_SHORT).show();

    }

    private void recuperarDadosSeguidores(){

        chefLogadoRef = chefsRef.child(idChefLogado);
        chefLogadoRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                        final DatabaseReference seguidoresRef = firebaseRef
                                .child("seguidores")
                                .child(idChefLogado);
                        seguidoresRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                seguidoresSnapshot = dataSnapshot;

                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {              }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {          }
                }
        );
    }

    private void publicarPostagem(Uri caminhoReceita){
        final Postagem postagem = new Postagem();
        postagem.setIdChef(idChefLogado);
        postagem.setUrlPostagem(caminhoReceita.toString());
        postagem.setDataPostagem(DateCustom.dataAtual());

        postagem.setIdReceita(idReceita);
        postagem.setNomeReceita(nomeReceita);
        postagem.setIngredientes(ingredientes);
        postagem.setModoPreparo(modoPreparo);
        postagem.setQtdPessoasServidas(qtdPessoasServidas);

        //Salvar postagem
        if (postagem.salvar(seguidoresSnapshot)) {

            Toast.makeText(NovaReceitaFotoActivity.this,
                    getString(R.string.toast_salvar_postagem),
                    Toast.LENGTH_SHORT).show();
            finish(); //após publicar a foto, a activity filtro é encerrada
        }
    }

}
