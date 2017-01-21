package presentation;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.channels.Channel;
import controller.Message;
import controller.Protocol;

import javax.swing.*;
import java.awt.event.*;
import java.util.concurrent.ExecutionException;

public class Login extends JFrame{

    private final ActorRef main;


    //private JTextField usernameTextField;
    private JTextField usernameTextField;
    private JPasswordField passwordPasswordField;
    private JButton SIGNINButton;
    private JPanel panel1;


    public Login( ActorRef main ){
        super("Login");
        this.main = main;

        this.setContentPane(panel1);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.pack();
        this.setVisible(true);

        SIGNINButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonLogin( SIGNINButton );
            }
        });
    }

    private void buttonLogin(JButton SIGNINButton){
        if( this.passwordPasswordField.getPassword() != null && !this.usernameTextField.getText().equals("")){

            Protocol.Request requestLogin = Protocol.Request.newBuilder()
                    .setLogin(
                            Protocol.Request.Login.newBuilder()
                            .setUsername(this.usernameTextField.getText())
                            .setPassword( new String( this.passwordPasswordField.getPassword() ) )
                            .build()
                    )
                    .build();

            try {

                main.send( new Message(
                        Message.Type.LOGIN_REQ,
                        null,
                        requestLogin
                ));

            } catch (SuspendExecution e) {
                e.printStackTrace();
            }

        }
    }

    public void login_reply( boolean result ){
        if( result ){
            System.out.println("LOGIN: OK");
            this.dispose();
        }else {
            JOptionPane.showMessageDialog(this,
                    "Username or Password Error",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createUIComponents() {
        usernameTextField = new HintTextField("Username");
        passwordPasswordField = new HintPasswordField("Password");
    }
}
