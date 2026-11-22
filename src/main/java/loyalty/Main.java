package loyalty;

import loyalty.exception.StorageException;
import loyalty.persistence.JsonStorageFactory;
import loyalty.persistence.MemoryStorageFactory;
import loyalty.persistence.StorageFactory;
import loyalty.service.LoyaltyService;
import loyalty.ui.ConsoleApp;

import java.util.Scanner;

public class Main {
    private static final String DATA_FILE = "data/loyalty.json";

    public static void main(String[] args) {
        // We only use the abstract StorageFactory here, never "new JsonStorage".
        StorageFactory factory = chooseFactory(args);
        try (Scanner scanner = new Scanner(System.in)) {
            LoyaltyService service = new LoyaltyService(factory.createStorage());
            System.out.println("Loyalty Program - " + service.getCustomers().size() + " customers loaded");
            new ConsoleApp(service, scanner, System.out).run();
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
