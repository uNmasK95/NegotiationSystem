package controller.entity;

import co.paralleluniverse.actors.ActorRef;

import java.util.ArrayList;
import java.util.List;

/*
  Lista de ordens do mesmo tipo (compra/venda) com o mesmo preco
 */
public class OrderList{

  private List<Order> orders;
  private float price;

  public OrderList(Order order){
    this.orders = new ArrayList<>();
    this.orders.add(order);
    this.price = order.getPrice();
  }
  public OrderList(ArrayList<Order> orders){
    this.orders = orders;
    this.price = orders.get(0).getPrice();
  }

  public float getPrice(){
    return this.price;
  }

  /*
   Devolve ordens que combinem com a ordem recebida,
   e mudifica estrutura interna e quantidade da ordem de input.
   Apenas se verificam quantidades, ou seja, assume-se
   que tipos (compra/venda) estao corretos, isto e,
   se a lista e constituida por compras,
   entao o input e uma venda, e vice-versa.
   Assume-se tambem que se o input e uma venda,
   entao o seu preco minimo e menor que o preco maximo
   desta lista; Se o input e uma compra, entao o preco maximo
   e maior que o preco minimo desta lista.
   */
  public List<Match> match(Order o){
    ArrayList<Match> matches = new ArrayList<>();

    while (o.getQuant() > 0 && !this.orders.isEmpty()) {
      Order this_order = this.orders.get(0);
      int min = Math.min(o.getQuant(),this_order.getQuant());
      o.decrementQuantity(min);
      this_order.decrementQuantity(min);
      ActorRef compradorRef = null;
      ActorRef vendedorRef = null;
      String comprador = null;
      String vendedor = null;
      switch (o.getTipo()){
        case COMPRA:
          compradorRef = o.getUserRef();
          comprador = o.getUser();
          vendedorRef = this_order.getUserRef();
          vendedor = this_order.getUser();
          break;
        case VENDA:
          vendedorRef = o.getUserRef();
          vendedor = o.getUser();
          compradorRef = this_order.getUserRef();
          comprador = this_order.getUser();
          break;
      }
      matches.add(new Match(
          (o.getPrice()+this_order.getPrice())/2,
          o.getCompany(),
          min,
          compradorRef,
          vendedorRef,
          comprador,
          vendedor
      ));
      if(this_order.isEmpty()){
        this.orders.remove(0);
      }
      else
        this_order = this.orders.get(0);
    }

    return matches;
  }

  public boolean isEmpty(){
    return this.orders.isEmpty();
  }

  public void add(Order o){
    this.orders.add(o);
  }

/*  @Override
  public int compareTo(OrderList orderList) {
    if(this.getPrice() > orderList.getPrice())
      return 1;
    else if(this.getPrice() < orderList.getPrice())
      return -1;
    else return 0;
  }*/
}