package com.rubensvaz.android.receitaboa.adapter;

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
import com.rubensfelipe.android.receitaboa.R;
import com.rubensvaz.android.receitaboa.model.Receitas;

import java.util.List;

public class ReceitasUsuariosAdapter extends RecyclerView.Adapter<ReceitasUsuariosAdapter.MyViewHolder> {

    private Context context;
    private List<Receitas> listaReceitasUsuarios;
    private MyViewHolder holder;
    private int position;

    public ReceitasUsuariosAdapter(List<Receitas> list, Context c) {
        this.listaReceitasUsuarios = list;
        this.context = c;
    }

    //ADICIONAR QUANDO FOR ARRUMAR A PESQUISA DE RECEITAS
    //indentifica se a lista que está sendo utilizada pelo adapter é a lista completa das minhas receitas ou a lista de busca de uma receita especifica da minha lista de receitas (assim as receitas mantem as suas posições da lista completa)
    public List<Receitas> getListaReceitasUsuarios(){
        return this.listaReceitasUsuarios;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_receita, parent,false);
        return new ReceitasUsuariosAdapter.MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        this.holder = holder;
        this.position = position;

        //Dados Receitas Amigo
        final Receitas receita = listaReceitasUsuarios.get(position);

        //Carrega os dados de texto da receita
        holder.nomeReceitaUsuario.setText(receita.getNome());
        holder.qtdPessoasServidasUser.setText(receita.getQtdPessoasServidas());

        holder.nomeUsuario.setText("Chef " + receita.getNomeChef());

        if (receita.getUrlFotoReceita() != null){
            //Recupera o caminho da foto da receita
            Uri uriFotoReceita = Uri.parse(receita.getUrlFotoReceita());

            //Carrega foto da receita na lista
            Glide.with(context).load(uriFotoReceita).into(holder.fotoReceitaUsuario);
        }else {
            holder.fotoReceitaUsuario.setImageResource(R.drawable.turkey_roast_3);
        }

    }


    @Override
    public int getItemCount() {
        return listaReceitasUsuarios.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView fotoReceitaUsuario;
        TextView nomeReceitaUsuario;
        TextView qtdPessoasServidasUser;

        TextView nomeUsuario;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            fotoReceitaUsuario = itemView.findViewById(R.id.ivFotoReceitaUsuario);
            nomeReceitaUsuario = itemView.findViewById(R.id.tvNomeReceitaUsuario);
            qtdPessoasServidasUser = itemView.findViewById(R.id.tvQtdPessoasServidasUser);

            nomeUsuario = itemView.findViewById(R.id.tvNomeUser);

        }
    }

}
