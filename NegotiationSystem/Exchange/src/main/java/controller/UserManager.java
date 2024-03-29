package controller;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.io.FiberSocketChannel;
import com.google.protobuf.CodedOutputStream;
import controller.entity.Order;
import controller.entity.User;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.IOException;
import java.nio.ByteBuffer;

public class UserManager extends BasicActor<Message, Void> {

    private final FiberSocketChannel socketChannel;
    private final ActorRef authenticator;
    private final ActorRef orderManager;

    private final ByteBuffer output;
    private final CodedOutputStream cout;

    private String user;

    public UserManager(FiberSocketChannel socketChannel, ActorRef authenticator, ActorRef orderManager) {
        this.socketChannel = socketChannel;
        this.authenticator = authenticator;
        this.orderManager = orderManager;

        this.output = ByteBuffer.allocate( 1024 );
        this.cout = CodedOutputStream.newInstance( this.output );
    }

    protected Void doRun() throws InterruptedException, SuspendExecution {
        new ReaderSocket( this.socketChannel, self() ).spawn();

        while ( receive( msg -> {
            switch (msg.type){
                case LOGIN_REQ:
                    login_request( msg );
                    break;
                case LOGIN_REP:
                    login_reply( msg );
                    break;
                case ORDER_REQ:
                    order_request( msg );
                    break;
                case ORDER_REP:
                    order_reply( msg );
                    break;
                case KO:
                    System.out.println("USERMANAGER " + this.user + ": KO");
                    return false;
                default:
                    break;
            }
            return true;
        }));
        return null;
    }

    private void login_request( Message msg ) throws SuspendExecution {
        Protocol.Request.Login login = (Protocol.Request.Login) msg.obj;
        this.user = login.getUsername();
        System.out.println("LOGIN: " + login.getUsername() + ";" + login.getPassword());
        this.authenticator.send( new Message(
                Message.Type.LOGIN_REQ,
                self(),
                new User(
                        login.getUsername(),
                        login.getPassword()
                )
        ));
    }

    /**
     * Envia um resposta ao utilizador com o resultado ao pedido de login do utilizador
     * @param msg
     */
    private void login_reply( Message msg ) throws SuspendExecution{
        Protocol.Reply reply = Protocol.Reply.newBuilder()
                .setType(Protocol.Reply.Type.Login)
                .setResult( (boolean) msg.obj)
                .setDescrition(this.user)
                .build();

        try {
            this.cout.writeRawVarint32( reply.toByteArray().length );
            this.cout.writeRawBytes( reply.toByteArray() );
            this.cout.flush();
            this.output.flip();
            this.socketChannel.write(this.output);
            this.output.compact();

        } catch (IOException e) {
            System.out.println("Error send login reply");
            e.printStackTrace();
        }
    }

    /**
     * Metodo responsavel por tratar uma ordem enviada pelo utilizador
     * @param msg
     * @throws SuspendExecution
     */
    private void order_request( Message msg ) throws SuspendExecution {
        Protocol.Request.Order request = (Protocol.Request.Order) msg.obj;

        System.out.println( "ORDER: " + request.getType() + ";" +
                request.getCompany() + ";" +
                request.getQuant() + ";" +
                request.getPrice() + ";");

        if(request.getType() == Protocol.Request.Order.Type.Buy){
            order_buy( request );
        }else{
            order_sell( request );
        }
    }

    /**
     * Metodo responsavel reencaminhar uma ordem de compra de um utilizador para o OrderManager
     * @param request
     * @throws SuspendExecution
     */
    private void order_buy(Protocol.Request.Order request) throws SuspendExecution {
        this.orderManager.send( new Message(
                Message.Type.ORDER_REQ,
                self(),
                new Order(
                        request.getCompany(),
                        request.getQuant(),
                        request.getPrice(),
                        this.user,
                        self(),
                        Order.Tipo.COMPRA
                )
        ));
    }

    /**
     * Metodo responsavel reencaminhar uma ordem de venda de um utilizador para o OrderManager
     * @param request
     * @throws SuspendExecution
     */
    private void order_sell(Protocol.Request.Order request) throws SuspendExecution {
        this.orderManager.send( new Message(
                Message.Type.ORDER_REQ,
                self(),
                new Order(
                        request.getCompany(),
                        request.getQuant(),
                        request.getPrice(),
                        this.user,
                        self(),
                        Order.Tipo.VENDA
                )
        ));
    }

    /**
     * Metodo responsavel por uma menssagem de ordem concluida
     * @param msg
     */
    private void order_reply( Message msg ) throws SuspendExecution{
        Protocol.Reply reply = Protocol.Reply.newBuilder()
                .setType(Protocol.Reply.Type.Order)
                .setResult(true)
                .setDescrition( (String) msg.obj)
                .build();

        try {
            this.cout.writeRawVarint32( reply.toByteArray().length );
            this.cout.writeRawBytes( reply.toByteArray() );
            this.cout.flush();
            this.output.flip();
            this.socketChannel.write(this.output);
            this.output.compact();

        } catch (IOException e) {
            System.out.println("Error send order reply");
            e.printStackTrace();
        }
    }


}