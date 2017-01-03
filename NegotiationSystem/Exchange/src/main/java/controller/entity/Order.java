package controller.entity;

import co.paralleluniverse.actors.ActorRef;

public class Order {

    private String company;
    private int quant;
    private float price;
    private String user;
    private ActorRef userRef;
    private Tipo tipo;

    public enum Tipo {COMPRA,VENDA};

    public Order(String company, int quant, float price, String user, ActorRef userRef, Tipo tipo) {
        this.company = company;
        this.quant = quant;
        this.price = price;
        this.user = user;
        this.userRef = userRef;
        this.tipo = tipo;
    }

    public void decrementQuantity(int quant){
        this.quant -= quant;
    }

    public void incrementQuantity(int quant){
        this.quant += quant;
    }

    public boolean isEmpty(){ return this.quant == 0;}

    public String getCompany() {
        return company;
    }

    public int getQuant() {
        return quant;
    }

    public float getPrice() {
        return price;
    }

    public String getUser() {
        return user;
    }

    public ActorRef getUserRef() {
        return userRef;
    }

    public Tipo getTipo(){ return tipo; }
}
