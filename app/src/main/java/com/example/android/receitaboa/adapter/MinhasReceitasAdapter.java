package com.example.android.receitaboa.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.android.receitaboa.R;
import com.example.android.receitaboa.model.Receitas;

import java.util.List;

public class MinhasReceitasAdapter extends RecyclerView.Adapter<MinhasReceitasAdapter.MyViewHolder> {

    private Context context;
    private List<Receitas> listaMinhasReceitas;
    private MyViewHolder holder;
    private int position;


    private RecyclerView.AdapterDataObserver observer = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {

        }
    };

    public MinhasReceitasAdapter(List<Receitas> list, Context c) {
        this.listaMinhasReceitas = list;
        this.context = c;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_minha_receita, parent,false);
        return new MinhasReceitasAdapter.MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        this.holder = holder;
        this.position = position;

        final Receitas receita = listaMinhasReceitas.get(position);


        if (receita.getUrlFotoReceita() != null){
            //Recupera o caminho da foto da receita
            Uri uriFotoReceita = Uri.parse(receita.getUrlFotoReceita());

            //Carrega foto da receita na lista
            Glide.with(context).load(uriFotoReceita).into(holder.fotoReceita);
        }else {
            holder.fotoReceita.setImageResource(R.drawable.cloche_tableware);
        }

        //Carrega os dados de texto da receita
        holder.nomeReceita.setText(receita.getNome());
        holder.qtdPessoasServidas.setText(receita.getQtdPessoasServidas());
    }


    @Override
    public int getItemCount() {
        return listaMinhasReceitas.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView fotoReceita;
        TextView nomeReceita;
        TextView qtdPessoasServidas;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            fotoReceita = itemView.findViewById(R.id.imageListaFotoReceita);
            nomeReceita = itemView.findViewById(R.id.textNomeReceita);
            qtdPessoasServidas = itemView.findViewById(R.id.textQtdPessoasServidas);

        }
    }

}
