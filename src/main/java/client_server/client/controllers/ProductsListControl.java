package client_server.client.controllers;

import client_server.client.GlobalContext;
import client_server.domain.Product;
import client_server.domain.ProductFilter;
import client_server.domain.packet.Message;
import client_server.domain.packet.Packet;
import com.google.common.primitives.UnsignedLong;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static client_server.client.controllers.LoginWindowControl.addingUser;
import static client_server.domain.packet.Message.cTypes.DELETE_PRODUCT;
import static client_server.domain.packet.Message.cTypes.GET_LIST_PRODUCTS;

public class ProductsListControl {

    @FXML
    private Button addUserBtn;

    @FXML
    private Button addNewProductBtn;

    @FXML
    private Button deleteProductBtn;

    @FXML
    private Button updateProductBtn;

    @FXML
    private TextField idFilter;

    @FXML
    private TextField priceFromFilter;

    @FXML
    private TextField priceToFilter;

    @FXML
    private TextField manufacturerFilter;

    @FXML
    private TextField groupIdFilter;

    @FXML
    private TableView<Product> productsTable;

    @FXML
    private TableColumn<Product, String> idCol;

    @FXML
    private TableColumn<Product, String> nameCol;

    @FXML
    private TableColumn<Product, String> priceCol;

    @FXML
    private TableColumn<Product, String> amountCol;

    @FXML
    private TableColumn<Product, String> descrCol;

    @FXML
    private TableColumn<Product, String> manufacturerCol;

    @FXML
    private TableColumn<Product, String> groupIdCol;

    @FXML
    private Label statusLabel;


    @FXML
    void addUser() throws MalformedURLException {
        addingUser(statusLabel);
    }

