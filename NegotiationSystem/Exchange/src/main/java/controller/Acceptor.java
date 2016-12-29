package controller;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.io.FiberServerSocketChannel;
import co.paralleluniverse.fibers.io.FiberSocketChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;


public class Acceptor extends BasicActor {
    private final int port;
    private final ActorRef authenticator;
    private final ActorRef orderManager;

    public Acceptor(int port) throws SQLException {
        this.port = port;
        this.authenticator = new Authenticator().spawn();
        this.orderManager = new OrderManager().spawn();
    }

    protected Void doRun() throws InterruptedException, SuspendExecution {
        try {
            FiberServerSocketChannel ss = FiberServerSocketChannel.open();
            ss.bind(new InetSocketAddress(port));
            while (true) {
                FiberSocketChannel socket = ss.accept();
                new User(socket, this.authenticator, this.orderManager).spawn();
            }
        } catch (IOException e) { }
        return null;
    }
}
