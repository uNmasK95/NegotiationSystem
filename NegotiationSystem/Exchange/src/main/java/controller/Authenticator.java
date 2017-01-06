package controller;

import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import controller.entity.User;
import data.UserDAO;

import java.sql.*;

public class Authenticator extends BasicActor<Message, Void> {

    private final UserDAO userDAO;

    public Authenticator() throws SQLException {
        this.userDAO = new UserDAO();
    }

    @Override
    protected Void doRun() throws InterruptedException, SuspendExecution {

        while (receive( msg -> {

            if(msg.type == Message.Type.LOGIN_REQ){
                loginRequest( msg );
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
                ((User) msg.obj).getUsername(),
                ((User) msg.obj).getPassword()
        );

        msg.source.send(new Message(Message.Type.LOGIN_REP , null, result));
    }

}
