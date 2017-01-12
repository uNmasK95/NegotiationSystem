package controller;

import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import controller.entity.Match;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.zeromq.ZMQ;

import javax.jms.*;

public class Transaction extends BasicActor<Message,Void> implements MessageListener {

    private final String hostXSub = "localhost";
    private final int portXSub = 12371;
    private final Match match;
    private final Connection connection;


    public Transaction( Match match ) throws JMSException {
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

            responseConsumer.setMessageListener(this);

            TextMessage m = session.createTextMessage("venda");
            m.setStringProperty("comprador",    this.match.getComprador() );
            m.setStringProperty("vendedor",     this.match.getVendedor() );
            m.setStringProperty("empresa",      this.match.getEmpresa() );
            m.setIntProperty(   "quantidade",   this.match.getQuantidade() );
            m.setFloatProperty( "preco",        this.match.getPreco() );

            m.setJMSReplyTo(tempDest);

            producer.send(m);

            producer.close();
            connection.close();



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

    @Override
    public void onMessage(javax.jms.Message message){
        String messageText = null;
        try {
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                messageText = textMessage.getText();
                if(messageText.equals("OK")) {

                    notify_users();
                    publish();

                }else if(messageText.equals("KO")){

                    //FIXME ver o que fazer se não for possivel fazer a tranferencia
                    System.err.print("ERRO: ");
                    /*
                    String erro = textMessage.getStringProperty("erro");
                    switch (erro){
                        case "dinheiro":
                            float saldo = textMessage.getFloatProperty("saldo");
                            System.err.println("Saldo insuficiente - " + saldo); //saldo atual
                            break;
                        case "acoes":
                            int acoes = textMessage.getIntProperty("acoes");
                            System.err.println("Acoes insuficentes - "+acoes);
                            break;
                        case "utilizador":
                            String utilizador = textMessage.getStringProperty("utilizador");
                            System.err.println("Utilizador nao existente - "+utilizador);
                            break;
                        default:
                            System.err.println("???");
                    }
                    */
                }
            }
        } catch (JMSException e) {
            //TODO ver o que acontece neste momento se a tranferencia já foi processada ou não
        } catch (SuspendExecution suspendExecution) {
            //TODO ver se isto vai funcionar
            suspendExecution.printStackTrace();
        }
    }

    /**
     * Metodo utilizado para criar uma notificação de que foi realizada uma transferência de ações
     * entre dois utilizadores para uma determinada empresa
     */
    private void publish(){
        //FIXME ter atenção a estas portas
        System.out.println("OK - subscrive");

        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socketPub = context.socket(ZMQ.PUB);
        socketPub.connect("tcp://" + hostXSub + ":" + portXSub);

        socketPub.send(this.match.getEmpresa()+": " + this.match.getQuantidade() + ";" + this.match.getPreco() + ";\n");

        socketPub.close();
        context.term();
    }

    /**
     * Metodo utilizado para realizar a notificação dos utilizadores que participaram na tranferência
     * @throws SuspendExecution
     */
    private void notify_users() throws SuspendExecution{
        //FIXME melhor isto. não tenho a certeza se posso fazer isto.
        System.out.println("OK - notify");
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
