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
import presentation.Login;
import presentation.Menu;

import javax.swing.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class Main extends BasicActor<Message,Void> {

    public final String hostPub = "localhost";
    public final int portPub = 12370;

    public final String hostExchange = "localhost";
    public final int portExchange = 12350;

    private final ByteBuffer output;
    private final CodedOutputStream cout;
    private final ZMQ.Socket socketSub;

    private Login login;
    private Menu menu;

    public Main( ) {
        this.output = ByteBuffer.allocate(1024);
        this.cout = CodedOutputStream.newInstance( this.output );

        ZMQ.Context context = ZMQ.context(1);
        this.socketSub = context.socket(ZMQ.SUB);
        this.socketSub.connect("tcp://" + hostPub + ":" + portPub);
    }

    @Override
    protected Void doRun() throws InterruptedException, SuspendExecution {

        this.login = new Login( self() );

        try {
            FiberSocketChannel socketChannel = FiberSocketChannel.open(new InetSocketAddress(hostExchange,portExchange));
            new Listener( self(), socketChannel ).spawn();
            new Subscriber( self(), this.socketSub ).spawn();

            while(receive( msg -> {
                switch (msg.type){
                    case LOGIN_REQ:
                        login_req( socketChannel, msg );
                        break;
                    case LOGIN_REP:
                        if( this.login != null){
                            Protocol.Reply result = (Protocol.Reply) msg.obj;
                            this.login.login_reply( result.getResult() );

                            if( result.getResult() ){
                                this.menu = new Menu( result.getDescrition(), self());
                                this.login = null;
                            }
                        }
                        break;

                    case ORDER_REQ:
                        send_request( socketChannel, msg );
                        break;
                    case ORDER_REP:
                        Protocol.Reply reply = (Protocol.Reply) msg.obj;
                        System.out.println("ORDER REPLY: " + reply.getResult() + ":" + reply.getDescrition());
                        this.menu.order_result(reply.getDescrition());

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
                    ";" + request.getOrder().getQuant() + ";" + request.getOrder().getPrice() + ";");
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
            System.out.println("Login: " + login.getLogin().getUsername() + ";" + login.getLogin().getPassword() + ";");
        } catch (IOException e) {
            System.out.println("Error send login request");
            e.printStackTrace();
        }
    }
}
