package com.rubensvaz.android.receitaboa.helper;

import java.text.SimpleDateFormat;

public class DateCustom {

    public static String dataAtual(){

        long data = System.currentTimeMillis(); //retorna data atual do sistema
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm"); //dd: 01,02 hh:mm:ss: 02:20:35
        String dataString = simpleDateFormat.format(data); //formata a data com formatação definida acima
        return dataString;

    }

}
