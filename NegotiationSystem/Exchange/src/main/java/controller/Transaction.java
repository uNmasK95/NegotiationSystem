package controller;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import controller.entity.Match;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.zeromq.ZMQ;

import javax.jms.*;

public class Transaction extends BasicActor<Message,Void> {

    private final String hostXSub = "localhost";
    private final int portXSub = 12371;
    private final Match match;
    private final Connection connection;
    private final ActorRef orderManager;


    public Transaction( ActorRef orderManager, Match match ) throws JMSException {
        this.orderManager = orderManager;
        this.match = match;
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        this.connection = connectionFactory.createConnection();
    }

    @Override
    protected Void doRun() throws InterruptedException, SuspendExecution {
        try {
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination q = session.createQueue("vendas");

            MessageProducer producer = session.createProducer(q);

            Destination tempDest = session.createTemporaryQueue();
            MessageConsumer responseConsumer = session.createConsumer(tempDest);

            //responseConsumer.setMessageListener(this);

            TextMessage m = session.createTextMessage("venda");
            m.setStringProperty("comprador",    this.match.getComprador() );
            m.setStringProperty("vendedor",     this.match.getVendedor() );
            m.setStringProperty("empresa",      this.match.getEmpresa() );
            m.setIntProperty(   "quantidade",   this.match.getQuantidade() );
            m.setFloatProperty( "preco",        this.match.getPreco() );

            m.setJMSReplyTo(tempDest);

            producer.send(m);

            onMessage(responseConsumer.receive());

        } catch (JMSException e) {
            e.printStackTrace();
            try {
                this.connection.close();
            } catch (JMSException e1) {
                e1.printStackTrace();
            }
        }

        //TODO Comunicao com Settlement. Exemplo em test/AMQRequest

        return null;
    }

    public void onMessage(javax.jms.Message message) throws SuspendExecution{
        String messageText = null;
        try {
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                messageText = textMessage.getText();
                if(messageText.equals("OK")) {

                    notify_users();
                    publish();

                }else if(messageText.equals("KO")){
/*
                    //FIXME ver o que fazer se não for possivel fazer a tranferencia
                    System.err.print("ERRO: ");

                    String erro = textMessage.getStringProperty("erro");
                    switch (erro){
                        case "dinheiro":
                            //TODO tratar caso em que um dos utilizadores não tem dinheiro. Comprardor não tem saldo.
                            float saldo = textMessage.getFloatProperty("saldo");
                            System.err.println("Saldo insuficiente - " + saldo); //saldo atual
                            break;
                        case "acoes":
                            //TODO tratar caso em que o vendedor não tem acções.
                            int acoes = textMessage.getIntProperty("acoes");
                            System.err.println("Acoes insuficentes - "+acoes);
                            break;
                        case "utilizador":
                            //TODO um utilizador não está registado no banco
                            String utilizador = textMessage.getStringProperty("utilizador");
                            System.err.println("Utilizador nao existente - "+utilizador);
                            break;
                        default:
                            System.err.println("???");
                    }*/

                }
            }
        } catch (JMSException e) {
            //TODO ver o que acontece neste momento se a tranferencia já foi processada ou não
        }
    }

    /**
     * Metodo utilizado para criar uma notificação de que foi realizada uma transferência de ações
     * entre dois utilizadores para uma determinada empresa
     */
    private void publish() throws SuspendExecution{
        //FIXME ter atenção a estas portas
        /*System.out.println("OK - subscrive");

        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socketPub = context.socket(ZMQ.PUB);
        socketPub.connect("tcp://" + hostXSub + ":" + portXSub);

        for(int i = 0; i < 10; i++ ){
            socketPub.send(this.match.getEmpresa()+":" + i  + this.match.getQuantidade() + ";" + this.match.getPreco() + ";\n");
        }
        System.out.println("enviou");
        //socketPub.close();
        //context.term();
        */
        orderManager.send( new Message(
                Message.Type.PUB_MES,
                null,
                this.match.getEmpresa()+":" + this.match.getQuantidade() + ";" + this.match.getPreco() + ";\n"
        ));
    }

    /**
     * Metodo utilizado para realizar a notificação dos utilizadores que participaram na tranferência
     * @throws SuspendExecution
     */
    private void notify_users() throws SuspendExecution {
        //FIXME melhor isto. não tenho a certeza se posso fazer isto.
        this.match.getCompradorRef().send(
                new Message(
                        Message.Type.ORDER_REP,
                        self(),
                        "Company: " + this.match.getEmpresa()+": \n" +
                                "\tSalesman: " + this.match.getVendedor() + ";\n" +
                                "\tAmount: " + this.match.getQuantidade() + ";\n" +
                                "\tPrice: " + this.match.getPreco() + ";\n"
                )
        );

        this.match.getVendedorRef().send(
                new Message(
                        Message.Type.ORDER_REP,
                        self(),
                        "Company: " + this.match.getEmpresa()+": \n" +
                                "\tBuyer: " + this.match.getComprador() + ";\n" +
                                "\tAmount: " + this.match.getQuantidade() + ";\n" +
                                "\tPrice: " + this.match.getPreco() + ";\n"
                )
        );

    }
}
