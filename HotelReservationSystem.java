import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Task 4: Hotel Reservation System
 * - Search, book and manage hotel rooms
 * - Room categories: Standard, Deluxe, Suite
 * - Make / cancel reservations
 * - Payment simulation + booking details view
 * - OOP + File I/O (bookings.txt) for persistence
 *
 * Compile: javac HotelReservationSystem.java
 * Run:     java HotelReservationSystem
 */
public class HotelReservationSystem {

    // ---------- Room categories ----------
    enum Category {
        STANDARD(2000), DELUXE(3500), SUITE(6000);
        final double pricePerNight;
        Category(double p) { pricePerNight = p; }
    }

    // ---------- Room ----------
    static class Room {
        private final int number;
        private final Category category;
        Room(int number, Category category) { this.number = number; this.category = category; }
        int getNumber() { return number; }
        Category getCategory() { return category; }
        double getPrice() { return category.pricePerNight; }
        @Override public String toString() {
            return String.format("Room %d | %-8s | Rs.%.0f/night", number, category, getPrice());
        }
    }

    // ---------- Reservation ----------
    static class Reservation {
        private final String id;
        private final String guest;
        private final int roomNumber;
        private final LocalDate checkIn, checkOut;
        private final double amount;
        private final String paymentMethod;
        private boolean active;

        Reservation(String id, String guest, int roomNumber, LocalDate in, LocalDate out,
                    double amount, String paymentMethod, boolean active) {
            this.id = id; this.guest = guest; this.roomNumber = roomNumber;
            this.checkIn = in; this.checkOut = out; this.amount = amount;
            this.paymentMethod = paymentMethod; this.active = active;
        }
        String getId() { return id; }
        String getGuest() { return guest; }
        int getRoomNumber() { return roomNumber; }
        LocalDate getCheckIn() { return checkIn; }
        LocalDate getCheckOut() { return checkOut; }
        boolean isActive() { return active; }
        void cancel() { active = false; }

        boolean overlaps(LocalDate in, LocalDate out) {
            return active && in.isBefore(checkOut) && out.isAfter(checkIn);
        }
        String toFileLine() {
            return String.join("|", id, guest, String.valueOf(roomNumber), checkIn.toString(),
                    checkOut.toString(), String.valueOf(amount), paymentMethod, String.valueOf(active));
        }
        static Reservation fromFileLine(String line) {
            String[] p = line.split("\\|");
            return new Reservation(p[0], p[1], Integer.parseInt(p[2]), LocalDate.parse(p[3]),
                    LocalDate.parse(p[4]), Double.parseDouble(p[5]), p[6], Boolean.parseBoolean(p[7]));
        }
        void print(Room room) {
            System.out.println("--------------------------------------");
            System.out.println("Booking ID   : " + id);
            System.out.println("Guest        : " + guest);
            System.out.println("Room         : " + roomNumber + " (" + (room != null ? room.getCategory() : "?") + ")");
            System.out.println("Check-in     : " + checkIn);
            System.out.println("Check-out    : " + checkOut);
            System.out.println("Nights       : " + ChronoUnit.DAYS.between(checkIn, checkOut));
            System.out.printf("Amount Paid  : Rs.%.2f (%s)%n", amount, paymentMethod);
            System.out.println("Status       : " + (active ? "CONFIRMED" : "CANCELLED"));
            System.out.println("--------------------------------------");
        }
    }

    // ---------- Hotel (business logic + file I/O) ----------
    static class Hotel {
        private static final String FILE = "bookings.txt";
        private final List<Room> rooms = new ArrayList<>();
        private final List<Reservation> reservations = new ArrayList<>();

        Hotel() {
            for (int i = 101; i <= 105; i++) rooms.add(new Room(i, Category.STANDARD));
            for (int i = 201; i <= 204; i++) rooms.add(new Room(i, Category.DELUXE));
            for (int i = 301; i <= 302; i++) rooms.add(new Room(i, Category.SUITE));
            load();
        }

        Room findRoom(int number) {
            for (Room r : rooms) if (r.getNumber() == number) return r;
            return null;
        }

        boolean isAvailable(Room room, LocalDate in, LocalDate out) {
            for (Reservation r : reservations)
                if (r.getRoomNumber() == room.getNumber() && r.overlaps(in, out)) return false;
            return true;
        }

        List<Room> search(Category cat, LocalDate in, LocalDate out) {
            List<Room> result = new ArrayList<>();
            for (Room r : rooms)
                if ((cat == null || r.getCategory() == cat) && isAvailable(r, in, out)) result.add(r);
            return result;
        }

        Reservation book(String guest, Room room, LocalDate in, LocalDate out, String method) {
            long nights = ChronoUnit.DAYS.between(in, out);
            double total = nights * room.getPrice();
            String id = "BK" + (1000 + reservations.size() + 1);
            Reservation res = new Reservation(id, guest, room.getNumber(), in, out, total, method, true);
            reservations.add(res);
            save();
            return res;
        }

        Reservation findReservation(String id) {
            for (Reservation r : reservations) if (r.getId().equalsIgnoreCase(id)) return r;
            return null;
        }

        boolean cancel(String id) {
            Reservation r = findReservation(id);
            if (r == null || !r.isActive()) return false;
            r.cancel();
            save();
            return true;
        }

        List<Reservation> all() { return reservations; }

        private void save() {
            try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
                for (Reservation r : reservations) pw.println(r.toFileLine());
            } catch (IOException e) {
                System.out.println("Error saving bookings: " + e.getMessage());
            }
        }

