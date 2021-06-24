package client_server.client.controllers;

import client_server.client.GlobalContext;
import client_server.domain.User;
import client_server.domain.packet.Message;
import client_server.domain.packet.Packet;
import com.google.common.primitives.UnsignedLong;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

import static client_server.domain.packet.Message.cTypes.ADD_USER;

public class AddUserControl {

    @FXML
    private TextField loginField;

    @FXML
    private TextField passwordField;

    @FXML
    private ChoiceBox<String> roleChoice;

    @FXML
    private Label statusLabel;

    @FXML
    void createUser() {
        if (loginField.getText().isEmpty() || passwordField.getText().isEmpty() || roleChoice.getValue().isEmpty()) {
            statusLabel.setText("Fill out all fields before adding.");
        } else {

            User user = new User(loginField.getText(), passwordField.getText(), roleChoice.getValue());
            Message msg = new Message(Message.cTypes.ADD_USER.ordinal(), 1, user.toJSON().toString().getBytes(StandardCharsets.UTF_8));

            Packet packet = new Packet((byte) 1, UnsignedLong.valueOf(GlobalContext.packetId++), msg);

            Packet receivedPacket = GlobalContext.clientTCP.sendPacket(packet.toPacket());

            int command = receivedPacket.getBMsq().getcType();
            Message.cTypes[] val = Message.cTypes.values();
            Message.cTypes command_type = val[command];

            if (command_type == ADD_USER) {
                String message = new String(receivedPacket.getBMsq().getMessage(), StandardCharsets.UTF_8);
                JSONObject information = new JSONObject(message);
                try {
                    statusLabel.setText(information.getString("message"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                statusLabel.setText("Can't add user.");
            }

        }
    }

    @FXML
    void initialize() {
        ObservableList<String> roles = FXCollections.observableArrayList();
        roles.add("admin");
        roles.add("user");
        roles.add("worker");

        roleChoice.setItems(roles);
    }
}
