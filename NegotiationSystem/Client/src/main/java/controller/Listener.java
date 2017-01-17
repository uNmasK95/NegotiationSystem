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

                //ler do socket para o buffer; ficar a espera enquanto não tiver nada
                if (socketChannel.read(this.input) > 0){
                    //colocar o buffer de for a que seja possivel ler dele
                    this.input.flip();

                    // quantos bytes o parse precisa de ler
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
                    //this.input.compact();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
