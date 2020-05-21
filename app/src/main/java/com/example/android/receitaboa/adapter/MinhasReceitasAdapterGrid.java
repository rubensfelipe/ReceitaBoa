package com.example.android.receitaboa.adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.android.receitaboa.R;
import com.example.android.receitaboa.model.Receitas;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class MinhasReceitasAdapterGrid extends ArrayAdapter<Receitas> {

    private Context context;
    private int layoutResource;
    private List<Receitas> listaMinhasReceitas;

    public MinhasReceitasAdapterGrid(@NonNull Context context, int resource, List<Receitas> objects) {
        super(context, resource, objects);
        this.context = context;
        this.layoutResource = resource;
        this.listaMinhasReceitas = objects;
    }

    public class ViewHolder{
        ImageView imagemReceita;
        TextView nomeReceita;
        TextView qtdPessoasServidas;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder viewHolder;

        //Caso a view seja nula, precisamos mostra-la
        if (convertView == null){

            viewHolder = new ViewHolder();
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(layoutResource, parent, false);
            viewHolder.imagemReceita = convertView.findViewById(R.id.imageListaFotoReceita);
            viewHolder.nomeReceita = convertView.findViewById(R.id.textNomeReceita);
            viewHolder.qtdPessoasServidas = convertView.findViewById(R.id.textQtdPessoasServidas);

            convertView.setTag(viewHolder);

        }else { //caso a lista já tenha sido criada, ela será recuperada (não será criada outra vez)
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //Recupera os dados das Minhas Receitas
        Receitas minhaListaReceitas = listaMinhasReceitas.get(position);

        viewHolder.nomeReceita.setText(minhaListaReceitas.getNomeReceita());
        viewHolder.qtdPessoasServidas.setText(minhaListaReceitas.getQtdPessoasServidas());


        if (minhaListaReceitas.getUrlFotoReceita()!=null){
            Uri fotoReceitaUri = Uri.parse(minhaListaReceitas.getUrlFotoReceita()); //String->Uri
            Glide.with(context).load(fotoReceitaUri).into(viewHolder.imagemReceita);
        }else {
           viewHolder.imagemReceita.setImageResource(R.drawable.cloche_tableware_png);
        }


        return convertView;
    }
}
