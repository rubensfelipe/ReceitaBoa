package com.rubensvaz.android.receitaboa.activity;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rubensfelipe.android.receitaboa.R;
import com.rubensvaz.android.receitaboa.helper.Base64Custom;
import com.rubensvaz.android.receitaboa.helper.ConfiguracaoFirebase;
import com.rubensvaz.android.receitaboa.helper.UsuarioFirebaseAuth;
import com.rubensvaz.android.receitaboa.model.Chef;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CadastroActivity extends AppCompatActivity {

    private EditText campoNome, campoEmail, campoSenha;
    private ProgressBar progressBarCadastro;
    private ImageView imageViewMiniChef;
    private FirebaseAuth autenticacao;
    private StorageReference storageReference;
    private String idChefLogado;

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 2;

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private Bitmap imagemSelecionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        configurarLaunchers();
        inicializarComponentes();
    }

    private void configurarLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bitmap imagem = (Bitmap) result.getData().getExtras().get("data");
                        if (imagem != null) {
                            // Ajustar orientação manualmente para imagens da câmera
                            imagem = ajustarOrientacaoImagem(imagem);
                            imagemSelecionada = imagem;
                            imageViewMiniChef.setImageBitmap(imagem);
                        }
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri localImagemSelecionada = result.getData().getData();
                        try {
                            Bitmap imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada);
                            if (imagem != null) {
                                imagem = ajustarOrientacaoImagem(localImagemSelecionada, imagem); // Ajustar orientação da imagem
                                imagemSelecionada = imagem;
                                imageViewMiniChef.setImageBitmap(imagem);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void inicializarComponentes() {
        campoNome = findViewById(R.id.editCadastroNome);
        campoEmail = findViewById(R.id.editCadastroEmail);
        campoSenha = findViewById(R.id.editCadastroSenha);
        progressBarCadastro = findViewById(R.id.progressCadastro);
        imageViewMiniChef = findViewById(R.id.miniChef);

        imageViewMiniChef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirPopupSelecaoImagem();
            }
        });

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        storageReference = ConfiguracaoFirebase.getFirebaseStorage();
    }

    private void abrirPopupSelecaoImagem() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.selecione_opcao));
        builder.setItems(new CharSequence[]{getString(R.string.galeria), getString(R.string.camera)}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        abrirGaleria();
                        break;
                    case 1:
                        if (ContextCompat.checkSelfPermission(CadastroActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            abrirCamera();
                        } else {
                            ActivityCompat.requestPermissions(CadastroActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                            mostrarToast("Permissão de camera não concedida");
                        }
                        break;
                }
            }
        });
        builder.show();
    }

    private void abrirCamera() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(i);
    }

    private void abrirGaleria() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(i);
    }

    public void validarCadastroChef(View view) {
        // Recuperar textos dos campos digitados
        String textoNome = campoNome.getText().toString();
        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();

        if (!textoNome.isEmpty()) {
            if (isNomeValido(textoNome)) {
                if (!textoEmail.isEmpty()) {
                    if (!textoSenha.isEmpty()) {
                        progressBarCadastro.setVisibility(View.VISIBLE);

                        Chef chef = new Chef();
                        chef.setNome(textoNome);
                        chef.setEmail(textoEmail);
                        chef.setSenha(textoSenha);

                        // Recuperar o novo token FCM do dispositivo
                        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                if (!task.isSuccessful()) {
                                    Log.w("FCM Token", "Fetching FCM registration token failed", task.getException());
                                    return;
                                }

                                // Obter o novo Token de Registro FCM
                                String token = task.getResult();
                                chef.setTokenCel(token);

                                cadastrarChefDbAuth(chef);
                            }
                        });

                    } else {
                        Toast.makeText(CadastroActivity.this, getString(R.string.ponha_senha), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CadastroActivity.this, getString(R.string.ponha_email), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(CadastroActivity.this, getString(R.string.sem_caracter_especial), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(CadastroActivity.this, getString(R.string.ponha_nome), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNomeValido(String nome) {
        return nome.matches("[a-zA-Z0-9 ]*");
    }

    public void cadastrarChefDbAuth(final Chef chef) {
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(chef.getEmail(), chef.getSenha())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(CadastroActivity.this, getString(R.string.bem_vindo) + chef.getNome() + getString(R.string.cadastro_finalizado), Toast.LENGTH_LONG).show();
                            UsuarioFirebaseAuth.atualizarNomeChefAuth(chef.getNome());
                            idChefLogado = Base64Custom.codificarBase64(chef.getEmail());
                            chef.setId(idChefLogado);
                            chef.salvarFirebaseDatabase();

                            if (imagemSelecionada != null) {
                                salvarImagemFirebase();
                            } else {
                                progressBarCadastro.setVisibility(View.GONE);
                                finish();
                            }
                        } else {
                            String excecao = "";
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                excecao = getString(R.string.senha_fraca);
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                excecao = getString(R.string.email_invalido);
                            } catch (FirebaseAuthUserCollisionException e) {
                                excecao = getString(R.string.conta_jacadastrada);
                            } catch (Exception e) {
                                excecao = getString(R.string.erro_cadastro) + e.getMessage();
                                e.printStackTrace();
                            }
                            Toast.makeText(CadastroActivity.this, excecao, Toast.LENGTH_SHORT).show();
                            progressBarCadastro.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void salvarImagemFirebase() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imagemSelecionada.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] dadosImagem = baos.toByteArray();

        StorageReference imagemRef = storageReference
                .child("imagens")
                .child("perfil")
                .child(idChefLogado + ".jpeg");

        UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CadastroActivity.this, "Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show();
                progressBarCadastro.setVisibility(View.GONE);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        Uri url = task.getResult();
                        atualizarFotoUsuario(url);
                        progressBarCadastro.setVisibility(View.GONE);
                        finish();
                    }
                });
            }
        });
    }

    // Função para ajustar a orientação da imagem com Uri e Bitmap
    private Bitmap ajustarOrientacaoImagem(Uri uri, Bitmap imagem) {
        try {
            if (uri != null) {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                ExifInterface exif = new ExifInterface(inputStream);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                return rotateBitmap(imagem, orientation);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imagem;
    }

    // Função para ajustar a orientação da imagem da câmera
    private Bitmap ajustarOrientacaoImagem(Bitmap imagem) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90); // Rotacionar 90 graus para ajustar para o modo retrato
        return Bitmap.createBitmap(imagem, 0, 0, imagem.getWidth(), imagem.getHeight(), matrix, true);
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            default:
                return bitmap;
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private void atualizarFotoUsuario(Uri url) {
        UsuarioFirebaseAuth.atualizarFotoChefAuth(url);
    }

    private void mostrarToast(String mensagem) {
        Log.d(TAG, "mostrarToast: " + mensagem);
        runOnUiThread(() -> Toast.makeText(CadastroActivity.this, mensagem, Toast.LENGTH_SHORT).show());
    }
}
