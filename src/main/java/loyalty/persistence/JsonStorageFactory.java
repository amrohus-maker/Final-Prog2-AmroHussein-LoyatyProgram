package loyalty.persistence;

public class JsonStorageFactory extends StorageFactory {
    private final String filePath;

    public JsonStorageFactory(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public DataStorage createStorage() {
        return new JsonStorage(this.filePath);
    }
}
