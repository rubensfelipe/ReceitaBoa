package com.rubensvaz.android.receitaboa.activity;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rubensfelipe.android.receitaboa.R;
import com.rubensvaz.android.receitaboa.helper.ConfiguracaoFirebase;
import com.rubensvaz.android.receitaboa.helper.DateCustom;
import com.rubensvaz.android.receitaboa.helper.UsuarioFirebaseAuth;
import com.rubensvaz.android.receitaboa.model.Postagem;
import com.rubensvaz.android.receitaboa.model.Receitas;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

public class NovaReceitaFotoActivity extends AppCompatActivity {

    private ImageView displayFotoReceita;
    private ImageView cameraMinhaReceita;
    private ImageView galeriaMinhaReceita;
    private ProgressBar progressBarFotoReceita;

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 2;

    private StorageReference storageRef;
    private String identificadorChef;
    private String idReceita;
    private String nomeReceita;
    private String ingredientes;
    private String modoPreparo;
    private String qtdPessoasServidas;

    private String idChefLogado;
    private DatabaseReference firebaseRef;
    private DatabaseReference chefLogadoRef;
    private DataSnapshot seguidoresSnapshot;

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    public NovaReceitaFotoActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nova_receita_foto);

        regastarDadosReceita();
        inicializarComponentes();
        configuracoesIniciais();
        verificarPermissoes();
        configurarLaunchers();
        configurarListeners();
        recuperarDadosSeguidores();
    }

    private void regastarDadosReceita() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            idReceita = (String) bundle.getSerializable("idReceita");
            nomeReceita = bundle.getString("nome");
            ingredientes = bundle.getString("ingredientes");
            modoPreparo = bundle.getString("modoPreparo");
            qtdPessoasServidas = bundle.getString("qtdPessoasServidas");
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
        chefLogadoRef = firebaseRef.child("chefs").child(idChefLogado);
    }

    private void verificarPermissoes() {
        boolean cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        if (!cameraPermission || !storagePermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    private void configurarLaunchers() {
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Bitmap fotoReceita = (Bitmap) Objects.requireNonNull(result.getData().getExtras()).get("data");
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
        cameraMinhaReceita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(NovaReceitaFotoActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    abrirCamera();
                } else {
                    ActivityCompat.requestPermissions(NovaReceitaFotoActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    mostrarToast("Permissão de camera não concedida");
                }
            }
        });

        galeriaMinhaReceita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirGaleria();
            }
        });
    }

    private void abrirCamera() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(i);
    }

    private void abrirGaleria() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(i);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mostrarToast("Permissão de câmera concedida");
            } else {
                Toast.makeText(this, "Permissão de câmera necessária", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mostrarToast("Permissão de armazenamento concedida");
            } else {
                Toast.makeText(this, "Permissão de armazenamento necessária", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void processarImagem(Bitmap fotoReceita) {
        if (fotoReceita != null) {
            carregarProgressBar();
            displayFotoReceita.setImageBitmap(fotoReceita);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            fotoReceita.compress(Bitmap.CompressFormat.JPEG, 75, baos);
            byte[] dadosImagemReceita = baos.toByteArray();

            StorageReference fotoReceitaRef = storageRef
                    .child("imagens")
                    .child("receitas")
                    .child(identificadorChef)
                    .child(idReceita + ".jpg");

            UploadTask uploadTask = fotoReceitaRef.putBytes(dadosImagemReceita);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(NovaReceitaFotoActivity.this, getApplicationContext().getString(R.string.erro_upload_img), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    recuperarUrlFirebaseStorage(taskSnapshot);
                }
            });
        }
    }

    public void carregarProgressBar() {
        progressBarFotoReceita.setVisibility(View.VISIBLE);
    }

    private void recuperarUrlFirebaseStorage(UploadTask.TaskSnapshot taskSnapshot) {
        if (taskSnapshot.getMetadata() != null && taskSnapshot.getMetadata().getReference() != null) {
            Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
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

    public void adicionarFotoReceitaFirebaseDb(final Uri url) {
        Receitas minhaReceita = new Receitas();
        minhaReceita.setUrlFotoReceita(url.toString());
        minhaReceita.adicionarUrlFotoFirebaseDb(idReceita);
        mensagemFotoAdicionada();
        fecharAtividadeAtualAnterior(NovaReceitaInfoActivity.atividadeAberta);
    }

    private void recuperarDadosSeguidores() {
        chefLogadoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                DatabaseReference seguidoresRef = firebaseRef.child("seguidores").child(idChefLogado);
                seguidoresRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        seguidoresSnapshot = dataSnapshot;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Tratar o erro aqui
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Tratar o erro aqui
            }
        });
    }

    private void publicarPostagem(Uri caminhoReceita) {
        final Postagem postagem = new Postagem();
        postagem.setIdChef(idChefLogado);
        postagem.setUrlPostagem(caminhoReceita.toString());
        postagem.setDataPostagem(DateCustom.dataAtual());

        postagem.setIdReceita(idReceita);
        postagem.setNomeReceita(nomeReceita);
        postagem.setIngredientes(ingredientes);
        postagem.setModoPreparo(modoPreparo);
        postagem.setQtdPessoasServidas(qtdPessoasServidas);

        // Salvar postagem
        if (postagem.salvar(seguidoresSnapshot)) {
            Toast.makeText(NovaReceitaFotoActivity.this, getString(R.string.toast_salvar_postagem), Toast.LENGTH_SHORT).show();
            finish(); // Após publicar a foto, a activity é encerrada
        }
    }

    private void fecharAtividadeAtualAnterior(Activity ativadadeAnterior) {
        ativadadeAnterior.finish();
        finish();
    }

    private void mensagemFotoAdicionada() {
        Toast.makeText(NovaReceitaFotoActivity.this, getApplicationContext().getString(R.string.foto_adicionada), Toast.LENGTH_SHORT).show();
    }

    private void mostrarToast(String mensagem) {
        Log.d(TAG, "mostrarToast: " + mensagem);
        runOnUiThread(() -> Toast.makeText(NovaReceitaFotoActivity.this, mensagem, Toast.LENGTH_SHORT).show());
    }

}
