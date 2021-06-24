package client_server.handlers;


public class parseResp {

    String reply;

    public parseResp() {
    }

    public void putField(String value){
        reply = "{\"message\":\""+value+"\"}";
    }

    public void putObject(String value){
        reply = "{\"object\":"+value+"}";
    }

    public String toString(){
        return reply;
    }
}
