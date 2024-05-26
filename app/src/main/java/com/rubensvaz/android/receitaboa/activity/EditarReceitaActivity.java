package com.rubensvaz.android.receitaboa.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.rubensfelipe.android.receitaboa.R;
import com.rubensvaz.android.receitaboa.helper.ConfiguracaoFirebase;
import com.rubensvaz.android.receitaboa.helper.DateCustom;
import com.rubensvaz.android.receitaboa.helper.UsuarioFirebaseAuth;
import com.rubensvaz.android.receitaboa.model.Chef;
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

public class EditarReceitaActivity extends AppCompatActivity {

    String permission[] = {Manifest.permission.CAMERA};

    private static final String TAG = "EditarReceitaActivity";

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 2;

    private AlertDialog dialog;

    private Bitmap fotoReceita;
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

    private String identificadorChef;
    private String idChefLogado;
    private StorageReference storageRef;
    private DataSnapshot seguidoresSnapshot;
    private DatabaseReference chefsRef;
    private DatabaseReference chefLogadoRef;
    private DatabaseReference firebaseRef;

    private Chef chefLogado;

    private Receitas receitaAtual;

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_receita);

        Log.d(TAG, "onCreate: Activity criada");

        // Validar permissões de Câmera e Galeria de Imagens
        verificarPermissoes();

        inicializarComponentes();
        configuracoesIniciais();
        configurarLaunchers();
        configurarListeners();
        recuperarDadosSeguidores();

        // Recupera os dados da Receita visualizada
        resgatarDadosReceita();

        // Atualiza os dados da receita no FirebaseDb
        configuracaoEventoBotaoAtualizar();
    }

    private void verificarPermissoes() {
        Log.d(TAG, "verificarPermissoes: Verificando permissões");

        boolean cameraPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean storagePermissionGranted;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            storagePermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            storagePermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }

        if (!cameraPermissionGranted || !storagePermissionGranted) {
            Log.d(TAG, "verificarPermissoes: Permissões não concedidas, solicitando permissões");

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ||
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Mostrar uma explicação ao usuário
                new AlertDialog.Builder(this)
                        .setTitle("Permissões Necessárias")
                        .setMessage("Este aplicativo precisa de permissões de câmera e armazenamento para funcionar corretamente.")
                        .setPositiveButton("Conceder", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    ActivityCompat.requestPermissions(EditarReceitaActivity.this,
                                            new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES},
                                            REQUEST_CAMERA_PERMISSION);
                                } else {
                                    ActivityCompat.requestPermissions(EditarReceitaActivity.this,
                                            new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                                            REQUEST_CAMERA_PERMISSION);
                                }
                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mostrarToast("Permissões de câmera e armazenamento são necessárias");
                            }
                        })
                        .show();
            } else {
                // Solicitar permissões diretamente
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES},
                            REQUEST_CAMERA_PERMISSION);
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CAMERA_PERMISSION);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            boolean cameraPermissionGranted = false;
            boolean storagePermissionGranted = false;

            if (grantResults.length > 0) {
                cameraPermissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    storagePermissionGranted = grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED;
                } else {
                    storagePermissionGranted = grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED;
                }
            }

            if (cameraPermissionGranted && storagePermissionGranted) {
                Log.d(TAG, "onRequestPermissionsResult: Permissões concedidas");
                mostrarToast("Permissões concedidas");
            } else {
                Log.d(TAG, "onRequestPermissionsResult: Permissões negadas");
                mostrarToast("Permissões de câmera e armazenamento são necessárias");
            }
        }
    }

    private void configuracoesIniciais() {
        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        identificadorChef = UsuarioFirebaseAuth.getIdentificadorChefAuth();
        receitaAtual = new Receitas();
        idChefLogado = UsuarioFirebaseAuth.getIdentificadorChefAuth();
        chefsRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios");
    }

    private void configurarLaunchers() {
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Bitmap fotoReceita = (Bitmap) result.getData().getExtras().get("data");
                processarImagem(fotoReceita);
            }
        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri localImagemSelecionada = result.getData().getData();
                try {
                    Bitmap fotoReceita = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada);
                    processarImagem(fotoReceita);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void configurarListeners() {
        Log.d(TAG, "configurarListeners: Configurando listeners");

        botaoAtualizarCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(EditarReceitaActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraLauncher.launch(i);
                } else {
                    Log.d(TAG, "Permissão de câmera não concedida");
                    mostrarToast("Permissão de câmera não concedida");
                    ActivityCompat.requestPermissions(EditarReceitaActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            REQUEST_CAMERA_PERMISSION);
                }
            }
        });

        botaoAtualizarGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryLauncher.launch(i);
            }
        });

    }

    private void resgatarDadosReceita() {
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

            if (receitaFoto != null) {
                Uri url = Uri.parse(receitaFoto);
                Glide.with(EditarReceitaActivity.this)
                        .load(url)
                        .into(displayAtualizarFotoReceita);
            } else {
                displayAtualizarFotoReceita.setImageResource(R.drawable.turkey_roast_3);
            }
        }
    }

    private void configuracaoEventoBotaoAtualizar() {
        botaoAtualizarReceita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fecharTeclado();
                String nome = atualizarNome.getText().toString();
                String ingredientes = atualizarIngredientes.getText().toString();
                String modoPreparo = atualizarModoPreparo.getText().toString();
                String qtdPessoasServidas = atualizarQtdPessoasServidas.getText().toString();
                receitaAtual.setNome(nome);
                receitaAtual.setIngredientes(ingredientes);
                receitaAtual.setModoPreparo(modoPreparo);
                receitaAtual.setQtdPessoasServidas(qtdPessoasServidas);
                receitaAtual.atualizarReceitaFirebaseDb(receitaId);

                if (receitaFoto == null) {
                    if (receitaAtual.getUrlFotoReceita() != null) {
                        publicarPostagem(receitaAtual.getUrlFotoReceita());
                    }
                }

                Toast.makeText(EditarReceitaActivity.this,
                        getString(R.string.receita) + nome + getString(R.string.atualizada),
                        Toast.LENGTH_SHORT).show();

                fecharAtividadeAtualAnterior(VisualizarReceitaActivity.atividadeAberta);
            }
        });
    }

    private void fecharTeclado() {
        atualizarNome.onEditorAction(EditorInfo.IME_ACTION_DONE);
        atualizarIngredientes.onEditorAction(EditorInfo.IME_ACTION_DONE);
        atualizarModoPreparo.onEditorAction(EditorInfo.IME_ACTION_DONE);
        atualizarQtdPessoasServidas.onEditorAction(EditorInfo.IME_ACTION_DONE);
    }

    private void fecharAtividadeAtualAnterior(Activity ativadadeAnterior) {
        ativadadeAnterior.finish();
        finish();
    }

    private void processarImagem(Bitmap fotoReceita) {
        if (fotoReceita != null) {
            abrirDialogCarregamento(getApplicationContext().getString(R.string.dialog_titulo_carregando_foto));
            displayAtualizarFotoReceita.setImageBitmap(fotoReceita);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            fotoReceita.compress(Bitmap.CompressFormat.JPEG, 75, baos);
            byte[] dadosImagemReceita = baos.toByteArray();

            StorageReference fotoReceitaJPGRef = storageRef
                    .child("imagens")
                    .child("receitas")
                    .child(identificadorChef)
                    .child(receitaId + ".jpg");

            UploadTask uploadTask = fotoReceitaJPGRef.putBytes(dadosImagemReceita);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.dismiss();
                    mensagemErroUpload();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    dialog.dismiss();
                    mensagemSucessoUpload();
                    recuperarUrlFirebaseStorage(taskSnapshot);
                }
            });
        }
    }

    private void recuperarUrlFirebaseStorage(UploadTask.TaskSnapshot taskSnapshot) {
        if (taskSnapshot.getMetadata() != null && taskSnapshot.getMetadata().getReference() != null) {
            Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Uri urlFotoReceitaAtualizada = uri;
                    receitaAtual.setUrlFotoReceita(urlFotoReceitaAtualizada.toString());
                }
            });
        }
    }

    private void mensagemErroUpload() {
        Toast.makeText(EditarReceitaActivity.this,
                getApplicationContext().getString(R.string.erro_upload_img),
                Toast.LENGTH_SHORT).show();
    }

    private void mensagemSucessoUpload() {
        Toast.makeText(EditarReceitaActivity.this,
                getApplicationContext().getString(R.string.foto_carregada),
                Toast.LENGTH_SHORT).show();
    }

    private void abrirDialogCarregamento(String titulo) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(titulo);
        alert.setCancelable(false); //impede que o usuario cancele o pop-up
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

    private void recuperarDadosSeguidores(){
        chefLogadoRef = chefsRef.child(idChefLogado);
        chefLogadoRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        chefLogado = dataSnapshot.getValue(Chef.class); //recupera os dados do usuario logado
                        //Recuperar os seguidores do usuario logado (para poder postar as fotos no feed dos seus seguidores)
                        final DatabaseReference seguidoresRef = firebaseRef
                                .child("seguidores")
                                .child(idChefLogado);
                        seguidoresRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                seguidoresSnapshot = dataSnapshot; //recupera os dados dos seguidores
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) { }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                }
        );
    }

    private void publicarPostagem(String caminhoReceita){
        final Postagem postagem = new Postagem();
        postagem.setIdChef(idChefLogado);
        postagem.setUrlPostagem(caminhoReceita);
        postagem.setDataPostagem(DateCustom.dataAtual());
        postagem.setIdReceita(receitaId);
        postagem.setNomeReceita(receitaNome);
        postagem.setIngredientes(receitaIngredientes);
        postagem.setModoPreparo(receitaModoPreparo);
        postagem.setQtdPessoasServidas(receitaQtdPessoasServidas);

        //Salvar postagem
        if (postagem.salvar(seguidoresSnapshot)) {
            Toast.makeText(EditarReceitaActivity.this,
                    getString(R.string.toast_salvar_postagem),
                    Toast.LENGTH_SHORT).show();
            finish(); //após publicar a foto
        }
    }

    private void mostrarToast(String mensagem) {
        Log.d(TAG, "mostrarToast: " + mensagem);
        runOnUiThread(() -> Toast.makeText(EditarReceitaActivity.this, mensagem, Toast.LENGTH_SHORT).show());
    }
}
