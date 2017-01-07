package controller;

import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import controller.entity.Match;
import org.zeromq.ZMQ;

import java.util.List;

public class Transaction extends BasicActor<Message,Void> {

    private final String host = "localhost";
    private final int port = 5559;
    private final List<Match> matchList;

    public Transaction( List<Match> matchList ) {
        this.matchList = matchList;
    }

    @Override
    protected Void doRun() throws InterruptedException, SuspendExecution {
/*
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket requester = context.socket(ZMQ.REQ);
        requester.connect("tcp://" + host + ":" + port);

        requester.send("ola");
        requester.recv();

        //TODO fazer pub da informação se a transferencia for bem sucedida

//        A comunicacao sera algo deste genero, mas com request/reply
//
//        javax.jms.Connection c = ??
//        c.start();
//
//        Session s = c.createSession(false, 0);
//        Queue q = s.createQueue("vendas");
//        MessageProducer p = s.createProducer(q);
//
//        TextMessage m = s.createTextMessage("venda");
//        m.setStringProperty("comprador",comprador);
//        m.setStringProperty("vendedor",vendedor);
//        m.setStringProperty("empresa",empresa);
//        m.setIntProperty("quantidade",quantidade);
//        m.setFloatProperty("preco",preco);
//        p.send(m);


        ZMQ.Socket socketPub = context.socket(ZMQ.PUB);
        socketPub.connect("tcp://localhost:" + 1256);

        socketPub.send("Company:Apple" + "()");


        //  We never get here but clean up anyhow
        requester.close();
        context.term();

*/
        return null;
    }
}
