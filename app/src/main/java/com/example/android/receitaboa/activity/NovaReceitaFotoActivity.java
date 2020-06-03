package com.example.android.receitaboa.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class NovaReceitaFotoActivity extends AppCompatActivity {

    private AlertDialog dialog;

    private ImageView displayFotoReceita;
    private ImageView cameraMinhaReceita;
    private ImageView galeriaMinhaReceita;
    private ProgressBar progressBarFotoReceita;

    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA = 200;

    private StorageReference storageRef;
    private String identificadorChef;

    private DatabaseReference receitasRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nova_receita_foto);

        //Inicializar componentes
        abrirDialog(); //inicializa ao o usuário chegar a essa tela
        displayFotoReceita = findViewById(R.id.displayFotoReceita);
        cameraMinhaReceita = findViewById(R.id.cameraMinhaReceita);
        galeriaMinhaReceita = findViewById(R.id.galeriaMinhaReceita);
        progressBarFotoReceita = findViewById(R.id.progressBarNR);
        progressBarFotoReceita.setVisibility(View.GONE);

        //Configurações iniciais
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        receitasRef = ConfiguracaoFirebase.getFirebaseDatabase().child("receitas");
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

    public void abrirDialog(){

        //Instancia AlertDialog
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        //Configura titulo e mensagem
        dialog.setTitle(getApplicationContext().getString(R.string.dialog_titulo_devo_adicionar_foto_receita));
        dialog.setMessage(getApplicationContext().getString(R.string.dialog_msg_devo_adicionar_foto_receita));

        //Configura cancelamento
        dialog.setCancelable(false); //false: se clicar fora do alerta do dialog, o alerta não é fechado e o usuário precisa clicar em sim ou não

        //Configura ações para o sim e o não
        dialog.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Validar permissões (para acesso da camera e da galeria de fotos do usuário)
                Permissao.validarPermissoes(permissoesNecessarias,NovaReceitaFotoActivity.this,1);

            }
        });

        dialog.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.nova_receita_adicionada), Toast.LENGTH_SHORT).show();

                //encerra a activity anterior ao salvar a foto da receita
                NovaReceitaInfoActivity.addInfoReceita.finish();

                finish();
            }
        });

        //Criar e exibir AlertDialog
        dialog.create();
        dialog.show();

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
                            Toast.makeText(NovaReceitaFotoActivity.this,
                                    getApplicationContext().getString(R.string.erro_upload_img),
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

                                            adicionarFotoReceitaFirebaseDb(urlImageReceita);

                                            finish();

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

   //atualiza o dado Url ao nó de receitas do usuário
    public void adicionarFotoReceitaFirebaseDb(final Uri url){

        //Primeiro recuperamos o id da última receita gerada no firebase
        DatabaseReference receitasIdChefaoRef = receitasRef.child(identificadorChef);
        Query receitaIdRef = receitasIdChefaoRef.orderByKey().limitToLast(1);  //Recupera o nó com o id da ultima Receita gerada na tela anterior ao salvar as informações da receita no FirebaseDb

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot dado: dataSnapshot.getChildren()){

                    String idReceitaRecuperada = dado.getKey(); //recupera o id da última receita criada no firebaseDb

                    Receitas minhaReceita = new Receitas();

                    //recupera o caminho da foto do FirebaseStorage e seta-o na classe Receitas
                    minhaReceita.setUrlFotoReceita(url.toString());

                    //SALVAR URL NO FIREBASEDB
                    //recupera todos os dados não alterados e os reescreve no FirebaseDatabase e também recupera o caminho da foto alterada e a atualiza no FirebaseDatabase
                    minhaReceita.adicionarUrlFotoFirebaseDb(idReceitaRecuperada);

                    mensagemFotoAdicionada();

                    //encerra a activity anterior ao salvar a foto da receita
                    NovaReceitaInfoActivity.addInfoReceita.finish();

                    finish();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        receitaIdRef.addListenerForSingleValueEvent(valueEventListener);
    }

    private void mensagemFotoAdicionada() {

        Toast.makeText(NovaReceitaFotoActivity.this,
                getApplicationContext().getString(R.string.foto_adicionada),
                Toast.LENGTH_SHORT).show();

    }
}
