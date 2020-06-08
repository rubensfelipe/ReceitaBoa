package com.example.android.receitaboa.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.android.receitaboa.R;
import com.example.android.receitaboa.model.Chef;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

//Inicia o layout da lista adaptada (adaptador) [Isso que será mostrado na tela do usuario, o design de cada item (foto a esquerda, nome a direita canto superior e logo abaixo a ultima msg enviada) da lista]
public class AmigosAdapter extends RecyclerView.Adapter<AmigosAdapter.MyViewHolder> {

    private List<Chef> amigos;
    private Context context;

    public AmigosAdapter(List<Chef> listaAmigos, Context c) {
        this.amigos =  listaAmigos;
        this.context = c;
    }

    public List<Chef> getListAmigos(){ //recupera a lista de amigos atualizada (seja na busca dos amigos ou na lista completa) esse método é importante para que ao pesquisar um amigo, a sua posição na lista não seja alterada
        return this.amigos;
    }

    @NonNull
    @Override
    //Seta os itens (nome, email, foto, ultima conversa) na lista
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_amigos, parent, false);
        return new MyViewHolder(itemLista);  //MyViewHolder: método do construtor
    }

    @Override
    public void onBindViewHolder(@NonNull AmigosAdapter.MyViewHolder holder, int position) {

        Chef chef = amigos.get(position);

        holder.nomeAmigo.setText(chef.getNome());
        holder.emailAmigo.setText(chef.getEmail());

        if(chef.getUrlFotoChef() != null){
            Uri uriFotoChef = Uri.parse(chef.getUrlFotoChef()); //String -> Uri
            Glide.with(context).load(uriFotoChef).into(holder.fotoAmigo);
        }else {
            holder.fotoAmigo.setImageResource(R.drawable.avatar);
        }


    }

    @Override
    public int getItemCount() {
        return amigos.size();
    }

    //inicializa os componentes
    public  class MyViewHolder extends RecyclerView.ViewHolder{

        CircleImageView fotoAmigo;
        TextView nomeAmigo, emailAmigo;

        //Construtor
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            fotoAmigo = itemView.findViewById(R.id.imageViewFotoAmigo);
            nomeAmigo = itemView.findViewById(R.id.textNomeAmigo);
            emailAmigo = itemView.findViewById(R.id.textEmailAmigo);

        }
    }

}
