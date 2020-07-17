package com.example.android.receitaboa.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.android.receitaboa.R;
import com.example.android.receitaboa.model.Feed;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterFeed extends RecyclerView.Adapter<AdapterFeed.MyViewHolder> {

    private List<Feed> listaFeed;
    private Context context;

    public AdapterFeed(List<Feed> listaFeed, Context context) {
        this.listaFeed = listaFeed;
        this.context = context;
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
        Uri uriFotoChef = Uri.parse(feed.getFotoUsuario());
        Uri uriFotoPostagem = Uri.parse(feed.getFotoPostagem());

        Glide.with(context).load(uriFotoChef).into(holder.fotoPerfil);
        Glide.with(context).load(uriFotoPostagem).into(holder.fotoPostagem);

        holder.nomeReceita.setText(feed.getNomeReceita());
        holder.nomeChef.setText(feed.getNomeChef());

    }

    @Override
    public int getItemCount() {
        return listaFeed.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

     CircleImageView fotoPerfil;
     TextView nomeChef, nomeReceita;
     ImageView fotoPostagem;

     public MyViewHolder(View itemView){
         super(itemView);

         fotoPerfil = itemView.findViewById(R.id.ciFotoPerfilFeed);
         fotoPostagem = itemView.findViewById(R.id.ivPostagem);
         nomeChef = itemView.findViewById(R.id.tvNomeChefFeed);
         nomeReceita = itemView.findViewById(R.id.tvNomeReceitaFeed);

     }
 }

}
