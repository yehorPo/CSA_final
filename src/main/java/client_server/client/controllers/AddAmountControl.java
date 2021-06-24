package client_server.client.controllers;
import client_server.domain.Product;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class AddAmountControl {

    private Product product;

    @FXML
    private TextField amountField;

    @FXML
    private Label statusLabel;

    @FXML
    void addAmount() {
        if(amountField.getText().isEmpty()){
            statusLabel.setText("Fill out the field before adding.");
        }else{
            Double amount = null;
            try{
                amount = Double.parseDouble(amountField.getText());
            }catch(NumberFormatException e){
                statusLabel.setText("Incorrect amount.");
            }
            if(amount>=0){

                Product productToUpdate = new Product(product.getId(), product.getName(), product.getPrice(), product.getAmount()+amount, product.getDescription(), product.getManufacturer(), product.getGroup_id());
                UpdateProdControl.updateProd(productToUpdate, statusLabel);

            }else{
                statusLabel.setText("Incorrect amount.");
            }
        }
    }

    @FXML
    void initialize() {

    }

    public void initData(Product selectedItem) {
        product = selectedItem;
    }
}
