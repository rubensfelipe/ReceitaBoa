package com.example.android.receitaboa.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.receitaboa.R;
import com.example.android.receitaboa.helper.ConfiguracaoFirebase;
import com.example.android.receitaboa.helper.Permissao;
import com.example.android.receitaboa.helper.UsuarioFirebaseAuth;
import com.example.android.receitaboa.model.Receitas;
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

public class EditarReceitaActivity extends AppCompatActivity {

    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA = 200;

    private AlertDialog dialog;
    private EditText atualizarNome;
    private EditText atualizarIngredientes;
    private EditText atualizarModoPreparo;
    private EditText atualizarQtdPessoasServidas;
    private ImageView botaoAtualizarCamera;
    private ImageView botaoAtualizarGaleria;
    private ImageView displayAtualizarFotoReceita;
    private Button botaoAtualizarReceita;

    private String receitaNome;
    private String receitaIngredientes;
    private String receitaModoPreparo;
    private String receitaQtdPessoasServidas;
    private String receitaFoto;
    private String receitaId;

    private StorageReference storageRef;
    private String identificadorChef;

    private Receitas receitaAtual;

    private DatabaseReference receitasRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_receita);

        inicializarComponentes();

        //Configurações iniciais
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        receitasRef = ConfiguracaoFirebase.getFirebaseDatabase().child("receitas");
        identificadorChef = UsuarioFirebaseAuth.getIdentificadorChefAuth();
        receitaAtual = new Receitas(); //ao iniciar essa tela, a classe Receitas será instanciada para uso

        //Validar permissões de Câmera e Galeria de Imagens
        Permissao.validarPermissoes(permissoesNecessarias, this, 1);

        botaoAtualizarCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if ( i.resolveActivity(getPackageManager()) != null ){ //se foi possivel acessar a camera
                    startActivityForResult(i, SELECAO_CAMERA); //starta a activity (camera do usuário) e captura o resultado da ação do usuario (resultado: capturar a foto tirada)
                }

            }
        });

        botaoAtualizarGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); //escolher a foto da galeria no local onde se encontra a galeria de um celular

                if ( i.resolveActivity(getPackageManager()) != null ){ //se foi possivel acessar a galeria
                    startActivityForResult(i, SELECAO_GALERIA); //starta a activity (galeria de imagens) e capta o resultado da ação do usuario (resultado: selecionada a imagem da galeria)
                }

            }
        });

        //Recupera os dados da Receita visualizada
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            receitaId = (String) bundle.getSerializable("idR");

            receitaNome = (String) bundle.getSerializable("nome");
            atualizarNome.setText(receitaNome);

            receitaIngredientes = (String) bundle.getSerializable("ingredientes");
            atualizarIngredientes.setText(receitaIngredientes);

            receitaModoPreparo = (String) bundle.getSerializable("modoPreparo");
            atualizarModoPreparo.setText(receitaModoPreparo);

            receitaQtdPessoasServidas = (String) bundle.getSerializable("qtdPessoasServidas");
            atualizarQtdPessoasServidas.setText(receitaQtdPessoasServidas);

            receitaFoto = (String) bundle.getSerializable("urlFoto");
            receitaAtual.setUrlFotoReceita(receitaFoto);

            if (receitaFoto != null){
                Uri url = Uri.parse(receitaFoto);
                Glide.with(EditarReceitaActivity.this)
                        .load(url)
                        .into(displayAtualizarFotoReceita);
            }else{
                displayAtualizarFotoReceita.setImageResource(R.drawable.cloche_tableware);
            }
        }

        //Atualiza os dados da receita no FirebaseDb
        botaoAtualizarReceita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String nome = atualizarNome.getText().toString();
                String ingredientes = atualizarIngredientes.getText().toString();
                String modoPreparo = atualizarModoPreparo.getText().toString();
                String qtdPessoasServidas = atualizarQtdPessoasServidas.getText().toString();

                receitaAtual.setNome(nome);
                receitaAtual.setIngredientes(ingredientes);
                receitaAtual.setModoPreparo(modoPreparo);
                receitaAtual.setQtdPessoasServidas(qtdPessoasServidas);

                receitaAtual.atualizarReceitaFirebaseDb(receitaId);

                Toast.makeText(EditarReceitaActivity.this,"Receita " + nome + " atualizada!",Toast.LENGTH_SHORT).show();

                Intent i = new Intent(EditarReceitaActivity.this, MainActivity.class);
                startActivity(i);

                finish();

            }

        });

    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_OK){
            Bitmap fotoReceita = null;

            try {

                switch (requestCode){
                    case SELECAO_GALERIA:
                        Uri localImagemSelecionada = data.getData();

                        fotoReceita = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada); //(depreciado API 29) MAS FUNCIONA A PARTIR DO API 16 EM DIANTE E FUNCIONA NOS APARELHOS API 29 em diante

                        //ImageDecoder.Source imgSource = ImageDecoder.createSource(getContentResolver(), localImagemSelecionada); //Primeiro cria a source
                        //fotoReceita = ImageDecoder.decodeBitmap(imgSource); //SEGUNDO: converte ImageDecoder.Source -> Bitmap //PARA O API 29 (PROBLEMA: SÓ FUNCIONA EM APARELHOS COM API MIN 28 - PIE - ANDROID 9)

                        break;
                    case SELECAO_CAMERA:
                        fotoReceita = (Bitmap) data.getExtras().get("data"); //dados internos da imagem, 0010011001
                        break;
                }

                //Se a foto foi selecionada ou da camera ou da galeria, temos uma imagem
                if (fotoReceita != null){

                    //Seta a imagem na tela
                    displayAtualizarFotoReceita.setImageBitmap(fotoReceita);

                    //Tratamento da Imagem e Conversão (Bitmap -> ByteArray)
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    fotoReceita.compress(Bitmap.CompressFormat.JPEG,75,baos);
                    byte[] dadosImagemReceita = baos.toByteArray();

                    //Salvar imagem firebaseStorage
                    StorageReference fotoReceitaRef = storageRef
                            .child("imagens")
                            .child("receitas")
                            .child(identificadorChef) //idChefAuth
                            .child(receitaId + ".jpg");

                    UploadTask uploadTask = fotoReceitaRef.putBytes(dadosImagemReceita);
                    //abrirDialogCarregamento("Carregando a foto, aguarde!"); //impede o que o chef atrapalhe o processo de salvar a imagem no FirebaseStorage

                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditarReceitaActivity.this,
                                    "Erro ao salvar a imagem, tente novamente!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Toast.makeText(EditarReceitaActivity.this,"Foto Carregada!",Toast.LENGTH_SHORT).show();

                            //Recupera a foto da receita do FirebaseStorage
                            if (taskSnapshot.getMetadata() != null) {
                                if (taskSnapshot.getMetadata().getReference() != null) {
                                    Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl(); //recupera a foto do firebaseStorage

                                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {

                                        @Override
                                        public void onSuccess(Uri uri) {

                                            Uri urlReceitaAtualizada = uri;

                                            receitaAtual.setUrlFotoReceita(urlReceitaAtualizada.toString());

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

    private void abrirDialogCarregamento(String titulo){ //para que os dados da imagem possam ser salvos no firebaseStorage após o chef selecionar a foto na camera ou galeria

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(titulo);
        alert.setCancelable(false); //impedi que o usuario cancele o pop-up
        alert.setView(R.layout.carregamento); //layout com apenas uma progressbar

        dialog = alert.create();
        dialog.show();

    }

    private void inicializarComponentes() {
        botaoAtualizarReceita = findViewById(R.id.botaoAtualizarReceita);
        atualizarNome = findViewById(R.id.atualizarNomeReceita);
        atualizarIngredientes = findViewById(R.id.atualizarIngredientes);
        atualizarModoPreparo = findViewById(R.id.atualizarModoPreparo);
        atualizarQtdPessoasServidas = findViewById(R.id.atualizarQtdPessoasServidas);
        botaoAtualizarCamera = findViewById(R.id.cameraAtualizarReceita);
        botaoAtualizarGaleria = findViewById(R.id.galeriaAtualizarReceita);
        displayAtualizarFotoReceita = findViewById(R.id.displayAtualizarFoto);
    }
}
