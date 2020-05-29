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
        dialog.setTitle("Foto da Receita");
        dialog.setMessage("Quer adicionar a foto da sua Receita agora?\n\n Se não tiver a foto ainda, não se preocupe, você pode adicioná-la após preparar a sua receita.");

        //Configura cancelamento
        dialog.setCancelable(false); //false: se clicar fora do alerta do dialog, o alerta não é fechado e o usuário precisa clicar em sim ou não

        //Configura icone
        //dialog.setIcon(android.R.drawable.btn_star);

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

                //Retorna para a main activity se o usuário decidir adicionar uma foto da receita no futuro
                Intent i = new Intent(NovaReceitaFotoActivity.this, MainActivity.class);
                startActivity(i);

                Toast.makeText(getApplicationContext(), "Sua Receita Boa foi adicionada com sucesso!", Toast.LENGTH_SHORT).show();
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
                        ImageDecoder.Source imgSource = ImageDecoder.createSource(getContentResolver(), localImagemSelecionada); //Primeiro cria a source
                        fotoReceita = ImageDecoder.decodeBitmap(imgSource); //SEGUNDO: converte ImageDecoder.Source -> Bitmap
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

                                        ///////////////////////////////

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

                    //Retorna para a main activity após o usuário selecionar uma foto
                    Intent i = new Intent(NovaReceitaFotoActivity.this, MainActivity.class);
                    startActivity(i);

                    Toast.makeText(NovaReceitaFotoActivity.this,"Sua foto foi adicionada com sucesso!",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        receitaIdRef.addListenerForSingleValueEvent(valueEventListener);
    }
}
