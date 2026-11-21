package loyalty.persistence;

public class MemoryStorageFactory extends StorageFactory {
    @Override
    public DataStorage createStorage() {
        return new MemoryStorage();
    }
}
