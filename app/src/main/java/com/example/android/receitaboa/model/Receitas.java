package com.example.android.receitaboa.model;

import com.example.android.receitaboa.helper.ConfiguracaoFirebase;
import com.example.android.receitaboa.helper.UsuarioFirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

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

    //Configurações iniciais
    String identificadorChef = UsuarioFirebaseAuth.getIdentificadorChefAuth(); //recupera o chef logado

    public Receitas() { //esse método é inicializado quando ele é instanciado em outras classes (Receitas minhasReceitas = new Receitas())
    }

    public void gerarIdReceita(){

        DatabaseReference firebaseDbRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference receitasRef = firebaseDbRef.child("receitas");

        //Cria um id para a nova receita criada pelo chef
        String identificadorReceita = receitasRef.push().getKey();
        setIdReceita(identificadorReceita);
    }

    //tree (receita-boa-asd -> receitas -> idChef(Base64) -> idReceita -> todos os dados da receitas)
    public void salvarMinhaReceitaFirebaseDb(){

        gerarIdReceita(); //gera um id único para cada receita (método chamado quando o botão salvar for acionado na activity_nova_receita_info)

        //Objeto dados Receita
        HashMap<String, Object> dadosReceita = new HashMap<>();
        dadosReceita.put("Nome", getNome());
        dadosReceita.put("Ingredientes",getIngredientes());
        dadosReceita.put("Modo de Prepraro",getModoPreparo());
        dadosReceita.put("Pessoas Servidas",getQtdPessoasServidas());
        dadosReceita.put("idReceita",getIdReceita());
        dadosReceita.put("idChefao",identificadorChef);

        DatabaseReference firebaseDbRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference chefRef = firebaseDbRef.child("receitas").child(identificadorChef);
        DatabaseReference receitaRef = chefRef.child(getIdReceita());

        //seta todos os dados [idchef, idReceita, nomeReceita, ingredientes, preparo e serveXpessoas], setados nessa classe, dentro do nó idReceita no FirebaseDatabase
        receitaRef.setValue(dadosReceita);

    }

    //Adiciona novos dados a um nó (nó idReceita) já foi criado anteriormente no FirebaseDatabase
    public void atualizarDadosFirebaseDb(String idRecipe){

        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase(); //instacia o FirebaseDatabase

        DatabaseReference receitaRef = database.child("receitas")
                                                    .child(identificadorChef)
                                                         .child(idRecipe); //os dados serão atualizados dentro do nó idChef

        Map<String,Object> urlFotoAdicionado = converterParaMap(); //email,nome,urlFotoChef (necessário para utilizar o método upadateChildren) [Converte: Classe Usuario -> Classe Map]

        //atualiza esses dados no FirebaseDatabase
        receitaRef.updateChildren(urlFotoAdicionado); //updateChildren: necessario utilizar como input um Map

    }

    @Exclude //não será executado dentro do app
    public Map<String,Object> converterParaMap(){ //converte a classe Receitas para Map (Faz a mesma função da classe Receitas) ingredientes = getIngredientes, ....
        HashMap<String,Object> usuarioMap = new HashMap<>();
        usuarioMap.put("Url", getUrlFotoReceita());

        return  usuarioMap;
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

    public String getNome() {
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
