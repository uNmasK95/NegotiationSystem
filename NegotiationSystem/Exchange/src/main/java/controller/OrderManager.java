package controller;

import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import controller.entity.OrderBuy;
import controller.entity.OrderSell;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OrderManager extends BasicActor<Message,Void> {


    private Map<String,OrderBuy> orderBuyMap;
    private Map<String,OrderSell> orderSellMap;

    public OrderManager() {
        this.orderBuyMap = new HashMap<>();
        this.orderSellMap = new HashMap<>();
    }

    @Override
    protected Void doRun() throws InterruptedException, SuspendExecution {

        while ( receive( msg -> {
            switch (msg.type){
                case BUY:
                    receiveOrderBuy( (OrderBuy) msg.obj);
                    break;
                case SELL:
                    receiveOrderSell( (OrderSell) msg.obj);
                    break;
            }
            return true;
        }));

        return null;
    }

    private void receiveOrderBuy( OrderBuy orderBuy){
        //TODO verificar se existe algo compativel caso contrario adionar á lista


        //se compativel
        OrderSell orderSell = orderSellMap.get("company");
        new Transaction(orderBuy , orderSell );

    }

    private void receiveOrderSell( OrderSell orderSell ){

    }
}
