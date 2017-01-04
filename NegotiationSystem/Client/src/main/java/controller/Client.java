package controller;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.channels.Channel;
import co.paralleluniverse.strands.channels.Channels;
import org.zeromq.ZMQ;
import presentation.Login;

import java.io.IOException;


public class Client{


    public static void main(String[] args) {
        final String portSubs = args[0];
        final String host = "localhost";

        final Channel<Protocol.Reply> channelLogin = Channels.newChannel(0);
        final Channel<String> channelSubscrib = Channels.newChannel(10);

        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.SUB);
        socket.connect("tcp://" + host + ":" + portSubs);


        ActorRef main = new Main( socket , channelLogin ).spawn();
        //Subscriber subscriber = new Subscriber( socket, channelSubscrib );

        Login login = new Login(main, channelLogin, channelSubscrib);

    }
}
