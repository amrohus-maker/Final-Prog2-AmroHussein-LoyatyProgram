package loyalty.persistence;

import loyalty.domain.Customer;
import loyalty.exception.StorageException;

import java.util.List;

// The common type for every way of saving the data. The rest of the program only knows this interface.
public interface DataStorage {
    void save(List<Customer> customers) throws StorageException;

    // gives an empty list when nothing was saved yet
    List<Customer> load() throws StorageException;
}
