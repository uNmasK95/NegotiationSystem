package data;

import controller.UserInfo;

import java.sql.*;

public class Users {

    public static final String host = "127.0.0.1";
    public static final int port = 12346;
    public static final String database = "utilizadores";

    private Connection conection;
    private PreparedStatement loginStatment;

    public Users() throws SQLException {
        this.conection = DriverManager.getConnection("jdbc:postgresql://" + host + ":" + port + "/" + database);
        this.loginStatment = this.conection.prepareStatement("SELECT COUNT(*) FROM utilizadores WHERE username = ? AND password = ?");
    }

    public boolean contains(String username, String password){
        boolean result = false;
        try {
            this.loginStatment.setString(1, username );
            this.loginStatment.setString(2, password);
            ResultSet rs = this.loginStatment.executeQuery();

            rs.first();
            result = rs.getInt("count") == 1;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
