package controller;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.io.FiberSocketChannel;

public class User extends BasicActor<Message, Void> {
    final FiberSocketChannel socket;
    final ActorRef authenticator;

    public User(FiberSocketChannel socket, ActorRef authenticator) {
        this.socket = socket;
        this.authenticator = authenticator;
    }

    protected Void doRun() throws InterruptedException, SuspendExecution {
      /*  new LineReader(self(), socket).spawn();
        room.send(new Msg(Type.ENTER, self()));
        while (receive(msg -> {
            try {
                switch (msg.type) {
                    case DATA:
                        room.send(new Msg(Type.LINE, msg.o));
                        return true;
                    case EOF:
                    case IOE:
                        room.send(new Msg(Type.LEAVE, self()));
                        socket.close();
                        return false;
                    case LINE:
                        socket.write(ByteBuffer.wrap((byte[])msg.o));
                        return true;
                }
            } catch (IOException e) {
                room.send(new Msg(Type.LEAVE, self()));
            }
            return false;  // stops the actor if some unexpected message is received
        }));*/
        return null;
    }
}