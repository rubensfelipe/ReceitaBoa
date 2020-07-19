package com.example.android.receitaboa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.receitaboa.R;
import com.example.android.receitaboa.model.Receitas;

import java.util.List;

public class RA_Adapter extends RecyclerView.Adapter<RA_Adapter.MyViewHolder> {

    private Context context;
    private List<Receitas> listaReceitasAmigos;
    private MyViewHolder holder;
    private int position;

    public RA_Adapter(List<Receitas> list, Context c) {
        this.listaReceitasAmigos = list;
        this.context = c;
    }

    //ADICIONAR QUANDO FOR ARRUMAR A PESQUISA DE RECEITAS
    //indentifica se a lista que está sendo utilizada pelo adapter é a lista completa das minhas receitas ou a lista de busca de uma receita especifica da minha lista de receitas (assim as receitas mantem as suas posições da lista completa)
    public List<Receitas> getListaReceitas(){
        return this.listaReceitasAmigos;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_ra, parent,false);
        //View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_receitas_amigos, parent,false);
        return new RA_Adapter.MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        this.holder = holder;
        this.position = position;

        //Dados Receitas Amigo
        final Receitas receita = listaReceitasAmigos.get(position);

        //Carrega os dados de texto da receita
        holder.nomeReceitaAmigo.setText(receita.getNome());
        holder.nomeAmigo.setText(receita.getNomeChef());

        /*
        if (receita.getUrlFotoReceita() != null){
            //Recupera o caminho da foto da receita
            Uri uriFotoReceita = Uri.parse(receita.getUrlFotoReceita());

            //Carrega foto da receita na lista
            Glide.with(context).load(uriFotoReceita).into(holder.fotoReceitaAmigo);
        }else {
            holder.fotoReceitaAmigo.setImageResource(R.drawable.avatar);
        }
         */


    }


    @Override
    public int getItemCount() {
        return listaReceitasAmigos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        //ImageView fotoReceitaAmigo;
        TextView nomeReceitaAmigo;
        TextView nomeAmigo;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            //fotoReceitaAmigo = itemView.findViewById(R.id.fotoReceitaAmigo);
            nomeReceitaAmigo = itemView.findViewById(R.id.tvNomeReceitaAmigo);
            nomeAmigo = itemView.findViewById(R.id.tvNomeAmigo);

        }
    }

}
