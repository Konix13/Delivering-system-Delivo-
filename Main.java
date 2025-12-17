import java.util.*;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        User user = new User("Guest");
        OrderManager orderManager = new OrderManager();
        orderManager.subscribe(user);

        FoodFactory factory = new SimpleFoodFactory();

        System.out.println("=== DELIVERING SYSTEM <<Delivo>> ===");

        while (true) {

            System.out.println("\nChoose role:");
            System.out.println("1) User");
            System.out.println("2) Admin");
            System.out.println("0) Exit");
            System.out.print("Choose: ");

            String role = sc.nextLine();

            if (role.equals("0")) {
                System.out.println("Bye!");
                return;
            }

            switch (role) {
                case "1" -> userMenu(sc, factory, orderManager, user);
                case "2" -> adminMenu(sc, orderManager);
                default -> System.out.println("Unknown option");
            }
        }
    }

    /* ================= USER MENU ================= */

    private static void userMenu(Scanner sc, FoodFactory factory, OrderManager manager, User user) {

        while (true) {

            System.out.println("\n--- USER MENU ---");
            System.out.println("1) Show menu");
            System.out.println("2) Create order");
            System.out.println("3) View my orders");
            System.out.println("0) Logout");
            System.out.print("Choose: ");

            String c = sc.nextLine();

            switch (c) {
                case "1" -> showMenu(sc, factory);
                case "2" -> createOrder(sc, factory, manager, user);
                case "3" -> viewOrders(user);
                case "0" -> { return; }
                default -> System.out.println("Unknown option");
            }
        }
    }

    /* ================= ADMIN MENU ================= */

    private static void adminMenu(Scanner sc, OrderManager manager) {

        while (true) {

            System.out.println("\n--- ADMIN MENU ---");
            System.out.println("1) Update order status");
            System.out.println("0) Logout");
            System.out.print("Choose: ");

            String c = sc.nextLine();

            switch (c) {
                case "1" -> updateStatus(sc, manager);
                case "0" -> { return; }
                default -> System.out.println("Unknown option");
            }
        }
    }

    /* ================= MENU ================= */

    private static void showMenu(Scanner sc, FoodFactory factory) {

        while (true) {
            printFoodMenu();
            System.out.println("0) Back");
            System.out.print("Choose to preview: ");

            String input = sc.nextLine();
            if (input.equals("0")) return;

            int i = parse(input);
            if (i < 1 || i > 5) continue;

            Food f = factory.createFood(foodType(i));
            System.out.println(f.name() + " | " + f.description() + " | $" + f.price());
            System.out.println("Extras: " + allowedExtras(f.type()));
        }
    }

    /* ================= ORDER ================= */

    private static void createOrder(Scanner sc, FoodFactory factory, OrderManager manager, User user) {

        Cart cart = new Cart();

        while (true) {

            System.out.println("\n--- CART ---");
            cart.print();
            System.out.println("1) Add food");
            System.out.println("2) Checkout");
            System.out.println("0) Cancel");
            System.out.print("Choose: ");

            String c = sc.nextLine();
            if (c.equals("0")) return;

            if (c.equals("1")) {
                Food base = chooseFood(sc, factory);
                if (base == null) continue;
                Food customized = chooseExtras(sc, base);
                cart.add(customized);
            }

            if (c.equals("2")) {
                if (cart.isEmpty()) {
                    System.out.println("Cart is empty.");
                    continue;
                }
                Order order = manager.createOrder(user, cart.getItems());
                System.out.println(order.receipt());
                return;
            }
        }
    }

    private static Food chooseFood(Scanner sc, FoodFactory factory) {

        printFoodMenu();
        System.out.println("0) Back");
        System.out.print("Choose food: ");

        String in = sc.nextLine();
        if (in.equals("0")) return null;

        int i = parse(in);
        if (i < 1 || i > 5) return null;

        return factory.createFood(foodType(i));
    }

    /* ================= EXTRAS ================= */

    private static Food chooseExtras(Scanner sc, Food base) {

        List<String> remaining = new ArrayList<>(allowedExtras(base.type()));
        Food result = base;

        while (!remaining.isEmpty()) {

            System.out.println("\nExtras for " + base.name() + ":");
            for (int i = 0; i < remaining.size(); i++) {
                System.out.println((i + 1) + ") " + remaining.get(i));
            }

            System.out.println("0) Finish extras");
            System.out.print("Choose extra: ");

            int c = parse(sc.nextLine());
            if (c == 0) break;

            if (c < 1 || c > remaining.size()) {
                System.out.println("Invalid choice");
                continue;
            }

            String selected = remaining.remove(c - 1);
            result = applyDecorator(result, selected);
            System.out.println("✓ Added: " + selected);
        }

        return result;
    }

    /* ================= STATUS ================= */

    private static void updateStatus(Scanner sc, OrderManager manager) {

        if (!manager.hasOrders()) {
            System.out.println("No orders yet.");
            return;
        }

        manager.printAllOrders();

        System.out.print("Enter order ID: ");
        int orderId = parse(sc.nextLine());

        System.out.println("1) PLACED");
        System.out.println("2) COOKING");
        System.out.println("3) READY");
        System.out.println("4) DELIVERED");
        System.out.print("Choose new status: ");

        int c = parse(sc.nextLine());

        String status = switch (c) {
            case 1 -> OrderStatuses.PLACED;
            case 2 -> OrderStatuses.COOKING;
            case 3 -> OrderStatuses.READY;
            case 4 -> OrderStatuses.DELIVERED;
            default -> null;
        };

        if (status != null) {
            manager.updateStatus(orderId, status);
        }
    }


    private static void viewOrders(User user) {

        if (user.getOrders().isEmpty()) {
            System.out.println("No orders.");
            return;
        }

        for (Order o : user.getOrders()) {
            System.out.println("\nOrder #" + o.getId());
            for (Food f : o.getItems()) {
                System.out.println(" - " + f.description() + " ($" + f.price() + ")");
            }
            System.out.println("Status: " + o.getStatus());
        }
    }

    /* ================= HELPERS ================= */

    private static void printFoodMenu() {
        System.out.println("1) Pizza  $10");
        System.out.println("2) Burger $7");
        System.out.println("3) Fries  $3.5");
        System.out.println("4) Cola   $2");
        System.out.println("5) Coffee $2.5");
    }

    private static int parse(String s) {
        try {
            return Integer.parseInt(s);
        }
        catch (Exception e) {
            return -1;
        }
    }

    private static String foodType(int i) {
        List<String> types = new ArrayList<>();
        types.add("PIZZA");
        types.add("BURGER");
        types.add("FRIES");
        types.add("COLA");
        types.add("COFFEE");

        return types.get(i - 1);
    }

    private static List<String> allowedExtras(String type) {
        List<String> extras = new ArrayList<>();
        switch (type) {
            case "PIZZA":
            case "BURGER":
                extras.add("CHEESE");
                extras.add("SPICY");
                extras.add("LARGE");
                break;

            case "FRIES":
                extras.add("SPICY");
                extras.add("LARGE");
                break;

            case "COLA":
            case "COFFEE":
                extras.add("LARGE");
                break;
        }

        return extras;
    }


    private static Food applyDecorator(Food f, String e) {
        return switch (e) {
            case "CHEESE" -> new CheeseDecorator(f);
            case "SPICY" -> new SpicyDecorator(f);
            case "LARGE" -> new LargeDecorator(f);
            default -> f;
        };
    }

    /* ================= DOMAIN ================= */

    interface Food {
        String type();
        String name();
        String description();
        double price();
    }

    static class Pizza implements Food {
        public String type() {
            return "PIZZA";
        }
        public String name() {
            return "Pizza";
        }
        public String description() {
            return "Classic pizza";
        }
        public double price() {
            return 10;
        }
    }

    static class Burger implements Food {
        public String type() {
            return "BURGER";
        }
        public String name() {
            return "Burger";
        }
        public String description() {
            return "Beef burger";
        }
        public double price() {
            return 7;
        }
    }

    static class Fries implements Food {
        public String type() {
            return "FRIES";
        }
        public String name() {
            return "Fries";
        }
        public String description() {
            return "Crispy fries";
        }
        public double price() {
            return 3.5;
        }
    }

    static class Cola implements Food {
        public String type() {
            return "COLA";
        }
        public String name() {
            return "Cola";
        }
        public String description() {
            return "Coca cola";
        }
        public double price() {
            return 2;
        }
    }

    static class Coffee implements Food {
        public String type() {
            return "COFFEE";
        }
        public String name() {
            return "Coffee";
        }
        public String description() {
            return "Hot coffee";
        }
        public double price() {
            return 2.5;
        }
    }

    /* ================= FACTORY ================= */

    interface FoodFactory {
        Food createFood(String type);
    }

    static class SimpleFoodFactory implements FoodFactory {
        public Food createFood(String t) {
            return switch (t) {
                case "PIZZA" -> new Pizza();
                case "BURGER" -> new Burger();
                case "FRIES" -> new Fries();
                case "COLA" -> new Cola();
                case "COFFEE" -> new Coffee();
                default -> throw new IllegalArgumentException();
            };
        }
    }

    /* ================= DECORATOR ================= */

    static abstract class FoodDecorator implements Food {
        protected Food base;
        FoodDecorator(Food b) {
            base = b;
        }
        public String type() {
            return base.type();
        }
        public String name() {
            return base.name();
        }
    }

    static class CheeseDecorator extends FoodDecorator {
        CheeseDecorator(Food b) {
            super(b);
        }
        public String description() {
            return base.description() + ", cheese";
        }
        public double price() {
            return base.price() + 1.5;
        }
    }

    static class SpicyDecorator extends FoodDecorator {
        SpicyDecorator(Food b) {
            super(b);
        }
        public String description() {
            return base.description() + ", spicy";
        }
        public double price() {
            return base.price() + 0.8;
        }
    }

    static class LargeDecorator extends FoodDecorator {
        LargeDecorator(Food b) {
            super(b);
        }
        public String description() {
            return base.description() + ", large";
        }
        public double price() {
            return base.price() + 2;
        }
    }

    /* ================= ORDER + OBSERVER ================= */

    static class Cart {
        private final List<Food> items = new ArrayList<>();
        void add(Food f) {
            items.add(f);
        }
        boolean isEmpty() {
            return items.isEmpty();
        }
        List<Food> getItems() {
            return items;
        }
        void print() {
            if (items.isEmpty()) {
                System.out.println("(empty)");
            }
            else {
                for (Food f : items) {
                    System.out.println("- " + f.description() + " ($" + f.price() + ")");
                }
            }
        }
    }

    interface OrderObserver {
        void update(String msg);
    }

    static class User implements OrderObserver {
        private final String name;
        private final List<Order> orders = new ArrayList<>();
        User(String n) {
            name = n;
        }
        void addOrder(Order o) {
            orders.add(o);
        }
        List<Order> getOrders() {
            return orders;
        }
        public void update(String msg) {
            System.out.println("🔔 " + msg);
        }
    }

    static class Order {
        private static int seq = 1;
        private final int id;
        private final List<Food> items;
        private String status = OrderStatuses.PLACED;

        Order(User u, List<Food> it) {
            id = seq++;
            items = it;
            u.addOrder(this);
        }

        int getId() {
            return id;
        }
        String getStatus() {
            return status;
        }
        List<Food> getItems() {
            return items;
        }
        void setStatus(String s) {
            status = s;
        }

        String receipt() {
            double total = 0;
            for (Food f : items) {
                total += f.price();
            }
            return "Order #" + id + " | Total $" + total + " | " + status;
        }
    }

    static class OrderManager {
        private final List<OrderObserver> observers = new ArrayList<>();
        private final Map<Integer, Order> ordersById = new HashMap<>();

        void subscribe(OrderObserver o) {
            observers.add(o);
        }

        Order createOrder(User u, List<Food> items) {
            Order o = new Order(u, new ArrayList<>(items));
            ordersById.put(o.getId(), o);
            notifyAllObservers("Order #" + o.getId() + " created");
            return o;
        }

        void updateStatus(int orderId, String status) {
            Order o = ordersById.get(orderId);
            if (o == null) {
                System.out.println("Order not found.");
                return;
            }
            o.setStatus(status);
            notifyAllObservers("Order #" + orderId + " status -> " + status);
        }

        void printAllOrders() {
            if (ordersById.isEmpty()) {
                System.out.println("No orders.");
                return;
            }
            for (Order o : ordersById.values()) {
                System.out.println("Order #" + o.getId() + " | Status: " + o.getStatus());
            }
        }

        private void notifyAllObservers(String msg) {
            for (OrderObserver o : observers) o.update(msg);
        }

        boolean hasOrders() {
            return !ordersById.isEmpty();
        }
    }

    static class OrderStatuses {
        static final String PLACED = "PLACED";
        static final String COOKING = "COOKING";
        static final String READY = "READY";
        static final String DELIVERED = "DELIVERED";
    }
}
