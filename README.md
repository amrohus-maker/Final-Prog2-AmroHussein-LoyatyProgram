# Loyalty Program

A console application for a shop. Customers join a loyalty program, collect points when they buy
something, move up from Bronze to Silver to Gold, and spend their points on rewards. All data is
saved in a JSON file, so it is still there the next time the program starts.

This is my project work for Programming 2 (Java OOP, open theme) at the University of Pécs.

## Problem statement

**Users.** The cashier at the till and the owner of the shop.

**Goal.** The cashier wants to find a customer, record a purchase and give out a reward in a few
seconds. The owner wants to see who the best customers are.

**Problem.** Paper stamp cards get lost, cannot be searched, and nobody knows how often a customer
really comes back. The program keeps every customer, every purchase and every point in one place,
and the rules (you cannot spend points you do not have, Gold rewards are only for Gold members) are
checked by the program, not by people.

## What the program does

- Registers customers (name and email are checked, emails must be unique, 50 welcome points)
- Records purchases and gives points, with a bonus for Silver and Gold customers
- Moves customers up a tier automatically and says so
- Redeems rewards (a free item or a discount) when the customer has enough points and the right tier
- Shows a customer's profile and history, lists customers in different orders, and searches
- Saves everything in a JSON file after every change and loads it again at the start
- Shows a clear message for every kind of mistake instead of crashing

## Status

Version 1.0.0 is complete. It was built step by step over five days:

- [x] Day 1: Maven project, problem statement, user stories, UML draft, backlog
- [x] Day 2: exception classes and the domain model
- [x] Day 3: the service with all the rules
- [x] Day 4: JSON storage and the Factory Method pattern
- [x] Day 5: console menu, final documents and the release

## Technology

Java 17, Maven, Gson (for the JSON file) and JUnit 5 (for the tests).

## Requirements to run it

- JDK 17 or newer
- Maven 3.9 or newer (IntelliJ IDEA has Maven built in)

## Build and run

From the project folder:

```bash
mvn clean package
```

This compiles the code, runs all the tests and creates `target/loyalty-program-1.0.0.jar`. Gson is
packed into that jar, so it runs on its own:

```bash
java -jar target/loyalty-program-1.0.0.jar
```

To start without the JSON file, keeping the data only in memory (nothing is written to the disk):

```bash
java -jar target/loyalty-program-1.0.0.jar --memory
```

To run only the tests:

```bash
mvn test
```

In IntelliJ you can also open `src/main/java/loyalty/Main.java` and click the green arrow next to `main`.

## Using the program

The program shows a menu. You type the number and press Enter.

| Choice | What it does |
| --- | --- |
| 1 | Register a customer: asks for a name and an email. The customer gets 50 welcome points. |
| 2 | Record a purchase: asks for the customer (id or email) and the amount. Shows the points earned and the new balance. |
| 3 | Redeem a reward: asks for the customer, shows the rewards and asks for the number of the reward. |
| 4 | Show a customer: the profile and the history of earned and spent points. |
| 5 | Search customers by a part of the name or the email. |
| 6 | List customers: the leaderboard, A to Z by name, or the newest members first. |
| 7 | Show the rewards. |
| 0 | Exit. |

**Points and tiers.** A purchase gives 1 point for every whole currency unit. The tier depends on the
total points a customer has ever earned, so spending points never lowers it.

| Tier | Total points needed | Bonus on purchases |
| --- | --- | --- |
| Bronze | 0 | none |
| Silver | 300 | +10% |
| Gold | 1000 | +25% |

**Rewards.**

| Number | Reward | Cost | Tier needed |
| --- | --- | --- | --- |
| 1 | Free coffee | 60 points | Bronze |
| 2 | Free pastry | 120 points | Bronze |
| 3 | 10% discount | 150 points | Bronze |
| 4 | Free lunch | 350 points | Silver |
| 5 | 25% discount | 500 points | Gold |
| 6 | Gift box | 700 points | Gold |

