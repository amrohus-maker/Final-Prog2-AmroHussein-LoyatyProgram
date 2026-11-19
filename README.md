# Loyalty Program

A console application for a shop. Customers join a loyalty program, collect points when they buy
something, move up from Bronze to Silver to Gold, and spend their points on rewards. All data is
saved in a JSON file, so it is still there the next time the program starts.

This is my project work for Programming 2 (Java OOP, open theme) at the University of Pécs. It is
built step by step over five days, so the status list below shows what is already done.

## Problem statement

**Users.** The cashier at the till and the owner of the shop.

**Goal.** The cashier wants to find a customer, record a purchase and give out a reward in a few
seconds. The owner wants to see who the best customers are.

**Problem.** Paper stamp cards get lost, cannot be searched, and nobody knows how often a customer
really comes back. The program keeps every customer, every purchase and every point in one place,
and the rules (you cannot spend points you do not have, Gold rewards are only for Gold members) are
checked by the program, not by people.

## What the finished program will do

- Register customers (name and email are checked, emails must be unique)
- Record purchases and give points, with a bonus for Silver and Gold customers
- Move customers up a tier automatically
- Redeem rewards (a free item or a discount) when the customer has enough points and the right tier
- Show a customer's profile and history, list customers in different orders, and search
- Save everything in a JSON file and load it again at start
- Show a clear message for every kind of mistake instead of crashing

## Status

- [x] Day 1: Maven project, problem statement, user stories, UML draft, backlog
- [x] Day 2: exception classes and the domain model
- [x] Day 3: the service with all the rules
- [ ] Day 4: JSON storage and the Factory Method pattern
- [ ] Day 5: console menu, final documents and the release

## Technology

Java 17, Maven, Gson (for the JSON file) and JUnit 5 (for the tests).

## Prerequisites

- JDK 17 or newer
- Maven 3.9 or newer (IntelliJ IDEA has Maven built in)

## Build and run

From the project folder:

```bash
mvn clean package
```

```bash
java -jar target/loyalty-program-1.0.0.jar
```

At the moment the program only prints one line to show that the setup works. In IntelliJ you can
also open `src/main/java/loyalty/Main.java` and click the green arrow next to `main`.

## Project structure

The packages are planned like this and will be filled in over the next days:

```
src/main/java/loyalty
  Main.java          starts the program
  domain/            Customer, Tier, Transaction, Reward and its subclasses
  exception/         one class for every kind of error
  service/           LoyaltyService with all the rules
  persistence/       saving and loading (JSON), with the Factory Method pattern
  ui/                the console menu
src/test/java/loyalty    JUnit tests
docs/                    user stories and UML diagrams
```

## Documentation

- [User stories](docs/user-stories.md)
- [UML class diagrams](docs/uml-class-diagram.md)

## Contributors

- Amro Aiman Hussein

## License

MIT, see [LICENSE](LICENSE).
