package Models;

public class Spacecraft {
    private int posX;
    private int posY;
    private int intervaloTiempo;
    private int imagen;
    private int vidas;
    private boolean movX;
    private boolean movY;
    private int centroX;
    private int centroY;
    private int ejeMenor;
    private int ejeMayor;

    public Spacecraft(int posX, int posY, int intervaloTiempo, int imagen, boolean movX, boolean movY) {
        this.posX = posX;
        this.posY = posY;
        this.intervaloTiempo = intervaloTiempo;
        this.imagen = imagen;
        this.movX = movX;
        this.movY = movY;
    }
    public Spacecraft(int posX, int posY, int intervaloTiempo, int imagen, boolean movX, boolean movY, int vidas) {
        this.posX = posX;
        this.posY = posY;
        this.intervaloTiempo = intervaloTiempo;
        this.imagen = imagen;
        this.movX = movX;
        this.movY = movY;
        this.vidas = vidas;
    }

    public Spacecraft(int posX, int posY, int imagen) {
        this.posX = posX;
        this.posY = posY;
        this.imagen = imagen;
    }

    public Spacecraft() {}

    public void reduceIntervaloTiempo(){
        if (this.intervaloTiempo > 10){
            this.intervaloTiempo-=10;
        }
    }

    public int getVidas() {return vidas;}

    public void setVidas(int vidas) {this.vidas = vidas;}

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {this.posX = posX;}

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public int getImagen() {
        return imagen;
    }

    public void setImagen(int imagen) {
        this.imagen = imagen;
    }

    public int getIntervaloTiempo() {
        return intervaloTiempo;
    }

    public void setIntervaloTiempo(int intervaloTiempo) {
        this.intervaloTiempo = intervaloTiempo;
    }

    public boolean isMovX() {
        return movX;
    }

    public void setMovX(boolean movX) {
        this.movX = movX;
    }

    public boolean isMovY() {
        return movY;
    }

    public void setMovY(boolean movY) {
        this.movY = movY;
    }
}
