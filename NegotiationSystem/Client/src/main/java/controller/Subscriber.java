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
    private final Channel channelSubscrib;

    public Subscriber(ZMQ.Socket socketSubscribe,  Channel channelSubscrib) {
        this.socketSubscribe = socketSubscribe;
        this.channelSubscrib = channelSubscrib;
    }

    @Override
    protected Void doRun() throws InterruptedException, SuspendExecution {

        while ( true ) {  //TODO ver melhor esta condição
            byte[] b = this.socketSubscribe.recv();
            System.out.println( new String(b) );
            channelSubscrib.send( new String(b) );
        }

    }
}
