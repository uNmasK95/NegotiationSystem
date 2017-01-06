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
    private JComboBox comboBox2;
    private JTextField textField_value;
    private JSpinner spinner_nActions;
    private JButton submitButton1;
    private JTextArea textArea1;
    private JRadioButton sellRadioButton;
    private JRadioButton buyRadioButton;
    private JTextField textField_company;

    private final ActorRef main;
    private final Channel channelSubscribe;

    public Menu(ActorRef main, Channel channelSubscribe){
        this.main = main;
        this.channelSubscribe = channelSubscribe;

        this.setContentPane(panel1);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.pack();
        this.setVisible(true);

        submitButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clickSubmitButton();
            }
        });
    }

    private void clickSubmitButton(){
        Protocol.Request.Order.Type type;
        String company = null;
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

                this.main.send( new Message(
                        Message.Type.ORDER_REQ,
                        null,
                        request
                ));

                String show = "Type: " + request.getOrder().getType() + "\n\t" +
                        request.getOrder().getCompany() + "\n\t" +
                        request.getOrder().getQuant() + "\n\t" +
                        request.getOrder().getPrice() + "";

                JOptionPane.showMessageDialog(this,
                        show,
                        "Offer Request",
                        JOptionPane.PLAIN_MESSAGE);

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


}
