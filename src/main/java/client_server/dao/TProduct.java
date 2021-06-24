package client_server.dao;

import client_server.domain.Group;
import client_server.domain.Product;
import client_server.domain.ProductFilter;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TProduct {

    private final Connection connection;
    public String database = "file.db";

    public TProduct(final String dbFile) {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
        } catch (ClassNotFoundException e) {
            System.out.println("Can't load SQLite JDBC class");
            throw new RuntimeException("Can't find class", e);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        initTable();
    }

    private void initTable() {

        try (final Statement statement = connection.createStatement()) {
            String query = "create table if not exists 'products' " +
                    "('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'name' text not null, 'price' decimal not null," +
                    " 'amount' decimal not null, 'description' text not null, 'manufacturer' text not null, " +
                    "'group_id' integer not null, foreign key(group_id) references groups(id) ON UPDATE CASCADE ON DELETE CASCADE);";
            statement.execute(query);
        } catch (SQLException e) {
            throw new RuntimeException("Can't create products table", e);
        }

    }

    public Product getProduct(final int id) {
        try (final Statement statement = connection.createStatement()) {

            final String sql = String.format("select id, name, ROUND(price, 2) as price, ROUND(amount, 3) as amount, description, " +
                    "manufacturer, group_id from 'products' where id = %s", id);
            final ResultSet resultSet = statement.executeQuery(sql);

            Product product = new Product(resultSet.getInt("id"),
                    resultSet.getString("name"),
                    resultSet.getDouble("price"),
                    resultSet.getDouble("amount"),
                    resultSet.getString("description"),
                    resultSet.getString("manufacturer"),
                    resultSet.getInt("group_id"));
            return product;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int insertProduct(final Product product) {
        TGroup tGroup = new TGroup(database);
        Group group = tGroup.getGroup(product.getGroup_id());

        if (isNameUnique(product.getName()) && group != null) {
            String query = "insert into 'products'" + " ('name', 'price', 'amount', 'description', 'manufacturer', 'group_id') values (?, ?, ?, ?, ?, ?);";
            try (final PreparedStatement insertStatement = connection.prepareStatement(query)) {

                insertStatement.setString(1, product.getName());
                insertStatement.setDouble(2, product.getPrice());
                insertStatement.setDouble(3, product.getAmount());
                insertStatement.setString(4, product.getDescription());
                insertStatement.setString(5, product.getManufacturer());
                insertStatement.setInt(6, product.getGroup_id());

                insertStatement.execute();

                final ResultSet result = insertStatement.getGeneratedKeys();
                return result.getInt("last_insert_rowid()");
            } catch (SQLException e) {
                e.printStackTrace();
                return -1;
            }
        }
        return -1;
    }

    public int updateProduct(Product product){
        Product product1 = getProductByName(product.getName());

        TGroup tGroup = new TGroup(database);
        Group group = tGroup.getGroup(product.getGroup_id());

        if((product1 == null || product.getId()==product1.getId())
                && (product.getAmount()>=0) && (product.getPrice()>=0) && group != null){
            try (final PreparedStatement preparedStatement =
                         connection.prepareStatement("update 'products' set name = ?, price = ?, amount = ?, description = ?, manufacturer = ?, group_id = ?  where id = ?")) {
                preparedStatement.setString(1, product.getName());
                preparedStatement.setDouble(2, product.getPrice());
                preparedStatement.setDouble(3, product.getAmount());
                preparedStatement.setString(4, product.getDescription());
                preparedStatement.setString(5, product.getManufacturer());
                preparedStatement.setInt(6, product.getGroup_id());
                preparedStatement.setDouble(7, product.getId());
                preparedStatement.executeUpdate();
                return product.getId();
            } catch (SQLException e) {
                e.printStackTrace();
                return -1;
            }
        }
        else{
            return -1;
        }
    }

    public int deleteProduct(int id) {

        try (final PreparedStatement preparedStatement = connection.prepareStatement("delete from 'products' where id = ?")) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();

            return id;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean isNameUnique(final String productName) {
        try (final Statement statement = connection.createStatement()) {
            final ResultSet resultSet = statement.executeQuery(
                    String.format("select count(*) as num_of_products from 'products' where name = '%s'", productName)
            );
            resultSet.next();
            return resultSet.getInt("num_of_products") == 0;
        } catch (SQLException e) {
            throw new RuntimeException("Can't check name", e);
        }
    }

    public List<Product> getAll(final int page, final int size) {
        return getList(page, size, new ProductFilter());
    }

    public void deleteAll() {
        try (final Statement statement = connection.createStatement()) {
            String query = "delete from 'products'";
            statement.execute(query);
        } catch (SQLException e) {
            throw new RuntimeException("Can't delete products", e);
        }
    }

    public void deleteTable() {
        try (final Statement statement = connection.createStatement()) {
            String query = "drop table 'products'";
            statement.execute(query);
        } catch (SQLException e) {
            throw new RuntimeException("Can't delete table", e);
        }
    }

    public List<Product> getList(final int page, final int size, final ProductFilter filter) {
        try (final Statement statement = connection.createStatement()) {
            final String query = Stream.of(
                    in("id", filter.getIds()),
                    gte("price", filter.getFromPrice()),
                    lte("price", filter.getToPrice()),
                    manufacturer("manufacturer", filter.getManufacturer()),
                    group("group_id", filter.getGroup_id())
            )
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(" AND "));

            final String where = query.isEmpty() ? "" : " where " + query;
            final String sql = String.format("select id, name, ROUND(price, 2) as price, ROUND(amount,3) as amount, description, manufacturer, group_id " +
                    "from 'products' %s limit %s offset %s", where, size, page * size);
            final ResultSet resultSet = statement.executeQuery(sql);

            final List<Product> products = new ArrayList<>();
            while (resultSet.next()) {
                products.add(new Product(resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getDouble("price"),
                        resultSet.getDouble("amount"),
                        resultSet.getString("description"),
                        resultSet.getString("manufacturer"),
                        resultSet.getInt("group_id")));
            }
            return products;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    public Product getProductByName(final String name) {
        try (final Statement statement = connection.createStatement()) {

            final String sql = String.format("select id, name, price, amount, description, " +
                    "manufacturer, group_id from 'products' where name = '%s'", name);
            final ResultSet resultSet = statement.executeQuery(sql);

            Product product = new Product(resultSet.getInt("id"),
                    resultSet.getString("name"),
                    resultSet.getDouble("price"),
                    resultSet.getDouble("amount"),
                    resultSet.getString("description"),
                    resultSet.getString("manufacturer"),
                    resultSet.getInt("group_id"));
            return product;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String in(final String fieldName, final Collection<?> collection) {
        if (collection == null || collection.isEmpty()) {
            return null;
        }
        return fieldName + " IN (" + collection.stream().map(Objects::toString).collect(Collectors.joining(", ")) + ")";
    }

    private static String gte(final String fieldName, final Double value) {
        if (value == null) {
            return null;
        }
        return fieldName + " >= " + value;
    }

    private static String manufacturer(final String fieldName, final String value) {
        if (value == null) {
            return null;
        }
        return fieldName + " LIKE '%" + value + "%'";
    }

    private static String group(final String fieldName, final Integer value) {
        if (value == null) {
            return null;
        }
        return fieldName + " = " + value;
    }

    private static String lte(final String fieldName, final Double value) {
        if (value == null) {
            return null;
        }
        return fieldName + " <= " + value;
    }

    public int deleteAllInGroup(int group_id) {
        try (final Statement statement = connection.createStatement()) {
            String query = String.format("delete from 'products' where group_id = '%s'", group_id);
            statement.execute(query);
            return 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

}
