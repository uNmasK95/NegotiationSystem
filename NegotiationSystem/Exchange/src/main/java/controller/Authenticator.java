package controller;

import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import com.esotericsoftware.minlog.Log;
import data.Users;

import java.sql.*;

public class Authenticator extends BasicActor<Message, Void> {

    private static final String TAG = "Authenticator";

    private final Users userDAO;

    public Authenticator() throws SQLException {
        this.userDAO = new Users();
    }

    @Override
    protected Void doRun() throws InterruptedException, SuspendExecution {

        while (receive( msg -> {

            if(msg.type == Message.Type.LOGIN_REQ){
                loginRequest( msg );
            }else {
                //TODO: enviar alguma informação para o user a informar que enviou a mensagem errada
            }

         return true;
        }));

        return null;
    }


    /**
     * Metodo responsavel por tratar um pedido de login
     * @param msg
     * @throws SuspendExecution
     */
    private void loginRequest(Message msg) throws SuspendExecution {

        boolean result = this.userDAO.contains(
                ((UserInfo) msg.obj).getUsername(),
                ((UserInfo) msg.obj).getPassword()
        );

        msg.source.send(new Message(Message.Type.LOGIN_REP , null, result));
    }

}
