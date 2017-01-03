package presentation;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.channels.Channel;
import controller.Message;
import controller.Message.Type;
import controller.Protocol;

import javax.swing.*;
import java.awt.event.*;

public class Login extends JFrame{

    private final ActorRef main;
    private final Channel channelLogin;
    private final Channel channelSubscribe;

    private JTextField usernameTextField;
    private JPasswordField passwordPasswordField;
    private JButton SIGNINButton;
    private JPanel panel1;


    public Login(ActorRef main, Channel channelLogin, Channel channelSubscribe){
        super("Login");
        this.main = main;
        this.channelLogin = channelLogin;
        this.channelSubscribe = channelSubscribe;

        this.setContentPane(panel1);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.pack();
        this.setVisible(true);
        init_usernameTextField();

        SIGNINButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonLogin( SIGNINButton );
            }
        });
    }

    private void buttonLogin(JButton SIGNINButton){
        if( this.passwordPasswordField.getPassword() != null && !this.usernameTextField.getText().equals("")){
            Protocol.LoginRequest login = Protocol.LoginRequest.newBuilder()
                    .setUsername( this.usernameTextField.getText())
                    .setPassword( new String( this.passwordPasswordField.getPassword() ) )
                    .build();

            System.out.println("user: " + login.getUsername());
            System.out.println("pass: " + login.getPassword());

            try {

                main.send( new Message(
                        Message.Type.LOGIN_REQ,
                        null,
                        login
                ));

                Protocol.Reply reply = (Protocol.Reply) channelLogin.receive();
                if( reply.getType() == Protocol.Reply.Type.Login && reply.getResult() ){
                    System.out.println("Login Realizado");
                    new Menu( main, channelSubscribe);
                }

            } catch (SuspendExecution | InterruptedException e1 ) {
                e1.printStackTrace();
            }
        }
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