### Sample session

This is a real run. The program prints the menu before every choice; here it is shown only the first
time to keep the sample short. Everything after a prompt like `Your choice:` is what the user typed.

```
Loyalty Program - 0 customers loaded

=== Loyalty Program ===
1. Register a customer
2. Record a purchase
3. Redeem a reward
4. Show a customer
5. Search customers
6. List customers
7. Show the rewards
0. Exit
Your choice: 1
Name: Ann Lee
Email: ann@example.com
Registered #1 Ann Lee (Bronze, 50 points)
The welcome bonus of 50 points is already added.

[the menu is shown again]
Your choice: 1
Name: Bob Ray
Email: bob@example
[error] That does not look like an email address: bob@example

[the menu is shown again]
Your choice: 2
Customer id or email: 1
Customer: #1 Ann Lee (Bronze, 50 points)
Purchase amount: 250
Points earned: 250
New balance: 300 points
Level up! Ann Lee is now Silver.

[the menu is shown again]
Your choice: 3
Customer id or email: ann@example.com
Customer: #1 Ann Lee (Silver, 300 points)
Rewards:
  1. Free coffee      60 points  needs Bronze  One free coffee
  2. Free pastry     120 points  needs Bronze  One free pastry
  3. 10% discount    150 points  needs Bronze  10% off your next order
  4. Free lunch      350 points  needs Silver  One free lunch menu
  5. 25% discount    500 points  needs Gold    25% off your next order
  6. Gift box        700 points  needs Gold    One free gift box
Reward number: 1
Redeemed: One free coffee
New balance: 240 points

[the menu is shown again]
Your choice: 3
Customer id or email: 1
Customer: #1 Ann Lee (Silver, 240 points)
[the rewards list is shown again]
Reward number: 5
[error] This reward needs the Gold tier or higher

[the menu is shown again]
Your choice: 4
Customer id or email: 1

#1 Ann Lee
Email:     ann@example.com
Joined on: 2026-11-21
Tier:      Silver
Points:    240 (300 earned in total)
History:
  2026-11-21    +50  Welcome bonus
  2026-11-21   +250  Purchase $250.00
  2026-11-21    -60  Redeemed: Free coffee

[the menu is shown again]
Your choice: 0
Goodbye!
```

Start the program again and Ann Lee is still there, because everything was saved in `data/loyalty.json`.
When the input ends (for example with Ctrl+D or Ctrl+Z) the program says goodbye and stops.

## Course requirements and where they are met

What the course asks for and where it can be found:

| Requirement | Where it is met |
| --- | --- |
| Maven project | `pom.xml` (Java 17, Gson, JUnit 5, one runnable jar) |
| Console user interface | `ui/ConsoleApp` (the menu), started by `Main` |
| One named design pattern | **Factory Method** for the storage, see below |
| Abstract skeleton and inheritance | abstract class `Reward` with `FreeItemReward` and `DiscountReward` |
| Interface and polymorphism | interface `DataStorage` with `JsonStorage` and `MemoryStorage` |
| Collections, `Comparable`, `Comparator`, lambda | `Customer` is `Comparable` (leaderboard), a `Comparator` sorts by name, a lambda sorts by join date |
| Overloading | `LoyaltyService.findCustomer(int id)` and `findCustomer(String email)` |
| Own exception classes, shown to the user | 11 classes in `exception/`, see below; `ConsoleApp` shows every one |
| Data saved in a file, with try-with-resources | `JsonStorage` writes and reads `data/loyalty.json` |
| Unit tests | JUnit 5 in `src/test/java`, run with `mvn test` |
| UML diagrams | [docs/uml-class-diagram.md](docs/uml-class-diagram.md) |
| User stories | [docs/user-stories.md](docs/user-stories.md) |
| Issues, branches, pull requests | on GitHub: issues #1 to #5 as the backlog, a branch for every piece of work, merged by pull requests |
| `.gitignore` and `LICENSE` | `.gitignore` (`.idea`, `*.iml`, `target`, `out`, `data`) and MIT |
| Release | tag `v1.0.0` and [docs/release-notes.md](docs/release-notes.md) |

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

