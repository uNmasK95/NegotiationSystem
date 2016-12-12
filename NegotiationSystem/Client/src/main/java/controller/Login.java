package controller;

import com.esotericsoftware.minlog.Log;
import org.zeromq.ZMQ;

public class Login {

    private static final String TAG = "Login";

    private String username;
    private String password;
    private String addr_target;

    public Login(String username, String password, String addr ) {
        this.username = username;
        this.password = password;
        this.addr_target = addr;
    }

    public boolean login(){
        try(final ZMQ.Context zmq = ZMQ.context(1);
            final ZMQ.Socket target = zmq.socket(ZMQ.REQ)){

            Log.info(TAG,"connecting to " +this.addr_target );

            target.connect(addr_target);
        }
        return false;
    }
}
