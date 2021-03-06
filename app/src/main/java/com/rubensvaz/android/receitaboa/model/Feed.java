package com.rubensvaz.android.receitaboa.model;

import java.io.Serializable;

public class Feed implements Serializable  {

    private String idPostagem;

    private String fotoPostagem;
    private String dataPostagem;
    private String nomeChef;

    private String fotoUsuario;

    private String idReceita;
    private String nomeReceita;
    private String ingredientes;
    private String modoPreparo;
    private String qtdPessoasServidas;

    public Feed() {
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

    public String getIdPostagem() { //pega o id da postagem no Feed
        return idPostagem;
    }

    public void setIdPostagem(String idPostagem) {
        this.idPostagem = idPostagem;
    }

    public String getFotoPostagem() {
        return fotoPostagem;
    }

    public void setFotoPostagem(String fotoPostagem) {
        this.fotoPostagem = fotoPostagem;
    }

    public String getNomeReceita() {
        return nomeReceita;
    }

    public void setNomeReceita(String nomeReceita) {
        this.nomeReceita = nomeReceita;
    }

    public String getNomeChef() {
        return nomeChef;
    }

    public void setNomeChef(String nomeChef) {
        this.nomeChef = nomeChef;
    }

    public String getFotoUsuario() {
        return fotoUsuario;
    }

    public void setFotoUsuario(String fotoUsuario) {
        this.fotoUsuario = fotoUsuario;
    }
}
