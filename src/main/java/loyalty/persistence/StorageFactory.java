package loyalty.persistence;

// Factory Method pattern. Every subclass decides which kind of storage it creates.
// The code that uses a factory only knows StorageFactory and DataStorage,
// so it never has to write "new JsonStorage(...)" itself.
public abstract class StorageFactory {
    public abstract DataStorage createStorage();
}
