import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * Order Management System
 * B.Tech 3rd Semester OOPJ Practical Examination Project
 *
 * Core Java + JDBC + MySQL Console Application
 */
public class Main {

    // ====================================================================
    // DATABASE CONFIGURATION
    // ====================================================================
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "ecommerce_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "2518"; // Set your MySQL password here

    private static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME +
            "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true";

    /**
     * Establishes and returns a connection to the MySQL database.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * Automatically ensures the Orders table exists and has sample records.
     * Prevents "Table 'ecommerce_db.orders' doesn't exist" errors.
     */
    public static void initializeDatabase(Connection conn) {
        String createTableSql = "CREATE TABLE IF NOT EXISTS Orders (" +
                "order_id INT PRIMARY KEY, " +
                "customer_name VARCHAR(100) NOT NULL, " +
                "product_name VARCHAR(100) NOT NULL, " +
                "quantity INT NOT NULL, " +
                "unit_price DOUBLE NOT NULL, " +
                "status VARCHAR(30) NOT NULL" +
                ")";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createTableSql);

            // Check if table is empty; if so, populate initial sample records
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Orders");
            if (rs.next() && rs.getInt(1) == 0) {
                String insertSamplesSql = "INSERT INTO Orders (order_id, customer_name, product_name, quantity, unit_price, status) VALUES " +
                        "(101, 'Aarav Sharma', 'Gaming Laptop', 1, 65000.00, 'Delivered'), " +
                        "(102, 'Priya Patel', 'Wireless Mouse', 3, 750.50, 'Pending'), " +
                        "(103, 'Rohan Verma', 'Mechanical Keyboard', 4, 2800.00, 'Shipped'), " +
                        "(104, 'Sneha Gupta', 'Noise-Cancelling Headphones', 1, 4500.00, 'Cancelled'), " +
                        "(105, 'Vikram Singh', 'USB-C Fast Charger', 5, 1200.00, 'Pending'), " +
                        "(106, 'Ananya Das', 'External SSD 1TB', 3, 5600.00, 'Cancelled')";
                stmt.executeUpdate(insertSamplesSql);
                System.out.println("System Notice: Orders table was auto-created and populated with sample records.");
            }
        } catch (SQLException e) {
            System.out.println("Notice during database check: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Connection conn = null;

        System.out.println("==================================================");
        System.out.println("   Welcome to Order Management System (JDBC)      ");
        System.out.println("==================================================");

        // Attempt initial connection to verify MySQL credentials and table
        try {
            conn = getConnection();
            initializeDatabase(conn);
            System.out.println("Status: Connected to MySQL database successfully!\n");
        } catch (SQLException e) {
            System.out.println("Notice: Could not connect to MySQL database.");
            System.out.println("Reason: " + e.getMessage());
            System.out.println("Please verify MySQL is running and DB_PASSWORD is correct in Main.java.\n");
        }

        boolean running = true;
        while (running) {
            System.out.println("========== ORDER MANAGEMENT SYSTEM ==========");
            System.out.println("1. Insert Orders");
            System.out.println("2. Search Orders by Status");
            System.out.println("3. Increase Order Quantity");
            System.out.println("4. Update Order Status");
            System.out.println("5. Calculate Total Value and Delete Cancelled Order");
            System.out.println("6. See All Orders");
            System.out.println("7. Exit");

            int choice = readInt(scanner, "Enter your choice: ");

            // If user selects any DB operation (1 to 6), ensure active connection
            if (choice >= 1 && choice <= 6) {
                if (conn == null || isClosed(conn)) {
                    try {
                        conn = getConnection();
                        initializeDatabase(conn);
                    } catch (SQLException e) {
                        System.out.println("\nDatabase Connection Error: " + e.getMessage());
                        System.out.println("Please ensure MySQL is running and DB_PASSWORD is correct in Main.java.\n");
                        continue;
                    }
                }
            }

            switch (choice) {
                case 1:
                    insertOrders(conn, scanner);
                    break;
                case 2:
                    searchOrdersByStatus(conn, scanner);
                    break;
                case 3:
                    increaseOrderQuantity(conn, scanner);
                    break;
                case 4:
                    updateOrderStatus(conn, scanner);
                    break;
                case 5:
                    calculateTotalAndDeleteCancelled(conn, scanner);
                    break;
                case 6:
                    seeAllOrders(conn);
                    break;
                case 7:
                    System.out.println("\nExiting application gracefully. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice! Please select an option between 1 and 7.\n");
                    break;
            }
        }

        // Close the database connection properly upon exit
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                    System.out.println("Database connection closed properly.");
                }
            } catch (SQLException e) {
                System.out.println("Error while closing database connection: " + e.getMessage());
            }
        }

        scanner.close();
    }

    // ====================================================================
    // OPERATION 1: INSERT ORDERS (QUESTION A)
    // ====================================================================
    private static void insertOrders(Connection conn, Scanner scanner) {
        System.out.println("\n--- Option 1: Insert Orders ---");
        System.out.println("1. Enter custom order details manually");
        System.out.println("2. Insert 3 default sample orders automatically");
        int subChoice = readInt(scanner, "Choose insertion mode (1 or 2): ");

        if (subChoice == 1) {
            int orderId = readInt(scanner, "Enter Order ID: ");

            // Check if order ID already exists before inserting
            if (orderExists(conn, orderId)) {
                System.out.println("Error: Order with ID " + orderId + " already exists! Duplicate order ID is not allowed.\n");
                return;
            }

            String customerName = readString(scanner, "Enter Customer Name: ");
            String productName = readString(scanner, "Enter Product Name: ");
            int quantity = readPositiveInt(scanner, "Enter Quantity: ");
            double unitPrice = readPositiveDouble(scanner, "Enter Unit Price: ");
            String status = readString(scanner, "Enter Status (Pending, Shipped, Delivered, Cancelled): ");

            insertSingleOrder(conn, orderId, customerName, productName, quantity, unitPrice, status);
        } else if (subChoice == 2) {
            System.out.println("\nInserting 3 sample order records...");
            insertSingleOrder(conn, 201, "Amit Kumar", "Gaming Headset", 3, 2499.00, "Pending");
            insertSingleOrder(conn, 202, "Neha Sharma", "Smart Watch", 4, 3999.00, "Shipped");
            insertSingleOrder(conn, 203, "Rajesh Nair", "Bluetooth Speaker", 2, 1899.00, "Delivered");
        } else {
            System.out.println("Invalid option! Returning to main menu.\n");
        }
    }

    private static void insertSingleOrder(Connection conn, int orderId, String customerName,
                                          String productName, int quantity, double unitPrice, String status) {
        String insertSql = "INSERT INTO Orders (order_id, customer_name, product_name, quantity, unit_price, status) " +
                           "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            pstmt.setInt(1, orderId);
            pstmt.setString(2, customerName);
            pstmt.setString(3, productName);
            pstmt.setInt(4, quantity);
            pstmt.setDouble(5, unitPrice);
            pstmt.setString(6, status);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Success: Order #" + orderId + " (" + productName + ") inserted successfully!");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062 || "23000".equals(e.getSQLState())) {
                System.out.println("Error: Order with ID " + orderId + " already exists!");
            } else {
                System.out.println("Database Error during insertion: " + e.getMessage());
            }
        }
    }

    // ====================================================================
    // OPERATION 2: SEARCH ORDERS (QUESTION B)
    // ====================================================================
    private static void searchOrdersByStatus(Connection conn, Scanner scanner) {
        System.out.println("\n--- Option 2: Search Orders by Status ---");
        String searchStatus = readString(scanner, "Enter Status to search (e.g., Pending, Shipped, Delivered, Cancelled): ");

        String sql = "SELECT * FROM Orders WHERE status = ? AND quantity > 2 ORDER BY unit_price DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, searchStatus);

            try (ResultSet rs = pstmt.executeQuery()) {
                boolean hasRecords = false;

                while (rs.next()) {
                    if (!hasRecords) {
                        System.out.println("\nMatching Orders (Status = '" + searchStatus + "' AND Quantity > 2, Sorted by Unit Price DESC):");
                        System.out.println("---------------------------------------------------------------------------------------------------------------");
                        System.out.printf("%-10s %-20s %-30s %-10s %-15s %-12s%n",
                                "Order ID", "Customer Name", "Product Name", "Quantity", "Unit Price", "Status");
                        System.out.println("---------------------------------------------------------------------------------------------------------------");
                        hasRecords = true;
                    }
                    System.out.printf("%-10d %-20s %-30s %-10d %-15.2f %-12s%n",
                            rs.getInt("order_id"),
                            rs.getString("customer_name"),
                            rs.getString("product_name"),
                            rs.getInt("quantity"),
                            rs.getDouble("unit_price"),
                            rs.getString("status"));
                }

                if (!hasRecords) {
                    System.out.println("No matching orders found with status '" + searchStatus + "' and quantity > 2.");
                } else {
                    System.out.println("---------------------------------------------------------------------------------------------------------------");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database Error while searching orders: " + e.getMessage());
        }
        System.out.println();
    }

    // ====================================================================
    // OPERATION 3: INCREASE ORDER QUANTITY (QUESTION C)
    // ====================================================================
    private static void increaseOrderQuantity(Connection conn, Scanner scanner) {
        System.out.println("\n--- Option 3: Increase Order Quantity ---");
        int orderId = readInt(scanner, "Enter Order ID: ");
        int addQuantity = readPositiveInt(scanner, "Enter Additional Quantity to add: ");

        String updateSql = "UPDATE Orders SET quantity = quantity + ? WHERE order_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
            pstmt.setInt(1, addQuantity);
            pstmt.setInt(2, orderId);

            int rowsAffected = pstmt.executeUpdate();

            // Check whether order exists using affected row count
            if (rowsAffected == 0) {
                System.out.println("Error: Order with ID " + orderId + " does not exist.\n");
            } else {
                // Retrieve and display updated quantity
                String selectSql = "SELECT quantity FROM Orders WHERE order_id = ?";
                try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                    selectStmt.setInt(1, orderId);
                    try (ResultSet rs = selectStmt.executeQuery()) {
                        if (rs.next()) {
                            int updatedQuantity = rs.getInt("quantity");
                            System.out.println("Success: Quantity increased by " + addQuantity + " for Order #" + orderId + ".");
                            System.out.println("Updated Quantity: " + updatedQuantity + "\n");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Database Error while updating quantity: " + e.getMessage() + "\n");
        }
    }

    // ====================================================================
    // OPERATION 4: UPDATE ORDER STATUS (QUESTION D)
    // ====================================================================
    private static void updateOrderStatus(Connection conn, Scanner scanner) {
        System.out.println("\n--- Option 4: Update Order Status ---");
        int orderId = readInt(scanner, "Enter Order ID: ");

        // Step 1: First retrieve and verify that the order exists
        String selectSql = "SELECT * FROM Orders WHERE order_id = ?";
        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setInt(1, orderId);

            try (ResultSet rs = selectStmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Error: Order with ID " + orderId + " does not exist.\n");
                    return;
                }

                // Retrieve current order details
                String customerName = rs.getString("customer_name");
                String productName = rs.getString("product_name");
                int quantity = rs.getInt("quantity");
                double unitPrice = rs.getDouble("unit_price");
                String currentStatus = rs.getString("status");

                System.out.println("Order Found: #" + orderId + " | " + productName +
                        " | Customer: " + customerName + " | Current Status: " + currentStatus);

                // Step 2: Accept new status and validate (Only: Pending, Shipped, Delivered)
                String newStatusInput = readString(scanner, "Enter New Status (Pending, Shipped, Delivered): ");

                String validStatus = null;
                if ("pending".equalsIgnoreCase(newStatusInput)) {
                    validStatus = "Pending";
                } else if ("shipped".equalsIgnoreCase(newStatusInput)) {
                    validStatus = "Shipped";
                } else if ("delivered".equalsIgnoreCase(newStatusInput)) {
                    validStatus = "Delivered";
                } else {
                    System.out.println("Error: Invalid status '" + newStatusInput + "'!");
                    System.out.println("Allowed statuses are ONLY: Pending, Shipped, Delivered.\n");
                    return;
                }

                // Step 3: Update status using PreparedStatement
                String updateSql = "UPDATE Orders SET status = ? WHERE order_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, validStatus);
                    updateStmt.setInt(2, orderId);
                    updateStmt.executeUpdate();

                    // Step 4: Display the updated order details
                    System.out.println("\nSuccess: Order status updated successfully!");
                    System.out.println("-------------------------------------------");
                    System.out.println("Order ID        : " + orderId);
                    System.out.println("Customer Name   : " + customerName);
                    System.out.println("Product Name    : " + productName);
                    System.out.println("Quantity        : " + quantity);
                    System.out.printf("Unit Price      : Rs. %.2f%n", unitPrice);
                    System.out.println("Previous Status : " + currentStatus);
                    System.out.println("Updated Status  : " + validStatus);
                    System.out.println("-------------------------------------------\n");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database Error while updating status: " + e.getMessage() + "\n");
        }
    }

    // ====================================================================
    // OPERATION 5: TOTAL ORDER VALUE AND DELETE (QUESTION E)
    // ====================================================================
    private static void calculateTotalAndDeleteCancelled(Connection conn, Scanner scanner) {
        System.out.println("\n--- Option 5: Total Order Value and Delete Cancelled Order ---");

        // PART 1: Calculate total order value using aggregate function SUM(quantity * unit_price)
        String sumSql = "SELECT SUM(quantity * unit_price) AS total_value FROM Orders";
        try (PreparedStatement sumStmt = conn.prepareStatement(sumSql);
             ResultSet rsSum = sumStmt.executeQuery()) {

            if (rsSum.next()) {
                double totalValue = rsSum.getDouble("total_value");
                if (rsSum.wasNull()) {
                    totalValue = 0.0;
                }
                System.out.println("---------------------------------------------------------");
                System.out.printf("Part 1: Total Order Value of all records: Rs. %.2f%n", totalValue);
                System.out.println("---------------------------------------------------------");
            }
        } catch (SQLException e) {
            System.out.println("Database Error calculating total value: " + e.getMessage());
        }

        // PART 2: Accept order ID and delete ONLY if status is Cancelled
        System.out.println("\n--- Part 2: Delete Cancelled Order ---");
        int orderId = readInt(scanner, "Enter Order ID to delete: ");

        String selectSql = "SELECT * FROM Orders WHERE order_id = ?";
        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setInt(1, orderId);

            try (ResultSet rs = selectStmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Error: Order with ID " + orderId + " does not exist.\n");
                    return;
                }

                String currentStatus = rs.getString("status");
                String productName = rs.getString("product_name");

                // Check if the order status is Cancelled
                if (!"cancelled".equalsIgnoreCase(currentStatus)) {
                    System.out.println("Error: Order #" + orderId + " (" + productName + ") cannot be deleted.");
                    System.out.println("Reason: Its current status is '" + currentStatus + "'. Only orders with status 'Cancelled' can be deleted.\n");
                    return;
                }

                // If status is Cancelled, perform deletion
                String deleteSql = "DELETE FROM Orders WHERE order_id = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                    deleteStmt.setInt(1, orderId);
                    int rowsDeleted = deleteStmt.executeUpdate();

                    if (rowsDeleted > 0) {
                        System.out.println("Success: Order #" + orderId + " ('" + productName + "', Status: Cancelled) has been deleted successfully.\n");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Database Error during deletion: " + e.getMessage() + "\n");
        }
    }

    // ====================================================================
    // OPERATION 6: SEE ALL ORDERS
    // ====================================================================
    private static void seeAllOrders(Connection conn) {
        System.out.println("\n--- See All Orders in Database ---");
        String sql = "SELECT * FROM Orders ORDER BY order_id ASC";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            boolean hasRecords = false;
            while (rs.next()) {
                if (!hasRecords) {
                    System.out.println("---------------------------------------------------------------------------------------------------------------");
                    System.out.printf("%-10s %-20s %-30s %-10s %-15s %-12s%n",
                            "Order ID", "Customer Name", "Product Name", "Quantity", "Unit Price", "Status");
                    System.out.println("---------------------------------------------------------------------------------------------------------------");
                    hasRecords = true;
                }
                System.out.printf("%-10d %-20s %-30s %-10d %-15.2f %-12s%n",
                        rs.getInt("order_id"),
                        rs.getString("customer_name"),
                        rs.getString("product_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("unit_price"),
                        rs.getString("status"));
            }

            if (!hasRecords) {
                System.out.println("No orders found in the database.");
            } else {
                System.out.println("---------------------------------------------------------------------------------------------------------------");
            }
        } catch (SQLException e) {
            System.out.println("Database Error while fetching all orders: " + e.getMessage());
        }
        System.out.println();
    }

    // ====================================================================
    // HELPER METHODS
    // ====================================================================

    /**
     * Checks if an order with the given ID already exists.
     */
    private static boolean orderExists(Connection conn, int orderId) {
        String sql = "SELECT order_id FROM Orders WHERE order_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Safely reads an integer from user input without crashing on invalid characters.
     */
    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid integer number.");
            }
        }
    }

    /**
     * Safely reads a positive integer (> 0).
     */
    private static int readPositiveInt(Scanner scanner, String prompt) {
        while (true) {
            int value = readInt(scanner, prompt);
            if (value > 0) {
                return value;
            }
            System.out.println("Value must be greater than zero. Please try again.");
        }
    }

    /**
     * Safely reads a positive double (> 0.0).
     */
    private static double readPositiveDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(input);
                if (value > 0) {
                    return value;
                }
                System.out.println("Price must be greater than zero. Please try again.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid decimal number.");
            }
        }
    }

    /**
     * Safely reads a non-empty string from user input.
     */
    private static String readString(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("Input cannot be blank. Please enter a valid value.");
        }
    }

    /**
     * Safely checks if the connection is null or closed.
     */
    private static boolean isClosed(Connection conn) {
        try {
            return conn == null || conn.isClosed();
        } catch (SQLException e) {
            return true;
        }
    }
}
