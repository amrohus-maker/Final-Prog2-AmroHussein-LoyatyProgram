package loyalty.persistence;

import loyalty.domain.Customer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageFactoryTest {
    @TempDir
    Path folder;

    @Test
    void aMemoryFactoryCreatesAMemoryStorage() {
        StorageFactory factory = new MemoryStorageFactory();
        DataStorage storage = factory.createStorage();
        assertTrue(storage instanceof MemoryStorage);
    }

    @Test
    void aJsonFactoryCreatesAJsonStorage() {
        StorageFactory factory = new JsonStorageFactory(folder.resolve("data.json").toString());
        DataStorage storage = factory.createStorage();
        assertTrue(storage instanceof JsonStorage);
    }

    @Test
    void everyFactoryGivesAStorageThatWorksTheSameWay() throws Exception {
        // both factories are used through the parent type StorageFactory
        List<StorageFactory> factories = new ArrayList<>();
        factories.add(new MemoryStorageFactory());
        factories.add(new JsonStorageFactory(folder.resolve("data.json").toString()));

        Customer ann = new Customer(1, "Ann Lee", "ann@example.com");
        ann.earn(50, "Welcome bonus");

        for (StorageFactory factory : factories) {
            DataStorage storage = factory.createStorage();
            List<Customer> list = new ArrayList<>();
            list.add(ann);
            storage.save(list);

            List<Customer> loaded = storage.load();

            assertEquals(1, loaded.size());
            assertEquals("Ann Lee", loaded.get(0).getName());
            assertEquals(50, loaded.get(0).getPoints());
        }
    }

    @Test
    void everyCallCreatesANewStorage() {
        StorageFactory factory = new MemoryStorageFactory();
        assertNotSame(factory.createStorage(), factory.createStorage());
    }

    @Test
    void aJsonFactoryWithoutAPathFailsWhenItCreatesTheStorage() {
        StorageFactory factory = new JsonStorageFactory("");
        assertThrows(IllegalArgumentException.class, factory::createStorage);
    }
}
