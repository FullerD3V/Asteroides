package com.example.asteroides;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import java.util.Vector;


public class VistaJuego extends View {

    // ########################################################################################################
    //	    ATRIBUTOS
    // ########################################################################################################

    // Nave.

    private Grafico nave;                   // Objeto gráfico de la nave.
    private Drawable nave1, nave2, nave3;	// Imagenes.
    private int giroNave;                   // Incremento de dirección.
    private float aceleracionNave;          // Aumento de velocidad.
    private int vidas = 3;                  // Vidas de la nave.
    private int puntos = 0;                 // Puntuación.
    // Incremento estándar de giro y aceleración.

    private static final int PASO_GIRO_NAVE = 5;
    private static final float PASO_ACELERACION_NAVE = 0.5f;
    private static final int MAX_VELOCIDAD_NAVE = 5;

    // Misiles.

    private Vector <Grafico> misiles = new Vector();// Vector con los misiles en juego.
    private int municion = 100;                      // Cantidad de misiles que se pueden disparar a la vez. Irá aumentando.
    private static int PASO_VELOCIDAD_MISIL = 20;	// Velocidad.
    private float mX=0, mY=0;                       // Coordenadas del misil.
    private boolean disparo = false;
    private Drawable drawableMisil;                 // Imagen.
    private final int PUNTOS_POWER_UP = 200;        // Al llegar a esta puntuación, se dispara de 3 en 3.
    private boolean armamentoMejorado = false;

    // Asteroides.

    private Drawable drawableAsteroide, drawableExplos, drawableMediano, drawableChico;	// Objeto gráfico de los asteroides y sus explosiones.
    private Vector<Grafico> Asteroides;					// Vector con los asteroides.
    private int numAsteroides = 5;       				// Número inicial de asteroides.
    private int numFragmentos = 2;       				// Fragmentos en los que se divide.
    private final int TIEMPO_RESPAWN_ASTEROIDE = 5;   	// Tiempo en los que reaparecen.
    private final int TIEMPO_QUITAR_EXPLOSION = 1;		// Tiempo en que desaparecen las explosiones.
    private final int LIMITE_ASTEROIDES_GRANDES = 20;   // Límite de asteroides que pueden aparecer.

    // Dimensiones del campo.

    private int alt;
    private int anc;

    // Hilo y tiempo.

    private ThreadJuego thread = new ThreadJuego();		// Hilo encargado de procesar el juego.

    private static int PERIODO_PROCESO = 50; 			// Cada cuanto queremos procesar cambios (en milisegundos). Menos de 50 petamos la tarjeta gráfica.
    private long ultimoProceso = 0; 					// Cuando se realizó el último proceso.
    protected long ahora = 0;

    // Mensajes.

    private boolean hayMensaje = false;                 // Mostramos un mensaje si es verdadero.
    private Drawable mensDrawable;                      // Su imagen.
    private Grafico mensaje;                            // Su gráfico.
    private boolean finDelJuego = false;                // Mostramos un mensaje GAME OVER si es verdadero.
    private Drawable mensajeGameOverDrawable;           // Su imagen.
    private Grafico mensajeGameOver;                    // Su gráfico.
    private Drawable fondoGameOverDrawable;             // Su imagen de fondo.
    private Grafico fondoGameOver;                      // Su gráfico de fondo.

    private View vista = this;

    MediaPlayer mpDisparo, mpExplosion;

    // ########################################################################################################
    //	    CONSTRUCTOR
    // ########################################################################################################

    public VistaJuego(Context context, AttributeSet attrs) {

        super(context, attrs);

        nave3 = context.getResources().getDrawable(R.drawable.nave);
        nave2 = context.getResources().getDrawable(R.drawable.nave);
        nave1 = context.getResources().getDrawable(R.drawable.nave);
        nave = new Grafico(this, context.getResources().getDrawable(R.drawable.nave));

        drawableMisil = context.getResources().getDrawable(R.drawable.misil1);

        mpDisparo = MediaPlayer.create(context, R.raw.disparo);
        mpExplosion = MediaPlayer.create(context, R.raw.explosion);


        drawableAsteroide = context.getResources().getDrawable(R.drawable.asteroide1);
        drawableMediano = context.getResources().getDrawable(R.drawable.asteroide2);
        drawableChico = context.getResources().getDrawable(R.drawable.asteroide3);
        drawableExplos = context.getResources().getDrawable(R.drawable.explos);

        Asteroides = new Vector();

        for (int i = 0; i < numAsteroides; i++) {

            Grafico asteroide = new Grafico(this, drawableAsteroide);

            asteroide.setIncY(Math.random() * 4 - 2);				// Velocidad aleatoria.
            asteroide.setIncX(Math.random() * 4 - 2);
            asteroide.setAngulo((int) (Math.random() * 360));		// Rotación aleatoria.
            asteroide.setRotacion((int) (Math.random() * 8 - 4));

            Asteroides.add(asteroide);                              // Asteroide añadido.
        }

        mensDrawable = getContext().getDrawable(R.drawable.mensajearmamento);
        mensaje = new Grafico(this, mensDrawable);
        mensajeGameOverDrawable = getContext().getDrawable(R.drawable.mensajefinal);
        mensajeGameOver = new Grafico(this, mensajeGameOverDrawable);
        fondoGameOverDrawable = getContext().getDrawable(R.drawable.fondonegro);
        fondoGameOver = new Grafico(this, fondoGameOverDrawable);
        fondoGameOver.setPosY(0);
        fondoGameOver.setPosX(0);
    }

