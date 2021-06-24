package client_server.client.controllers;

import client_server.client.GlobalContext;
import client_server.domain.Group;
import client_server.domain.Product;
import client_server.domain.packet.Message;
import client_server.domain.packet.Packet;
import com.google.common.primitives.UnsignedLong;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

import static client_server.domain.packet.Message.cTypes.GET_LIST_GROUPS;
import static client_server.domain.packet.Message.cTypes.UPDATE_PRODUCT;

public class UpdateProdControl {

    private Integer productID;

    @FXML
    private TextField nameField;

    @FXML
    private TextField priceField;

    @FXML
    private TextField amountField;

    @FXML
    private TextField descrField;

    @FXML
    private TextField manufField;

    @FXML
    private ChoiceBox<Group> groupIdChoice;

    @FXML
    private Label statusLabel;

    @FXML
    void updateProd() {
        if(nameField.getText().isEmpty() || descrField.getText().isEmpty() || priceField.getText().isEmpty()
                || amountField.getText().isEmpty() || manufField.getText().isEmpty()){
            statusLabel.setText("Fill out all fields before updateProduct.");
        }else{
            Double price = null;
            Double amount = null;
            try{
                price = Double.parseDouble(priceField.getText());
            }catch(NumberFormatException e){
                statusLabel.setText("Incorrect price.");
            }
            try{
                amount = Double.parseDouble(amountField.getText());
            }catch(NumberFormatException e){
                statusLabel.setText("Incorrect amount.");
            }
            if(price != null && amount != null && price>=0 && amount>=0) {

                Product product = new Product(productID, nameField.getText(), price, amount, descrField.getText(), manufField.getText(), groupIdChoice.getValue().getId());
                updateProd(product, statusLabel);
            }else {
                statusLabel.setText("Price and amount should be positive.");
            }
        }
    }

    @FXML
    void initialize() {

    }

    public void initData(Product product) {
        productID = product.getId();
        nameField.setText(product.getName());
        double price = product.getPrice();
        priceField.setText(Double.toString(price));
        double amount = product.getAmount();
        amountField.setText(Double.toString(amount));
        descrField.setText(product.getDescription());
        manufField.setText(product.getManufacturer());

        Message msg = new Message(GET_LIST_GROUPS.ordinal(), 1, "".getBytes(StandardCharsets.UTF_8));
        Packet packet = new Packet((byte) 1, UnsignedLong.valueOf(GlobalContext.packetId++), msg);

        Packet receivedPacket = GlobalContext.clientTCP.sendPacket(packet.toPacket());

        int command = receivedPacket.getBMsq().getcType();
        Message.cTypes[] val = Message.cTypes.values();
        Message.cTypes command_type = val[command];

        if (command_type == GET_LIST_GROUPS) {
            String message = new String(receivedPacket.getBMsq().getMessage(), StandardCharsets.UTF_8);
            JSONObject information = new JSONObject(message);
            System.out.println("command");
            try {
                JSONObject list = information.getJSONObject("object");
                JSONArray array = list.getJSONArray("list");

                ObservableList<Group> groups = FXCollections.observableArrayList();

                for (int i = 0; i < array.length(); i++) {
                    Group group = new Group(array.getJSONObject(i));
                    groups.add(group);
                    if(group.getId().equals(product.getGroup_id())){
                        groupIdChoice.setValue(group);
                    }
                }

                groupIdChoice.setItems(groups);

                System.out.println(groups.get(0).toString());
            } catch (JSONException e) {
                e.printStackTrace();
                statusLabel.setText("Failed to get list of groups!");
            }
        }
    }

    static void updateProd(Product productToUpdate, Label statusLabel) {
        Message msg = new Message(Message.cTypes.UPDATE_PRODUCT.ordinal(), 1, productToUpdate.toJSON().toString().getBytes(StandardCharsets.UTF_8));

        Packet packet = new Packet((byte) 1, UnsignedLong.valueOf(GlobalContext.packetId++), msg);

        Packet receivedPacket = GlobalContext.clientTCP.sendPacket(packet.toPacket());

        int command = receivedPacket.getBMsq().getcType();
        Message.cTypes[] val = Message.cTypes.values();
        Message.cTypes command_type = val[command];

        if (command_type == UPDATE_PRODUCT) {
            String message = new String(receivedPacket.getBMsq().getMessage(), StandardCharsets.UTF_8);
            JSONObject information = new JSONObject(message);
            try {
                statusLabel.setText(information.getString("message"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            statusLabel.setText("Failed to update product!");
        }
    }
}