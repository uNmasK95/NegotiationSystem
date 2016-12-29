package controller;

import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.io.FiberSocketChannel;

import java.io.IOException;
import java.net.InetSocketAddress;

public class TesteConnection extends BasicActor {
    @Override
    protected Object doRun() throws InterruptedException, SuspendExecution {
        try {
            FiberSocketChannel ch = FiberSocketChannel.open(new InetSocketAddress(12350));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        new TesteConnection().spawn();
    }
}



