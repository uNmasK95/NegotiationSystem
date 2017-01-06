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

  public List<Match> match(Order o){
    ArrayList<Match> matches = new ArrayList<>();

    while (o.getQuant() > 0 && !this.orders.isEmpty()) {
      Order this_order = this.orders.get(0);
      int min = Math.min(o.getQuant(),this_order.getQuant());
      o.decrementQuantity(min);
      this_order.decrementQuantity(min);
      ActorRef compradorRef = null, vendedorRef = null;
      String comprador = null, vendedor = null;
      float precoV = 0, precoC = 0;
      switch (o.getTipo()){
        case COMPRA:
          compradorRef = o.getUserRef();
          comprador = o.getUser();
          precoC = o.getPrice();
          vendedorRef = this_order.getUserRef();
          vendedor = this_order.getUser();
          precoV = this_order.getPrice();
          break;
        case VENDA:
          vendedorRef = o.getUserRef();
          vendedor = o.getUser();
          precoV = o.getPrice();
          compradorRef = this_order.getUserRef();
          comprador = this_order.getUser();
          precoC = o.getPrice();
          break;
      }
      matches.add(new Match(
          (precoV+precoC)/2,
          precoV,
          precoC,
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