package com.example.android.receitaboa.model;

import com.example.android.receitaboa.helper.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class Receitas {

    private String idChef;
    private String idReceita;
    private String nomeReceita;
    private String ingredientes;
    private String modoPreparo;
    private String qtdPessoasServidas;
    private String urlFotoReceita;
    private String urlFotinho;

    public Receitas() { //esse método é inicializado quando ele é instanciado em outras classes (Receitas minhasReceitas = new Receitas())

        DatabaseReference firebaseDbRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference receitasRef = firebaseDbRef.child("receitas");

        //Cria um id para a nova receita criada pelo chef
        String identificadorReceita = receitasRef.push().getKey();
        setIdReceita(identificadorReceita);

    }
    //tree (receita-boa-asd -> receitas -> idChef(Base64) -> idReceita -> todos os dados da receitas)
    public void salvarMinhaReceitaFirebaseDatabase(){

        //Objeto dados Receita
        HashMap<String, Object> dadosReceita = new HashMap<>();
        dadosReceita.put("nomeReceita",getNomeReceita());
        dadosReceita.put("ingredientes",getIngredientes());
        dadosReceita.put("modoPreparo",getModoPreparo());
        dadosReceita.put("qtdPessoasServidas",getQtdPessoasServidas());
        dadosReceita.put("urlFotoReceita",getUrlFotoReceita());
        dadosReceita.put("idReceita",getIdReceita());
        dadosReceita.put("idChefao",getIdChef());


        DatabaseReference firebaseDbRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference chefRef = firebaseDbRef.child("receitas").child(getIdChef());
        DatabaseReference receitaRef = chefRef.child(getIdReceita());

        //atualizarFotoReceitaFirebaseDb();

        //seta todos os dados [idchef, idReceita, nomeReceita, ingredientes, preparo e serveXpessoas], setados nessa classe, dentro do nó idReceita no FirebaseDatabase
        receitaRef.setValue(dadosReceita);

    }

    public String guardarUrlFotoReceita(){

        return urlFotinho = getUrlFotoReceita();

    }

    public String getUrlFotoReceita() {
        return urlFotoReceita;
    }

    public void setUrlFotoReceita(String urlFotoReceita) {
        this.urlFotoReceita = urlFotoReceita;
    }

    public String getIdChef() {
        return idChef;
    }

    public void setIdChef(String idChef) {
        this.idChef = idChef;
    }

    public String getIdReceita() {
        return idReceita;
    }

    public void setIdReceita(String idReceita) {
        this.idReceita = idReceita;
    }

    public String getNomeReceita() {
        return nomeReceita;
    }

    public void setNomeReceita(String nomeReceita) {
        this.nomeReceita = nomeReceita;
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
}
