package ShopingManagement;


import java.sql.*;
import java.util.Scanner;

public class ShopingManage {

    // JDBC connection details
    final private static String URL = "jdbc:mysql://localhost:3306/shoping";
    final private static String USER = "root";
    final private static String PASSWORD = "shr123";
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("Connected to the database.");

            while (true) {
                scanner.nextLine(); 
                System.out.print("\033[H\033[2J");
                System.out.println("\n--- Shopping Management System ---");
                System.out.println("1. Add Product");
                System.out.println("2. Add Customer");
                System.out.println("3. Create Order");
                System.out.println("4. View Products");
                System.out.println("5. View Customers");
                System.out.println("6. View Orders");
                System.out.println("7. Exit");
                System.out.print("Please choose an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine();  // Consume newline character

                switch (choice) {
                    case 1:
                        addProduct(connection, scanner);
                        break;
                    case 2:
                        addCustomer(connection, scanner);
                        break;
                    case 3:
                        createOrder(connection, scanner);
                        break;
                    case 4:
                        viewProducts(connection);
                        break;
                    case 5:
                        viewCustomers(connection);
                        break;
                    case 6:
                        viewOrders(connection);
                        break;
                    case 7:
                        System.out.println("Exiting...");
                        scanner.close();
                        return;
                    default:
                        System.out.println("Invalid choice, please try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Add a product to the database
    // System.out.print("\033[H\033[2J");
    // System.out.flush();
    public static void addProduct(Connection connection, Scanner scanner) throws SQLException {
        //For clear Screen 

        System.out.print("\033[H\033[2J");
       
        System.out.print("Enter Product Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Product Price: ");
        double price = scanner.nextDouble();
        System.out.print("Enter Stock Quantity: ");
        int stock = scanner.nextInt();
        scanner.nextLine(); 

        String sql = "INSERT INTO Productdetails(Product_Name, Product_Price, Product_Quantity) VALUES (?, ?, ?)";
        try (PreparedStatement st1 = connection.prepareStatement(sql)) {
            st1.setString(1, name);
            st1.setDouble(2, price);
            st1.setInt(3, stock);
            st1.executeUpdate();
            System.out.println("Product added successfully.");
            scanner.nextLine(); 
            System.out.print("\033[H\033[2J");
        }
    }

    // Add a customer to the database

    public static void addCustomer(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("\033[H\033[2J");
        System.out.print("Enter customer name: ");
        String name = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter phone number: ");
        String phone = scanner.nextLine();

        String sql = "INSERT INTO Customerdetails (customer_Name, Email, Phone_number) VALUES (?, ?, ?)";
        try (PreparedStatement st2 = connection.prepareStatement(sql)) {
            st2.setString(1, name);
            st2.setString(2, email);
            st2.setString(3, phone);
            st2.executeUpdate();
            System.out.println("\n\nCustomer added successfully.");
            scanner.nextLine(); 
            System.out.print("\033[H\033[2J");
        }
    }

    // Create  order for a customer
    public static void createOrder(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("\033[H\033[2J");
        System.out.print("Enter customer email: ");
        String email = scanner.nextLine();

        int customerId = getCustomerIdByEmail(connection, email);
        if (customerId == -1) {
            System.out.println("Customer not found.");
            return;
        }

        System.out.print("Enter total order amount: ");
        double totalAmount = scanner.nextDouble();
        scanner.nextLine();    //........try this

        String orderSql = "INSERT INTO Orderdetails (Customer_ID, Total_Amount) VALUES (?, ?)";
        try (PreparedStatement st3 = connection.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
            st3.setInt(1, customerId);
            st3.setDouble(2, totalAmount);
            st3.executeUpdate();

            // Get the generated order ID
            ResultSet rs = st3.getGeneratedKeys();
            if (rs.next()) {
                int orderitem_Id = rs.getInt(1);
                System.out.println("Order created with ID: " + orderitem_Id);

                // Add order items (simplified)
                System.out.print("Enter product ID for the order item: ");
                int productId = scanner.nextInt();
                System.out.print("Enter quantity: ");
                int quantity = scanner.nextInt();
                scanner.nextLine(); 

                // Retrieve product price
                double productPrice = getProductPrice(connection, productId);
                if (productPrice == -1) {
                    System.out.println("Product not found.");
                    return;
                }

                String orderItemSql = "INSERT INTO OrderItems (Product_ID, Quantity, Price) VALUES (?, ?, ?)";
                try (PreparedStatement st4 = connection.prepareStatement(orderItemSql)) {
                  //  st4.setInt(1, orderitem_Id);
                    st4.setInt(1, productId);
                    st4.setInt(2, quantity);
                    st4.setDouble(3, productPrice);
                   // st4.setInt(4, Order_id);
                    st4.executeUpdate();
                    System.out.println("\n\nOrder item added successfully.");
                    scanner.nextLine(); 
                    System.out.print("\033[H\033[2J");
                }
            }
        }
    }

    // Get customer ID by email
    public static int getCustomerIdByEmail(Connection connection, String email) throws SQLException {
        String sql = "SELECT Customer_ID FROM Customerdetails WHERE Email = ?";
        try (PreparedStatement st5 = connection.prepareStatement(sql)) {
            st5.setString(1, email);
            ResultSet rs = st5.executeQuery();
            if (rs.next()) {
                return rs.getInt("Customer_ID");
            }
        }
        return -1;
    }

    // Get product price by product ID
    public static double getProductPrice(Connection connection, int productId) throws SQLException {
        String sql = "SELECT Product_Price FROM Productdetails WHERE Product_ID = ?";
        try (PreparedStatement st6 = connection.prepareStatement(sql)) {
            st6.setInt(1, productId);
            ResultSet rs = st6.executeQuery();
            if (rs.next()) {
                return rs.getDouble("Product_Price");
            }
        }
        return -1;
    }

    // View all products
    public static void viewProducts(Connection connection) throws SQLException {
        System.out.print("\033[H\033[2J");
        String sql = "SELECT * FROM Productdetails";
        try (Statement st7 = connection.createStatement(); ResultSet rs = st7.executeQuery(sql)) {
            System.out.println("\n--- Products ---");
            while (rs.next()) {
                System.out.println("\nID: " + rs.getInt("product_id") + ",\n Name: " + rs.getString("product_name") +
                        ",\n Price: $" + rs.getDouble("Product_price") + ",\n Stock: " + rs.getInt("Product_quantity"));
                             

                        
            }
           
        }
    }

    // View all customers
    public static void viewCustomers(Connection connection) throws SQLException {
        System.out.print("\033[H\033[2J");
        String sql = "SELECT * FROM Customerdetails";
        try (Statement st8 = connection.createStatement(); ResultSet rs = st8.executeQuery(sql)) {
            System.out.println("\n--- Customers ---");
            while (rs.next()) {
                System.out.println("\nID: " + rs.getInt("customer_id") + ", Name: " + rs.getString("customer_name") +
                        ", Email: " + rs.getString("email") + ", Phone: " + rs.getString("phone_number"));
            }
        }
    }

    // View all orders
    public static void viewOrders(Connection connection) throws SQLException {
        System.out.print("\033[H\033[2J");
        String sql = "SELECT o.Order_ID, c.Customer_Name, o.Total_Amount, o.Order_date FROM Orderdetails o JOIN Customerdetails c ON o.Customer_ID = c.Customer_ID";
        try (Statement st9 = connection.createStatement(); ResultSet rs = st9.executeQuery(sql)) {
            System.out.println("\n--- Orders ---");
            while (rs.next()) {
                System.out.println("\nOrder ID: " + rs.getInt("order_id") + ", Customer: " + rs.getString("customer_name") +
                        ", Total Amount: $" + rs.getDouble("total_amount") + ", Date: " + rs.getTimestamp("order_date"));
            }
        }
    }
}