        private void load() {
            File f = new File(FILE);
            if (!f.exists()) return;
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null)
                    if (!line.isBlank()) reservations.add(Reservation.fromFileLine(line));
            } catch (Exception e) {
                System.out.println("Error loading bookings: " + e.getMessage());
            }
        }
    }

    // ---------- Payment simulation ----------
    static boolean simulatePayment(Scanner sc, double amount, String[] methodOut) {
        System.out.printf("Total amount to pay: Rs.%.2f%n", amount);
        System.out.println("Payment method: 1) Credit/Debit Card  2) UPI  3) Cash at hotel");
        System.out.print("Choose: ");
        String c = sc.nextLine().trim();
        switch (c) {
            case "1":
                System.out.print("Enter 16-digit card number: ");
                String card = sc.nextLine().replaceAll("\\s", "");
                if (!card.matches("\\d{16}")) { System.out.println("Invalid card number."); return false; }
                methodOut[0] = "Card(****" + card.substring(12) + ")";
                break;
            case "2":
                System.out.print("Enter UPI ID: ");
                String upi = sc.nextLine().trim();
                if (!upi.contains("@")) { System.out.println("Invalid UPI ID."); return false; }
                methodOut[0] = "UPI(" + upi + ")";
                break;
            case "3":
                methodOut[0] = "Cash";
                return true;
            default:
                System.out.println("Invalid choice.");
                return false;
        }
        System.out.println("Processing payment...");
        try { Thread.sleep(1000); } catch (InterruptedException ignored) { }
        System.out.println("Payment successful!");
        return true;
    }

    // ---------- Input helpers ----------
    static LocalDate readDate(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt + " (YYYY-MM-DD): ");
            try { return LocalDate.parse(sc.nextLine().trim()); }
            catch (Exception e) { System.out.println("Invalid date format."); }
        }
    }

    static Category readCategory(Scanner sc, boolean allowAll) {
        System.out.print("Category 1) Standard 2) Deluxe 3) Suite" + (allowAll ? " 4) All" : "") + ": ");
        switch (sc.nextLine().trim()) {
            case "1": return Category.STANDARD;
            case "2": return Category.DELUXE;
            case "3": return Category.SUITE;
            default:  return null; // "All" or invalid
        }
    }

    // ---------- Main ----------
    public static void main(String[] args) {
        Hotel hotel = new Hotel();
        Scanner sc = new Scanner(System.in);
        System.out.println("===== HOTEL RESERVATION SYSTEM =====");

        while (true) {
            System.out.println("\n1. Search available rooms");
            System.out.println("2. Book a room");
            System.out.println("3. Cancel a reservation");
            System.out.println("4. View booking details");
            System.out.println("5. View all bookings");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1": {
                    Category cat = readCategory(sc, true);
                    LocalDate in = readDate(sc, "Check-in");
                    LocalDate out = readDate(sc, "Check-out");
                    if (!out.isAfter(in)) { System.out.println("Check-out must be after check-in."); break; }
                    List<Room> found = hotel.search(cat, in, out);
                    if (found.isEmpty()) System.out.println("No rooms available.");
                    else found.forEach(System.out::println);
                    break;
                }
                case "2": {
                    System.out.print("Guest name: ");
                    String guest = sc.nextLine().trim();
                    if (guest.isEmpty() || guest.contains("|")) { System.out.println("Invalid name."); break; }
                    Category cat = readCategory(sc, true);
                    LocalDate in = readDate(sc, "Check-in");
                    LocalDate out = readDate(sc, "Check-out");
                    if (in.isBefore(LocalDate.now()) || !out.isAfter(in)) {
                        System.out.println("Invalid dates."); break;
                    }
                    List<Room> found = hotel.search(cat, in, out);
                    if (found.isEmpty()) { System.out.println("No rooms available."); break; }
                    found.forEach(System.out::println);
                    System.out.print("Enter room number to book: ");
                    Room room;
                    try { room = hotel.findRoom(Integer.parseInt(sc.nextLine().trim())); }
                    catch (NumberFormatException e) { room = null; }
                    if (room == null || !found.contains(room)) { System.out.println("Invalid room selection."); break; }

                    double total = ChronoUnit.DAYS.between(in, out) * room.getPrice();
                    String[] method = new String[1];
                    if (simulatePayment(sc, total, method)) {
                        Reservation r = hotel.book(guest, room, in, out, method[0]);
                        System.out.println("\nBooking confirmed!");
                        r.print(room);
                    } else {
                        System.out.println("Payment failed. Booking not made.");
                    }
                    break;
                }
                case "3": {
                    System.out.print("Enter booking ID to cancel: ");
                    String id = sc.nextLine().trim();
                    System.out.println(hotel.cancel(id)
                            ? "Reservation cancelled successfully."
                            : "Booking not found or already cancelled.");
                    break;
                }
                case "4": {
                    System.out.print("Enter booking ID: ");
                    Reservation r = hotel.findReservation(sc.nextLine().trim());
                    if (r == null) System.out.println("Booking not found.");
                    else r.print(hotel.findRoom(r.getRoomNumber()));
                    break;
                }
                case "5": {
                    if (hotel.all().isEmpty()) System.out.println("No bookings yet.");
                    for (Reservation r : hotel.all()) r.print(hotel.findRoom(r.getRoomNumber()));
                    break;
                }
                case "0":
                    System.out.println("Thank you! Goodbye.");
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }
}