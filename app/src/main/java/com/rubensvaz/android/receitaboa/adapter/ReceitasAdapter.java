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

public class ReceitasAdapter extends RecyclerView.Adapter<ReceitasAdapter.MyViewHolder> {

    private Context context;
    private List<Receitas> listaReceitas;
    private MyViewHolder holder;
    private int position;

    public ReceitasAdapter(List<Receitas> listaSelecionada, Context c) {
        this.listaReceitas = listaSelecionada;
        this.context = c;
    }

    //indentifica se a lista que está sendo utilizada pelo adapter é a lista completa das minhas receitas ou a lista de busca de uma receita especifica da minha lista de receitas (assim as receitas mantem as suas posições da lista completa)
    public List<Receitas> getListaReceitas(){
        return this.listaReceitas;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_minha_receita, parent,false);
        return new ReceitasAdapter.MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        this.holder = holder;
        this.position = position;

        final Receitas receita = listaReceitas.get(position);

        //Carrega os dados de texto da receita
        holder.nomeReceita.setText(receita.getNome());
        holder.qtdPessoasServidas.setText(receita.getQtdPessoasServidas());

        if (receita.getUrlFotoReceita() != null){
            //Recupera o caminho da foto da receita
            Uri uriFotoReceita = Uri.parse(receita.getUrlFotoReceita());

            //Carrega foto da receita na lista
            Glide.with(context).load(uriFotoReceita).into(holder.fotoReceita);
        }else {
            holder.fotoReceita.setImageResource(R.drawable.turkey_roast_3);
        }



        //verica se o usuário já colocou uma foto na receita

        /*
        if (receita.getUrlFotoReceita() != null){

            mostrarImagem(receita.getUrlFotoReceita(), holder);

        }else {
            String imgPadrao = "android.resource://com.rubensfelipe.android.receitaboa/drawable/turkey_roast_3";

            mostrarImagem(imgPadrao, holder);
        }
         */



    }


    @Override
    public int getItemCount() {
        return listaReceitas.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView fotoReceita;
        TextView nomeReceita;
        TextView qtdPessoasServidas;

        //ProgressBar progressBarCard;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            //progressBarCard = itemView.findViewById(R.id.progressBarCard);

            fotoReceita = itemView.findViewById(R.id.imageListaFotoReceita);
            nomeReceita = itemView.findViewById(R.id.textNomeReceitaPerfil);
            qtdPessoasServidas = itemView.findViewById(R.id.textQtdPessoasServidas);

        }
    }

    /*
    private void mostrarImagem(String caminhoFoto, final MyViewHolder vHolder) {

        //Recuperar as fotos da lista de receitas do amigo de acordo com a posição e seta elas na GridView
        ImageLoader imageLoader = ImageLoader.getInstance();

        imageLoader.displayImage(caminhoFoto, vHolder.fotoReceita,
                new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        vHolder.progressBarCard.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        vHolder.progressBarCard.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        vHolder.progressBarCard.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        vHolder.progressBarCard.setVisibility(View.GONE);
                    }
                });
    }
     */


}
