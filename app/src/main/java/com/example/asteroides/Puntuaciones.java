package com.example.asteroides;

import android.app.ListActivity;
import android.os.Bundle;

public class Puntuaciones extends ListActivity {

    @Override public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puntuaciones);

        setListAdapter(new Adaptador(this, MainActivity.almacen.listaPuntuaciones(10)));
    }
}