package com.rubensvaz.android.receitaboa.model;

import com.rubensvaz.android.receitaboa.helper.ConfiguracaoFirebase;
import com.rubensvaz.android.receitaboa.helper.UsuarioFirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Receitas implements Serializable {

    private String idChef;
    private String idReceita;
    private String nome;
    private String ingredientes;
    private String modoPreparo;
    private String qtdPessoasServidas;
    private String urlFotoReceita;

    private String nomeChef;

    //Configurações iniciais
    String identificadorChef = UsuarioFirebaseAuth.getIdentificadorChefAuth(); //recupera o chef logado
    //FirebaseUser chefAuth = getChefAtualAuth();

    public Receitas() { //esse método é inicializado quando ele é instanciado em outras classes (Receitas minhasReceitas = new Receitas())
    }

    public void gerarIdReceita(){

        DatabaseReference firebaseDbRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference receitasRef = firebaseDbRef.child("receitas");

        //Cria um id para a nova receita criada pelo chef
        String identificadorReceita = receitasRef.push().getKey();

        setIdReceita(identificadorReceita);
    }

    //salva todas as receitas publicadas
    public void salvarReceitas(){

        HashMap<String, Object> dadosUltimaPostagem = new HashMap<>();

        //recuperar os dados
        dadosUltimaPostagem.put("nome", getNome());
        dadosUltimaPostagem.put("ingredientes", getIngredientes());
        dadosUltimaPostagem.put("modoPreparo", getModoPreparo());
        dadosUltimaPostagem.put("qtdPessoasServidas", getQtdPessoasServidas());
        dadosUltimaPostagem.put("idUltimaPostagem", getIdReceita());

        dadosUltimaPostagem.put("idChef", identificadorChef);
        dadosUltimaPostagem.put("nomeChef", getNomeChef());

        //Configurações iniciais, referencia firebase
        DatabaseReference firebaseDbRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference postsRef = firebaseDbRef.child("ultimasPostagens");
        DatabaseReference ultimasPostagensRef = postsRef.child(getIdReceita());

        /*salvar os dados firebasedb*/
        ultimasPostagensRef.setValue(dadosUltimaPostagem);

    }

    //tree (receita-boa-asd -> receitas -> idChef(Base64) -> idReceita -> todos os dados da receitas)
    public void salvarMinhaReceitaFirebaseDb(){

        gerarIdReceita(); //gera um id único para cada receita (método chamado quando o botão salvar for acionado na activity_nova_receita_info)

        //Objeto dados Receita
        HashMap<String, Object> dadosReceita = new HashMap<>();

        dadosReceita.put("nome", getNome());
        dadosReceita.put("ingredientes", getIngredientes());
        dadosReceita.put("modoPreparo", getModoPreparo());
        dadosReceita.put("qtdPessoasServidas", getQtdPessoasServidas());
        dadosReceita.put("idReceita", getIdReceita());

        dadosReceita.put("idChef", identificadorChef);
        dadosReceita.put("nomeChef", getNomeChef());
        //dadosReceita.put("nomeChef", chefAuth.getDisplayName());

        DatabaseReference firebaseDbRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference chefRef = firebaseDbRef.child("receitas").child(identificadorChef);
        DatabaseReference receitaRef = chefRef.child(getIdReceita());

        //seta todos os dados [idchef, idReceita, nomeReceita, ingredientes, preparo e serveXpessoas], setados nessa classe, dentro do nó idReceita no FirebaseDatabase
        receitaRef.setValue(dadosReceita);

        salvarReceitas();

    }

    //Adiciona novos dados (url) a um nó (nó idReceita) já criado anteriormente no FirebaseDatabase
    public void adicionarUrlFotoFirebaseDb(String idRecipe){

        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase(); //instacia o FirebaseDatabase

        DatabaseReference receitaRef = database.child("receitas")
                                                    .child(identificadorChef)
                                                         .child(idRecipe); //os dados serão atualizados dentro do nó idChef

        DatabaseReference ultimaPostagemRef = database.child("ultimasPostagens")
                .child(idRecipe);

        Map<String,Object> urlFotoAdicionada = converterParaMap(); //email,nome,urlFotoChef (necessário para utilizar o método upadateChildren) [Converte: Classe Usuario -> Classe Map]

        //atualiza esses dados no FirebaseDatabase
        receitaRef.updateChildren(urlFotoAdicionada); //updateChildren: necessario utilizar como input um Map
        ultimaPostagemRef.updateChildren(urlFotoAdicionada);

    }

    @Exclude //não será executado dentro do app
    public Map<String,Object> converterParaMap(){ //converte a classe Receitas para Map (Faz a mesma função da classe Receitas) ingredientes = getIngredientes, ....
        HashMap<String,Object> novaReceitaMap = new HashMap<>();
        novaReceitaMap.put("urlFotoReceita", getUrlFotoReceita());

        return  novaReceitaMap;
    }

    public void atualizarReceitaFirebaseDb(String idReceituario){

        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase(); //instacia o FirebaseDatabase

        DatabaseReference receitaRef = database.child("receitas")
                .child(identificadorChef)
                .child(idReceituario); //os dados serão atualizados dentro do nó idChef
        Map<String,Object> receitaAtualizada = converterParaMap2();

        //atualiza esses dados no nó receitas FirebaseDatabase
        receitaRef.updateChildren(receitaAtualizada);

        //referencia para o nó ultimas postagens id da receita atualizada
        DatabaseReference ultimaPostagemRef = database.child("ultimasPostagens")
                .child(idReceituario);

        //atualizando a receita no nó ultimas postagens
        ultimaPostagemRef.updateChildren(receitaAtualizada);

    }

    @Exclude //não será executado dentro do app
    public Map<String,Object> converterParaMap2(){ //converte a classe Receitas para Map (Faz a mesma função da classe Receitas) ingredientes = getIngredientes, ....
        HashMap<String,Object> receitaAtualMap = new HashMap<>();
        receitaAtualMap.put("nome", getNome());
        receitaAtualMap.put("ingredientes", getIngredientes());
        receitaAtualMap.put("modoPreparo", getModoPreparo());
        receitaAtualMap.put("qtdPessoasServidas", getQtdPessoasServidas());
        receitaAtualMap.put("urlFotoReceita", getUrlFotoReceita());

        return  receitaAtualMap;
    }

    public String getNomeChef() {
        return nomeChef;
    }

    public void setNomeChef(String nomeChef) {
        this.nomeChef = nomeChef;
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
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
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
