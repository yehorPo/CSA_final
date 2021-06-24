package client_server.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@Data
@NoArgsConstructor(staticName = "empty")
@AllArgsConstructor
public class Product {

    private Integer id;
    private String name;
    private double price;
    private double amount;
    private String description;
    private String manufacturer;
    private Integer group_id;

    public Product(JSONObject product) {
        this.id = product.getInt("id");
        this.name = product.getString("name");
        this.price = product.getDouble("price");
        this.amount = product.getDouble("amount");
        this.description = product.getString("description");
        this.manufacturer = product.getString("manufacturer");
        this.group_id = product.getInt("group_id");
    }

    public Product(String name, double price, double amount, String description, String manufacturer, Integer group_id) {
        this.name = name;
        this.price = price;
        this.amount = amount;
        this.description = description;
        this.manufacturer = manufacturer;
        this.group_id = group_id;
    }

    public JSONObject toJSON() {

        JSONObject json = new JSONObject("{" + "\"id\":" + id + ", \"name\":\"" + name +
                "\", \"price\":" + price + ", \"amount\":" + amount +
                ", \"description\":\"" + description + "\", \"manufacturer\":\"" + manufacturer +
                "\", \"group_id\":" + group_id + "}");

        return json;
    }

    public static JSONObject toJSONObject(List<Product> products){
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append("{\"list\":[");

        for (Product g: products) {
            stringBuffer.append(g.toJSON().toString() + ", ");
        }
        stringBuffer.delete(stringBuffer.length()-1, stringBuffer.length()-1);
        stringBuffer.append("]}");
        return new JSONObject(stringBuffer.toString());
    }

    @Override
    public String toString() {
        return "Product(" + "id=" + id + ", name=" + name + ", price=" + price +
                ", amount=" + amount + ", description=" + description +
                ", manufacturer=" + manufacturer + ", group_id=" + group_id + ')';
    }

    public boolean equals(Product p) {
        if (this.id.equals(p.getId()) && this.name.equals(p.getName()) && this.amount == p.getAmount()
                && this.price == p.getPrice() && this.description.equals(p.getDescription())
                && this.manufacturer.equals(p.getManufacturer()) && this.group_id.equals(p.getGroup_id())) {
            return true;
        }
        return false;
    }
}

