package controller;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import controller.entity.Match;
import controller.entity.Order;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.zeromq.ZMQ;

import javax.jms.*;

public class Transaction extends BasicActor<Message,Void> {

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

        return null;
    }

    public void onMessage(javax.jms.Message message) throws SuspendExecution{
        try {
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                String messageText = textMessage.getText();
                if(messageText.equals("OK")) {
                    notify_users();
                    publish();
                }else if(messageText.equals("KO")){
                    System.err.print("ERRO: ");

                    String erro = textMessage.getStringProperty("erro");
                    switch (erro){
                        //TODO ver se é para tratar mais algum caso
                        case "acoes":
                            int acoes = textMessage.getIntProperty("acoes");
                            System.err.println(this.match.getVendedor() + ": Acoes insuficentes - " + acoes);
                            //TODO verificar se o que está em baixo está bem
                            orderManager.send(
                                    new Message(
                                            Message.Type.LOGIN_REQ,
                                            this.match.getCompradorRef(),
                                            new Order(
                                                    this.match.getEmpresa(),
                                                    this.match.getQuantidade(),
                                                    this.match.getPreco(),
                                                    this.match.getComprador(),
                                                    self(),
                                                    Order.Tipo.COMPRA
                                            )
                                    )
                            );
                            break;
                        default:
                            System.err.println("???");
                    }

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
