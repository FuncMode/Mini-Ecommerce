import java.sql.*;
import java.util.*;
import java.net.http.*;
import java.net.URI;
import com.google.gson.*;
import java.text.NumberFormat;

public class Main {
    private Scanner scanner;
    private String currentUser;

    public Main(Scanner scanner) {
        this.scanner = scanner;
    }

    // ===================== APPLICATION ENTRY =====================
    public static void main(String[] args) {
        boolean isServer = System.getenv("RAILWAY_ENVIRONMENT") != null;

        if (isServer) {
            System.out.println("Running in Railway (server) mode — console input disabled.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        Main app = new Main(scanner);
        app.run();
        scanner.close();
    }

// ===================== MAIN MENU =====================
    public void run() {
        clear();
        System.out.println(" === Welcome to Mini E-Commerce ===");

        while (true) {
            System.out.println("\n 1. Register");
            System.out.println(" 2. Login");
            System.out.println(" 3. Open Web Version");
            System.out.println(" 4. Exit");

            String choice = prompt("\n Choose an option: ");

            switch (choice) {
                case "1" -> registerUser();
                case "2" -> { if (loginUser()) menu(); }
                case "3" -> {
                    clear();
                    System.out.println(" 🌐 Visit the web version here:");
                    System.out.println(" https://mini-ecommerce-web-production.up.railway.app/");
                    prompt("\n Press Enter to return to menu...");
                }
                case "4" -> { clear(); System.out.println(" Thanks for using Mini-Ecommerce!"); return; }
                default -> message(" Invalid option.");
            }
        }
    }


    // ===================== BASIC UTILS =====================
    private void clear() {
        try {
            if (System.getProperty("os.name").contains("Windows"))
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            else System.out.print("\033[H\033[2J");
        } catch (Exception ignored) {}
    }

    private String prompt(String text) {
        System.out.print(text);
        return scanner.nextLine().trim();
    }

    private void message(String msg) {
        clear();
        System.out.println(" " + msg);
    }

    private boolean validateInput(String input, String type) {
        if (input.isEmpty()) {
            System.out.println(" " + type + " cannot be empty!");
            return false;
        }

        if (input.length() < 6) {
            System.out.println(type + " must be at least 6 characters!");
            return false;
        }

        if (type.equals("Password") && !input.matches(".*\\d.*")) {
            System.out.println(" Password must include at least 1 number!");
            return false;
        }

        return true;
    }

    // ===================== USER SYSTEM =====================
    private void registerUser() {
        clear();
        System.out.println("\n ===== USER REGISTRATION =====");

        String username;
        while (true) {
            username = prompt(" Enter username: ");
            if (!validateInput(username, "Username")) continue;
            if (userExists(username)) { System.out.println(" Username already taken."); continue; }
            break;
        }

        String password;
        while (true) {
            password = prompt(" Enter password: ");
            if (!validateInput(password, "Password")) continue;
            break;
        }

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)")) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();
            message(" Registration successful!");
        } catch (SQLException e) {
            System.out.println(" Registration failed: " + e.getMessage());
        }
    }

    private boolean userExists(String username) {
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM users WHERE username=?")) {
            ps.setString(1, username);
            return ps.executeQuery().next();
        } catch (Exception e) { return false; }
    }

