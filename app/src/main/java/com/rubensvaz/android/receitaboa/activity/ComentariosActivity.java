package com.rubensvaz.android.receitaboa.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.rubensfelipe.android.receitaboa.R;
import com.rubensvaz.android.receitaboa.adapter.AdapterComentario;
import com.rubensvaz.android.receitaboa.helper.ConfiguracaoFirebase;
import com.rubensvaz.android.receitaboa.helper.UsuarioFirebaseAuth;
import com.rubensvaz.android.receitaboa.model.Chef;
import com.rubensvaz.android.receitaboa.model.Comentario;

import java.util.ArrayList;
import java.util.List;

public class ComentariosActivity extends AppCompatActivity {

    private EditText etComentario;
    private RecyclerView recyclerComentarios;
    private Toolbar toolbar;

    private String idPostagem;
    private Chef chef;
    private AdapterComentario adapterComentario;
    private List<Comentario> listaComentarios = new ArrayList<>();

    private DatabaseReference firebaseRef;
    private DatabaseReference comentariosRef;
    private ValueEventListener valueEventListenerComentarios;

    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comentarios);

        inicializarComponentes();

        configuracoesIniciais();

        configurarToolbar();

        configurarAdapterMaisRecyclerView();

        recpuperarIdPostagem();

    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarComentarios();
    }

    private void inicializarComponentes() {
        toolbar = findViewById(R.id.toolbarPrincipal);
        etComentario = findViewById(R.id.editComentario);
        recyclerComentarios =findViewById(R.id.recyclerComentarios);
    }

    private void configuracoesIniciais() {
        chef = UsuarioFirebaseAuth.getDadosChefLogadoAuth();
        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    }

    private void configurarToolbar() {
        toolbar.setTitle("Comentários");
        setSupportActionBar(toolbar);
        //adiciona o botão voltar (na barra superior) para MainActivity (padrão) (PARTE 1)
        //OBS: deve-se adicionar android:parentActivityName=".activity.MainActivity" no Android Manifest na parte do EditarPerfilActivity, botão voltar retorna a parentActivity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp); //customiza o botão voltar para o ícone q vc desejar
    }

    //Sobrescreve o método  getSupportActionBar().setDisplayHomeAsUpEnabled(true) que controla o botão voltar; (PARTE 2)
    @Override
    public boolean onSupportNavigateUp() {
        finish(); //fecha a activity atual
        return false; //impede que a operação padrão (voltar para MainActivity) seja realizado
    }

    private void configurarAdapterMaisRecyclerView() {
        adapterComentario = new AdapterComentario(listaComentarios,getApplicationContext());

        recyclerComentarios.setHasFixedSize(true);
        recyclerComentarios.setLayoutManager(new LinearLayoutManager(this));
        recyclerComentarios.setAdapter(adapterComentario);
    }

    private void recpuperarIdPostagem() {

        //Recupera id da postagem (bundle é utilizado para receber os dados que estão sendo enviados de outra Classe, Activity, etc)
        bundle = getIntent().getExtras(); //recuperado do AdapterFeed (putExtras)

        if (bundle != null){
            idPostagem = bundle.getString("idPostagem"); //recuperado do putExtras
        }

    }

    public void salvarComentario(View view){

        String textoComentario = etComentario.getText().toString();
        if(textoComentario != null && !textoComentario.equals("")) { //se houver comentário e o usuário enviar um comentário não vazio

            Comentario comentario = new Comentario();
            comentario.setIdPostagem(idPostagem);
            comentario.setIdUsuario(chef.getId());
            comentario.setNomeUsuario(chef.getNome());
            comentario.setCaminhoFoto(chef.getUrlFotoChef());
            comentario.setComentario(textoComentario);

            if(comentario.salvar()){ //cria o nó de comentarios no FirebaseDatabase
                Toast.makeText(this,
                        getString(R.string.comentario_enviado),
                        Toast.LENGTH_SHORT).show();
            }

        }else{
            Toast.makeText(this,
                    getString(R.string.comentario_vazio),
                    Toast.LENGTH_SHORT).show();
        }

        //Limpa comentário que foi digitado
        etComentario.setText("");

    }

    private void recuperarComentarios(){

            comentariosRef = firebaseRef.child("comentarios")
                    .child(idPostagem);


        valueEventListenerComentarios = comentariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listaComentarios.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    listaComentarios.add(ds.getValue(Comentario.class)); //recupera todos os comentarios que foram salvos no firebaseDatabase
                }
                adapterComentario.notifyDataSetChanged(); //atualiza a lista de comentarios no nosso AdapterComentarios
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {         }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        comentariosRef.removeEventListener(valueEventListenerComentarios);
    }
}