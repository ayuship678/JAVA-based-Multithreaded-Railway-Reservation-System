import java.io.*;
import java.util.*;

// === Train Class (Thread-Safe) ===
class Train {
    private final boolean[] seats;

    public Train(int totalSeats) {
        seats = new boolean[totalSeats];
    }

    public synchronized boolean bookSeat(int seatNumber) {
        if (seatNumber < 0 || seatNumber >= seats.length) {
            System.out.println("Invalid seat number!");
            return false;
        }
        if (seats[seatNumber]) {
            return false;
        }
        seats[seatNumber] = true;
        return true;
    }

    public synchronized boolean cancelSeat(int seatNumber) {
        if (seatNumber < 0 || seatNumber >= seats.length) {
            System.out.println("Invalid seat number!");
            return false;
        }
        if (!seats[seatNumber]) {
            return false;
        }
        seats[seatNumber] = false;
        return true;
    }

    public synchronized void showSeats() {
        System.out.println("\n===== Current Seat Status =====");
        for (int i = 0; i < seats.length; i++) {
            System.out.println("Seat " + (i + 1) + ": " + (seats[i] ? "Booked" : "Available"));
        }
    }
}

// === DAO Layer ===
class BookingDAO {
    private static final String FILE_NAME = "bookings.txt";

    static {
        try {
            File file = new File(FILE_NAME);
            if (!file.exists()) file.createNewFile();
        } catch (IOException e) {
            System.out.println("Error initializing database file.");
        }
    }

    public static synchronized void saveBooking(String name, int seatNumber) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            bw.write(name + "," + (seatNumber + 1));
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error saving booking: " + e.getMessage());
        }
    }

    public static synchronized void cancelBooking(int seatNumber) {
        File inputFile = new File(FILE_NAME);
        File tempFile = new File("temp.txt");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2 && Integer.parseInt(parts[1]) == seatNumber + 1) {
                    continue; // skip cancelled booking
                }
                writer.write(line);
                writer.newLine();
            }

        } catch (IOException e) {
            System.out.println("Error cancelling booking: " + e.getMessage());
        }

        inputFile.delete();
        tempFile.renameTo(inputFile);
    }

    public static synchronized void showBookings() {
        System.out.println("\n===== All Bookings =====");
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            boolean empty = true;

            while ((line = br.readLine()) != null) {
                System.out.println("- " + line);
                empty = false;
            }

            if (empty) System.out.println("No bookings found.");

        } catch (IOException e) {
            System.out.println("Error reading bookings: " + e.getMessage());
        }
    }
}

// === Payment Gateway ===
class PaymentGateway {
    public static boolean validatePayment(String user, double amount) {
        System.out.println("Validating payment of Rs." + amount + " for " + user + "...");
        return amount > 0;
    }
}

// === Passenger Thread ===
class Passenger implements Runnable {
    private final Train train;
    private final int seatNumber;
    private final String name;
    private final double payment;
    private final boolean isCancel;

    public Passenger(Train train, int seatNumber, String name, double payment, boolean isCancel) {
        this.train = train;
        this.seatNumber = seatNumber;
        this.name = name;
        this.payment = payment;
        this.isCancel = isCancel;
    }

    @Override
    public void run() {
        if (isCancel) {
            boolean cancelled = train.cancelSeat(seatNumber);
            if (cancelled) {
                BookingDAO.cancelBooking(seatNumber);
                System.out.println(name + " successfully cancelled seat " + (seatNumber + 1));
            } else {
                System.out.println(name + " failed to cancel seat " + (seatNumber + 1));
            }
        } else {
            if (PaymentGateway.validatePayment(name, payment)) {
                boolean booked = train.bookSeat(seatNumber);
                if (booked) {
                    BookingDAO.saveBooking(name, seatNumber);
                    System.out.println(name + " successfully booked seat " + (seatNumber + 1));
                } else {
                    System.out.println("Seat already booked!");
                }
            } else {
                System.out.println("Payment failed for " + name);
            }
        }
    }
}

// === Main UI ===
class RailwayReservationSystem {
    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);
        Train train = new Train(10);

        boolean running = true;

        while (running) {
            System.out.println("\n========= Railway Reservation System =========");
            System.out.println("1. Book Seat");
            System.out.println("2. Cancel Seat");
            System.out.println("3. View Seat Status");
            System.out.println("4. View Bookings");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1 -> {
                    scanner.nextLine();
                    System.out.print("Enter your name: ");
                    String name = scanner.nextLine();

                    System.out.print("Enter seat number (1-10): ");
                    int seat = scanner.nextInt() - 1;

                    System.out.print("Enter payment amount: ");
                    double amount = scanner.nextDouble();

                    Thread bookThread = new Thread(new Passenger(train, seat, name, amount, false));
                    bookThread.start();
                    bookThread.join();
                }
                case 2 -> {
                    scanner.nextLine();
                    System.out.print("Enter your name: ");
                    String name = scanner.nextLine();

                    System.out.print("Enter seat number to cancel (1-10): ");
                    int seat = scanner.nextInt() - 1;

                    Thread cancelThread = new Thread(new Passenger(train, seat, name, 0, true));
                    cancelThread.start();
                    cancelThread.join();
                }
                case 3 -> train.showSeats();
                case 4 -> BookingDAO.showBookings();
                case 5 -> {
                    running = false;
                    System.out.println("Exiting system. Bye!");
                }
                default -> System.out.println("Invalid option. Try again!");
            }
        }
        scanner.close();
    }
}
