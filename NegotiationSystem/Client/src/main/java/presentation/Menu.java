package presentation;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.channels.Channel;
import controller.Message;
import controller.Protocol;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
    private JTextField textField_unsub;
    private JButton subscribeButton;
    private JButton unsubscribeButton;

    private final ActorRef main;

    public Menu( String title, ActorRef main ){
        super( title );
        this.main = main;

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
                if( !textField_sub.getText().equals("") ){
                    try {
                        main.send( new Message(
                                Message.Type.SUB_KEY,
                                null,
                                textField_sub.getText()
                        ));
                    } catch (SuspendExecution suspendExecution) {
                        //TODO impossivel fazer sub alterar
                        suspendExecution.printStackTrace();
                    }
                }

            }
        });
    }

    private void unsubscribeButton_ActionListener(){
        unsubscribeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if( !textField_unsub.getText().equals("") ){
                    try {
                        main.send( new Message(
                                Message.Type.UNSUB_KEY,
                                null,
                                textField_unsub.getText()
                        ));
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
        this.sellRadioButton.setSelected(true);
        this.textField_sub.setText("");
        this.textField_unsub.setText("");
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

        textArea1.append( result );
    }

    public void order_result(String result){
        JOptionPane.showMessageDialog(this,
                result,
                "Offer Result",
                JOptionPane.INFORMATION_MESSAGE);

    }
}
