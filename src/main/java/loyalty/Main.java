package loyalty;

import loyalty.exception.StorageException;
import loyalty.persistence.JsonStorageFactory;
import loyalty.persistence.MemoryStorageFactory;
import loyalty.persistence.StorageFactory;
import loyalty.service.LoyaltyService;

public class Main {
    private static final String DATA_FILE = "data/loyalty.json";

    public static void main(String[] args) {
        // We only use the abstract StorageFactory here, never "new JsonStorage".
        StorageFactory factory = chooseFactory(args);
        try {
            LoyaltyService service = new LoyaltyService(factory.createStorage());
            // the console menu is added on Day 5
            System.out.println("Loyalty Program - " + service.getCustomers().size() + " customers loaded");
        } catch (StorageException e) {
            System.out.println("[error] " + e.getMessage());
        }
    }

    // "--memory" keeps the data in memory only, without any file
    private static StorageFactory chooseFactory(String[] args) {
        for (String arg : args) {
            if (arg.equals("--memory")) {
                return new MemoryStorageFactory();
            }
        }
        return new JsonStorageFactory(DATA_FILE);
    }
}
