package presentation;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.channels.Channel;
import controller.Message;
import controller.Protocol;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;

public class Menu extends JFrame{
    private JPanel panel1;
    private JPanel panel_sellbuy;
    private JTextField textField_value;
    private JSpinner spinner_nActions;
    private JButton submitButton;
    private JTextArea textArea1;
    private JRadioButton sellRadioButton;
    private JRadioButton buyRadioButton;
    private JTextField textField_company;
    private JTextField textField_sub;
    private JComboBox<String> textField_unsub;
    private JButton subscribeButton;
    private JButton unsubscribeButton;

    private HashSet<String> subscriptions = new HashSet<>();
    private final ActorRef main;
    private static final int SUBSCRIPTIONS_LIMIT = 5;

    public Menu( String title, ActorRef main ){
        super( title );
        this.main = main;
        textField_unsub.addItem("");
        textField_unsub.setSelectedItem("");

        this.setContentPane(panel1);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.pack();
        this.setVisible(true);

        this.clearSubmit();

        submitButton_ActionListener();
        subscribeButton_ActionListener();
        unsubscribeButton_ActionListener();

    }

    private void submitButton_ActionListener(){
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                clickSubmitButton();
                clearSubmit();
            }
        });
    }

    private void subscribeButton_ActionListener(){
        subscribeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sub = textField_sub.getText();
                if( !sub.equals("") && !subscriptions.contains(sub)){
                    if(subscriptions.size() < SUBSCRIPTIONS_LIMIT) {
                        try {
                            main.send(new Message(
                                Message.Type.SUB_KEY,
                                null,
                                sub
                            ));
                            subscriptions.add(sub);
                            textField_unsub.addItem(sub);
                            textArea1.append("SUBSCRIBE: " + sub + "\n");
                            textField_sub.setText("");
                        } catch (SuspendExecution suspendExecution) {
                            //TODO impossivel fazer sub alterar
                            suspendExecution.printStackTrace();
                        }
                    }
                    else{
                        JOptionPane.showMessageDialog(Menu.this, "Subscription limit reached");
                    }
                }
            }
        });
    }

    private void unsubscribeButton_ActionListener(){
        unsubscribeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String unsub = (String) textField_unsub.getSelectedItem();
                if( !unsub.equals("") ){
                    try {
                        main.send( new Message(
                                Message.Type.UNSUB_KEY,
                                null,
                                unsub
                        ));
                        textArea1.append("UNSUBSCRIBE: " + unsub + "\n");
                        subscriptions.remove(unsub);
                        textField_unsub.removeItem(unsub);
                        textField_unsub.setSelectedItem("");
                    } catch (SuspendExecution suspendExecution) {
                        //TODO impossivel fazer sub alterar
                        suspendExecution.printStackTrace();
                    }
                }
            }
        });
    }

    private void clearSubmit(){
        this.textField_company.setText("");
        this.textField_value.setText("");
        this.spinner_nActions.setValue(1);
        //this.sellRadioButton.setSelected(true);
        this.textField_sub.setText("");
        this.textField_unsub.setSelectedItem("");
    }

    private void clickSubmitButton(){
        Protocol.Request.Order.Type type;

        if(this.sellRadioButton.isSelected()){
            type = Protocol.Request.Order.Type.Sell;
        }else{
            type = Protocol.Request.Order.Type.Buy;
        }

        if( !textField_company.getText().equals("") && !textField_value.getText().equals("")){
            Protocol.Request request = Protocol.Request.newBuilder()
                    .setOrder(
                            Protocol.Request.Order.newBuilder()
                            .setCompany(textField_company.getText())
                            .setPrice(Float.parseFloat(textField_value.getText()))
                            .setQuant((Integer) spinner_nActions.getValue())
                            .setType(type)
                            .build()
                    )
                    .build();

            try {

                String show = "Type: " + request.getOrder().getType() + "\n\t" +
                        request.getOrder().getCompany() + "\n\t" +
                        request.getOrder().getQuant() + "\n\t" +
                        request.getOrder().getPrice() + "";

                int result = JOptionPane.showConfirmDialog(this,
                        show,
                        "Offer Request",
                        JOptionPane.YES_NO_OPTION);

                if ( result == JOptionPane.YES_OPTION ){
                    this.main.send( new Message(
                            Message.Type.ORDER_REQ,
                            null,
                            request
                    ));
                    this.textArea1.append("Order placed: "+request.getOrder().getType()+", " +
                        request.getOrder().getCompany() + ", "+
                        request.getOrder().getQuant() + ","+
                        request.getOrder().getPrice()+"\n");
                }

            } catch (SuspendExecution suspendExecution) {
                suspendExecution.printStackTrace();
            }

        }else {
            JOptionPane.showMessageDialog(this,
                    "Need Company name and value.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
        }




    }

    public void setSubcribeResult( String result ){
        textArea1.append( "Notification: " + result );
    }

    public void order_result(String result){
//        JOptionPane.showMessageDialog(this,
//                result,
//                "Offer Result",
//                JOptionPane.INFORMATION_MESSAGE);
        this.textArea1.append(result);

    }
}
