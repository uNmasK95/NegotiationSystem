import controller.entity.Match;
import controller.entity.Order;
import controller.entity.Orders;

import java.util.List;

public class OrderMatchingTest {
  public static void main(String[] args) {
    Orders orders = new Orders();

    orders.add(new Order("microsoft", 30, 35, "user_micro_30", null, Order.Tipo.COMPRA));
    orders.add(new Order("microsoft", 20, 20, "user_micro_20", null, Order.Tipo.COMPRA));
    orders.add(new Order("microsoft", 40, 40, "user_micro_40", null, Order.Tipo.COMPRA));
    orders.add(new Order("microsoft", 10, 40, "user_micro_40", null, Order.Tipo.COMPRA));
    //orders.add(new Order("microsoft",60, 35, "user_micro_35",null, Order.Tipo.VENDA));

    List<Match> matches = orders.add(new Order("microsoft", 60, 35, "user_sell_micro_30", null, Order.Tipo.VENDA));
    for(Match m: matches){
      System.out.println(m.getQuantidade() + " - " + m.getPreco() + "€");
    }
    orders.add(new Order("microsoft", 20, 20, "user_sell_micro_20", null, Order.Tipo.VENDA));
    orders.add(new Order("microsoft", 40, 40, "user_sell_micro_40", null, Order.Tipo.VENDA));
    orders.add(new Order("microsoft", 10, 20, "user_sell_micro_20", null, Order.Tipo.VENDA));
    //orders.add(new Order("microsoft",50, 35, "user_buy_micro_35",null, Order.Tipo.COMPRA));

  }
}