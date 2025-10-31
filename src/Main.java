import java.sql.*;
import java.util.*;

public class Main {
    private Scanner scanner;
    private String currentUser;

    public Main(Scanner scanner) {
        this.scanner = scanner;
    }

    public void appRun() {
        clear();
        System.out.println(" === Welcome to Mini E-Commerce ===");
        while (true) {
            System.out.println("\n 1. Register");
            System.out.println(" 2. Login");
            System.out.println(" 3. Exit");

            String choice = showUserPrompt("\n Choose an option: ");

            if (choice.isBlank()) {
                clear();
                System.out.println(" Input cannot be empty!");
                continue;
            }

            switch (choice) {
                case "1" -> {
                    clear();
                    registerUser();
                }
                case "2" -> {
                    if (loginUser()) {
                        showMenu();
                    }
                }
                case "3" -> {
                    clear();
                    System.out.println("Thanks for using our Mini-Ecommerce!");
                    return;
                }
                default -> {
                    clear();
                    System.out.println(" Invalid option, try again.");
                }
            }
        }
    }

    private void clear() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            System.out.println("Unable to clear console.");
        }
    }

    private String showUserPrompt(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private boolean validateInput(String input, String fieldName) {
        if (input == null || input.trim().isEmpty()) {
            System.out.println(" " + fieldName + " cannot be empty!");
            return false;
        }
        if (input.length() < 4) {
            System.out.println(" " + fieldName + " must be at least 4 characters!");
            return false;
        }
        if (!input.matches("[A-Za-z]+") && fieldName.equals("Username")) {
            System.out.println(" Please input valid characters!");
            return false;
        }
        return true;
    }

    private void registerUser() {
        clear();
        System.out.println("\n                 -USER REGISTRATION-                ");
        String username, password;

        while (true) {
            username = showUserPrompt("\n Enter username: ");
            if (!validateInput(username, "Username")) continue;

            if (userExists(username)) {
                System.out.println(" Username already exists. Try another.");
                continue;
            }
            break;
        }

        while (true) {
            password = showUserPrompt(" Enter password: ");
            if (validateInput(password, "Password")) break;
        }

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)")) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();
            clear();
            System.out.println("\n Registration successful! You can now log in.");
        } catch (SQLException e) {
            System.out.println(" Registration failed: " + e.getMessage());
        }
    }

    private boolean userExists(String username) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username = ?")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println(" DB Error: " + e.getMessage());
            return false;
        }
    }

    private boolean loginUser() {
        clear();
        System.out.println("\n                     -LOGIN-                        ");

        String username = showUserPrompt("\n Username: ");
        String password = showUserPrompt(" Password: ");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?")) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                currentUser = username;
                clear();
                System.out.println(" ==================================================");
                System.out.println("                 WELCOME, " + username.toUpperCase() + "!           ");
                System.out.println(" ==================================================");
                System.out.println(" Login successful! Redirecting to the main menu...");
                return true;
            } else {
                clear();
                System.out.println(" Invalid credentials. Please try again.\n");
                return false;
            }
        } catch (SQLException e) {
            System.out.println(" Login failed: " + e.getMessage());
            return false;
        }
    }

    private void showMenu() {
        while (true) {
            List<String> devices = getDevices();

            System.out.println("\n           === Device List ===");
            System.out.printf(" %-5s %-20s %-10s %-10s%n", "No.", "Device", "Price", "Stock");
            System.out.println(" ----------------------------------------------");

            for (String device : devices) {
                System.out.println(device);
            }

            System.out.println("\n Options:");
            System.out.println(" [1]. Add to Cart");
            System.out.println(" [2]. Remove from Cart");
            System.out.println(" [3]. View Cart");
            System.out.println(" [4]. Checkout");
            System.out.println(" [5]. Logout");

            String choice = showUserPrompt("\n Choose an option: ");
            switch (choice) {
                case "1" -> addItemToCart();
                case "2" -> removeItemFromCart();
                case "3" -> viewCart();
                case "4" -> checkout();
                case "5" -> {
                    logout();
                    return;
                }
                default -> {
                    clear();
                    System.out.println(" Invalid option!");
                }
            }
        }
    }

    private List<String> getDevices() {
        List<String> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM devices")) {
            ResultSet rs = ps.executeQuery();
            int index = 1;
            while (rs.next()) {
                list.add(String.format(" %-5d %-20s $%-9.2f   %-10d",
                        index++, rs.getString("name"), rs.getDouble("price"), rs.getInt("stock")));
            }
        } catch (SQLException e) {
            System.out.println(" DB Error: " + e.getMessage());
        }
        return list;
    }

    // ================== ADD TO CART ==================
    private void addItemToCart() {
        clear();
        List<String> deviceNames = new ArrayList<>();
        System.out.println("\n           === Device List ===");
        System.out.printf(" %-5s %-20s %-10s %-10s%n", "No.", "Device", "Price", "Stock");
        System.out.println(" ----------------------------------------------");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM devices")) {
            ResultSet rs = ps.executeQuery();
            int index = 1;
            while (rs.next()) {
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                int stock = rs.getInt("stock");
                deviceNames.add(name);
                System.out.printf(" %-5d %-20s $%-9.2f %-10d%n", index++, name, price, stock);
            }
        } catch (SQLException e) {
            System.out.println(" DB Error: " + e.getMessage());
            return;
        }

        String input = showUserPrompt("\n Enter the number of the device to add: ");
        int choice;
        try {
            choice = Integer.parseInt(input);
            if (choice < 1 || choice > deviceNames.size()) {
                System.out.println(" Invalid choice!");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println(" Invalid number!");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(showUserPrompt(" Enter quantity: "));
            if (quantity <= 0) {
                System.out.println(" Quantity must be positive!");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println(" Invalid quantity!");
            return;
        }

        String deviceName = deviceNames.get(choice - 1);

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement psCheck = conn.prepareStatement("SELECT stock FROM devices WHERE name=?");
            psCheck.setString(1, deviceName);
            ResultSet rs = psCheck.executeQuery();

            if (!rs.next()) {
                clear();
                System.out.println(" Device not found!");
                return;
            }

            int stock = rs.getInt("stock");
            if (stock < quantity) {
                clear();
                System.out.println(" Not enough stock available!");
                return;
            }

            PreparedStatement psInsert = conn.prepareStatement(
                    "INSERT INTO carts (username, device_name, quantity) VALUES (?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)");
            psInsert.setString(1, currentUser);
            psInsert.setString(2, deviceName);
            psInsert.setInt(3, quantity);
            psInsert.executeUpdate();

            PreparedStatement psUpdateStock = conn.prepareStatement("UPDATE devices SET stock = stock - ? WHERE name = ?");
            psUpdateStock.setInt(1, quantity);
            psUpdateStock.setString(2, deviceName);
            psUpdateStock.executeUpdate();

            clear();
            System.out.println(" " + quantity + " x " + deviceName + " added to cart!");
        } catch (SQLException e) {
            System.out.println(" Add to cart failed: " + e.getMessage());
        }
    }

    // ================== REMOVE FROM CART ==================
    private void removeItemFromCart() {
        clear();
        List<String> cartItems = new ArrayList<>();
        List<Integer> cartQuantities = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM carts WHERE username=?")) {
            ps.setString(1, currentUser);
            ResultSet rs = ps.executeQuery();

            if (!rs.isBeforeFirst()) {
                System.out.println(" Your cart is empty. Nothing to remove.");
                return;
            }

            System.out.println("\n ================== YOUR CART ==================");
            System.out.printf(" %-5s %-20s %-10s %-10s%n", "No.", "Item", "Qty", "Price");
            System.out.println(" ----------------------------------------------");

            int index = 1;
            while (rs.next()) {
                String item = rs.getString("device_name");
                int qty = rs.getInt("quantity");
                double price = getPrice(item);
                cartItems.add(item);
                cartQuantities.add(qty);
                System.out.printf(" %-5d %-20s %-10d $%-9.2f%n", index++, item, qty, price);
            }

            String input = showUserPrompt("\n Enter the number of the item to remove: ");
            int choice;
            try {
                choice = Integer.parseInt(input);
                if (choice < 1 || choice > cartItems.size()) {
                    System.out.println(" Invalid choice!");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println(" Invalid number!");
                return;
            }

            int quantity;
            try {
                quantity = Integer.parseInt(showUserPrompt(" Enter quantity to remove: "));
                if (quantity <= 0) {
                    System.out.println(" Quantity must be positive!");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println(" Invalid quantity!");
                return;
            }

            String itemToRemove = cartItems.get(choice - 1);
            int currentQty = cartQuantities.get(choice - 1);

            try (Connection conn2 = DBConnection.getConnection()) {
                if (quantity >= currentQty) {
                    PreparedStatement del = conn2.prepareStatement("DELETE FROM carts WHERE username=? AND device_name=?");
                    del.setString(1, currentUser);
                    del.setString(2, itemToRemove);
                    del.executeUpdate();
                } else {
                    PreparedStatement upd = conn2.prepareStatement("UPDATE carts SET quantity=quantity-? WHERE username=? AND device_name=?");
                    upd.setInt(1, quantity);
                    upd.setString(2, currentUser);
                    upd.setString(3, itemToRemove);
                    upd.executeUpdate();
                }

                PreparedStatement restore = conn2.prepareStatement("UPDATE devices SET stock=stock+? WHERE name=?");
                restore.setInt(1, quantity);
                restore.setString(2, itemToRemove);
                restore.executeUpdate();

                clear();
                System.out.println(" Removed " + quantity + " x " + itemToRemove + " from cart.");
            } catch (SQLException e) {
                System.out.println(" Remove failed: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.out.println(" DB Error: " + e.getMessage());
        }
    }

    private void viewCart() {
        clear();
        System.out.println("\n ================== YOUR CART ==================");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM carts WHERE username=?")) {
            ps.setString(1, currentUser);
            ResultSet rs = ps.executeQuery();

            if (!rs.isBeforeFirst()) {
                System.out.println(" Your cart is empty.");
                return;
            }

            double total = 0;
            int index = 1;
            while (rs.next()) {
                String item = rs.getString("device_name");
                int qty = rs.getInt("quantity");
                double price = getPrice(item);
                double subtotal = qty * price;
                total += subtotal;
                System.out.printf(" %-5d %-20s %-10d $%-9.2f $%-9.2f%n", index++, item, qty, price, subtotal);
            }
            System.out.println(" ----------------------------------------------");
            System.out.printf(" %-5s %-20s %-10s %-10s $%-9.2f%n", "", "", "", "TOTAL:", total);
        } catch (SQLException e) {
            System.out.println(" DB Error: " + e.getMessage());
        }
    }

    private double getPrice(String name) {
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT price FROM devices WHERE name=?")) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("price");
        } catch (SQLException ignored) {}
        return 0;
    }

    // ================== CHECKOUT ==================
    private void checkout() {
        clear();
        System.out.println("\n               === Checkout ===");
        double total = 0;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement psCheck = conn.prepareStatement("SELECT * FROM carts WHERE username=?")) {
            psCheck.setString(1, currentUser);
            ResultSet rsCheck = psCheck.executeQuery();

            if (!rsCheck.isBeforeFirst()) {
                System.out.println(" Your cart is empty. Nothing to checkout.");
                return;
            }

            while (rsCheck.next()) {
                total += getPrice(rsCheck.getString("device_name")) * rsCheck.getInt("quantity");
            }

            PreparedStatement del = conn.prepareStatement("DELETE FROM carts WHERE username=?");
            del.setString(1, currentUser);
            del.executeUpdate();

            System.out.printf(" Total amount: $%.2f%n", total);
            System.out.println(" Checkout complete! Thank you for your purchase.");
        } catch (SQLException e) {
            System.out.println(" Checkout failed: " + e.getMessage());
        }
    }

    private void logout() {
        clear();
        System.out.println(" Logged out successfully, " + currentUser + "!");
        currentUser = null;
    }

    // public static void main(String[] args) {
    //     Scanner scanner = new Scanner(System.in);
    //     Main app = new Main(scanner);
    //     app.appRun();
    //     scanner.close();
    // }

    public static void main(String[] args) {
        boolean isServer = System.getenv("RAILWAY_ENVIRONMENT") != null;

        if (isServer) {
            System.out.println("🚀 Running in Railway (server) mode — interactive input disabled.");
            System.out.println("🔗 Testing DB connection...");

            try (Connection conn = DBConnection.getConnection()) {
                if (conn != null) {
                    System.out.println("✅ Connected successfully!");
                    ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM devices");
                    if (rs.next()) {
                        System.out.println("📦 Devices available in DB: " + rs.getInt(1));
                    }
                } else {
                    System.out.println("⚠️ Could not establish DB connection.");
                }
            } catch (Exception e) {
                System.out.println("❌ DB test failed: " + e.getMessage());
            }

            System.out.println("🛑 Application exited (no console input allowed on Railway).");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        Main app = new Main(scanner);
        app.appRun();
        scanner.close();
    }
}
