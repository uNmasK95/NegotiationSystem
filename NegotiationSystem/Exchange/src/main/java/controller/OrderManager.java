package controller;

import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import controller.entity.Match;
import controller.entity.Order;
import controller.entity.Orders;

import javax.jms.JMSException;
import java.util.*;

public class OrderManager extends BasicActor<Message,Void> {

    private final Orders orders;

    public OrderManager() {

        this.orders = new Orders();
    }

    @Override
    protected Void doRun() throws InterruptedException, SuspendExecution {

        while ( receive( msg -> {

            switch ( msg.type ){
                case ORDER_REQ:
                    receiveOrder( (Order) msg.obj);
                    break;
                default:
                    break;
            }
            return true;
        }));

        return null;
    }

    private void receiveOrder( Order order ){

        List<Match> result = this.orders.add( order );
        if(!result.isEmpty()){
            System.out.println("Foi encontrado match");
            for ( Match m : result ) {
                try {
                    new Transaction( m ).spawn();
                } catch (JMSException e) {
                    e.printStackTrace();
                    //TODO ver o que vou fazer aqui
                }
            }
        }else{
            System.out.println("Não foi encontrado match");
        }

    }
}
