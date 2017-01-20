package controller;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.common.util.SystemProperties;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.io.FiberSocketChannel;
import co.paralleluniverse.strands.channels.Channel;
import com.esotericsoftware.minlog.Log;
import com.google.protobuf.CodedOutputStream;
import org.zeromq.ZMQ;
import presentation.Menu;

import javax.swing.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class Main extends BasicActor<Message,Void> {

    public final String hostPub = "localhost";
    public final int portPub = 12370;

    private final ByteBuffer output;
    private final CodedOutputStream cout;
    private final ZMQ.Socket socketSub;
    private final Channel channelLogin;

    private Menu menu;

    public Main( Channel channelLogin ) {
        this.output = ByteBuffer.allocate(1024);
        this.cout = CodedOutputStream.newInstance( this.output );
        this.channelLogin = channelLogin;

        ZMQ.Context context = ZMQ.context(1);
        this.socketSub = context.socket(ZMQ.SUB);
        this.socketSub.connect("tcp://" + hostPub + ":" + portPub);
    }

    @Override
    protected Void doRun() throws InterruptedException, SuspendExecution {
        try {
            FiberSocketChannel socketChannel = FiberSocketChannel.open(new InetSocketAddress(12350));
            new Listener( self(), socketChannel ).spawn();
            new Subscriber( self(), this.socketSub ).spawn();

            while(receive( msg -> {
                switch (msg.type){
                    case LOGIN_REQ:
                        login_req( socketChannel, msg );
                        break;
                    case LOGIN_REP:
                        System.out.println("Recebi o login reply");
                        //TODO altera isto tirar o channel
                        channelLogin.send(msg.obj);
                        Protocol.Reply result = (Protocol.Reply) msg.obj;

                        if( result.getResult() ){
                            this.menu = new Menu( result.getDescrition(), self());
                        }

                        System.out.println("Enviei para o channel");

                        break;

                    case ORDER_REQ:
                        send_request( socketChannel, msg );
                        break;
                    case ORDER_REP:
                        Protocol.Reply reply = (Protocol.Reply) msg.obj;
                        System.out.println("ORDER REPLY: " + reply.getResult() + ":" + reply.getDescrition());
                        this.menu.order_result(reply.getResult() + ":" + reply.getDescrition());

                        break;

                    case SUB_KEY:
                        this.socketSub.subscribe( ((String) msg.obj).getBytes() );
                        break;

                    case UNSUB_KEY:
                        this.socketSub.unsubscribe( ((String) msg.obj).getBytes() );
                        break;

                    case SUB_MES:
                        this.menu.setSubcribeResult( (String) msg.obj );
                        break;

                    case KO:
                        return false;
                    default:
                        break;
                }
                return true;
            }));

            this.socketSub.close();
            socketChannel.close();

        } catch (IOException e) {
            System.out.println("Actor Main fail");
            //e.printStackTrace();
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
            System.out.println("Order: " + request.getOrder().getCompany() + ";" + request.getOrder().getType() +
                    ";" + request.getOrder().getQuant() + ";" + request.getOrder().getPrice() + ";\n");
        } catch (IOException e) {
            System.out.println("Error send order request");
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
            System.out.println("Login: " + login.getLogin().getUsername() + ";" + login.getLogin().getPassword() + ";\n");
        } catch (IOException e) {
            System.out.println("Error send login request");
            e.printStackTrace();
        }
    }
}
