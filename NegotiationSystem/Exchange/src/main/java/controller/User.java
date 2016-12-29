package controller;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.io.FiberSocketChannel;
import com.google.protobuf.CodedOutputStream;

import java.nio.ByteBuffer;

public class User extends BasicActor<Message, Void> {

    private final FiberSocketChannel socket;
    private final ActorRef authenticator;
    private final ActorRef orderManager;
    private final ByteBuffer output;
    private final CodedOutputStream cout;

    public User(FiberSocketChannel socket, ActorRef authenticator, ActorRef orderManager) {
        this.socket = socket;
        this.authenticator = authenticator;
        this.orderManager = orderManager;
        this.output = ByteBuffer.allocate(1024);
        this.cout = CodedOutputStream.newInstance( this.output );
    }

    protected Void doRun() throws InterruptedException, SuspendExecution {

        new ReaderSocket(socket,self()).spawn();

        System.out.println("Cheguei");

        while ( receive( msg -> {
            switch (msg.type){
                case LOGIN_REQ:
                    login_request();
                    break;
                case LOGIN_REP:
                    login_reply();
                    break;
                case ORDER_REQ:
                    order_request();
                    break;
                case ORDER_REP:
                    order_reply();
                    break;
                case KO:
                    // seja qual for a messagem
                    return false;
                default:
                    break;
            }
            return true;
        }));
        return null;
    }

    private void order_reply() {
    }

    private void order_request() {
    }

    private void login_request() {
    }

    private void login_reply() {
    }
}