package client_server.client.controllers;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import client_server.client.GlobalContext;
import client_server.domain.Group;
import client_server.domain.packet.Message;
import client_server.domain.packet.Packet;
import com.google.common.primitives.UnsignedLong;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static client_server.client.controllers.LoginWindowControl.addingUser;
import static client_server.domain.packet.Message.cTypes.*;

public class GroupsListControl {
    @FXML
    private Button addNewGroupBtn;

    @FXML
    private Button deleteGroupBtn;

    @FXML
    private Button updateGroupBtn;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField idFilterField;

    @FXML
    private TableView<Group> groupsTable;

    @FXML
    private TableColumn<Group, String> idCol;

    @FXML
    private TableColumn<Group, String> nameCol;

    @FXML
    private TableColumn<Group, String> descrCol;

    @FXML
    private Label statusLabel;
    @FXML
    void addNewGWindow(ActionEvent event) throws MalformedURLException {
        statusLabel.setText("");

        URL url = new File("src/main/java/client_server/client/views/addG.fxml").toURI().toURL();
        Parent root = null;
        try {
            root = FXMLLoader.load(url);
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Function is not available.");
        }
        Stage stage = new Stage();
        stage.setTitle("New Group");
        stage.setScene(new Scene(root));

        stage.setOnHiding(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                resetTable();
            }
        });

        stage.show();
    }

    @FXML
    void updateGWindow(ActionEvent event) throws MalformedURLException {
        statusLabel.setText("");

        Group group = groupsTable.getSelectionModel().getSelectedItem();

        if(group != null) {
            FXMLLoader loader = new FXMLLoader();
            URL url = new File("src/main/java/client_server/client/views/updateG.fxml").toURI().toURL();
            loader.setLocation(url);
            try {
                loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Parent root = loader.getRoot();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Update group");

            UpdateGroupControl controller = loader.getController();
            controller.initData(group);

            stage.setOnHiding(new EventHandler<WindowEvent>() {
                public void handle(WindowEvent we) {
                    resetTable();
                }
            });

            stage.show();
        }else{
            statusLabel.setText("Choose product to update!");
        }
    }

    @FXML
    void deleteGroup(ActionEvent event) {
        Group group = groupsTable.getSelectionModel().getSelectedItem();

        if(group != null) {
            Message msg = new Message(Message.cTypes.DELETE_GROUP.ordinal(), 1, group.getId().toString().getBytes(StandardCharsets.UTF_8));
            Packet packet = new Packet((byte) 1, UnsignedLong.valueOf(GlobalContext.packetId++), msg);

            Packet receivedPacket = GlobalContext.clientTCP.sendPacket(packet.toPacket());
            packet.getBMsq().setCType(DELETE_ALL_IN_GROUP.ordinal());
            Packet receivedPacket2 = GlobalContext.clientTCP.sendPacket(packet.toPacket());

            int command = receivedPacket.getBMsq().getcType();
            Message.cTypes[] val = Message.cTypes.values();
            Message.cTypes command_type = val[command];


            if (command_type == DELETE_GROUP) {
                String message = new String(receivedPacket.getBMsq().getMessage(), StandardCharsets.UTF_8);
                JSONObject information = new JSONObject(message);
                try {
                    statusLabel.setText(information.getString("message"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                statusLabel.setText("Can't delete group.");
            }


            command = receivedPacket2.getBMsq().getcType();
            command_type = val[command];


            if (command_type == DELETE_ALL_IN_GROUP) {
                String message = new String(receivedPacket.getBMsq().getMessage(), StandardCharsets.UTF_8);
                JSONObject information = new JSONObject(message);
                try {
                    statusLabel.setText(information.getString("message"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                statusLabel.setText("Can't delete products from group.");
            }

            resetTable();
        }else{
            statusLabel.setText("Choose group to delete!");
        }
    }

    @FXML
    void filterGroups(ActionEvent event) {

        statusLabel.setText("");

        try {
            int id = Integer.parseInt(idFilterField.getText());
            if (id >= 0) {
                Message msg = new Message(Message.cTypes.GET_GROUP.ordinal(), 1, idFilterField.getText().getBytes(StandardCharsets.UTF_8));
                Packet packet = new Packet((byte) 1, UnsignedLong.valueOf(GlobalContext.packetId++), msg);


                Packet receivedPacket = GlobalContext.clientTCP.sendPacket(packet.toPacket());

                int command = receivedPacket.getBMsq().getcType();
                Message.cTypes[] val = Message.cTypes.values();
                Message.cTypes command_type = val[command];


                if (command_type == GET_GROUP) {
                    String message = new String(receivedPacket.getBMsq().getMessage(), StandardCharsets.UTF_8);
                    JSONObject information = new JSONObject(message);
                    try {
                        JSONObject object = information.getJSONObject("object");
                        Group group = new Group(object);

                        groupsTable.getItems().clear();
                        groupsTable.getItems().add(group);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        statusLabel.setText(information.getString("message"));
                    }
                } else {
                    statusLabel.setText("Can't show group.");
                }
            }else{
                statusLabel.setText("Incorrect group ID.");
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Incorrect group ID.");
        }

        idFilterField.clear();

    }

    @FXML
    void toProdList(ActionEvent event) throws MalformedURLException {
        FXMLLoader loader = new FXMLLoader();
        Stage stage = (Stage) idFilterField.getScene().getWindow();
        URL url = null;

        url = new File("src/main/java/client_server/client/views/productsL.fxml").toURI().toURL();

        Parent root = null;
        try {
            root = FXMLLoader.load(url);
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Can't open products.");
        }
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }

    @FXML
    void logOut(ActionEvent event) throws MalformedURLException {
        LoginWindowControl.logOut(addNewGroupBtn);
    }

    @FXML
    void showAllGroups(ActionEvent event) {
        statusLabel.setText(" ");
        resetTable();
    }

    @FXML
    void initialize() {
        if (!GlobalContext.role.equals("admin")) {
            addNewGroupBtn.setDisable(true);
            deleteGroupBtn.setDisable(true);
            updateGroupBtn.setDisable(true);
        }

        descrCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        resetTable();
    }

    private void resetTable() {
        Message msg = new Message(GET_LIST_GROUPS.ordinal(), 1, "".getBytes(StandardCharsets.UTF_8));
        Packet packet = new Packet((byte) 1, UnsignedLong.valueOf(GlobalContext.packetId++), msg);

        Packet receivedPacket = GlobalContext.clientTCP.sendPacket(packet.toPacket());

        int command = receivedPacket.getBMsq().getcType();
        Message.cTypes[] val = Message.cTypes.values();
        Message.cTypes command_type = val[command];


        if (command_type == GET_LIST_GROUPS) {
            String message = new String(receivedPacket.getBMsq().getMessage(), StandardCharsets.UTF_8);
            JSONObject information = new JSONObject(message);

            try {
                JSONObject list = information.getJSONObject("object");
                JSONArray array = list.getJSONArray("list");

                List<Group> groups = new ArrayList<>();

                for (int i = 0; i < array.length(); i++) {
                    groups.add(new Group(array.getJSONObject(i)));
                }

                groupsTable.getItems().clear();
                groupsTable.getItems().addAll(groups);

            } catch (JSONException e) {
                e.printStackTrace();
                statusLabel.setText("Failed to get groups!");
            }
        } else {
            statusLabel.setText("Failed to get groups!");
        }
    }
}