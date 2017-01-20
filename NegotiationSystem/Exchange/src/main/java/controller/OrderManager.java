package controller;

import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import controller.entity.Match;
import controller.entity.Order;
import controller.entity.Orders;
import org.zeromq.ZMQ;

import javax.jms.JMSException;
import java.util.*;

public class OrderManager extends BasicActor<Message,Void> {


    private final int portPub = 12370;
    private final Orders orders;
    private final ZMQ.Socket socketPubs;

    public OrderManager() {
        this.orders = new Orders();

        ZMQ.Context context = ZMQ.context(1);
        this.socketPubs = context.socket(ZMQ.PUB);
        this.socketPubs.bind("tcp://*:" + portPub);
        System.out.println("Proxy1");
    }

    @Override
    protected Void doRun() throws InterruptedException, SuspendExecution {

        while ( receive( msg -> {

            switch ( msg.type ){
                case ORDER_REQ:
                    receiveOrder( (Order) msg.obj);
                    break;
                case PUB_MES:
                    send_pub( (String) msg.obj );
                    break;
                default:
                    break;
            }
            return true;
        }));

        return null;
    }

    private void send_pub(String obj) {
        this.socketPubs.send(obj);
    }

    private void receiveOrder( Order order ){

        List<Match> result = this.orders.add( order );
        if(!result.isEmpty()){
            System.out.println("Foi encontrado match");
            for ( Match m : result ) {
                try {
                    new Transaction( self(), m ).spawn();
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
