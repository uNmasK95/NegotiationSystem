package controller;

import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import controller.entity.OrderBuy;
import controller.entity.OrderSell;
import org.zeromq.ZMQ;

public class Transaction extends BasicActor<Message,Void> {

    private final String host = "localhost";
    private final int port = 5559;
    private final OrderBuy orderBuy;
    private final OrderSell orderSell;

    public Transaction(OrderBuy orderBuy, OrderSell orderSell) {
        this.orderBuy = orderBuy;
        this.orderSell = orderSell;
    }

    @Override
    protected Void doRun() throws InterruptedException, SuspendExecution {

        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket requester = context.socket(ZMQ.REQ);
        requester.connect("tcp://" + host + ":" + port);

        requester.send("ola");
        requester.recv();

        //TODO fazer pub da informação se a transferencia for bem sucedida


        ZMQ.Socket socketPub = context.socket(ZMQ.PUB);
        socketPub.connect("tcp://localhost:" + 1256);

        socketPub.send("Company:Apple" + "()");


        //  We never get here but clean up anyhow
        requester.close();
        context.term();


        return null;
    }
}
