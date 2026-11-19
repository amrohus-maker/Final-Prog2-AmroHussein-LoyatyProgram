# UML class diagrams (draft)

This is the plan from Day 1. The diagrams are updated whenever the code changes, so that they match
the finished program.

They are written in [Mermaid](https://mermaid.js.org/), so GitHub shows them as pictures and the text
under each picture is the editable source. Notation: `+` public, `-` private, `*` abstract, `$` static.
Plain getters are left out to keep the pictures readable.

## 1. Domain and service

The things the shop talks about, and the class that holds the rules.

```mermaid
classDiagram
    class Customer {
        +int WELCOME_BONUS$
        -int id
        -String name
        -String email
        -String joinedOn
        -int points
        -int totalEarned
        +earn(int, String) void
        +spend(int, String) void
        +getTier() Tier
        +getHistory() List~Transaction~
        +compareTo(Customer) int
    }

    class Transaction {
        -String date
        -int points
        -String description
    }

    class Tier {
        <<enumeration>>
        BRONZE
        SILVER
        GOLD
        -String label
        -int minTotalPoints
        -int bonusPercent
        +forTotalPoints(int)$ Tier
        +isAtLeast(Tier) boolean
    }

    class Comparable~T~ {
        <<interface>>
        +compareTo(T) int
    }

    class Reward {
        <<abstract>>
        -String name
        -int cost
        -Tier minimumTier
        +redeem(Customer) String
        +getBenefitText()* String
    }

    class FreeItemReward {
        -String item
        +getBenefitText() String
    }

    class DiscountReward {
        -int percent
        +getBenefitText() String
    }

    class LoyaltyService {
        +double MAX_PURCHASE$
        -List~Customer~ customers
        -List~Reward~ rewards
        -int nextId
        +register(String, String) Customer
        +findCustomer(int) Customer
        +findCustomer(String) Customer
        +recordPurchase(Customer, double) int
        +redeem(Customer, int) String
        +getCustomers() List~Customer~
        +getRewards() List~Reward~
        +getLeaderboard() List~Customer~
        +getCustomersByName() List~Customer~
        +getCustomersByJoinDate() List~Customer~
        +search(String) List~Customer~
    }

    LoyaltyService "1" o-- "0..*" Customer : customers
    LoyaltyService "1" o-- "1..*" Reward : rewards
    Customer "1" *-- "0..*" Transaction : history
    Customer ..> Tier : getTier
    Reward "0..*" --> "1" Tier : minimumTier
    Reward ..> Customer : redeem

    Comparable <|.. Customer
    Reward <|-- FreeItemReward
    Reward <|-- DiscountReward
```

- `Reward.redeem` is the fixed skeleton: it checks the tier, takes the points, and then returns the
  text of the benefit. The one missing piece is `getBenefitText`, which each subclass fills in.
- The empty diamond is aggregation (the service holds customers and rewards). The filled diamond is
  composition (a transaction only exists as part of one customer).

## 2. Storage and the Factory Method pattern

How the data is saved. `Main` and the service only use the abstract types, never `new JsonStorage()`.

```mermaid
classDiagram
    class DataStorage {
        <<interface>>
        +save(List~Customer~) void
        +load() List~Customer~
    }

    class JsonStorage {
        -String filePath
        +save(List~Customer~) void
        +load() List~Customer~
    }

    class MemoryStorage {
        -List~Customer~ saved
        +save(List~Customer~) void
        +load() List~Customer~
    }

    class StorageFactory {
        <<abstract>>
        +createStorage()* DataStorage
    }

    class JsonStorageFactory {
        -String filePath
        +createStorage() DataStorage
    }

    class MemoryStorageFactory {
        +createStorage() DataStorage
    }

    class LoyaltyService {
        -DataStorage storage
    }

    class Main {
        +main(String[])$ void
    }

    DataStorage <|.. JsonStorage
    DataStorage <|.. MemoryStorage
    StorageFactory <|-- JsonStorageFactory
    StorageFactory <|-- MemoryStorageFactory
    StorageFactory ..> DataStorage : creates
    JsonStorageFactory ..> JsonStorage : creates
    MemoryStorageFactory ..> MemoryStorage : creates
    LoyaltyService "1" --> "1" DataStorage : storage
    Main ..> StorageFactory : uses
    Main ..> LoyaltyService : creates
```

| Role in the Factory Method pattern | Class |
| --- | --- |
| Product (interface) | `DataStorage` |
| Concrete products | `JsonStorage`, `MemoryStorage` |
| Creator (abstract) | `StorageFactory` with the factory method `createStorage()` |
| Concrete creators | `JsonStorageFactory`, `MemoryStorageFactory` |

## 3. Exceptions

Every kind of error has its own class. All of them extend `LoyaltyException`, which is a checked
exception, so the compiler makes the caller handle them.

```mermaid
classDiagram
    class Exception
    class LoyaltyException
    class InvalidNameException
    class InvalidEmailException
    class InvalidAmountException
    class InvalidChoiceException
    class DuplicateEmailException
    class CustomerNotFoundException
    class RewardNotFoundException
    class InsufficientPointsException {
        -int needed
        -int available
        +getMissing() int
    }
    class TierTooLowException {
        -String tierName
    }
    class StorageException

    Exception <|-- LoyaltyException
    LoyaltyException <|-- InvalidNameException
    LoyaltyException <|-- InvalidEmailException
    LoyaltyException <|-- InvalidAmountException
    LoyaltyException <|-- InvalidChoiceException
    LoyaltyException <|-- DuplicateEmailException
    LoyaltyException <|-- CustomerNotFoundException
    LoyaltyException <|-- RewardNotFoundException
    LoyaltyException <|-- InsufficientPointsException
    LoyaltyException <|-- TierTooLowException
    LoyaltyException <|-- StorageException
```
