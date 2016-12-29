package controller.entity;

import co.paralleluniverse.actors.ActorRef;

public class OrderBuy {

    private String company;
    private int quant;
    private float price;
    private ActorRef user;

    public OrderBuy(String company, int quant, float price, ActorRef user) {
        this.company = company;
        this.quant = quant;
        this.price = price;
        this.user = user;
    }

}
