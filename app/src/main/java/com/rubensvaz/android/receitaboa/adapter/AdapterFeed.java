package com.rubensvaz.android.receitaboa.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.rubensfelipe.android.receitaboa.R;
import com.rubensvaz.android.receitaboa.activity.ComentariosActivity;
import com.rubensvaz.android.receitaboa.activity.VisualizarReceitaActivity;
import com.rubensvaz.android.receitaboa.model.Feed;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterFeed extends RecyclerView.Adapter<AdapterFeed.MyViewHolder> {
    private List<Feed> listaFeed;
    private Context context;

    public AdapterFeed(List<Feed> listaFeed, Context context) {
        this.listaFeed = listaFeed;
        this.context = context;
    }

    //indentifica se a lista que está sendo utilizada pelo adapter é a lista completa das postagens do feed ou a lista de busca das postagens de um usuario (amigo) (assim as postagens mantem as suas posições definidas na lista completa)
    public List<Feed> getListaFeed(){
        return this.listaFeed;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_feed,parent,false);
        return new AdapterFeed.MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        final Feed feed = listaFeed.get(position);

        //Carrega dados do feed
        /*Uri uriFotoChef = Uri.parse(feed.getFotoUsuario());
        Glide.with(context).load(uriFotoChef).into(holder.fotoPerfil);*/

        Uri uriFotoPostagem = Uri.parse(feed.getFotoPostagem());
        Glide.with(context).load(uriFotoPostagem).into(holder.fotoPostagem);

        if(feed.getFotoUsuario() != null){
            Uri uriFotoChef = Uri.parse(feed.getFotoUsuario()); //String -> Uri
            Glide.with(context).load(uriFotoChef).into(holder.fotoPerfil);
        }else {
            //holder.fotoPerfil.setImageResource(R.drawable.avatar); //estava dando erro
            holder.fotoPerfil.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.avatar));
        }

        holder.nomeReceita.setText(feed.getNomeReceita());
        holder.nomeChef.setText(feed.getNomeChef());

        holder.dataPostagem.setText(feed.getDataPostagem());

        //Adiciona evento de clique nos comentários
        holder.visualizarComentario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, ComentariosActivity.class);
                i.putExtra("idPostagem", feed.getIdPostagem());
                context.startActivity(i); //método que é utilizado em adapters
            }
        });

        eventoClickPostagem(holder, position);

    }

    private void eventoClickPostagem(final MyViewHolder hold, final int position) {

        hold.fotoPostagem.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                List<Feed> listaFeedAtualizada = getListaFeed(); //permite que a posição na lista da receitas não se altere msm qdo houve uma busca

                Feed postagemSelecionada = listaFeedAtualizada.get(position); //recupera qual item foi clicado de acordo com a posição na lista no momento do click

                Intent i = new Intent(context, VisualizarReceitaActivity.class);
                i.putExtra("dadosReceitaFeedClicada", postagemSelecionada);
                context.startActivity(i);

            }
        });

    }

    @Override
    public int getItemCount() {
        return listaFeed.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

     CircleImageView fotoPerfil;
     TextView nomeChef, nomeReceita, dataPostagem;
     ImageView fotoPostagem, visualizarComentario;

         public MyViewHolder(View itemView){
             super(itemView);

             dataPostagem = itemView.findViewById(R.id.dataPostagem);
             fotoPerfil = itemView.findViewById(R.id.ciFotoPerfilFeed);
             fotoPostagem = itemView.findViewById(R.id.ivPostagem);
             nomeChef = itemView.findViewById(R.id.tvNomeChefFeed);
             nomeReceita = itemView.findViewById(R.id.tvNomeReceitaFeed);

             visualizarComentario = itemView.findViewById(R.id.comentariosFeed);

         }

    }

}