## Exceptions

Every kind of mistake has its own class. All of them extend `LoyaltyException`, which is a checked
exception, so the compiler makes sure the menu handles them.

| Exception | Shown when |
| --- | --- |
| `InvalidNameException` | the name is empty |
| `InvalidEmailException` | the email is not like `name@site.com` (for example no `@`, no dot after the `@`, or a space) |
| `DuplicateEmailException` | the email is already registered |
| `InvalidAmountException` | the purchase amount is not a number, is 0 or less, or is above 100000 |
| `InvalidChoiceException` | a menu choice is not on the list, or a reward number is not a number |
| `CustomerNotFoundException` | no customer has that id or email |
| `RewardNotFoundException` | there is no reward with that number |
| `InsufficientPointsException` | the customer has too few points (the message says how many are missing) |
| `TierTooLowException` | the reward needs a higher tier |
| `StorageException` | the data file cannot be read or written, or it is damaged |

`ConsoleApp` catches them in one place, `handle`, and prints `[error]` and the message. After the message
the menu comes back. `IllegalArgumentException` is used only when our own code is wrong (for example a
reward that costs 0 points). Typing can never cause it, so it is never shown to the user.

## Design decisions

- **Layers.** `ConsoleApp` only reads and prints. The rules are in `LoyaltyService` and the domain
  classes. Files are only touched in `persistence`. Because of this, each part can be tested alone.
- **The menu gets its keyboard and screen from outside.** `ConsoleApp` receives a `Scanner` and a
  `PrintStream` in its constructor. `Main` gives it the real ones; `ConsoleAppTest` gives it a text and
  reads what it prints, so the whole menu is tested without a keyboard.
- **The tier depends on the points ever earned**, not on the balance. A customer who spends points
  does not drop down a tier.
- **Dates are saved as text** (`2026-11-21`). Gson can read and write text without extra work.
- **Emails are saved in lower case**, so `Ann@Example.com` and `ann@example.com` are the same customer.
- **A damaged data file stops the program** instead of starting empty, because starting empty would
  overwrite the file and lose the customers.

## Tests

`mvn test` runs the JUnit 5 tests. They cover the domain rules, every exception, the JSON file
(including damaged files), the service (including what is saved) and the whole console menu. The tests
use the memory storage, so they never write files.

## Project structure

```
src/main/java/loyalty
  Main.java          starts the program
  domain/            Customer, Tier, Transaction, Reward and its subclasses
  exception/         one class for every kind of error
  service/           LoyaltyService with all the rules
  persistence/       DataStorage, JsonStorage, MemoryStorage and the storage factories
  ui/                ConsoleApp, the console menu
src/test/java/loyalty    JUnit tests
docs/                    user stories, UML diagrams and release notes
```

## Known limitations

- One program at a time: the data file is read at the start and written after every change, so two
  programs using the same file would overwrite each other.
- If saving fails (for example the disk is full), the message is shown and the change stays in memory
  until the program closes. The next successful change saves everything again.
- The rewards are fixed in the code. Changing or adding rewards inside the program is a "could have"
  user story (US-16).
- Customers cannot be edited or deleted (US-14 is a "could have").
- Points come only from whole currency units: a purchase of 12.99 gives 12 points.
- Letters that the console cannot show (for example some accents) may look wrong in the menu. This
  depends on the console, not on the program.

## Documentation

- [User stories](docs/user-stories.md)
- [UML class diagrams](docs/uml-class-diagram.md)
- [Release notes](docs/release-notes.md)

## Contributors

- Amro Aiman Hussein

## License

MIT, see [LICENSE](LICENSE).
