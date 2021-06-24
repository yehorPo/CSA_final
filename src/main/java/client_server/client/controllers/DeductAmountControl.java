package client_server.client.controllers;

import client_server.domain.Product;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class DeductAmountControl {

    private Product product;

    @FXML
    private TextField amountField;

    @FXML
    private Label statusLabel;

    @FXML
    void deductAmount() {
        if(amountField.getText().isEmpty()){
            statusLabel.setText("Fill out the field before deducting.");
        }else{
            Double amount = null;
            try{
                amount = Double.parseDouble(amountField.getText());
            }catch(NumberFormatException e){
                statusLabel.setText("Incorrect amount.");
            }
            if (amount < 0 || !(amount <= product.getAmount())) {
                statusLabel.setText("Incorrect amount.");
            } else {

                Product productToUpdate = new Product(product.getId(), product.getName(), product.getPrice(), product.getAmount()-amount, product.getDescription(), product.getManufacturer(), product.getGroup_id());
                UpdateProdControl.updateProd(productToUpdate, statusLabel);

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
