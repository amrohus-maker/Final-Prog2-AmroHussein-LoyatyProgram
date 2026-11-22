# Release notes

## v1.0.0

The first complete version of the Loyalty Program, a console application for a shop. Customers join,
collect points when they buy, move up from Bronze to Silver to Gold and spend their points on rewards.
All data is saved in a JSON file.

### What you can do

- Register customers: the name and the email are checked, emails are unique, and every new customer
  gets 50 welcome points.
- Record purchases: 1 point for every whole currency unit, plus 10% for Silver and 25% for Gold. The
  tier changes by itself and the program says so.
- Redeem six rewards (free items and discounts). Clear messages say when the points or the tier are
  not enough.
- Show a customer with the full history, search by a part of a name or email, and list the customers
  by points, by name or by join date.
- Save the data in `data/loyalty.json` after every change and load it at the start. With `--memory`
  nothing is written to the disk.
- Every kind of mistake has its own exception class and a clear `[error]` message. The program does
  not crash.

### How to run

You need JDK 17 or newer and Maven.

```bash
mvn clean package
java -jar target/loyalty-program-1.0.0.jar
```

### What is inside

- Design pattern: Factory Method (`StorageFactory`, `JsonStorageFactory`, `MemoryStorageFactory`)
- Abstract class `Reward` with `FreeItemReward` and `DiscountReward`
- 11 exception classes that extend `LoyaltyException`
- 133 JUnit 5 tests
- User stories and UML diagrams in `docs/`

### Known limitations

- One program at a time: two programs using the same data file would overwrite each other.
- The rewards are fixed in the code, and customers cannot be edited or deleted.
- If saving fails, the message is shown and the change stays in memory until the program closes.

The full list is in the [README](../README.md).
