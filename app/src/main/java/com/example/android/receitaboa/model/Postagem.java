package com.example.android.receitaboa.model;

/*
Modelo de postagem:

postagens
  <id_usuario>
     <id_postagem_firebase>
        descricao
        caminhoFoto
        idUsuario
 */

import com.example.android.receitaboa.helper.ConfiguracaoFirebase;
import com.example.android.receitaboa.helper.UsuarioFirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Postagem implements Serializable { //Serializable passar dados entre activities (getExtra,putExtra)

    private String id;
    private String idUsuario;
    private String descricao;
    private String urlPostagem;

    public Postagem() { //iniciado quando se é instanciado a postagem ( new Postagem() )

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference postagemRef = firebaseRef.child("postagens");
        String idPostagem = postagemRef.push().getKey(); //push gera um id aleatório, getKey recupera esse id e salva na variável idPostagem
        setId(idPostagem);

    }

    public boolean salvar(DataSnapshot seguidoresSnapshot){ //salva a postagem para o usuario logado e para seus seguidores

        Map objeto = new HashMap();
        Chef chefLogado = UsuarioFirebaseAuth.getDadosChefLogadoAuth();

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();

        //Referencia para postagem
        String combinacaoId = "/" + getIdChef() + "/" + getId(); //idUsurio + idPostagem
        objeto.put("/postagens" + combinacaoId, this); // = firebaseRef.child("postagens").child(getIdUsuario()); this: esta postagem

        //Referencia para postagem (espalhamento da msma informação para varios nós)
        for (DataSnapshot seguidores : seguidoresSnapshot.getChildren() ){ //recupera os seguidores do usuario logado (tree: feed->id_meu_seguidor->id-postagem->minha postagem)

            String idSeguidor = seguidores.getKey(); //recupera na tree q é o id do seguidor

            //Monta objeto para salvar
            HashMap<String, Object> dadosSeguidor = new HashMap<>();
            dadosSeguidor.put("fotoPostagem", getUrlPostagem());
            dadosSeguidor.put("nomeReceita", getNomeReceita());
            dadosSeguidor.put("idPostagem", getId()); //idPostagem
            dadosSeguidor.put("nomeChef", chefLogado.getNome()); //dados do nome do usuario que postou a foto
            dadosSeguidor.put("fotoUsuario", chefLogado.getUrlFotoChef()); //foto de perfil do usuario que postou a foto no seu feed

            String idsAtualizacao = "/" + idSeguidor + "/" + getId(); //idMeuSeguidor + idPostagem
            objeto.put("/feed" + idsAtualizacao, dadosSeguidor); //local onde eu quero salvar as postagens será salva nessa variável dadosSeguidor

        }

        firebaseRef.updateChildren(objeto);
        return true;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdChef() {
        return idUsuario;
    }

    public void setIdChef(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNomeReceita() {
        return descricao;
    }

    public void setNomeReceita(String descricao) {
        this.descricao = descricao;
    }

    public String getUrlPostagem() {
        return urlPostagem;
    }

    public void setUrlPostagem(String urlPostagem) {
        this.urlPostagem = urlPostagem;
    }
}
