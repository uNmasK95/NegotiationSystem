package controller;

import co.paralleluniverse.actors.ActorRef;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

public class Exchange {
    public static void main(String[] args) {

        try {
            int port =  Integer.parseInt(args[0]);
            System.out.println("Exchange listening on port " + port);
            try {
                Acceptor acceptor = new Acceptor(port);
                acceptor.spawn();
                acceptor.join();
            } catch (SQLException e) {
                System.out.println("Connection to database close");
            }

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
