package loyalty.persistence;

import loyalty.domain.Customer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemoryStorageTest {
    @Test
    void aNewStorageIsEmpty() {
        assertTrue(new MemoryStorage().load().isEmpty());
    }

    @Test
    void whatWeSaveWeLoadAgain() throws Exception {
        Customer ann = new Customer(1, "Ann Lee", "ann@example.com");
        Customer bob = new Customer(2, "Bob Ray", "bob@example.com");
        List<Customer> list = new ArrayList<>();
        list.add(ann);
        list.add(bob);

        MemoryStorage storage = new MemoryStorage();
        storage.save(list);
        List<Customer> loaded = storage.load();

        assertEquals(2, loaded.size());
        assertSame(ann, loaded.get(0));
        assertSame(bob, loaded.get(1));
    }

    @Test
    void savingKeepsACopyOfTheList() throws Exception {
        List<Customer> list = new ArrayList<>();
        list.add(new Customer(1, "Ann Lee", "ann@example.com"));

        MemoryStorage storage = new MemoryStorage();
        storage.save(list);
        list.clear(); // changing our list afterwards must not change what was saved

        assertEquals(1, storage.load().size());
    }

    @Test
    void loadingGivesACopyOfTheList() throws Exception {
        List<Customer> list = new ArrayList<>();
        list.add(new Customer(1, "Ann Lee", "ann@example.com"));
        MemoryStorage storage = new MemoryStorage();
        storage.save(list);

        storage.load().clear();

        assertEquals(1, storage.load().size());
    }
}
