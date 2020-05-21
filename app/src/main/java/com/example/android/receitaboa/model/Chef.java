package com.example.android.receitaboa.model;

import com.example.android.receitaboa.helper.ConfiguracaoFirebase;
import com.example.android.receitaboa.helper.UsuarioFirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Chef {

    private String id;
    private String nome;
    private String email;
    private String senha;
    private String urlFotoChef;

    public Chef() {
    }

    public void salvarFirebaseDatabase(){

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference chef = firebaseRef.child("chefs").child(getId()); //tree (receita-boa-asd -> chefs -> idChef(Base64)

        chef.setValue(this); //salva todos os dados da Classe Chef dentro do caminho do chef  (salvo FirebaseDatabase [nome chef, email])
    }

    //Atualiza os dados alterados e reescreve os dados não alterados no FirebaseDatabase
    public void atualizarFirebaseDatabase(){

        String identificadorChef = UsuarioFirebaseAuth.getIdentificadorChefAuth();
        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase(); //instacia o FirebaseDatabase

        DatabaseReference chefsRef = database.child("chefs")
                .child(identificadorChef); //os dados serão atualizados dentro do nó idChef

        Map<String,Object> dadosChef = converterParaMap(); //email,nome,urlFotoChef (necessário para utilizar o método upadateChildren) [Converte: Classe Usuario -> Classe Map]

        //atualiza esses dados no FirebaseDatabase
        chefsRef.updateChildren(dadosChef); //updateChildren: necessario utilizar como input um Map

    }

    @Exclude //não será executado dentro do app
    public Map<String,Object> converterParaMap(){ //converte a classe Chef para Map (Faz a mesma função da classe Chef) email = getEmail, nome = getNome....
        HashMap<String,Object> usuarioMap = new HashMap<>();
        usuarioMap.put("email",getEmail());
        usuarioMap.put("nome",getNome());
        usuarioMap.put("urlFotoChef", getUrlFotoChef());

        return  usuarioMap;
    }

    public String getUrlFotoChef() {
        return urlFotoChef;
    }

    public void setUrlFotoChef(String urlFotoChef) {
        this.urlFotoChef = urlFotoChef;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
