import org.zeromq.ZMQ;

public class TesteZMQ {

    private static final String hostXSub = "localhost";
    private static final int portXSub = 12370;

    public static void main(String[] args) {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socketPub = context.socket(ZMQ.PUB);
        socketPub.connect("tcp://" + hostXSub + ":" + portXSub);

        socketPub.send("merda\n");

    }
}