    // ########################################################################################################
    //	    EVENTOS
    // ########################################################################################################

    @Override protected void onSizeChanged(int ancho, int alto, int ancho_anter, int alto_anter) {

        super.onSizeChanged(ancho, alto, ancho_anter, alto_anter);

        anc = ancho;
        alt = alto;

        nave.setPosX((ancho/2)-(nave.getAncho()/2));
        nave.setPosY((alto/2)-(nave.getAlto()/2));

        for (Grafico asteroide: Asteroides) {

            do{
                asteroide.setPosX(Math.random()*(ancho-asteroide.getAncho()));
                asteroide.setPosY(Math.random()*(alto-asteroide.getAlto()));

            } while(asteroide.distancia(nave) < (ancho+alto)/5);
        }

        // Posicionamos el mensaje, game over...

        mensaje.setPosX((ancho/2)-(mensaje.getAncho()/2));
        mensaje.setPosY(100);

        mensajeGameOver.setPosX((ancho/2)-(mensajeGameOver.getAncho()/2));
        mensajeGameOver.setPosY((alto/2)-(mensajeGameOver.getAlto()/2));

        ultimoProceso = System.currentTimeMillis();
        thread.start();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        nave.dibujaGrafico(canvas);
        try {
            if (!(misiles.isEmpty())) {
                for (int m = 0; m < misiles.size(); m++) {
                    misiles.elementAt(m).dibujaGrafico(canvas);
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        try {
            for (Grafico asteroide : Asteroides) {
                asteroide.dibujaGrafico(canvas);
            }
        } catch (Exception e) {
            System.err.println("Error en el onDraw...");
            System.err.println(e.getMessage());
        }

        if (hayMensaje) {
            mensaje.dibujaGrafico(canvas);
        }

        if (finDelJuego) {
            fondoGameOver.dibujaGrafico(canvas);
            mensajeGameOver.dibujaGrafico(canvas);
        }

        Paint pincel = new Paint();
        pincel.setColor(Color.WHITE);
        pincel.setStrokeWidth(4);
        pincel.setStyle(Paint.Style.FILL);
        pincel.setTextSize(40);
        pincel.setTypeface(Typeface.SANS_SERIF);
        canvas.drawText("Puntos: "+puntos, 50,100,pincel);
    }

    @Override public boolean onKeyDown(int codigoTecla, KeyEvent evento) {

        super.onKeyDown(codigoTecla, evento);
        boolean procesada = true;

        switch (codigoTecla) {
            case KeyEvent.KEYCODE_DPAD_UP:
                aceleracionNave = +PASO_ACELERACION_NAVE;
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                giroNave = -PASO_GIRO_NAVE;
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                giroNave = +PASO_GIRO_NAVE;
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_P:
                if (thread.isPaused()) {
                    thread.reanudar();
                } else {
                    thread.pausar();
                }
            case KeyEvent.KEYCODE_ENTER:
                activaMisil();
                break;
            case KeyEvent.KEYCODE_M:
                colocarMina();
            default:
                procesada = false;
                break;
        }

        return procesada;
    }

    @Override
    public boolean onKeyUp(int codigoTecla, KeyEvent evento) {

        super.onKeyUp(codigoTecla, evento);


        boolean procesada = true;

        switch (codigoTecla) {
            case KeyEvent.KEYCODE_DPAD_UP:
                aceleracionNave = 0;
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                giroNave = 0;
                break;
            default:
                procesada = false;
                break;
        }
        return procesada;
    }

    @Override
    public boolean onTouchEvent (MotionEvent event) {

        super.onTouchEvent(event);

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                disparo=true;

                break;
            case MotionEvent.ACTION_MOVE:

                float dx = Math.abs(x - mX);
                float dy = Math.abs(y - mY);

                if (dy<6 && dx>6){
                    giroNave = Math.round((x - mX) / 2);
                    disparo = false;
                } else if (dx<6 && dy>6){
                    aceleracionNave = Math.round((mY - y) / 25);
                    disparo = false;
                }

                break;

            case MotionEvent.ACTION_UP:

                giroNave = 0;
                aceleracionNave = 0;

                if (disparo){
                    activaMisil();
                }
                break;
        }
        mX=x; mY=y;
        return true;
    }

    synchronized private void actualizaFisica() {

        ahora = System.currentTimeMillis();

        if (ultimoProceso + PERIODO_PROCESO > ahora) {return;}

        double retardo = (ahora - ultimoProceso) / PERIODO_PROCESO; // Para una ejecución en tiempo real, calculamos retardo.
        ultimoProceso = ahora;                                      // Para la próxima vez.

        nave.setAngulo((int) (nave.getAngulo() + giroNave * retardo));

        double nIncX = nave.getIncX() + aceleracionNave * Math.cos(Math.toRadians(nave.getAngulo())) * retardo;
        double nIncY = nave.getIncY() + aceleracionNave * Math.sin(Math.toRadians(nave.getAngulo())) * retardo;

        if (Math.hypot(nIncX,nIncY) <= MAX_VELOCIDAD_NAVE){
            nave.setIncX(nIncX);
            nave.setIncY(nIncY);
        }

        nave.incrementaPos(retardo);

        try {
            for (Grafico asteroide : Asteroides) {      // Movemos los asteroides.
                asteroide.incrementaPos(retardo);
            }
        } catch (Exception e) {
            System.err.println("Error en el actualizaFisica al querer mover los asteroides.");
            System.err.println(e.getMessage());
        }

        try {
            if (!(misiles.isEmpty())) {
                for (int m = 0; m < misiles.size(); m++) {

                    misiles.elementAt(m).incrementaPos(retardo);

                    for (int i = 0; i < Asteroides.size(); i++) {
                        if (misiles.elementAt(m).verificaColision(Asteroides.elementAt(i)) && (Asteroides.elementAt(i).getDrawable() == drawableAsteroide || Asteroides.elementAt(i).getDrawable() == drawableMediano || Asteroides.elementAt(i).getDrawable() == drawableChico)) {
                            destruyeAsteroide(i, m);                // Asteroide destruido.
                            mpExplosion.seekTo(0);
                            if(!mpExplosion.isPlaying())
                                mpExplosion.start();
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {System.err.println(e.getMessage());}

        try {
            for (int i = 0; i < Asteroides.size(); i++) {
                if (nave.verificaColision(Asteroides.elementAt(i)) && (Asteroides.elementAt(i).getDrawable() == drawableAsteroide || Asteroides.elementAt(i).getDrawable() == drawableMediano || Asteroides.elementAt(i).getDrawable() == drawableChico)) {

                    destruyeAsteroide(i);

                    vidas--;

                    if (vidas == 2) {
                        nave.setDrawable(nave2);
                    } else if (vidas == 1) {
                        nave.setDrawable(nave1);
                    } else if (vidas == 0) {
                        new ThreadGameOver().start();
                    }

                    break;
                }

            }
        } catch (Exception e) {
            System.err.println("Error de índice de array en actualizaFísica...");
            System.err.println(e.getMessage());
        }
    }

    private void colocarMina() {
        Drawable minaDrawable = getContext().getDrawable(R.drawable.mina);
        Grafico mina = new Grafico(vista, minaDrawable);

        mina.setAngulo(1);
        mina.setRotacion(1);

        mina.setCenX(nave.getCenX());
        mina.setCenY(nave.getCenY());

    }

    private void destruyeAsteroide(int i) {

        boolean eraUnoGrande = false;

        if (Asteroides.elementAt(i).getDrawable() == drawableAsteroide) {
            eraUnoGrande = true;
        }

        Grafico explos = new Grafico(this, drawableExplos);

        explos.setCenX(Asteroides.get(i).getCenX());
        explos.setCenY(Asteroides.get(i).getCenY());

        Asteroides.add(explos);
        Asteroides.remove(i);

        new ThreadQuitarExplosion(explos).start();

        if (eraUnoGrande) {
            municion++;
            new ThreadNuevoAsteroide().start();
            if (Asteroides.size() < LIMITE_ASTEROIDES_GRANDES) {
                new ThreadNuevoAsteroide().start();
            }
        }
    }

    private void destruyeAsteroide(int i, int m) {
        boolean eraUnoGrande = false;

        if (Asteroides.elementAt(i).getDrawable() == drawableAsteroide) {
            eraUnoGrande = true;
        }

        puntos += 10;

        if (puntos == PUNTOS_POWER_UP) {
            armamentoMejorado = true;
            new ThreadMensaje().start();
        }

        Grafico explos = new Grafico(this, drawableExplos);

        explos.setCenX(Asteroides.get(i).getCenX());
        explos.setCenY(Asteroides.get(i).getCenY());

        if (eraUnoGrande) {
            for (int f = 0; f < numFragmentos; f++) {
                Grafico fragmento = new Grafico(this, drawableMediano);
                fragmento.setIncY(Math.random() * 4 - 2);
                fragmento.setIncX(Math.random() * 4 - 2);
                fragmento.setAngulo((int) (Math.random() * 360));
                fragmento.setRotacion((int) (Math.random() * 8 - 4));
                fragmento.setCenX(Asteroides.get(i).getCenX());
                fragmento.setCenY(Asteroides.get(i).getCenY());
                Asteroides.add(fragmento);
            }
        } else if (Asteroides.elementAt(i).getDrawable() == drawableMediano) {
            for (int f = 0; f < numFragmentos; f++) {
                Grafico fragmento = new Grafico(this, drawableChico);
                fragmento.setIncY(Math.random() * 4 - 2);
                fragmento.setIncX(Math.random() * 4 - 2);
                fragmento.setAngulo((int) (Math.random() * 360));
                fragmento.setRotacion((int) (Math.random() * 8 - 4));
                fragmento.setCenX(Asteroides.get(i).getCenX());
                fragmento.setCenY(Asteroides.get(i).getCenY());
                Asteroides.add(fragmento);
            }
        }

        Asteroides.add(explos);
        Asteroides.remove(i);
        misiles.remove(m);



        new ThreadQuitarExplosion(explos).start();

        System.out.println(puntos);
        if (eraUnoGrande) {
            municion++;
            new ThreadNuevoAsteroide().start();
            if (Asteroides.size() < LIMITE_ASTEROIDES_GRANDES) {
                new ThreadNuevoAsteroide().start();
            }
        }
    }

    private void activaMisil() {
        mpDisparo.seekTo(0);
        if(!mpDisparo.isPlaying())
            mpDisparo.start();
        try {
            if (misiles.size() <= municion) {

                Grafico misil = new Grafico(this, drawableMisil);

                misil.setCenX(nave.getCenX());
                misil.setCenY(nave.getCenY());
                misil.setAngulo((int) nave.getAngulo());
                misil.setIncX(Math.cos(Math.toRadians(misil.getAngulo())) * PASO_VELOCIDAD_MISIL);
                misil.setIncY(Math.sin(Math.toRadians(misil.getAngulo())) * PASO_VELOCIDAD_MISIL);

                misiles.add(misil);

                try {
                    if (armamentoMejorado) {

                        Grafico misil2 = new Grafico(this, drawableMisil);

                        misil2.setCenX(nave.getCenX());
                        misil2.setCenY(nave.getCenY());
                        misil2.setAngulo((int) nave.getAngulo()-2);
                        misil2.setIncX((Math.cos(Math.toRadians(misil.getAngulo())) * PASO_VELOCIDAD_MISIL)-2);
                        misil2.setIncY((Math.sin(Math.toRadians(misil.getAngulo())) * PASO_VELOCIDAD_MISIL)-2);

                        misiles.add(misil2);

                        Grafico misil3 = new Grafico(this, drawableMisil);

                        misil3.setCenX(nave.getCenX());
                        misil3.setCenY(nave.getCenY());
                        misil3.setAngulo((int) nave.getAngulo()+2);
                        misil3.setIncX((Math.cos(Math.toRadians(misil.getAngulo())) * PASO_VELOCIDAD_MISIL)+2);
                        misil3.setIncY((Math.sin(Math.toRadians(misil.getAngulo())) * PASO_VELOCIDAD_MISIL)+2);

                        misiles.add(misil3);

                        Grafico misil4 = new Grafico(this, drawableMisil);

                        misil4.setCenX(nave.getCenX());
                        misil4.setCenY(nave.getCenY());
                        misil4.setAngulo((int) nave.getAngulo()-3);
                        misil4.setIncX((Math.cos(Math.toRadians(misil.getAngulo())) * PASO_VELOCIDAD_MISIL)-3);
                        misil4.setIncY((Math.sin(Math.toRadians(misil.getAngulo())) * PASO_VELOCIDAD_MISIL)-3);

                        misiles.add(misil4);

                        Grafico misil5 = new Grafico(this, drawableMisil);

                        misil5.setCenX(nave.getCenX());
                        misil5.setCenY(nave.getCenY());
                        misil5.setAngulo((int) nave.getAngulo()+3);
                        misil5.setIncX((Math.cos(Math.toRadians(misil.getAngulo())) * PASO_VELOCIDAD_MISIL)+3);
                        misil5.setIncY((Math.sin(Math.toRadians(misil.getAngulo())) * PASO_VELOCIDAD_MISIL)+3);

                        misiles.add(misil5);

                        Grafico misil6 = new Grafico(this, drawableMisil);

                        misil6.setCenX(nave.getCenX());
                        misil6.setCenY(nave.getCenY());
                        misil6.setAngulo((int) nave.getAngulo()-4);
                        misil6.setIncX((Math.cos(Math.toRadians(misil.getAngulo())) * PASO_VELOCIDAD_MISIL)-4);
                        misil6.setIncY((Math.sin(Math.toRadians(misil.getAngulo())) * PASO_VELOCIDAD_MISIL)-4);

                        misiles.add(misil6);

                        Grafico misil7 = new Grafico(this, drawableMisil);

                        misil7.setCenX(nave.getCenX());
                        misil7.setCenY(nave.getCenY());
                        misil7.setAngulo((int) nave.getAngulo()+4);
                        misil7.setIncX((Math.cos(Math.toRadians(misil.getAngulo())) * PASO_VELOCIDAD_MISIL)+4);
                        misil7.setIncY((Math.sin(Math.toRadians(misil.getAngulo())) * PASO_VELOCIDAD_MISIL)+4);

                        misiles.add(misil7);
                    }
                } catch (Exception e) {System.err.println(e.getMessage());}

            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    // ########################################################################################################
    //	    HILOS
    // ########################################################################################################

    class ThreadJuego extends Thread {

        private boolean pausa, corriendo;

        public boolean isPaused() {return pausa;}

        public synchronized void pausar() {pausa = true;}

        public synchronized void reanudar() {
            pausa = false;
            notify();
        }

        public void detener() {
            corriendo = false;
            if (pausa) reanudar();
        }

        @Override public void run() {
            corriendo = true;
            while (corriendo) {
                actualizaFisica();
                synchronized (this) {
                    while (pausa)
                        try {
                            wait();
                        } catch (Exception e) {}
                }
            }
        }
    }

    class ThreadNuevoAsteroide extends Thread {
        @Override
        public void run() {

            int tiempo = TIEMPO_RESPAWN_ASTEROIDE;

            while (tiempo > 0) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {e.printStackTrace();}
                tiempo--;
            }

            Grafico asteroide = new Grafico(vista, drawableAsteroide);

            do {
                asteroide.setIncY(Math.random() * 4 - 2);
                asteroide.setIncX(Math.random() * 4 - 2);
                asteroide.setAngulo((int) (Math.random() * 360));
                asteroide.setRotacion((int) (Math.random() * 8 - 4));
                asteroide.setPosX(Math.random()*(anc-asteroide.getAncho()));
                asteroide.setPosY(Math.random()*(alt-asteroide.getAlto()));

            } while (asteroide.distancia(nave) < (anc+alt)/5);

            Asteroides.add(asteroide);
            System.gc();
        }
    }

    class ThreadQuitarExplosion extends Thread {

        private Grafico ex;

        public ThreadQuitarExplosion(Grafico grafico) {
            this.ex = grafico;
        }

        @Override
        public void run() {

            int tiempo = TIEMPO_QUITAR_EXPLOSION;

            while (tiempo > 0) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {e.printStackTrace();}
                tiempo--;
            }

            for (int i = 0; i < Asteroides.size(); i++) {
                if (Asteroides.get(i) == ex) {
                    Asteroides.remove(i);
                    break;
                }
            }
        }
    }

    class ThreadMensaje extends Thread {

        @Override
        public void run() {

            hayMensaje = true;

            int tiempo = 3;

            while (tiempo > 0) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {e.printStackTrace();}
                tiempo--;
            }

            hayMensaje = false;
        }
    }

    class ThreadGameOver extends Thread {
        @Override
        public void run() {

            Asteroides.clear();
            misiles.clear();

            System.gc();

            int tiempo = 1;
            finDelJuego = true;

            while (tiempo > 0) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {e.printStackTrace();}
                tiempo--;
            }

            ((Activity) getContext()).finish();
        }
    }

    public ThreadJuego getThread() {
        return thread;
    }
    public int getNumFragmentos() {return numFragmentos;}
    public void setNumFragmentos(int numFragmentos) {this.numFragmentos = numFragmentos;}
}
