package controller;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.io.FiberSocketChannel;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ReaderSocket extends BasicActor<Message,Void> {

    private final FiberSocketChannel socketChannel;
    private final ActorRef user;
    private final ByteBuffer input;
    private final CodedInputStream cin;
    private final boolean needLogin;



    public ReaderSocket(FiberSocketChannel socketChannel, ActorRef user) {
        this.socketChannel = socketChannel;
        this.user = user;
        this.input = ByteBuffer.allocate(1024);
        this.cin = CodedInputStream.newInstance( this.input );
        this.needLogin = true;
    }

    @Override
    protected Void doRun() throws InterruptedException, SuspendExecution {

        try {

            while (socketChannel.isOpen()) {

                //ler do socket para o buffer; ficar a espera enquanto não tiver nada
                while (socketChannel.read(this.input) <= 0);

                //colocar o buffer de for a que seja possivel ler dele
                this.input.flip();

                // quantos bytes o parse precisa de ler
                int len = this.cin.readRawVarint32();

                if (needLogin){
                    Protocol.LoginRequest loginRequest = Protocol.LoginRequest.parseFrom(cin.readRawBytes(len) );
                    this.user.send( new Message(Message.Type.LOGIN_REQ, self(), loginRequest));
                }else {
                    Protocol.Request request = Protocol.Request.parseFrom(cin.readRawBytes(len));
                    this.user.send( new Message( Message.Type.ORDER_REQ, self(), request) );
                }
                this.input.compact();
            }
        } catch (IOException e) {
            this.user.send( new Message(Message.Type.KO, self(),"IOException"));
        }
        this.user.send( new Message(Message.Type.KO, self(),"Socket is close!"));
        return null;
    }
}
