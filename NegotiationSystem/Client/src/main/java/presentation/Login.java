package presentation;

import javax.swing.*;
import java.awt.event.*;

public class Login extends JFrame{
    private JTextField usernameTextField;
    private JPasswordField passwordPasswordField;
    private JButton SIGNINButton;
    private JPanel panel1;


    public Login(){
        super("Login");
        this.setContentPane(panel1);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.pack();
        this.setVisible(true);
        init_usernameTextField();

        SIGNINButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

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
