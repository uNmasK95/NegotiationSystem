package controller;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.io.FiberSocketChannel;
import com.google.protobuf.CodedInputStream;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ReaderSocket extends BasicActor<Message,Void> {

    private final FiberSocketChannel socketChannel;
    private final ActorRef user;
    private final ByteBuffer input;
    private final CodedInputStream cin;

    public ReaderSocket(FiberSocketChannel socketChannel, ActorRef user) {
        this.socketChannel = socketChannel;
        this.user = user;
        this.input = ByteBuffer.allocate(1024);
        this.cin = CodedInputStream.newInstance( this.input );
    }

    @Override
    protected Void doRun() throws InterruptedException, SuspendExecution {

        while (socketChannel.isOpen()) {
            try {
                //ler do socket para o buffer; ficar a espera enquanto não tiver nada
                if (socketChannel.read(this.input) > 0) {
                    //colocar o buffer de for a que seja possivel ler dele
                    this.input.flip();
                    // quantos bytes o parse precisa de ler
                    int len = this.cin.readRawVarint32();
                    Protocol.Request request = Protocol.Request.parseFrom(this.cin.readRawBytes(len));

                    if (request.hasLogin()) {
                        this.user.send(new Message(Message.Type.LOGIN_REQ, self(), request.getLogin()));
                    } else {
                        this.user.send(new Message(Message.Type.ORDER_REQ, self(), request.getOrder()));
                    }
                    this.input.clear();
                }
            } catch (IOException e) {
                System.out.println("IOException");
            }
        }
        this.user.send(new Message(Message.Type.KO, self(), "Socket is close!"));
        System.out.println("Socket is close!");
        return null;
    }
}
