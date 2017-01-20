package controller;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import org.zeromq.ZMQ;
import co.paralleluniverse.strands.channels.Channel;
import co.paralleluniverse.strands.channels.Channels;

public class Subscriber extends BasicActor<Message,Void> {

    private final ZMQ.Socket socketSubscribe;
    private final ActorRef main;

    public Subscriber(ActorRef main, ZMQ.Socket socketSubscribe ) {
        this.main = main;
        this.socketSubscribe = socketSubscribe;
    }

    @Override
    protected Void doRun() throws InterruptedException, SuspendExecution {
        //TODO mudar isto
        socketSubscribe.subscribe("".getBytes());
        while ( true ) {
            //TODO ver melhor esta condição
            byte[] b = this.socketSubscribe.recv();
            System.out.println( "SUB: " + new String(b) );
            main.send( new Message(
                    Message.Type.SUB_MES,
                    null,
                    new String(b)
            ) );
        }
    }
}
