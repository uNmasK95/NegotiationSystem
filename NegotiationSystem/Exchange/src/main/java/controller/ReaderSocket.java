package controller;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.io.FiberSocketChannel;

import java.io.IOException;

public class ReaderSocket extends BasicActor<Message,Void> {

    private final FiberSocketChannel socketChannel;
    private final ActorRef user;


    public ReaderSocket(FiberSocketChannel socketChannel, ActorRef user) {
        this.socketChannel = socketChannel;
        this.user = user;
    }

    @Override
    protected Void doRun() throws InterruptedException, SuspendExecution {
        try {
            while (true) {
                new Protocol.LoginReply.Builder().
                socketChannel.read();

                Protocol.Reply rep = Protocol.Reply.parseDelimitedFrom();

                socketChannel


                if (socket.read(in) <= 0) eof = true;
                in.flip();
                while(in.hasRemaining()) {
                    b = in.get();
                    out.put(b);
                    if (b == '\n') break;
                }
                if (eof || b == '\n') { // send line
                    out.flip();
                    if (out.remaining() > 0) {
                        byte[] ba = new byte[out.remaining()];
                        out.get(ba);
                        out.clear();
                        dest.send(new Server.Msg(Server.Type.DATA, ba));
                    }
                }
                if (eof && !in.hasRemaining()) break;
                in.compact();
            }
            dest.send(new Server.Msg(Server.Type.EOF, null));
            return null;
        } catch (IOException e) {
            dest.send(new Server.Msg(Server.Type.IOE, null));
            return null;
        }



        return null;
    }
}
