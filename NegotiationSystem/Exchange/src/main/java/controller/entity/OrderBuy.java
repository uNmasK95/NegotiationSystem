package controller.entity;

import co.paralleluniverse.actors.ActorRef;

import javax.jws.soap.SOAPBinding;

public class OrderBuy {

    private String company;
    private int quant;
    private float price;
    private User user;
    private ActorRef userRef;

    public OrderBuy(String company, int quant, float price, User user, ActorRef userRef) {
        this.company = company;
        this.quant = quant;
        this.price = price;
        this.user = user;
        this.userRef = userRef;
    }

    public String getCompany() {
        return company;
    }

    public int getQuant() {
        return quant;
    }

    public float getPrice() {
        return price;
    }

    public User getUser() {
        return user;
    }

    public ActorRef getUserRef() {
        return userRef;
    }
}
