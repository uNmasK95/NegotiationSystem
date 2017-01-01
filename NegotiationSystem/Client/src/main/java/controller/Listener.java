package controller;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.io.FiberSocketChannel;

public class Listener extends BasicActor<Message,Void> {

    private final ActorRef main;
    private final FiberSocketChannel socketChannel;

    public Listener(ActorRef main, FiberSocketChannel socketChannel) {
        this.main = main;
        this.socketChannel = socketChannel;
    }

    @Override
    protected Void doRun() throws InterruptedException, SuspendExecution {


        return null;
    }
}