private boolean loginUser() {
    clear();
    System.out.println("\n ===== LOGIN ===== \n");

    String username;
    while (true) {
        username = prompt(" Username: ");
        if (username.isBlank() || username.length() < 6) {
            System.out.println(" Username must be at least 6 characters. Please try again.");
            continue;
        }
        break;
    }

    String password;
    while (true) {
        password = prompt(" Password: ");
        if (password.isBlank() || password.length() < 6) {
            System.out.println(" Password must be at least 6 characters. Please try again.");
            continue;
        }
        break;
    }

    try (Connection conn = DBConnection.getConnection()) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT password FROM users WHERE username=?")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                System.out.println(" Username not found. Please register first.");
                return false;
            }

            String correctPassword = rs.getString("password");
            if (!correctPassword.equals(password)) {
                System.out.println(" Wrong password. Please check your password.");
                return false;
            }
        }

        currentUser = username;
        System.out.println(" Welcome, " + username + "!");
        return true;

    } catch (SQLException e) {
        System.out.println(" Login failed: " + e.getMessage());
        return false;
    }
}


    // ===================== PRODUCT FETCH =====================
    private List<Map<String, Object>> getProducts() {
        List<Map<String, Object>> list = new ArrayList<>();
        double conversionRate = 56.0; // USD → PHP example

        try {
            HttpResponse<String> res = HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder().uri(URI.create("https://fakestoreapi.com/products")).build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            JsonArray arr = JsonParser.parseString(res.body()).getAsJsonArray();
            for (JsonElement el : arr) {
                JsonObject o = el.getAsJsonObject();
                Map<String, Object> p = new HashMap<>();
                p.put("id", o.get("id").getAsInt());
                p.put("name", o.get("title").getAsString());
                double phpPrice = o.get("price").getAsDouble() * conversionRate;
                p.put("price", phpPrice);
                list.add(p);
            }
        } catch (Exception e) {
            System.out.println(" Failed to load products: " + e.getMessage());
        }

        return list;
    }

    // ===================== MENU =====================
    private void menu() {
        NumberFormat pesoFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-PH"));

        while (true) {
            clear();
            List<Map<String, Object>> p = getProducts();

            System.out.println("\n ===== PRODUCT LIST =====");
            for (int i = 0; i < p.size(); i++)
                System.out.printf(" %d. %s (%s)\n", i + 1, p.get(i).get("name"),
                        pesoFormat.format((double) p.get(i).get("price")));

            System.out.println("\n 1. Add to Cart");
            System.out.println(" 2. Remove from Cart");
            System.out.println(" 3. View Cart");
            System.out.println(" 4. Checkout");
            System.out.println(" 5. Logout");

            switch (prompt("\n Choose: ")) {
                case "1" -> addToCart(p);
                case "2" -> removeFromCart();
                case "3" -> viewCart();
                case "4" -> checkout();
                case "5" -> {
                    logout();
                    return;
                }
                default -> message(" Invalid option.");
            }
        }
    }

    // ===================== CART SYSTEM =====================
    private void addToCart(List<Map<String, Object>> p) {
        int choice;
        try { choice = Integer.parseInt(prompt(" Enter product number: ")); }
        catch (Exception e) { return; }
        if (choice < 1 || choice > p.size()) return;

        int qty;
        try { qty = Integer.parseInt(prompt(" Enter quantity: ")); }
        catch (Exception e) { return; }
        if (qty <= 0) return;

        Map<String, Object> item = p.get(choice - 1);

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO carts (username, product_id, product_name, price, quantity) VALUES (?, ?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)")) {
            ps.setString(1, currentUser);
            ps.setInt(2, (int) item.get("id"));
            ps.setString(3, (String) item.get("name"));
            ps.setDouble(4, (double) item.get("price"));
            ps.setInt(5, qty);
            ps.executeUpdate();
            message("Added to cart!");
        } catch (Exception e) {
            System.out.println(" Add failed: " + e.getMessage());
        }
    }

    private void removeFromCart() {
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement("SELECT * FROM carts WHERE username=?")) {
        ps.setString(1, currentUser);
        ResultSet rs = ps.executeQuery();

        List<Integer> ids = new ArrayList<>();
        List<Integer> qtys = new ArrayList<>();
        int i = 1;
        while (rs.next()) {
            System.out.printf(" %d. %s (x%d)\n", i++, rs.getString("product_name"), rs.getInt("quantity"));
            ids.add(rs.getInt("product_id"));
            qtys.add(rs.getInt("quantity"));
        }
        if (ids.isEmpty()) { message("Cart is empty."); return; }

        int ch;
        try { ch = Integer.parseInt(prompt(" Select item: ")); }
        catch (Exception e) { return; }
        if (ch < 1 || ch > ids.size()) return;

        int currentQty = qtys.get(ch - 1);
        int toRemove = 1;

        if (currentQty > 1) {
            String input = prompt("Item has " + currentQty + " pcs. How many to remove? ");
            try { toRemove = Integer.parseInt(input); }
            catch (Exception e) { return; }

            if (toRemove < 1) toRemove = 1;
            if (toRemove > currentQty) toRemove = currentQty;
        }

        if (toRemove >= currentQty) {
            // delete row if removing all
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM carts WHERE username=? AND product_id=?")) {
                del.setString(1, currentUser);
                del.setInt(2, ids.get(ch - 1));
                del.executeUpdate();
            }
        } else {
            // decrease quantity
            try (PreparedStatement upd = conn.prepareStatement("UPDATE carts SET quantity = quantity - ? WHERE username=? AND product_id=?")) {
                upd.setInt(1, toRemove);
                upd.setString(2, currentUser);
                upd.setInt(3, ids.get(ch - 1));
                upd.executeUpdate();
            }
        }

        message("Removed " + toRemove + " item(s).");

    } catch (Exception e) {
        System.out.println(" Remove failed: " + e.getMessage());
    }
}


    private void viewCart() {
        clear();
        NumberFormat pesoFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-PH"));
        System.out.println("\n ===== YOUR CART =====");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM carts WHERE username=?")) {
            ps.setString(1, currentUser);
            ResultSet rs = ps.executeQuery();

            double total = 0;
            while (rs.next()) {
                double sub = rs.getDouble("price") * rs.getInt("quantity");
                total += sub;
                System.out.printf(" %s (x%d) - %s\n", rs.getString("product_name"), rs.getInt("quantity"),
                        pesoFormat.format(sub));
            }
            System.out.println(" Total: " + pesoFormat.format(total));
        } catch (Exception ignored) {}
        prompt("\n Press Enter...");
    }

    private void checkout() {
        clear();
        NumberFormat pesoFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-PH"));
        System.out.println("\n ===== CHECKOUT =====");

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT price, quantity FROM carts WHERE username=?")) {
            ps.setString(1, currentUser);
            ResultSet rs = ps.executeQuery();

            double total = 0;
            while (rs.next()) total += rs.getDouble("price") * rs.getInt("quantity");

            try (PreparedStatement del = conn.prepareStatement("DELETE FROM carts WHERE username=?")) {
                del.setString(1, currentUser);
                del.executeUpdate();
            }

            System.out.println(" Total Paid: " + pesoFormat.format(total));
            prompt("\n Press Enter to continue...");
        } catch (Exception ignored) {}
    }

    private void logout() {
        message("Logged out: " + currentUser);
        currentUser = null;
    }
}
