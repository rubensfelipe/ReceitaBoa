package com.example.android.receitaboa.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.android.receitaboa.R;
import com.example.android.receitaboa.helper.ConfiguracaoFirebase;
import com.example.android.receitaboa.helper.Permissao;
import com.example.android.receitaboa.helper.UsuarioFirebaseAuth;
import com.example.android.receitaboa.model.Receitas;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nova_receita_foto);

        //Recupera o id da receita gerada na tela anterior
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) { idReceita = (String) bundle.getSerializable("idReceita");    }

        //Inicializar componentes
        displayFotoReceita = findViewById(R.id.displayFotoReceita);
        cameraMinhaReceita = findViewById(R.id.cameraMinhaReceita);
        galeriaMinhaReceita = findViewById(R.id.galeriaMinhaReceita);
        progressBarFotoReceita = findViewById(R.id.progressBarNR);
        progressBarFotoReceita.setVisibility(View.GONE);

        //Configurações iniciais
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        identificadorChef = UsuarioFirebaseAuth.getIdentificadorChefAuth();

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
    protected void onStart() {
        super.onStart();
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

                    //salvarImagemDirCelular(fotoReceita);

                    //Seta a imagem na tela
                    displayFotoReceita.setImageBitmap(fotoReceita);

                    //Tratamento da Imagem e Conversão (Bitmap -> ByteArray)
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    fotoReceita.compress(Bitmap.CompressFormat.JPEG,75,baos);
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


    private void salvarImagemDirCelular(Bitmap imgFoto) throws IOException {

        criarDiretorioImagem();

        criarArquivoImagem();

        salvarArquivoImagem(imgFoto);

    }

    public File criarDiretorioImagem() {

        File filePath = Environment.getExternalStorageDirectory();
        File imageDir = new File(filePath.getAbsolutePath() + "/Receita Boa/" + idReceita); //caminho onde será salva a imagem na memoria interna do celular
        imageDir.mkdir();

        return imageDir;
    }

    public File criarArquivoImagem() throws IOException {
        String nomeImagem = idReceita + ".jpg";

        File imgFile = new File(criarDiretorioImagem(), nomeImagem);

        if (!imgFile.exists()) {
            imgFile.createNewFile(); //cria uma pasta com o arquivo a imagem dentro da galeria de fotos
        }
        return imgFile;
    }

    private void salvarArquivoImagem(Bitmap imgFoto) {

        try {
            FileOutputStream outputStream = new FileOutputStream(criarArquivoImagem());
            imgFoto.compress(Bitmap.CompressFormat.JPEG, 75, outputStream);
            Toast.makeText(getApplicationContext(), "Image save in internal storage", Toast.LENGTH_SHORT).show();

            outputStream.flush();
            outputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
