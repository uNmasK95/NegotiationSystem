package controller;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import org.zeromq.ZMQ;
import co.paralleluniverse.strands.channels.Channel;
import co.paralleluniverse.strands.channels.Channels;

public class Subscriber extends BasicActor<Message,Void> {

    private final ZMQ.Socket socketSubscribe;
    //private final String hostXPub = "localhost";
    //private final int portXPub = 12370;
    private final ActorRef main;

    public Subscriber(ActorRef main, ZMQ.Socket socketSubscribe ) {
        this.main = main;
        this.socketSubscribe = socketSubscribe;
    }

    @Override
    protected Void doRun() throws InterruptedException, SuspendExecution {
        //TODO mudar isto
        System.out.println("Susbscriver iniciou");
        socketSubscribe.subscribe("microsoft".getBytes());
        while ( true ) {  //TODO ver melhor esta condição
            System.out.println("Susbscriver while true");
            byte[] b = this.socketSubscribe.recv();
            System.out.println( new String(b) );
            main.send( new Message(
                    Message.Type.SUB_MES,
                    null,
                    new String(b)
            ) );
        }
    }
}
