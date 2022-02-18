package com.example.asteroides;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

class Grafico {

    private Drawable drawable;

    private double posX, posY;
    private double incX, incY;

    private int angulo, rotacion;
    private int ancho, alto;
    private int radioColision;

    private View view;



    public static final int MAX_VELOCIDAD = 20;

    public Grafico(View view, Drawable drawable) {

        this.view = view;
        this.drawable = drawable;

        ancho = drawable.getIntrinsicWidth();
        alto = drawable.getIntrinsicHeight();
        radioColision = (alto + ancho) / 4;
    }

    public void dibujaGrafico(Canvas canvas) {

        canvas.save();

        int x = (int) (posX + ancho / 2);
        int y = (int) (posY + alto / 2);

        canvas.rotate((float) angulo, (float) x, (float) y);

        drawable.setBounds((int) posX, (int) posY,(int) posX + ancho, (int) posY + alto);
        drawable.draw(canvas);

        canvas.restore();

        int rInval = (int) Math.hypot(ancho, alto) / 2 + MAX_VELOCIDAD;

        view.invalidate(x - rInval, y - rInval, x + rInval, y + rInval);
    }

    public void incrementaPos(double factor) {

        posX += incX * factor;

        if (posX < -ancho / 2) {posX = view.getWidth() - ancho / 2;}
        if (posX > view.getWidth() - ancho / 2) {posX = -ancho / 2;}

        posY += incY * factor;

        if (posY < -alto / 2) {posY = view.getHeight() - alto / 2;}
        if (posY > view.getHeight() - alto / 2) {posY = -alto / 2;}

        angulo += rotacion * factor;
    }

    public double distancia(Grafico g) {
        return Math.hypot(posX - g.posX, posY - g.posY);
    }

    public boolean verificaColision(Grafico g) {
        return (distancia(g) < (radioColision + g.radioColision));
    }

    public int getAncho() {return ancho;}
    public int getAlto() {return alto;}
    public int getCenX() {return (int) this.posX + this.ancho / 2;}
    public int getCenY() {return (int) this.posY + this.alto / 2;}
    public double getAngulo() {return this.angulo;}
    public double getIncX() {return this.incX;}
    public double getIncY() {return this.incY;}

    public void setIncX(double incX) {this.incX = incX;}
    public void setIncY(double incY) {this.incY = incY;}
    public void setAngulo(int angulo) {this.angulo = angulo;}
    public void setRotacion(int rotacion) {this.rotacion = rotacion;}
    public void setPosX(double posX) {this.posX = posX;}
    public void setPosY(double posY) {this.posY = posY;}
    public void setCenX(int cenX) {this.posX = cenX-(ancho/2);}
    public void setCenY(int cenY) {this.posY = cenY-(alto/2);}

    public void setDrawable(Drawable drawable) {this.drawable = drawable;}

    public Drawable getDrawable() {
        return drawable;
    }

    public double getPosX() {
        return this.posX;
    }
    public double getPosY() {
        return this.posY;
    }

    public int getRotacion() {return rotacion;}
}
