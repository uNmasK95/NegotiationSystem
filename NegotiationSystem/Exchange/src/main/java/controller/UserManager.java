package controller;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.io.FiberSocketChannel;
import com.google.protobuf.CodedOutputStream;
import controller.entity.OrderBuy;
import controller.entity.OrderSell;
import controller.entity.User;

import java.io.IOException;
import java.nio.ByteBuffer;

public class UserManager extends BasicActor<Message, Void> {

    private final FiberSocketChannel socketChannel;
    private final ActorRef authenticator;
    private final ActorRef orderManager;

    private final ByteBuffer output;
    private final CodedOutputStream cout;

    public UserManager(FiberSocketChannel socketChannel, ActorRef authenticator, ActorRef orderManager) {
        this.socketChannel = socketChannel;
        this.authenticator = authenticator;
        this.orderManager = orderManager;

        this.output = ByteBuffer.allocate( 1024 );
        this.cout = CodedOutputStream.newInstance( this.output );
    }

    protected Void doRun() throws InterruptedException, SuspendExecution {
        new ReaderSocket( this.socketChannel, self() ).spawn();
        System.out.println( "UserManager " );

        while ( receive( msg -> {
            switch (msg.type){
                case LOGIN_REQ:
                    System.out.println("Login Request");
                    login_request( msg );
                    break;
                case LOGIN_REP:
                    System.out.println("Login Reply");
                    login_reply( msg );
                    break;
                case ORDER_REQ:
                    System.out.println("Order Request");
                    order_request( msg );
                    break;
                case ORDER_REP:
                    System.out.println("Order REply");
                    order_reply( msg );
                    break;
                case KO:
                    System.out.println("KO");
                    // seja qual for a messagem
                    return false;
                default:
                    break;
            }
            return true;
        }));
        return null;
    }



    private void login_request( Message msg ) throws SuspendExecution {
        Protocol.LoginRequest login = (Protocol.LoginRequest) msg.obj;
        System.out.println("user: " +login.getUsername() );
        System.out.println("pass: " +login.getPassword() );

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
    private void login_reply( Message msg ) {
        Protocol.Reply reply = Protocol.Reply.newBuilder()
                .setType(Protocol.Reply.Type.Login)
                .setResult( (boolean) msg.obj)
                .setDescrition("Reply Login")
                .build();

        try {
            this.cout.writeRawVarint32( reply.toByteArray().length );
            this.cout.writeRawBytes( reply.toByteArray() );
            this.cout.flush();
            this.output.flip();
            this.socketChannel.write(this.output);

            this.output.compact();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo responsavel por tratar uma ordem enviada pelo utilizador
     * @param msg
     * @throws SuspendExecution
     */
    private void order_request( Message msg ) throws SuspendExecution {
        Protocol.Request request = (Protocol.Request) msg.obj;
        if(request.getType() == Protocol.Request.Type.Buy){
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
    private void order_buy(Protocol.Request request) throws SuspendExecution {
        this.orderManager.send( new Message(
                Message.Type.BUY,
                self(),
                new OrderBuy(
                        request.getCompany(),
                        request.getQuant(),
                        request.getPrice(),
                        new User("",""),
                        self()
                        // FIXME ver o que colocar aqui no user

                )
        ));
    }

    /**
     * Metodo responsavel reencaminhar uma ordem de venda de um utilizador para o OrderManager
     * @param request
     * @throws SuspendExecution
     */
    private void order_sell(Protocol.Request request) throws SuspendExecution {
        this.orderManager.send( new Message(
                Message.Type.SELL,
                self(),
                new OrderSell(
                        request.getCompany(),
                        request.getQuant(),
                        request.getPrice(),
                        new User("",""),
                        self())
                // FIXME ver o que colocar aqui no user
        ));
    }

    /**
     * Metodo responsavel por uma menssagem de ordem concluida
     * @param msg
     */
    private void order_reply( Message msg ) {
        Protocol.Reply reply = Protocol.Reply.newBuilder()
                .setResult(true)
                .setDescrition("Coisas")
                .build();

        try {
            this.cout.writeRawVarint32( reply.toByteArray().length );
            this.cout.writeRawBytes( reply.toByteArray() );
            this.cout.flush();
            this.output.flip();
            this.socketChannel.write(this.output);
            this.output.compact();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}