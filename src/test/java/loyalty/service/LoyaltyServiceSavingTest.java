package loyalty.service;

import loyalty.domain.Customer;
import loyalty.exception.InsufficientPointsException;
import loyalty.exception.InvalidAmountException;
import loyalty.exception.InvalidEmailException;
import loyalty.exception.RewardNotFoundException;
import loyalty.exception.StorageException;
import loyalty.persistence.DataStorage;
import loyalty.persistence.JsonStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoyaltyServiceSavingTest {
    @TempDir
    Path folder;

    // a storage that counts how often it was asked to save
    private static class RecordingStorage implements DataStorage {
        private int saveCount = 0;
        private List<Customer> saved = new ArrayList<>();

        @Override
        public void save(List<Customer> customers) {
            this.saveCount++;
            this.saved = new ArrayList<>(customers);
        }

        @Override
        public List<Customer> load() {
            return new ArrayList<>(this.saved);
        }
    }

    // a storage that always fails, like a full disk
    private static class FailingStorage implements DataStorage {
        @Override
        public void save(List<Customer> customers) throws StorageException {
            throw new StorageException("The disk is full");
        }

        @Override
        public List<Customer> load() {
            return new ArrayList<>();
        }
    }

    @Test
    void theServiceLoadsTheSavedCustomersWhenItStarts() throws Exception {
        Customer old = new Customer(7, "Old Timer", "old@example.com");
        old.earn(50, "Welcome bonus");
        RecordingStorage storage = new RecordingStorage();
        List<Customer> list = new ArrayList<>();
        list.add(old);
        storage.save(list);

        LoyaltyService service = new LoyaltyService(storage);

        assertEquals(1, service.getCustomers().size());
        assertSame(old, service.findCustomer(7));
        assertEquals(8, service.register("New Person", "new@example.com").getId()); // ids go on after the highest one
    }

    @Test
    void startingTheServiceDoesNotSaveAnything() throws Exception {
        RecordingStorage storage = new RecordingStorage();
        new LoyaltyService(storage);
        assertEquals(0, storage.saveCount);
    }

    @Test
    void registeringSaves() throws Exception {
        RecordingStorage storage = new RecordingStorage();
        LoyaltyService service = new LoyaltyService(storage);

        service.register("Ann Lee", "ann@example.com");

        assertEquals(1, storage.saveCount);
        assertEquals(1, storage.load().size());
    }

    @Test
    void aPurchaseThatEarnsPointsSaves() throws Exception {
        RecordingStorage storage = new RecordingStorage();
        LoyaltyService service = new LoyaltyService(storage);
        Customer ann = service.register("Ann Lee", "ann@example.com");

        service.recordPurchase(ann, 20.0);

        assertEquals(2, storage.saveCount);
    }

    @Test
    void aPurchaseThatEarnsNothingDoesNotSave() throws Exception {
        RecordingStorage storage = new RecordingStorage();
        LoyaltyService service = new LoyaltyService(storage);
        Customer ann = service.register("Ann Lee", "ann@example.com");

        service.recordPurchase(ann, 0.5);

        assertEquals(1, storage.saveCount); // only the registering
    }

    @Test
    void redeemingSaves() throws Exception {
        RecordingStorage storage = new RecordingStorage();
        LoyaltyService service = new LoyaltyService(storage);
        Customer ann = service.register("Ann Lee", "ann@example.com");
        service.recordPurchase(ann, 100.0); // 150 points

        service.redeem(ann, 1);

        assertEquals(3, storage.saveCount);
    }

    @Test
    void actionsThatAreRefusedDoNotSave() throws Exception {
        RecordingStorage storage = new RecordingStorage();
        LoyaltyService service = new LoyaltyService(storage);
        Customer ann = service.register("Ann Lee", "ann@example.com"); // 50 points, one save

        assertThrows(InvalidEmailException.class, () -> service.register("Bob", "nope"));
        assertThrows(InvalidAmountException.class, () -> service.recordPurchase(ann, -5));
        assertThrows(RewardNotFoundException.class, () -> service.redeem(ann, 99));
        assertThrows(InsufficientPointsException.class, () -> service.redeem(ann, 1)); // the coffee costs 60

        assertEquals(1, storage.saveCount);
    }

    @Test
    void lookingAtTheDataNeverSaves() throws Exception {
        RecordingStorage storage = new RecordingStorage();
        LoyaltyService service = new LoyaltyService(storage);
        service.register("Ann Lee", "ann@example.com");

        service.findCustomer(1);
        service.getCustomers();
        service.getLeaderboard();
        service.getCustomersByName();
        service.getCustomersByJoinDate();
        service.search("ann");
        service.getRewards();

        assertEquals(1, storage.saveCount);
    }

    @Test
    void aStorageThatFailsIsReported() throws Exception {
        LoyaltyService service = new LoyaltyService(new FailingStorage());

        StorageException e = assertThrows(StorageException.class, () -> service.register("Ann Lee", "ann@example.com"));

        assertEquals("The disk is full", e.getMessage());
    }

    @Test
    void aDamagedFileStopsTheServiceFromStarting() throws Exception {
        Path file = folder.resolve("loyalty.json");
        Files.writeString(file, "this is not json");

        assertThrows(StorageException.class, () -> new LoyaltyService(new JsonStorage(file.toString())));
        assertEquals("this is not json", Files.readString(file)); // and the damaged file was not touched
    }

    @Test
    void theDataSurvivesARestartWithAJsonFile() throws Exception {
        String path = folder.resolve("data").resolve("loyalty.json").toString();

        LoyaltyService first = new LoyaltyService(new JsonStorage(path));
        Customer ann = first.register("Ann Lee", "ann@example.com");
        first.register("Bob Ray", "bob@example.com");
        first.recordPurchase(ann, 100.0);
        first.redeem(ann, 1); // 50 + 100 - 60 = 90 points

        LoyaltyService second = new LoyaltyService(new JsonStorage(path));
        Customer again = second.findCustomer("ann@example.com");

        assertEquals(2, second.getCustomers().size());
        assertEquals(90, again.getPoints());
        assertEquals(150, again.getTotalEarned());
        assertEquals(ann.getHistory().size(), again.getHistory().size());
        assertEquals(3, second.register("Cy Doe", "cy@example.com").getId()); // ids continue
    }
}
