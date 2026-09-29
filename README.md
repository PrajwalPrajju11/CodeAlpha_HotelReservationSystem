# Hotel Reservation System (Java)

A console-based hotel reservation system built in Java as part of the **CodeAlpha Java Programming Internship (Task 4)**.

## Features

- Search available rooms by category and date range
- Room categories: **Standard**, **Deluxe**, **Suite**
- Make and cancel reservations
- Prevents double booking of a room for overlapping dates
- Payment simulation (Card, UPI, Cash)
- Booking details view (single booking or all bookings)
- Data persistence using file I/O (`bookings.txt`)

## Room Details

| Category | Rooms        | Price per Night |
|----------|--------------|-----------------|
| Standard | 101 - 105    | Rs. 2000        |
| Deluxe   | 201 - 204    | Rs. 3500        |
| Suite    | 301 - 302    | Rs. 6000        |

## Concepts Used

- Object-Oriented Programming (`Room`, `Reservation`, `Hotel` classes and a `Category` enum)
- Collections (`ArrayList`)
- File I/O (`BufferedReader`, `FileWriter`)
- `java.time.LocalDate` for date handling
- Input validation and exception handling

## How to Run

**Requirements:** JDK 8 or higher

```bash
javac HotelReservationSystem.java
java HotelReservationSystem
```

## Sample Usage

```
===== HOTEL RESERVATION SYSTEM =====

1. Search available rooms
2. Book a room
3. Cancel a reservation
4. View booking details
5. View all bookings
0. Exit
```

**Booking a room:**

```
Guest name: Rahul Sharma
Category 1) Standard 2) Deluxe 3) Suite 4) All: 1
Check-in (YYYY-MM-DD): 2026-10-10
Check-out (YYYY-MM-DD): 2026-10-13
Enter room number to book: 101
Payment method: 1) Credit/Debit Card  2) UPI  3) Cash at hotel
```

> Dates must be entered in **YYYY-MM-DD** format.

## Project Structure

```
CodeAlpha_HotelReservationSystem/
├── HotelReservationSystem.java
├── README.md
└── bookings.txt   (auto-generated on first booking)
```

## Author

**Prajwal H C**
CodeAlpha Java Programming Intern
