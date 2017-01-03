package controller.entity;

import java.util.*;


public class Orders {

  //Map<Empresa, <Preco, ordens_preco_empresa>>
  private TreeMap<String,TreeMap<Float,OrderList>> compras;
  private TreeMap<String,TreeMap<Float,OrderList>> vendas;

  public Orders(){
    this.compras = new TreeMap<>();
    this.vendas = new TreeMap<>();
  }

  public List<Match> add(Order o){
    List<Match> matches = null;
    switch (o.getTipo()){
      case VENDA:
        matches = this.findMatchesToSellOrder(o);
        if (o.getQuant() > 0)
          this.addVenda(o);
        break;
      case COMPRA:
        matches = this.findMatchesToBuyOrder(o);
        if(o.getQuant() > 0)
          this.addCompra(o);
        break;
    }
    return matches;
  }

  private void addCompra(Order o) {
    TreeMap<Float,OrderList> compras = this.compras.get(o.getCompany());
    if(compras == null){
      TreeMap<Float,OrderList> novo = new TreeMap<>();
      novo.put(o.getPrice(),new OrderList(o));
      this.compras.put(o.getCompany(),novo);
    }
    else {
      OrderList list = compras.get(o.getPrice());
      if(list == null)
        compras.put(o.getPrice(),new OrderList(o));
      else
        list.add(o);
    }
  }

  private void addVenda(Order o) {
    TreeMap<Float,OrderList> vendas = this.vendas.get(o.getCompany());
    if(vendas == null){
      TreeMap<Float,OrderList> novo = new TreeMap<>();
      novo.put(o.getPrice(),new OrderList(o));
      this.vendas.put(o.getCompany(),novo);
    }
    else {
      OrderList list = vendas.get(o.getPrice());
      if(list == null)
        vendas.put(o.getPrice(),new OrderList(o));
      else
        list.add(o);
    }
  }

  private List<Match> findMatchesToBuyOrder(Order o) {
    ArrayList<Match> matches = new ArrayList<>();

    TreeMap<Float,OrderList> sellOrders = this.vendas.get(o.getCompany());
    if(sellOrders != null){
      OrderList maxList = sellOrders.firstEntry().getValue();
      while(maxList != null && !o.isEmpty() && maxList.getPrice() <= o.getPrice()){
        matches.addAll(maxList.match(o));
        if(maxList.isEmpty())
          sellOrders.remove(maxList.getPrice());
        if(sellOrders.isEmpty()){
          this.compras.remove(o.getCompany());
          maxList = null;
        }
        else
          maxList = sellOrders.firstEntry().getValue();
      }
    }

    return matches;
  }

  private List<Match> findMatchesToSellOrder(Order o) {
    ArrayList<Match> matches = new ArrayList<>();

    TreeMap<Float,OrderList> buyOrders = this.compras.get(o.getCompany());
    if(buyOrders != null){
      OrderList maxList = buyOrders.lastEntry().getValue();
      while(maxList != null && !o.isEmpty() && maxList.getPrice() >= o.getPrice()){
        matches.addAll(maxList.match(o));
        if(maxList.isEmpty())
          buyOrders.remove(maxList.getPrice());
        if(buyOrders.isEmpty()){
          this.compras.remove(o.getCompany());
          maxList = null;
        }
        else
          maxList = buyOrders.lastEntry().getValue();
      }
    }

    return matches;
  }


}
