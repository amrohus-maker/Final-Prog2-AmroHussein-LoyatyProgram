package loyalty.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import loyalty.domain.Customer;
import loyalty.exception.StorageException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// Saves the customers in a JSON file and reads them back. Gson does the converting.
public class JsonStorage implements DataStorage {
    private final String filePath;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public JsonStorage(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("The file path cannot be empty");
        }
        this.filePath = filePath;
    }

    @Override
    public void save(List<Customer> customers) throws StorageException {
        Path file = Paths.get(this.filePath);
        try {
            Path folder = file.toAbsolutePath().getParent();
            if (folder != null) {
                Files.createDirectories(folder);
            }
            // try-with-resources closes the writer by itself, even when an error happens
            try (Writer writer = Files.newBufferedWriter(file)) {
                this.gson.toJson(customers, writer);
            }
        } catch (IOException | JsonIOException e) {
            throw new StorageException("The data file could not be written: " + this.filePath, e);
        }
    }

    @Override
    public List<Customer> load() throws StorageException {
        Path file = Paths.get(this.filePath);
        if (!Files.exists(file)) {
            return new ArrayList<>(); // first run, nothing was saved yet
        }
        Customer[] loaded;
        try (Reader reader = Files.newBufferedReader(file)) {
            loaded = this.gson.fromJson(reader, Customer[].class);
        } catch (IOException e) {
            throw new StorageException("The data file could not be read: " + this.filePath, e);
        } catch (JsonParseException e) {
            throw new StorageException("The data file is damaged (it is not the JSON we expect): " + this.filePath, e);
        }
        return this.checkAll(loaded);
    }

    // Gson does not call the constructors, so we check that what it loaded makes sense.
    private List<Customer> checkAll(Customer[] loaded) throws StorageException {
        List<Customer> result = new ArrayList<>();
        if (loaded == null) {
            return result; // an empty file
        }
        for (Customer customer : loaded) {
            if (customer == null || !customer.isValid()) {
                throw new StorageException("The data file is damaged (a customer is wrong or incomplete): " + this.filePath);
            }
            for (Customer other : result) {
                if (other.getId() == customer.getId() || other.getEmail().equals(customer.getEmail())) {
                    throw new StorageException("The data file is damaged (the same id or email twice): " + this.filePath);
                }
            }
            result.add(customer);
        }
        return result;
    }
}
