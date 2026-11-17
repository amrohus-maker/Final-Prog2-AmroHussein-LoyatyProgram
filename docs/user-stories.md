# User stories

The people who use the program:

- **The cashier** works at the till. They want things to be quick and do not want to remember rules.
- **The owner** decides how the program is run and wants to know who the best customers are.

The customer never uses the program. The cashier uses it while the customer is at the till.

Priority: **M** = must have for version 1.0.0, **C** = could have later.

## Must have

| ID | Story | Acceptance criteria | Prio |
| --- | --- | --- | --- |
| US-01 | As a cashier, I want to register a new customer so that they can start collecting points. | A name and an email are required. An empty name shows "invalid name". An email without `@` or a dot shows "invalid email". An email that is already registered shows "duplicate email". A new customer gets 50 welcome points. | M |
| US-02 | As a cashier, I want to record a purchase so that the customer gets points. | The amount must be a number above 0, otherwise "invalid amount" is shown. The points are 1 per whole currency unit, plus 10% for Silver and 25% for Gold customers. The receipt shows the points earned and the new balance. | M |
| US-03 | As a cashier, I want the customer's tier to change by itself so that I never have to calculate it. | Bronze starts at 0 total points, Silver at 300 and Gold at 1000. Spending points never lowers the tier. A level-up message is shown when a purchase moves the customer up. | M |
| US-04 | As a cashier, I want to redeem a reward for a customer so that they get what they saved for. | The points are taken from the balance and the benefit is shown (for example "One free coffee" or "10% off your next order"). The new balance is shown. | M |
| US-05 | As a cashier, I want to know why a reward cannot be redeemed so that I can tell the customer. | Too few points shows "insufficient points" and how many are missing. A tier that is too low shows "tier too low" and which tier is needed. An unknown reward number shows "reward not found". The balance does not change. | M |
| US-06 | As a cashier, I want to open a customer's profile so that I can see their status. | Shows name, email, id, join date, points, total points, tier and the history of earned and spent points. | M |
| US-07 | As a cashier, I want to find a customer by id or email, or search by part of a name, so that no card is needed. | An id or a full email opens the customer. An unknown id or email shows "customer not found". A search lists every name or email that contains the text. | M |
| US-08 | As the owner, I want to list the customers in different orders so that I can see my best customers. | The leaderboard (most total points first), A to Z by name, and newest members first. | M |
| US-09 | As a cashier, I want to see the rewards catalog so that I can tell customers what they can save for. | Each reward shows its name, its cost, the tier it needs and what the customer gets. | M |
| US-10 | As a user of the menu, I want a clear message when I type something wrong so that the program never crashes. | A menu choice that is not on the list shows "invalid choice" and the menu is shown again. Every error in the program has its own message. | M |
| US-11 | As the owner, I want the data to be saved so that nothing is lost when the program closes. | Every change is saved in a JSON file. At the next start the customers, their points and their history are back. | M |
| US-12 | As the owner, I want a clear message when the data file cannot be read so that I know what went wrong. | A missing or damaged JSON file shows a "storage" error and does not crash the program. A damaged file is not overwritten without a warning. | M |
| US-13 | As the owner, I want to choose where the data is kept when the program starts so that I can try it without writing a file. | By default the data is saved in the JSON file. Starting with `--memory` keeps the data only in memory. | M |

## Could have later

| ID | Story | Prio |
| --- | --- | --- |
| US-14 | As a cashier, I want to correct a customer's name or email so that typing mistakes can be fixed. | C |
| US-15 | As the owner, I want points to expire after a year so that old points do not pile up. | C |
| US-16 | As the owner, I want to add my own rewards inside the program instead of in the code. | C |
