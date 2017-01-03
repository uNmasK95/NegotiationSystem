package presentation;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.channels.Channel;
import controller.Message;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Menu extends JFrame{
    private JPanel panel1;
    private JPanel tabPanel_buy;
    private JComboBox comboBox2;
    private JTextField textField2;
    private JSpinner spinner2;
    private JButton submitButton1;
    private JTextArea textArea1;
    private JRadioButton sellRadioButton;
    private JRadioButton buyRadioButton;

    private final String[] companies = {"Apple", "Meias Pinto"};

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
        Message.Type type;
        String company = null;
        if(this.sellRadioButton.isSelected()){
            type = Message.Type.SELL;
        }else{
            type = Message.Type.BUY;
        }

        if(this.comboBox2.getSelectedIndex() >=0 ){
            company = companies[ this.comboBox2.getSelectedIndex() ];
        }else{

        }

        try {
            this.main.send( new Message(
                    type,
                    null,
                    null
            ));
        } catch (SuspendExecution suspendExecution) {
            suspendExecution.printStackTrace();
        }
    }


}
