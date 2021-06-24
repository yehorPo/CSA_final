package client_server.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONObject;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCred {

    private String login;
    private String password;

    public JSONObject toJSON(){

        return new JSONObject("{"+"\"login\":\""+login+"\", \"password\":\""+password+"\"}");
    }
}