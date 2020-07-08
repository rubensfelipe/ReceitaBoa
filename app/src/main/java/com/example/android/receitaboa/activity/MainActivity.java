package com.example.android.receitaboa.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.android.receitaboa.R;
import com.example.android.receitaboa.fragment.FeedFragment;
import com.example.android.receitaboa.fragment.BuscarAmigosFragment;
import com.example.android.receitaboa.fragment.ReceitasAmigosFragment;
import com.example.android.receitaboa.fragment.MinhasReceitasFragment;
import com.example.android.receitaboa.helper.ConfiguracaoFirebase;
import com.google.firebase.auth.FirebaseAuth;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    public MaterialSearchView searchView;

    public boolean iconePesquisaClicado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Receita Boa");
        setSupportActionBar(toolbar);

        //Configuração do searchView
        searchView = findViewById(R.id.materialSearchPrincipal);

        //Configurar abas com os respectivos fragments
        final FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(),
                FragmentPagerItems.with(this)
                .add("Receitas", MinhasReceitasFragment.class)
                .add("Feed", FeedFragment.class)
                .add("Receitas Amigos", ReceitasAmigosFragment.class)
                .add("Buscar Amigos", BuscarAmigosFragment.class)
                .create()
        );

        final ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);

        SmartTabLayout viewPagerTab = findViewById(R.id.viewPagerTab);
        viewPagerTab.setViewPager(viewPager);

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {

                switch (viewPager.getCurrentItem()){
                    /*
                    case 0:
                        MinhasReceitasFragment mrFrag = (MinhasReceitasFragment) adapter.getPage(0);
                        mrFrag.recarregarMinhasReceitas();
                        break;
                     */

                    case  3:
                        BuscarAmigosFragment friendsFrag = (BuscarAmigosFragment) adapter.getPage(3);
                        friendsFrag.recarregarAmigos();
                        break;

                }
            }
        });

        //Listener para caixa de texto de pesquisa
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {

                //Verifica se o usuario está pesquisando dentro d qual fragment
                switch (viewPager.getCurrentItem()){ // viewPager.getCurrentItem(): 0 (fragment MinhasReceitas), 1 (fragment Amigos)

                      /*
                    case 0: //MinhasReceitas
                        MinhasReceitasFragment minhasReceitasFrag = (MinhasReceitasFragment) adapter.getPage(0);

                            if (newText != null && !newText.isEmpty()){
                                minhasReceitasFrag.pesquisarMinhasReceitas(newText.toLowerCase());

                            }else {

                                minhasReceitasFrag.recarregarMinhasReceitas(); //recupera a lista completa se a pesquisa estiver vazia ou o chef tenha saido da aba pesquisa
                            }

                        break;



                    case 2: //Todas as Receitas
                        ReceitasFragment receitasFrag = (ReceitasFragment) adapter.getPage(2);
                        if (newText != null && !newText.isEmpty()){
                            receitasFrag.pesquisarReceitas(newText.toLowerCase());
                        }else {
                            receitasFrag.recarregarReceitas(); //recupera a lista completa se a pesquisa estiver vazia ou o chef tenha saido da aba pesquisa
                        }
                        break;
                        */
                    case 3: //Amigos
                        BuscarAmigosFragment amigosFrag = (BuscarAmigosFragment) adapter.getPage(3);
                        if (newText != null && !newText.isEmpty()){
                            amigosFrag.pesquisarAmigos(newText.toLowerCase());
                        }else {
                            amigosFrag.recarregarAmigos(); //recupera a lista completa se a pesquisa estiver vazia ou o chef tenha saido da aba pesquisa
                        }
                        break;
                }
                return true;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);

        //Configurar botão de pesquisa
        MenuItem item = menu.findItem(R.id.menuPesquisa); //configura o icone de pesquisa como um MenuItem
        searchView.setMenuItem(item); //ao clicar nesse item, transforma a view numa caixa de texto de pesquisa para buscar o usuário na lista

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menuDeslogar:
                deslogarChef();
                finish();
                break;
            case R.id.menuConfiguracoes:
                abrirConfiguracoes();
                break;
        }

        return super.onOptionsItemSelected(item);
    }



    public void deslogarChef(){

        try {
            autenticacao.signOut();
            Toast.makeText(MainActivity.this,"Chef Deslogado",Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Toast.makeText(MainActivity.this,"Erro ao deslogar Chef",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    public void abrirConfiguracoes(){
        Intent intent = new Intent(MainActivity.this, ConfiguracoesActivity.class);
        startActivity(intent);
    }

}
