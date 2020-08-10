package com.example.android.receitaboa.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.receitaboa.R;
import com.example.android.receitaboa.helper.ConfiguracaoFirebase;
import com.example.android.receitaboa.helper.Permissao;
import com.example.android.receitaboa.helper.UsuarioFirebaseAuth;
import com.example.android.receitaboa.model.Chef;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConfiguracoesActivity extends AppCompatActivity {

    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private ImageButton imageButtonCamera, imageButtonGaleria;
    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA = 200;
    private CircleImageView circleImageViewPerfil;
    private EditText editPerfilNome;
    private StorageReference storageReference;
    private String idChefLogado;
    private Chef chefLogado;
    private ImageView botaoAtualizarNome;

    public List<String> listaSeguidoresUserLogado = new ArrayList<>();
    private DatabaseReference firebaseDb, seguidoresRef, amigosRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes);

        configuracoesIniciais();

        inicializarComponentes();

        configuracaoToolbar();

        resgatarDadosChefLogado();

        eventoClickCamera();

        eventoClickGaleriaFotos();

        eventoAtualizarNomeFirebaseDb();

    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarIdSeguidores();
    }

    private void configuracoesIniciais() {
        Permissao.validarPermissoes(permissoesNecessarias, this, 1);

        storageReference = ConfiguracaoFirebase.getFirebaseStorage();
        idChefLogado = UsuarioFirebaseAuth.getIdentificadorChefAuth(); //recupera o id do usuario (que é o email codificiado em base64) [email recuperado do FirebaseAuth]
        chefLogado = UsuarioFirebaseAuth.getDadosChefLogadoAuth();

        firebaseDb = ConfiguracaoFirebase.getFirebaseDatabase();
        seguidoresRef = firebaseDb.child("seguidores").child(idChefLogado);
        amigosRef = firebaseDb.child("amigos");
    }

    private void inicializarComponentes() {
        imageButtonCamera = findViewById(R.id.imageButtonCamera);
        imageButtonGaleria = findViewById(R.id.imageButtonGaleria);
        circleImageViewPerfil = findViewById(R.id.circleImageViewFotoPerfil);
        editPerfilNome = findViewById(R.id.editPerfilNome);
        botaoAtualizarNome = findViewById(R.id.botaoAtualizarNome);
    }

    private void configuracaoToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Configurações");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //botao voltar
    }

    private void resgatarDadosChefLogado() {

        //Recuperar dados do usuário do FirebaseUser (FirebaseAuth)
        FirebaseUser chefAuth = UsuarioFirebaseAuth.getChefAtualAuth(); //FirebaseAuth
        Uri urlAuth = chefAuth.getPhotoUrl(); //FirebaseAuth

        //Recupera a imagem do FirebaseUser (Auth) e carrega no perfil do usuário
        if (urlAuth != null){
            Glide.with(ConfiguracoesActivity.this)
                    .load(urlAuth) //carrega o caminho da foto que foi recuperado da classe FirebaseUser
                    .into(circleImageViewPerfil);
        }else {
            circleImageViewPerfil.setImageResource(R.drawable.avatar);
        }

        //carrega o nome do usuário na editText
        editPerfilNome.setText(chefAuth.getDisplayName());

    }

    private void eventoClickCamera() {

        imageButtonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if ( i.resolveActivity(getPackageManager()) != null ){ //se foi possivel acessar a camera
                    startActivityForResult(i, SELECAO_CAMERA); //starta a activity (camera do usuário) e captura o resultado da ação do usuario (resultado: capturar a foto tirada)
                }

            }
        });

    }

    private void eventoClickGaleriaFotos() {

        imageButtonGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); //escolher a foto da galeria no local onde se encontra a galeria de um celular

                if ( i.resolveActivity(getPackageManager()) != null ){ //se foi possivel acessar a galeria
                    startActivityForResult(i, SELECAO_GALERIA); //starta a activity (galeria de imagens) e capta o resultado da ação do usuario (resultado: selecionada a imagem da galeria)
                }

            }
        });

    }

    private void eventoAtualizarNomeFirebaseDb() {

        //Renomeia o nome do usuário no App e no autualiza no FirebaseDatabase
        botaoAtualizarNome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String nome = editPerfilNome.getText().toString();
                boolean retorno = UsuarioFirebaseAuth.atualizarNomeChefAuth(nome);
                if (retorno){

                    chefLogado.setNome(nome); //seta o nome que foi alterado pelo chef na Classe Chef
                    chefLogado.atualizarDadosFirebaseDb(); //recupera todos os dados já configurados (email, foto) e o nome que foi alterado e atualiza no FirebaseDatabase

                    Toast.makeText(ConfiguracoesActivity.this,"Nome alterado com sucesso!",Toast.LENGTH_SHORT).show();

                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { //resultado da seleção da imagem através da Intent
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){ //se recuperou os dados da imagem corretamente
            Bitmap imagem = null;

            try {

                switch (requestCode){
                    case SELECAO_CAMERA:
                        imagem = (Bitmap) data.getExtras().get("data"); //dados: imagem
                        break;
                    case SELECAO_GALERIA:
                        Uri localImagemSelecionada = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada); //Depreciado no API 29 (melhor deixar seu android studio no API 28)
                        /*
                        ImageDecoder.Source imgSource = ImageDecoder.createSource(getContentResolver(),localImagemSelecionada); //PRIMEIRO: Cria Source
                        imagem = ImageDecoder.decodeBitmap(imgSource); //SEGUNDO: converte ImageDecoder.Source -> Bitmap
                         */

                        break;
                }

                if (imagem != null){

                    circleImageViewPerfil.setImageBitmap(imagem);

                    //Recuperar dados da imagem selecionada pelo usuário
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG,70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    //Salvar imagem no FirebaseStorage
                    StorageReference imagemRef = storageReference
                            .child("imagens") //pasta
                            .child("perfil")
                            .child(idChefLogado + ".jpeg");

                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem); //upload para o FirebaseStorage

                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ConfiguracoesActivity.this,"Erro ao fazer upload da imagem!",Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(ConfiguracoesActivity.this,"Upload da imagem feito com sucesso!",Toast.LENGTH_SHORT).show();

                            //Uri url = taskSnapshot.getDownloadUrl() DEPRECIADO no FirebaseStorage
                            //Recupera a foto do chef do FirebaseStorage
                            if (taskSnapshot.getMetadata() != null) {
                                if (taskSnapshot.getMetadata().getReference() != null) {
                                    Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl(); //recupera a foto do firebaseStorage
                                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri url) {
                                            Uri imageUrlAuth = url;

                                            atualizarFotoChefAuth(imageUrlAuth); //atualiza o caminho da foto do chef no FibaseUser (Auth)
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

    //adiciona o caminho da foto do usuario no FirebaseUser (Auth) e ao FirebaseDb ao ele trocar de foto ou adicionar uma foto
    public void atualizarFotoChefAuth(Uri urlFotoChefAuth){

        boolean retorno = UsuarioFirebaseAuth.atualizarFotoChefAuth(urlFotoChefAuth); //atualizando o caminho do foto no FirebaseAuth
        if (retorno){

            chefLogado.setUrlFotoChefAuth(urlFotoChefAuth.toString()); //recupera o caminho da foto do FirebaseAuth e seta-o na classe Chef
            chefLogado.atualizarDadosFirebaseDb(); //recupera todos os dados não alterados e os reescreve no FirebaseDatabase e também recupera o caminho da foto alterada e a atualiza no FirebaseDatabase

            //atualiza a foto de perfil para seus seguidores
            for (String idAmigo : listaSeguidoresUserLogado){

                chefLogado.atualizarMeusDadosSeguidores(idAmigo);

            }
            Toast.makeText(ConfiguracoesActivity.this,"Sua foto foi atualizada com sucesso!", Toast.LENGTH_SHORT).show();
        }
    }

    public void recuperarIdSeguidores(){

        seguidoresRef.addValueEventListener(new ValueEventListener(){

            @Override
            public void onDataChange(@NonNull final DataSnapshot ds) {


                for (DataSnapshot dados: ds.getChildren()){

                    String idSeguidor = dados.getKey();

                    listaSeguidoresUserLogado.add(idSeguidor);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {   }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResultado : grantResults){
            if (permissaoResultado == PackageManager.PERMISSION_DENIED){ //se o usuario negar a utilização da camera e/ou da galeria de imagens
                alertaValidacaoPermissao();
            }
        }

    }

    private void alertaValidacaoPermissao(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões do uso da Câmera e da Galeria de Imagens");
        builder.setCancelable(false); //impede que o usuario feche o dialog apertando fora da dialog
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
