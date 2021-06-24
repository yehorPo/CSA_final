package client_server.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONObject;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    private Integer id;
    private String login;
    private String password;
    private String role;

    public User(String login, String password, String role) {
        this.login = login;
        this.password = password;
        this.role = role;
    }

    public JSONObject toJSON(){
        JSONObject json = new JSONObject("{"+"\"login\":"+login+", \"password\":\""+password+"\", \"role\":\""+role+"\"}");
        return json;
    }

    public boolean equals(User u) {
        if (this.login.equals(u.getLogin()) && this.password.equals(u.getPassword())
                && this.role.equals(u.getRole())) {
            return true;
        }
        return false;
    }
}