    @FXML
    void addAmount() throws MalformedURLException {
        Product product = productsTable.getSelectionModel().getSelectedItem();

        if (product != null) {
            FXMLLoader loader = new FXMLLoader();
            URL url = new File("src/main/java/client_server/client/views/addPInStore.fxml").toURI().toURL();
            loader.setLocation(url);
            try {
                loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Parent root = loader.getRoot();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Add amount");

            AddAmountControl controller = loader.getController();
            controller.initData(product);

            stage.setOnHiding(we -> resetTable());

            stage.show();
        } else {
            statusLabel.setText("Choose product first!");
        }
    }

    @FXML
    void deductAmount() throws MalformedURLException {
        Product product = productsTable.getSelectionModel().getSelectedItem();

        if (product != null) {
            FXMLLoader loader = new FXMLLoader();
            URL url = new File("src/main/java/client_server/client/views/removeP.fxml").toURI().toURL();
            loader.setLocation(url);
            try {
                loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Parent root = loader.getRoot();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Deduct amount");

            DeductAmountControl controller = loader.getController();
            controller.initData(product);

            stage.setOnHiding(we -> resetTable());

            stage.show();
        }else{
            statusLabel.setText("Choose product first.");
        }
    }

    @FXML
    void addNewPWindow() throws MalformedURLException {
        URL url = new File("src/main/java/client_server/client/views/addP.fxml").toURI().toURL();
        Parent root = null;
        try {
            root = FXMLLoader.load(url);
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Function is not available.");
        }
        Stage stage = new Stage();
        stage.setTitle("New Product");
        stage.setScene(new Scene(Objects.requireNonNull(root)));

        stage.setOnHiding(we -> resetTable());

        stage.show();
    }

    @FXML
    void updatePWindow() throws MalformedURLException {
        Product product = productsTable.getSelectionModel().getSelectedItem();

        if (product != null) {
            FXMLLoader loader = new FXMLLoader();
            URL url = new File("src/main/java/client_server/client/views/updateP.fxml").toURI().toURL();
            loader.setLocation(url);
            try {
                loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Parent root = loader.getRoot();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Update Product");

            UpdateProdControl controller = loader.getController();
            controller.initData(product);

            stage.setOnHiding(we -> resetTable());

            stage.show();
        } else {
            statusLabel.setText("Choose product to update!");
        }
    }

    @FXML
    void deleteP() {
        Product product = productsTable.getSelectionModel().getSelectedItem();

        if (product != null) {
            Message msg = new Message(Message.cTypes.DELETE_PRODUCT.ordinal(), 1, product.getId().toString().getBytes(StandardCharsets.UTF_8));
            Packet packet = new Packet((byte) 1, UnsignedLong.valueOf(GlobalContext.packetId++), msg);

            Packet receivedPacket = GlobalContext.clientTCP.sendPacket(packet.toPacket());

            int command = receivedPacket.getBMsq().getcType();
            Message.cTypes[] val = Message.cTypes.values();
            Message.cTypes command_type = val[command];

            if (command_type == DELETE_PRODUCT) {
                String message = new String(receivedPacket.getBMsq().getMessage(), StandardCharsets.UTF_8);
                JSONObject information = new JSONObject(message);
                try {
                    statusLabel.setText(information.getString("message"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                statusLabel.setText("Can't delete product.");
            }
            resetTable();

        }else{
            statusLabel.setText("Choose product to delete!");
        }
    }

    @FXML
    void filterP() {
        statusLabel.setText("");

        List<Integer> listId = new ArrayList<>();
        ProductFilter fl = new ProductFilter();
        if (!idFilter.getText().isEmpty()) {
            try {
                int id = Integer.parseInt(idFilter.getText());
                if (id >= 0) {
                    listId.add(id);
                    fl.setIds(listId);
                } else {
                    statusLabel.setText("Incorrect product ID.");
                }
            } catch (NumberFormatException e) {
                statusLabel.setText("Incorrect product ID.");
            }
        }

        if (!priceFromFilter.getText().isEmpty()) {
            try {
                double price = Double.parseDouble(priceFromFilter.getText());
                if (price >= 0) {
                    fl.setFromPrice(price);
                } else {
                    statusLabel.setText("Incorrect price \"from\".");
                }
            } catch (NumberFormatException e) {
                statusLabel.setText("Incorrect price \"from\".");
            }
        }

        if (!priceToFilter.getText().isEmpty()) {
            try {
                double price = Double.parseDouble(priceToFilter.getText());
                if (price >= 0) {
                    fl.setToPrice(price);
                } else {
                    statusLabel.setText("Incorrect price \"to\".");
                }
            } catch (NumberFormatException e) {
                statusLabel.setText("Incorrect price \"to\".");
            }
        }

        if (!manufacturerFilter.getText().isEmpty()) {
            fl.setManufacturer(manufacturerFilter.getText());
        }

        if (!groupIdFilter.getText().isEmpty()) {
            try {
                int gr_id = Integer.parseInt(groupIdFilter.getText());
                if (gr_id >= 0) {
                    fl.setGroup_id(gr_id);
                } else {
                    statusLabel.setText("Incorrect group ID.");
                }
            } catch (NumberFormatException e) {
                statusLabel.setText("Incorrect group ID.");
            }
        }

        showFilteredP(fl);
        idFilter.clear();
        priceToFilter.clear();
        priceFromFilter.clear();
        manufacturerFilter.clear();
        groupIdFilter.clear();
    }


    @FXML
    void toGroupList() throws MalformedURLException {
        new FXMLLoader();
        Stage stage = (Stage) idFilter.getScene().getWindow();
        URL url;

        url = new File("src/main/java/client_server/client/views/groupL.fxml").toURI().toURL();

        Parent root = null;
        try {
            root = FXMLLoader.load(url);
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Can't open groups.");
        }
        assert root != null;
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }


    @FXML
    void logOut() throws MalformedURLException {
        LoginWindowControl.logOut(deleteProductBtn);
    }



    @FXML
    void showAllProd() {
        statusLabel.setText("");
        resetTable();
    }

    @FXML
    void initialize() {
        if (!GlobalContext.role.equals("admin")) {
            addNewProductBtn.setDisable(true);
            deleteProductBtn.setDisable(true);
            updateProductBtn.setDisable(true);
            addUserBtn.setVisible(false);
        }

        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        descrCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        manufacturerCol.setCellValueFactory(new PropertyValueFactory<>("manufacturer"));
        groupIdCol.setCellValueFactory(new PropertyValueFactory<>("group_id"));

        resetTable();
    }

    private void resetTable() {
        ProductFilter fl = new ProductFilter();
        showFilteredP(fl);
    }


    private void showFilteredP(ProductFilter fl) {
        JSONObject jsonObj = new JSONObject("{" + "\"page\":" + 0 + ", \"size\":" + 1000 +
                ", \"productFilter\":" + fl.toJSON().toString() + "}");
        Message msg = new Message(GET_LIST_PRODUCTS.ordinal(), 1, jsonObj.toString().getBytes(StandardCharsets.UTF_8));

        Packet packet = new Packet((byte) 1, UnsignedLong.valueOf(GlobalContext.packetId++), msg);

        Packet receivedPacket = GlobalContext.clientTCP.sendPacket(packet.toPacket());

        int command = receivedPacket.getBMsq().getcType();
        Message.cTypes[] val = Message.cTypes.values();
        Message.cTypes command_type = val[command];


        if (command_type == GET_LIST_PRODUCTS) {
            String message = new String(receivedPacket.getBMsq().getMessage(), StandardCharsets.UTF_8);
            JSONObject information = new JSONObject(message);
            try {
                JSONObject list = information.getJSONObject("object");
                JSONArray array = list.getJSONArray("list");

                List<Product> products = new ArrayList<>();

                for (int i = 0; i < array.length(); i++) {
                    products.add(new Product(array.getJSONObject(i)));
                }

                productsTable.getItems().clear();
                productsTable.getItems().addAll(products);

            } catch (JSONException e) {
                e.printStackTrace();
                statusLabel.setText("Failed to get list of products!");
            }

        } else {
            statusLabel.setText("Failed to get list of products!");
        }
    }
}