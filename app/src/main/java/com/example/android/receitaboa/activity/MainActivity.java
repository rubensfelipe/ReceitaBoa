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
import com.example.android.receitaboa.fragment.PesquisaAmigosFragment;
import com.example.android.receitaboa.fragment.PesquisaReceitasFragment;
import com.example.android.receitaboa.fragment.MinhasReceitasFragment;
import com.example.android.receitaboa.helper.ConfiguracaoFirebase;
import com.google.firebase.auth.FirebaseAuth;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Receita Boa");
        setSupportActionBar(toolbar);

        //inicializar componentes

        //Configurar abas
        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(),
                FragmentPagerItems.with(this)
                .add("Receitas", MinhasReceitasFragment.class)
                .add("Feed", FeedFragment.class)
                .add("Buscar Receitas", PesquisaReceitasFragment.class)
                .add("Buscar Amigos", PesquisaAmigosFragment.class)
                .create()
        );
        ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);

        SmartTabLayout viewPagerTab = findViewById(R.id.viewPagerTab);
        viewPagerTab.setViewPager(viewPager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menuApagar:
                deslogarChef();
                finish();
                break;
            case R.id.menuEdicao:
                abrirConfiguracoes();
        }

        return super.onOptionsItemSelected(item);
    }

    public void deslogarChef(){

        try {
            autenticacao.signOut();
            Toast.makeText(MainActivity.this,"Deslogado",Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Toast.makeText(MainActivity.this,"ERRO",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    public void abrirConfiguracoes(){
        Intent intent = new Intent(MainActivity.this, ConfiguracoesActivity.class);
        startActivity(intent);
    }

}
