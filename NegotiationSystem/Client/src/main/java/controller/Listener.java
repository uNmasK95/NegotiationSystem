package controller;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.io.FiberSocketChannel;
import com.google.protobuf.CodedInputStream;


import java.io.IOException;
import java.nio.ByteBuffer;

public class Listener extends BasicActor<Message,Void> {

    private final ActorRef main;
    private final FiberSocketChannel socketChannel;
    private final ByteBuffer input;
    private final CodedInputStream cin;


    public Listener(ActorRef main, FiberSocketChannel socketChannel) {
        this.main = main;
        this.socketChannel = socketChannel;
        this.input = ByteBuffer.allocate(1024);
        this.cin = CodedInputStream.newInstance( this.input );
    }

    @Override
    protected Void doRun() throws InterruptedException, SuspendExecution {

        try {
            while (socketChannel.isOpen()) {

                if (socketChannel.read(this.input) > 0){

                    this.input.flip();
                    int len = this.cin.readRawVarint32();
                    Protocol.Reply reply = Protocol.Reply.parseFrom(cin.readRawBytes(len) );

                    if( reply.getType() == Protocol.Reply.Type.Login){
                        this.main.send( new Message(
                                Message.Type.LOGIN_REP,
                                self(),
                                reply
                        ));
                    }else{
                        this.main.send( new Message(
                                Message.Type.ORDER_REP,
                                self(),
                                reply
                        ));
                    }
                    this.input.clear();
                }

            }
        } catch (IOException e) {
            main.send( new Message(
                    Message.Type.KO,
                    null,
                    "Listener KO"));

            System.out.println("Listener KO");
            //e.printStackTrace();
        }

        return null;
    }
}
