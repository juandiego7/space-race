package Models;

public class Meteorito {
    private int posX;
    private int posY;
    private float pendiente;
    private float intercepto;
    private int tamaño;
    private int imagen;

    public int getImagen() {
        return imagen;
    }

    public void setImagen(int imagen) {
        this.imagen = imagen;
    }

    public Meteorito(int posX, int posY, float intercepto, float pendiente, int tamaño, int imagen) {
        this.posX = posX;
        this.posY = posY;
        this.intercepto = intercepto;
        this.pendiente = pendiente;
        this.tamaño = tamaño;
        this.imagen = imagen;
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public float getPendiente() {
        return pendiente;
    }

    public void setPendiente(float pendiente) {
        this.pendiente = pendiente;
    }

    public float getIntercepto() {
        return intercepto;
    }

    public void setIntercepto(float intercepto) {
        this.intercepto = intercepto;
    }

    public int getTamaño() {
        return tamaño;
    }

    public void setTamaño(int tamaño) {
        this.tamaño = tamaño;
    }
}
