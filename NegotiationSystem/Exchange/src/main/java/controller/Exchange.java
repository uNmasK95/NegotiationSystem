package controller;

import co.paralleluniverse.actors.ActorRef;
import org.zeromq.ZMQ;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

/**
 * Class principal do modulo Exchange
 */
public class Exchange {

    public static void main(String[] args) {

        try {
            //int port =  Integer.parseInt(args[0]);
            int port = 12350;
            System.out.println("Exchange listening on port " + port);
            try {
                Acceptor acceptor = new Acceptor(port);
                acceptor.spawn();
                acceptor.join();

            } catch (SQLException e) {
                System.out.println("Connection to database close");
                e.printStackTrace();
            }

        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Exchange fail");
            e.printStackTrace();
        }
    }
}
