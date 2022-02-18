package com.example.asteroides;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.prefs.Preferences;

public class MainActivity extends AppCompatActivity {

    public static AlmacenPuntuaciones almacen = new AlmacenPuntuacionesArray();

    MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View vista = findViewById(R.id.fondo_id);
        vista.setBackgroundResource(R.drawable.degradado);
        mp = MediaPlayer.create(this, R.raw.audio);
        mp.start();
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            lanzarPreferencias(null);
            return true;
        }

        if (id == R.id.acercaDe) {
            lanzarAcercaDe(null);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void lanzarJuego(View view) {
        Intent i = new Intent(this, Juego.class);
        Toast.makeText(getApplicationContext(), "Abrió la ventana de JUEGO", Toast.LENGTH_LONG).show();
        startActivity(i);
    }

    public void lanzarAcercaDe(View view) {
        Intent i = new Intent(this, AcercaDe.class);
        Toast.makeText(getApplicationContext(), "Abrió la ventana acercade", Toast.LENGTH_LONG).show();
        startActivity(i);
    }

    public void lanzarPreferencias(View view) {
        Intent i = new Intent(this, Preferencias.class);
        Toast.makeText(getApplicationContext(), "Abrió la ventana de preferencias", Toast.LENGTH_LONG).show();
        startActivity(i);
    }

    public void mostrarPreferencias(View view){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String s = "música: " + pref.getBoolean("musica", true) + ", gráficos: " + pref.getString("graficos", "?");
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    public void lanzarPuntuaciones(View view) {
        Intent i = new Intent(this, Puntuaciones.class);
        startActivity(i);
    }
}