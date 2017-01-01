package controller;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.fibers.SuspendExecution;
import presentation.Login;

import java.io.IOException;


public class Client{

    public static void main(String[] args) {
        try {
            ActorRef main = new Main().spawn();
            Login l = new Login(main);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SuspendExecution suspendExecution) {
            suspendExecution.printStackTrace();
        }

    }
}
