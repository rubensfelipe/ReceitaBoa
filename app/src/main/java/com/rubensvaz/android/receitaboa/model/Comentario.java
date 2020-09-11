package com.rubensvaz.android.receitaboa.model;

import com.google.firebase.database.DatabaseReference;
import com.rubensvaz.android.receitaboa.helper.ConfiguracaoFirebase;

public class Comentario {

    private String idComentario;
    private String idPostagem;
    private String idUsuario;
    private String caminhoFoto;
    private String nomeUsuario;
    private String comentario;

    public Comentario() {
    }

    /*
        + Comentarios
            + id_postagem
                +id_comentario
                    comentario
     */
    public boolean salvar(){

        DatabaseReference comentariosRef = ConfiguracaoFirebase.getFirebaseDatabase()
                .child("comentarios")
                .child(getIdPostagem());

        String chaveComentario = comentariosRef.push().getKey(); //id do comentario gerado pelo firebase
        setIdComentario(chaveComentario);
        comentariosRef.child(getIdComentario()).setValue(this); //cria todos os nós de comentario

        return true;
    }

    public String getIdComentario() {
        return idComentario;
    }

    public void setIdComentario(String idComentario) {
        this.idComentario = idComentario;
    }

    public String getIdPostagem() {
        return idPostagem;
    }

    public void setIdPostagem(String idPostagem) {
        this.idPostagem = idPostagem;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getCaminhoFoto() {
        return caminhoFoto;
    }

    public void setCaminhoFoto(String caminhoFoto) {
        this.caminhoFoto = caminhoFoto;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
