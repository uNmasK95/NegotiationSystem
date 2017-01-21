package controller;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.io.FiberSocketChannel;
import co.paralleluniverse.strands.channels.Channel;
import co.paralleluniverse.strands.channels.Channels;
import org.zeromq.ZMQ;
import presentation.Login;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Client{



    public static void main(String[] args) {

        ActorRef main = new Main().spawn();

    }
}
