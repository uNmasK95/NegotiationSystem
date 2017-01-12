package controller;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.common.util.SystemProperties;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.io.FiberSocketChannel;
import co.paralleluniverse.strands.channels.Channel;
import com.google.protobuf.CodedOutputStream;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class Main extends BasicActor<Message,Void> {

    public final String hostXPub = "localhost";
    public final int portXPub = 12370;

    private final ByteBuffer output;
    private final CodedOutputStream cout;
    private final ZMQ.Socket socketSub;
    private final Channel channelLogin;
    private final ActorRef subscriber;

    public Main( Channel channelLogin, Channel channelSubscrib ) {
        this.output = ByteBuffer.allocate(1024);
        this.cout = CodedOutputStream.newInstance( this.output );
        this.channelLogin = channelLogin;

        ZMQ.Context context = ZMQ.context(1);
        this.socketSub = context.socket(ZMQ.SUB);
        this.socketSub.connect("tcp://" + hostXPub + ":" + portXPub);

        this.subscriber = new Subscriber( this.socketSub, channelSubscrib ).spawn();

    }



    @Override
    protected Void doRun() throws InterruptedException, SuspendExecution {
        System.out.println("new Main");
        try {
            FiberSocketChannel socketChannel = FiberSocketChannel.open(new InetSocketAddress(12350));
            new Listener( self(), socketChannel ).spawn();

            while(receive( msg -> {
                switch (msg.type){
                    case LOGIN_REQ:
                        login_req( socketChannel, msg );
                        System.out.println("Login request send");
                        break;
                    case LOGIN_REP:
                        // ver como vamos fazer para comonicar com a interface
                        System.out.println("Recebi o login reply");
                        channelLogin.send(msg.obj);
                        System.out.println("Enviei para o channel");
                        break;
                    case ORDER_REQ:
                        System.out.println("Recebi o order request");
                        send_request( socketChannel, msg );
                        break;
                    case SUB:
                        this.socketSub.subscribe( ((String) msg.obj).getBytes() );
                    case UNSUB:
                        this.socketSub.unsubscribe( ((String) msg.obj).getBytes() );
                    default:
                        break;
                }
                return true;
            }));

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void send_request(FiberSocketChannel socketChannel, Message msg) throws SuspendExecution{
        Protocol.Request request = (Protocol.Request) msg.obj;
        try {
            cout.writeRawVarint32(request.toByteArray().length);
            cout.writeRawBytes(request.toByteArray());
            cout.flush();
            output.flip();
            socketChannel.write(output);
            this.output.compact();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void login_req(FiberSocketChannel socketChannel, Message msg) throws SuspendExecution{
        Protocol.Request login = (Protocol.Request) msg.obj;
        try {
            cout.writeRawVarint32(login.toByteArray().length);
            cout.writeRawBytes(login.toByteArray());
            cout.flush();
            output.flip();
            socketChannel.write(output);
            this.output.compact();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
