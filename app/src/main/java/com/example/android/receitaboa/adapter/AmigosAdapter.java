package com.example.android.receitaboa.adapter;
;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.receitaboa.R;
import com.example.android.receitaboa.model.Chef;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.List;

public class AmigosAdapter extends ArrayAdapter<Chef>  {

    private Context context;
    private int layoutResource;
    private List<Chef> listaAmigos;

    public AmigosAdapter(Context context, int resource, List<Chef> objects){
        super(context, resource, objects);
        this.context = context;
        this.layoutResource = resource;
        this.listaAmigos = objects;
    }

    public List<Chef> getListaAmigos(){ //recupera a lista de amigos atualizada (seja na busca dos amigos ou na lista completa) esse método é importante para que ao pesquisar um amigo, a sua posição na lista não seja alterada
        return this.listaAmigos;
    }

    public class ViewHolder{
        ImageView fotoAmigo;
        TextView nomeAmigo;
        ProgressBar progressBar;
    }

    public View getView(int position, View convertView, ViewGroup parent){

        final ViewHolder viewHolder;

        if (convertView == null){

            viewHolder = new ViewHolder();
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(layoutResource, parent,false);

            viewHolder.nomeAmigo = convertView.findViewById(R.id.textNomeReceitaPerfil);
            viewHolder.fotoAmigo = convertView.findViewById(R.id.fotoReceitaPerfil);
            viewHolder.progressBar = convertView.findViewById(R.id.progressGridPerfil);

            convertView.setTag(viewHolder);

        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final Chef amigo = listaAmigos.get(position);

        viewHolder.nomeAmigo.setText(amigo.getNome());

        //verica se o usuário já colocou uma foto
        if (amigo.getUrlFotoChef() != null){

            mostrarImagem(amigo.getUrlFotoChef(), viewHolder);

        }else {

            String imgPadrao = "android.resource://com.example.android.receitaboa/drawable/avatar";

            mostrarImagem(imgPadrao, viewHolder);
        }
        return convertView;
    }

    private void mostrarImagem(String caminhoFoto, final ViewHolder vHolder) {

        //Recuperar as fotos da lista de receitas do amigo de acordo com a posição e seta elas na GridView
        ImageLoader imageLoader = ImageLoader.getInstance();

        imageLoader.displayImage(caminhoFoto, vHolder.fotoAmigo,
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
