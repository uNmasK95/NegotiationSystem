package controller;

import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.io.FiberSocketChannel;
import com.google.protobuf.CodedOutputStream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class Main extends BasicActor<Message,Void> {

    private final FiberSocketChannel socketChannel;
    private final ByteBuffer output;
    private final CodedOutputStream cout;

    public Main() throws IOException, SuspendExecution {
        this.socketChannel = FiberSocketChannel.open(new InetSocketAddress(12350));
        this.output = ByteBuffer.allocate(1024);
        this.cout = CodedOutputStream.newInstance( this.output );
    }

    @Override
    protected Void doRun() throws InterruptedException, SuspendExecution {


        while(receive( msg -> {
            switch (msg.type){
                case LOGIN_REQ:
                    login_req( msg );
                    break;
                case LOGIN_REP:
                    // ver como vamos fazer para comonicar com a interface
                    break;
                case ORDER_REQ:
                    send_request( msg );
                    break;
                default:
                    break;
            }
            return true;
        }));

        return null;


    }

    private void send_request(Message msg) {
        Protocol.Request request = (Protocol.Request) msg.obj;
        try {
            cout.writeRawVarint32(request.toByteArray().length);
            cout.writeRawBytes(request.toByteArray());
            cout.flush();
            output.flip();
            socketChannel.write(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void login_req(Message msg) {
        Protocol.LoginRequest login = (Protocol.LoginRequest) msg.obj;
        try {
            cout.writeRawVarint32(login.toByteArray().length);
            cout.writeRawBytes(login.toByteArray());
            cout.flush();
            output.flip();
            socketChannel.write(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
