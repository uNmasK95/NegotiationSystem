package controller;

import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;

public class OrderManager extends BasicActor<Message,Void> {

    @Override
    protected Void doRun() throws InterruptedException, SuspendExecution {
        return null;
    }
}
