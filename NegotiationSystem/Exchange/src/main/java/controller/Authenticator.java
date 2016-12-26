package controller;

import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import com.esotericsoftware.minlog.Log;

import java.sql.*;

public class Authenticator extends BasicActor<Message, Void> {

    static final String TAG = "Authenticator";
    final Connection conection;
    final PreparedStatement loginStatment;

    public Authenticator() throws SQLException {
        this.conection = DriverManager.getConnection("jdbc:postgresql://localhost/utilizadores:12346");
        this.loginStatment = this.conection.prepareStatement("SELECT * FROM utilizadores WHERE username = ? AND password = ?");
    }

    @Override
    protected Void doRun() throws InterruptedException, SuspendExecution {

        while (receive( msg -> {

            if(msg.type == Message.Type.LOGIN_REQ){

            }else {
                //TODO: enviar alguma informação para o user a informar que enviou a mensagem errada
            }

         return true;
        }));

        return null;
    }

    private void loginRequest(Message msg) throws SuspendExecution {
        boolean result;
        try {
            this.loginStatment.setString(1, ((UserInfo) msg.obj).getUsername() );
            this.loginStatment.setString(2, ((UserInfo) msg.obj).getUsername() );
            ResultSet rs = this.loginStatment.executeQuery();
            rs.first();
            result = rs.getRow() == 1; //verifica se existe uma linha so resultset
        } catch (SQLException e) {
            Log.debug(TAG , "Não foi possivel realizar o pedido á base de dados");
            //TODO: remover print e verificar connection
            e.printStackTrace();
        }
        msg.source.send(new Message(Message.Type.LOGIN_REP , null, null));
    }

}
