package loyalty.persistence;

import loyalty.domain.Customer;

import java.util.ArrayList;
import java.util.List;

// Keeps the data only while the program runs. Good for tests and for trying the program out.
public class MemoryStorage implements DataStorage {
    private List<Customer> saved = new ArrayList<>();

    @Override
    public void save(List<Customer> customers) {
        this.saved = new ArrayList<>(customers);
    }

    @Override
    public List<Customer> load() {
        return new ArrayList<>(this.saved);
    }
}
