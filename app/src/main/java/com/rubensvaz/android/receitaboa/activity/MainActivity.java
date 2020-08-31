package com.rubensvaz.android.receitaboa.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.rubensfelipe.android.receitaboa.R;
import com.rubensvaz.android.receitaboa.fragment.AmigosFragment;
import com.rubensvaz.android.receitaboa.fragment.FeedFragment;
import com.rubensvaz.android.receitaboa.fragment.BuscarAmigosFragment;
import com.rubensvaz.android.receitaboa.fragment.ReceitasUsuariosFragment;
import com.rubensvaz.android.receitaboa.fragment.MinhasReceitasFragment;
import com.rubensvaz.android.receitaboa.helper.ConfiguracaoFirebase;
import com.google.firebase.auth.FirebaseAuth;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    public MaterialSearchView searchView;
    private ViewPager viewPager;
    private FragmentPagerItemAdapter adapter;

    //public boolean iconePesquisaClicado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configuracaoInicial();

        configurarToolbar();

        inicializarSearchView();

        //configurarBottomNavigation();

        //Configurar abas com os respectivos fragments e seus nomes
        adapter = criarAbas();

        //Configura as páginas de visualização nas fragments
        viewPager = configuracaVisualPagina(adapter);

        //Configura as abas dos fragments
        configuracaoVisualAba(viewPager);

        fazerPesquisa(viewPager, adapter);

        eventoFecharPesquisa(viewPager, adapter);

    }

    private void configuracaoInicial() {
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    }

    private void configurarToolbar() {

        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Receita Boa");
        setSupportActionBar(toolbar);

    }

    private void inicializarSearchView() {
        searchView = findViewById(R.id.materialSearchPrincipal);
    }

    public FragmentPagerItemAdapter criarAbas() {
        final FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(),
                FragmentPagerItems.with(this)
                        .add(R.string.meu_cardapio, MinhasReceitasFragment.class)
                        .add(R.string.feed, FeedFragment.class)
                        .add(R.string.meus_amigos, AmigosFragment.class)
                        .add(R.string.receitas, ReceitasUsuariosFragment.class)
                        .add(R.string.buscar_amigos, BuscarAmigosFragment.class)
                        .create()
        );
        return adapter;
    }

    public ViewPager configuracaVisualPagina(FragmentPagerItemAdapter adaptador) {
        final ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adaptador);

        return viewPager;
    }

    private void configuracaoVisualAba(ViewPager visualPagina) {
        SmartTabLayout viewPagerTab = findViewById(R.id.viewPagerTab);
        viewPagerTab.setViewPager(visualPagina);


    }

    private void fazerPesquisa(final ViewPager paginaVisualizada, final FragmentPagerItemAdapter adaptador) {
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; } //ao pressionar enter ou clicar no botão para fazer a pesquisa -> execute a ação nenhuma

            @Override
            public boolean onQueryTextChange(String newText) { //ao apenas digitar algum texto na caixa de pesquisa, esse método já é chamado

                //Verifica em qual tela (fragment) o usuario está pesquisando
                switch (paginaVisualizada.getCurrentItem()){ // viewPager.getCurrentItem(): 0 (fragment MinhasReceitas), 1 (fragment Amigos)


                    case 0: //Meu Cardápio
                        MinhasReceitasFragment minhasReceitasFrag = (MinhasReceitasFragment) adaptador.getPage(0);

                            if (newText != null && !newText.isEmpty()){
                                minhasReceitasFrag.pesquisarMinhasReceitas(newText.toLowerCase());
                            }else {
                                //Aparente esse campo fica vazio (vamos ver)
                            }

                        break;

                    case 1: //Feed
                        FeedFragment feedFrag = (FeedFragment) adaptador.getPage(1);

                        if (newText != null && !newText.isEmpty()){
                            feedFrag.pesquisarFeed(newText.toLowerCase());
                        }else {
                            //Aparente esse campo fica vazio (vamos ver)
                        }

                        break;

                    case 2: //Meus Amigos
                        AmigosFragment meusAmigosFrag = (AmigosFragment) adaptador.getPage(2);
                        if (newText != null && !newText.isEmpty()){
                            meusAmigosFrag.pesquisarAmigos(newText.toLowerCase());
                        }else {
                            /*Aparente esse campo fica vazio (vamos ver)
                            receitasFrag.recuperarReceitasAmigos(); //1o cria uma lista atualizada a partir dos dados firebase (caso haja alterações)
                            receitasFrag.recarregarReceitasAmigos(); //2o recupera a lista criada acima se a pesquisa estiver vazia ou o chef tenha saido da aba pesquisa
                             */
                        }
                        break;

                        case 3: //Receitas do App
                        ReceitasUsuariosFragment receitasFrag = (ReceitasUsuariosFragment) adaptador.getPage(3);
                        if (newText != null && !newText.isEmpty()){
                            receitasFrag.pesquisarReceitas(newText.toLowerCase());
                        }else {
                            /*Aparente esse campo fica vazio (vamos ver)
                            receitasFrag.recuperarReceitasAmigos(); //1o cria uma lista atualizada a partir dos dados firebase (caso haja alterações)
                            receitasFrag.recarregarReceitasAmigos(); //2o recupera a lista criada acima se a pesquisa estiver vazia ou o chef tenha saido da aba pesquisa
                             */
                        }
                        break;

                    case 4: //Buscar Amigos
                        BuscarAmigosFragment amigosFrag = (BuscarAmigosFragment) adaptador.getPage(4);
                        if (newText != null && !newText.isEmpty()){ //se houver texto na caixa de pesquisa, (executar ação da pesquisa)
                            amigosFrag.pesquisarAmigos(newText.toLowerCase());
                        }else { //se não houver texto na caixa de pesquisa, recarrega a lista de amigos completa
                            amigosFrag.recarregarListaUsuarios(); //recupera a lista completa se a pesquisa estiver vazia ou o chef tenha saido da aba pesquisa
                        }
                        break;
                }
                return true;
            }
        });
    }

    private void eventoFecharPesquisa(final ViewPager paginaVisualizada, final FragmentPagerItemAdapter adaptador) {
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {     }

            @Override
            public void onSearchViewClosed() { //ao fechar a caixa de pesquisa recarregar lista completa

                switch (paginaVisualizada.getCurrentItem()){

                    case 0:
                        MinhasReceitasFragment mrFrag = (MinhasReceitasFragment) adaptador.getPage(0);
                        mrFrag.recarregarMinhasReceitas();
                        break;

                    case 1:
                        FeedFragment fdFrag = (FeedFragment) adaptador.getPage(1);
                        fdFrag.recarregarFeed();
                        break;

                    case 2:
                        AmigosFragment amigosFrag = (AmigosFragment) adaptador.getPage(2);
                        amigosFrag.recarregarAmigos();
                     break;

                    case 3:
                        ReceitasUsuariosFragment ruFrag = (ReceitasUsuariosFragment) adaptador.getPage(3);
                        ruFrag.recarregarReceitasUsuarios();
                        break;

                    case 4:
                        BuscarAmigosFragment friendsFrag = (BuscarAmigosFragment) adaptador.getPage(4);
                        friendsFrag.recarregarListaUsuarios();
                        break;

                }
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
            case R.id.menuPoliticasPrivacidade:
                abrirPoliticasPrivacidade();
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
            Toast.makeText(MainActivity.this,
                    R.string.logout,
                    Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Toast.makeText(MainActivity.this,
                    R.string.erro_logout,
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    public void abrirConfiguracoes(){
        Intent intent = new Intent(MainActivity.this, ConfiguracoesActivity.class);
        startActivity(intent);
    }

    public void abrirPoliticasPrivacidade(){

        String urlPoliticas = "https://rubensfelipevaz.wixsite.com/politicas-receitaboa";
        Uri uri = Uri.parse(urlPoliticas);

        Intent abrirNavegador = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(abrirNavegador);

    }

    @Override
    protected void onStop() {
        super.onStop();
        searchView.closeSearch();
    }
}
