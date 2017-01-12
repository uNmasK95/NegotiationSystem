package controller;

import co.paralleluniverse.actors.ActorRef;
import org.zeromq.ZMQ;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

/**
 * Class principal do modulo Exchange
 */
public class Exchange {

    public static int portPub = 12370;
    public static int portSub = 12371;

    public static void main(String[] args) {

        try {
            int port =  Integer.parseInt(args[0]);
            System.out.println("Exchange listening on port " + port);
            try {
                Acceptor acceptor = new Acceptor(port);
                acceptor.spawn();

                ZMQ.Context context = ZMQ.context(1);
                ZMQ.Socket socketPubs = context.socket(ZMQ.XPUB);
                socketPubs.bind("tcp://*:" + portSub);

                ZMQ.Socket socketSubs = context.socket(ZMQ.XSUB);
                socketSubs.bind("tcp://*:" + portPub);

                ZMQ.proxy(socketPubs, socketSubs, null);

                acceptor.join();

            } catch (SQLException e) {
                System.out.println("Connection to database close");
                e.printStackTrace();
            }

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
