package controller;

import co.paralleluniverse.actors.ActorRef;

public class Message {

    public static enum Type { LOGIN_REQ , LOGIN_REP, ORDER_REQ, ORDER_REP , SUB_KEY, UNSUB_KEY, SUB_MES,  KO}

    final Type type;
    final ActorRef source;
    final Object obj;  // careful with mutable objects, such as the byte array

    public Message(Type type, ActorRef source, Object obj) {
        this.type = type;
        this.source = source;
        this.obj = obj;
    }

}
