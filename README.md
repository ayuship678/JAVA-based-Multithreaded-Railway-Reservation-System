📌 Multi-threaded Railway Reservation System

📌 Project Overview
This Java-based console application simulates a real-world railway ticket booking system. The system is capable of handling multiple user requests simultaneously using Java's multithreading and synchronization mechanisms.

🎯 Core Functionalities
✅ Seat Selection: Users can book available seats.
❌ Cancellation: Cancel previously booked tickets.
💳 Payment Validation: Ensures payment before booking.
🧵 Thread Safety: Prevents overbooking with synchronized blocks.


🔧 Technologies Used
Technology	Purpose
Java	Core programming, threading, synchronization
File I/O	Storing booking records
DAO Pattern	Decoupled logic for booking management
Multithreading	Simulating multiple users
Git/GitHub	Version control

🛠️ System Workflow
User interacts with the CLI menu.
Requests to book or cancel seats are managed by threads.
Synchronization ensures no two users book the same seat.
Payment is validated before confirming a seat.


Bookings are saved to a file bookings.txt.
RailwayReservationSystem.java      # Main source code file
bookings.txt                       # Booking records (auto-created)
README.md                          # Project overview and instructions

🚀 How to Run
Compile the project:

javac RailwayReservationSystem.java
Run the application:

java RailwayReservationSystem

Project Repository: 
