package controller;

import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import org.zeromq.ZMQ;

public class Notification extends Thread{


    private final int portXPub = 12370;
    private final int portXSub = 12371;


    @Override
    public void run() {
        System.out.println("Notification start");
        ZMQ.Context context = ZMQ.context(1);

        ZMQ.Socket socketPubs = context.socket(ZMQ.XPUB);
        socketPubs.bind("tcp://*:" + portXPub);
        System.out.println("Proxy1");

        ZMQ.Socket socketSubs = context.socket(ZMQ.XSUB);
        socketSubs.bind("tcp://*:" + portXSub);

        System.out.println("Proxy3");

        ZMQ.proxy(socketPubs, socketSubs, null);
        System.out.println("Proxy");

    }



}
