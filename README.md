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
- [x] Day 4: JSON storage and the Factory Method pattern
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

At the moment the program loads the saved customers and prints how many there are. The console menu
comes on Day 5. To start without the JSON file, keeping the data only in memory:

```bash
java -jar target/loyalty-program-1.0.0.jar --memory
```

In IntelliJ you can also open `src/main/java/loyalty/Main.java` and click the green arrow next to `main`.

## Design pattern: Factory Method

**Problem.** The program has to save its data, but *how* can change: a JSON file for real use, only the
memory for tests and for trying the program out, maybe a database later. If `LoyaltyService` or `Main`
wrote `new JsonStorage(...)` themselves, every change of storage would mean changing the code that uses
it, and the tests would write real files.

**Solution.** Creating the storage is moved into factory classes. The program only talks to the abstract
types `StorageFactory` and `DataStorage`.

| Role in the pattern | Class |
| --- | --- |
| Product (interface) | `DataStorage` |
| Concrete products | `JsonStorage`, `MemoryStorage` |
| Creator (abstract class) | `StorageFactory`, with the factory method `createStorage()` |
| Concrete creators | `JsonStorageFactory`, `MemoryStorageFactory` |
| Client | `Main` chooses the factory; `LoyaltyService` only uses the `DataStorage` it is given |

`Main` picks the factory when the program starts: `JsonStorageFactory` normally, `MemoryStorageFactory`
when you start with `--memory`. Nothing else in the program changes.

**Benefit.**

- A new way of saving, for example a CSV file, is two new classes: a storage and its factory.
  `LoyaltyService` does not change.
- The tests of the service get their storage from `MemoryStorageFactory`, so they do not touch the disk.
  `StorageFactoryTest` checks that every factory gives a storage that works in the same way.
- The decision about the storage is made in one place, `Main.chooseFactory`.

## The JSON file

The data is saved in `data/loyalty.json`, next to the place where you start the program, after every
change. The `data` folder is in `.gitignore`, because the file holds customer names and emails. This is
what a customer looks like in the file (the other customers follow in the same way):

```json
[
  {
    "id": 1,
    "name": "Ann Lee",
    "email": "ann@example.com",
    "joinedOn": "2026-11-21",
    "points": 110,
    "totalEarned": 170,
    "history": [
      { "date": "2026-11-21", "points": 50, "description": "Welcome bonus" },
      { "date": "2026-11-21", "points": 120, "description": "Purchase $120.50" },
      { "date": "2026-11-21", "points": -60, "description": "Redeemed: Free coffee" }
    ]
  }
]
```

- The file is written with Gson. `JsonStorage` opens it in a try-with-resources block, so it is always closed.
- If there is no file yet, the program starts empty.
- Gson does not call our constructors, so `JsonStorage` checks every loaded customer with
  `Customer.isValid()`: the name and email must be correct, the ids and emails must be unique, and the
  points must be what the history adds up to.
- If the file is damaged, the program shows a `StorageException` message and stops. It does not touch the
  file, so nothing is overwritten. Fix or delete the file and start again.

## Project structure

The packages (the `ui` package is added on Day 5):

```
src/main/java/loyalty
  Main.java          starts the program
  domain/            Customer, Tier, Transaction, Reward and its subclasses
  exception/         one class for every kind of error
  service/           LoyaltyService with all the rules
  persistence/       DataStorage, JsonStorage, MemoryStorage and the storage factories
  ui/                the console menu (Day 5)
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
