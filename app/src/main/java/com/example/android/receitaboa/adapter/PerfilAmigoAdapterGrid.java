package com.example.android.receitaboa.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.android.receitaboa.R;
import com.example.android.receitaboa.model.Receitas;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.List;

public class PerfilAmigoAdapterGrid extends ArrayAdapter<Receitas> {

    private Context context;
    private int layoutResource;
    private List<Receitas> listaReceitasAmigo;


    public PerfilAmigoAdapterGrid(@NonNull Context context, int resource, List<Receitas> objects) {
        super(context, resource, objects);
        this.context = context;
        this.layoutResource = resource;
        this.listaReceitasAmigo = objects;
    }

    public class ViewHolder{
        ImageView fotoReceita;
        TextView nomeReceita;
        ProgressBar progressBar;
    }

    public View getView(int position, View convertView, ViewGroup parent){

        final ViewHolder viewHolder;

        if (convertView == null){

            viewHolder = new ViewHolder();
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(layoutResource, parent,false);
            viewHolder.nomeReceita = convertView.findViewById(R.id.textNomeReceitaPerfil);
            viewHolder.progressBar = convertView.findViewById(R.id.progressGridPerfil);

            viewHolder.fotoReceita = convertView.findViewById(R.id.fotoReceitaPerfil);

            convertView.setTag(viewHolder);

        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final Receitas receitasAmigo = listaReceitasAmigo.get(position);

        viewHolder.nomeReceita.setText(receitasAmigo.getNome());

        //verica se o usuário já colocou uma foto na receita
        if (receitasAmigo.getUrlFotoReceita()==null){

            String imgPadrao = "android.resource://com.example.android.receitaboa/drawable/avatar";

            mostrarImagem(imgPadrao, viewHolder);

        }else {
            mostrarImagem(receitasAmigo.getUrlFotoReceita(), viewHolder);
        }
                return convertView;

    }

    private void mostrarImagem(String caminhoFoto, final ViewHolder vHolder) {

        //Recuperar as fotos da lista de receitas do amigo de acordo com a posição e seta elas na GridView
        ImageLoader imageLoader = ImageLoader.getInstance();

        imageLoader.displayImage(caminhoFoto, vHolder.fotoReceita,
                new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        vHolder.progressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        vHolder.progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        vHolder.progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        vHolder.progressBar.setVisibility(View.GONE);
                    }
                });

    }

}
