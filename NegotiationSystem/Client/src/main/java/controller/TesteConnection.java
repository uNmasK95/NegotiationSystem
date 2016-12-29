package controller;

import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.io.FiberSocketChannel;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class TesteConnection extends BasicActor {

    private final ByteBuffer input;
    private final ByteBuffer output;
    private final CodedInputStream cin;
    private final CodedOutputStream cout;

    public TesteConnection() {
        this.input = ByteBuffer.allocate(1024);
        this.output = ByteBuffer.allocate(1024);
        this.cin = CodedInputStream.newInstance( this.input );
        this.cout = CodedOutputStream.newInstance( this.output );
    }

    @Override
    protected Object doRun() throws InterruptedException, SuspendExecution {
        try {
            FiberSocketChannel ch = FiberSocketChannel.open(new InetSocketAddress(12350));

            Protocol.LoginRequest lr = Protocol.LoginRequest.newBuilder()
                    .setUsername("rui")
                    .setPassword("password")
                    .build();


            cout.writeRawVarint32(lr.toByteArray().length);
            cout.writeRawBytes(lr.toByteArray());
            cout.flush();

            System.out.println("Tamanho do buffer = " + this.output.position());
            output.flip();
            ch.write(output);

            System.out.println("fim");

            ch.read(input);
            Protocol.LoginReply lreply = Protocol.LoginReply.parseFrom(this.cin);
            System.out.println("result :" + lreply.getLogin());





        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        new TesteConnection().spawn();
    }
}



