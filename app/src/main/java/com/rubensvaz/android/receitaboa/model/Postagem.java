package com.rubensvaz.android.receitaboa.model;

/*
Modelo de postagem:

postagens
  <id_usuario>
     <id_postagem_firebase>
        descricao
        caminhoFoto
        idUsuario
 */

import com.rubensvaz.android.receitaboa.helper.ConfiguracaoFirebase;
import com.rubensvaz.android.receitaboa.helper.UsuarioFirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Postagem implements Serializable { //Serializable passar dados entre activities (getExtra,putExtra)

    private String id;
    private String idChef;
    private String idReceita;
    private String urlPostagem;
    private String dataPostagem;

    private String nomeReceita;
    private String ingredientes;
    private String modoPreparo;
    private String qtdPessoasServidas;

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
        objeto.put("/postagens" + combinacaoId, this); // = firebaseRef.child("postagens").child(getIdUsuario()); this: salva todos os dados dessa classe

        //Monta objeto para salvar
        HashMap<String, Object> dadosPostagemFeed = new HashMap<>();
        dadosPostagemFeed.put("fotoPostagem", getUrlPostagem());
        dadosPostagemFeed.put("dataPostagem", getDataPostagem());
        dadosPostagemFeed.put("idPostagem", getId()); //idPostagem
        dadosPostagemFeed.put("nomeChef", chefLogado.getNome()); //dados do nome do usuario que postou a foto

        if (chefLogado.getUrlFotoChef() != null)
        dadosPostagemFeed.put("fotoUsuario", chefLogado.getUrlFotoChef()); //foto de perfil do usuario que postou a foto no seu feed

        dadosPostagemFeed.put("idReceita", getIdReceita());
        dadosPostagemFeed.put("nomeReceita", getNomeReceita());
        dadosPostagemFeed.put("ingredientes", getIngredientes());
        dadosPostagemFeed.put("modoPreparo", getModoPreparo());
        dadosPostagemFeed.put("qtdPessoasServidas", getQtdPessoasServidas());

        //Referencia para postagem (armazenamento da msma informação para varios nó, no caso os seguidores do perfil que postou a foto)
        for (DataSnapshot seguidores : seguidoresSnapshot.getChildren() ){ //recupera os seguidores do usuario logado (tree: feed->id_meu_seguidor->id-postagem->minha postagem)

            String idSeguidor = seguidores.getKey(); //recupera na tree q é o id do seguidor

            String idsAtualizacao = "/" + idSeguidor + "/" + getId(); //idMeuSeguidor + idPostagem
            objeto.put("/feed" + idsAtualizacao, dadosPostagemFeed); //local onde eu quero salvar as postagens. Será salva nessa variável dadosPostagemFeed

        }

            //a postagem do chef logado também aparecerá no feed dele, além de aparecer no feed dos seguidores
            objeto.put("/feed/" + getIdChef() + "/" + getId(), dadosPostagemFeed); //local onde eu quero salvar as postagens será salva nessa variável dadosPostagemFeed

            firebaseRef.updateChildren(objeto);
            return true;

    }

    public String getIdReceita() {
        return idReceita;
    }

    public void setIdReceita(String idReceita) {
        this.idReceita = idReceita;
    }

    public String getDataPostagem() {
        return dataPostagem;
    }

    public void setDataPostagem(String dataPostagem) {
        this.dataPostagem = dataPostagem;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdChef() {
        return idChef;
    }

    public void setIdChef(String idUsuario) {
        this.idChef = idUsuario;
    }

    public String getNomeReceita() {
        return nomeReceita;
    }

    public void setNomeReceita(String descricao) {
        this.nomeReceita = descricao;
    }

    public String getIngredientes() {
        return ingredientes;
    }

    public void setIngredientes(String ingredientes) {
        this.ingredientes = ingredientes;
    }

    public String getModoPreparo() {
        return modoPreparo;
    }

    public void setModoPreparo(String modoPreparo) {
        this.modoPreparo = modoPreparo;
    }

    public String getQtdPessoasServidas() {
        return qtdPessoasServidas;
    }

    public void setQtdPessoasServidas(String qtdPessoasServidas) {
        this.qtdPessoasServidas = qtdPessoasServidas;
    }

    public String getUrlPostagem() {
        return urlPostagem;
    }

    public void setUrlPostagem(String urlPostagem) {
        this.urlPostagem = urlPostagem;
    }
}
