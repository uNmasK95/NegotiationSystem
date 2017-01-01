package presentation;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.fibers.SuspendExecution;
import controller.Message;
import controller.Message.Type;

import javax.swing.*;
import java.awt.event.*;

public class Login extends JFrame{
    private final ActorRef main;
    private JTextField usernameTextField;
    private JPasswordField passwordPasswordField;
    private JButton SIGNINButton;
    private JPanel panel1;


    public Login(ActorRef main){
        super("Login");
        this.main = main;
        this.setContentPane(panel1);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.pack();
        this.setVisible(true);
        init_usernameTextField();

        SIGNINButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    main.send( new Message(
                            Message.Type.LOGIN_REQ,
                            null,
                            null
                    ));
                } catch (SuspendExecution suspendExecution) {
                    suspendExecution.printStackTrace();
                }


            }
        });
    }

    private void init_usernameTextField(){
        usernameTextField.setText("Username!");
        usernameTextField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                usernameTextField.setText("");
            }
        });
    }


}
