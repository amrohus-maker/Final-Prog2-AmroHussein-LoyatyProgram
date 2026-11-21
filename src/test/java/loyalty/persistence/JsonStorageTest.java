package loyalty.persistence;

import com.google.gson.JsonParser;
import loyalty.domain.Customer;
import loyalty.domain.Tier;
import loyalty.domain.Transaction;
import loyalty.exception.StorageException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonStorageTest {
    @TempDir
    Path folder;

    private String path(String name) {
        return folder.resolve(name).toString();
    }

    private void write(String name, String text) throws IOException {
        Files.writeString(folder.resolve(name), text);
    }

    private List<Customer> listOf(Customer... customers) {
        return new ArrayList<>(Arrays.asList(customers));
    }

    // a customer that is correct in every way, written by hand as it would be in the file
    private static String customerJson(int id, String email) {
        return "{\"id\":" + id + ",\"name\":\"Ann Lee\",\"email\":\"" + email + "\",\"joinedOn\":\"2026-11-21\","
                + "\"points\":50,\"totalEarned\":50,"
                + "\"history\":[{\"date\":\"2026-11-21\",\"points\":50,\"description\":\"Welcome bonus\"}]}";
    }

    private Customer annWithHistory() throws Exception {
        Customer ann = new Customer(1, "Ann Lee", "ann@example.com");
        ann.earn(50, "Welcome bonus");
        ann.earn(120, "Purchase $120.00");
        ann.spend(60, "Redeemed: Free coffee");
        return ann;
    }

    // ---------- saving and loading ----------

    @Test
    void aMissingFileGivesAnEmptyList() throws Exception {
        assertTrue(new JsonStorage(path("nothing.json")).load().isEmpty());
    }

    @Test
    void savedCustomersComeBackExactly() throws Exception {
        Customer ann = annWithHistory();
        Customer bob = new Customer(2, "Bob Ray", "bob@example.com");
        bob.earn(50, "Welcome bonus");

        new JsonStorage(path("data.json")).save(listOf(ann, bob));
        List<Customer> loaded = new JsonStorage(path("data.json")).load();

        assertEquals(2, loaded.size());
        Customer again = loaded.get(0);
        assertEquals(1, again.getId());
        assertEquals("Ann Lee", again.getName());
        assertEquals("ann@example.com", again.getEmail());
        assertEquals(ann.getJoinedOn(), again.getJoinedOn());
        assertEquals(110, again.getPoints());
        assertEquals(170, again.getTotalEarned());
        assertEquals(Tier.BRONZE, again.getTier());

        List<Transaction> before = ann.getHistory();
        List<Transaction> after = again.getHistory();
        assertEquals(before.size(), after.size());
        for (int i = 0; i < before.size(); i++) {
            assertEquals(before.get(i).getDate(), after.get(i).getDate());
            assertEquals(before.get(i).getPoints(), after.get(i).getPoints());
            assertEquals(before.get(i).getDescription(), after.get(i).getDescription());
        }
        assertEquals("Bob Ray", loaded.get(1).getName());
    }

    @Test
    void theFileIsReadableJsonText() throws Exception {
        new JsonStorage(path("data.json")).save(listOf(annWithHistory()));

        String text = Files.readString(folder.resolve("data.json"));

        assertTrue(JsonParser.parseString(text).isJsonArray());
        assertEquals(1, JsonParser.parseString(text).getAsJsonArray().size());
        assertTrue(text.contains("\"name\": \"Ann Lee\""));
    }

    @Test
    void savingCreatesTheFolderWhenItIsMissing() throws Exception {
        String deep = folder.resolve("one").resolve("two").resolve("data.json").toString();
        new JsonStorage(deep).save(listOf());
        assertTrue(Files.exists(Path.of(deep)));
    }

    @Test
    void savingAgainReplacesTheOldContent() throws Exception {
        JsonStorage storage = new JsonStorage(path("data.json"));
        storage.save(listOf(new Customer(1, "Ann Lee", "ann@example.com"), new Customer(2, "Bob Ray", "bob@example.com")));
        storage.save(listOf(new Customer(1, "Ann Lee", "ann@example.com")));

        assertEquals(1, storage.load().size());
    }

    @Test
    void loadedCustomersKeepWorking() throws Exception {
        new JsonStorage(path("data.json")).save(listOf(annWithHistory()));

        Customer loaded = new JsonStorage(path("data.json")).load().get(0);
        int linesBefore = loaded.getHistory().size();
        loaded.earn(10, "Purchase $10.00");

        assertEquals(120, loaded.getPoints());
        assertEquals(linesBefore + 1, loaded.getHistory().size());
    }

    @Test
    void specialCharactersSurviveTheTrip() throws Exception {
        String name = "Zoë \"Z\" Ünal & Co <x>";
        Customer customer = new Customer(1, name, "zoe@example.com");
        customer.earn(50, "Welcome bonus");

        new JsonStorage(path("data.json")).save(listOf(customer));
        String text = Files.readString(folder.resolve("data.json"));
        Customer loaded = new JsonStorage(path("data.json")).load().get(0);

        assertEquals(name, loaded.getName());
        assertTrue(text.contains("Ünal & Co <x>")); // not turned into & and so on
    }

    @Test
    void anEmptyFileGivesAnEmptyList() throws Exception {
        write("empty.json", "");
        write("spaces.json", "   \n  ");
        assertTrue(new JsonStorage(path("empty.json")).load().isEmpty());
        assertTrue(new JsonStorage(path("spaces.json")).load().isEmpty());
    }

    // ---------- damaged files ----------

    @Test
    void aCorrectFileWrittenByHandIsAccepted() throws Exception {
        write("good.json", "[" + customerJson(1, "ann@example.com") + "]");
        List<Customer> loaded = new JsonStorage(path("good.json")).load();
        assertEquals(1, loaded.size());
        assertEquals("Ann Lee", loaded.get(0).getName());
    }

    @Test
    void textThatIsNotTheJsonWeExpectIsReported() throws Exception {
        String[] bad = {"this is not json", "{", "[1, 2, 3]", "{\"id\": 1}", "[{\"id\": \"abc\"}]", "[null]"};
        for (String content : bad) {
            write("bad.json", content);
            StorageException e = assertThrows(StorageException.class, () -> new JsonStorage(path("bad.json")).load(),
                    "should have been refused: " + content);
            assertTrue(e.getMessage().contains("damaged"), content);
        }
    }

    @Test
    void customersThatDoNotMakeSenseAreReported() throws Exception {
        String good = customerJson(1, "ann@example.com");
        String[] bad = {
                "[{\"id\":1}]",                                                          // nearly everything is missing
                "[" + good.replace("\"id\":1", "\"id\":0") + "]",                        // id must be above 0
                "[" + good.replace("\"points\":50,\"totalEarned\"", "\"points\":40,\"totalEarned\"") + "]",  // points do not match the history
                "[" + good.replace("ann@example.com", "annexample.com") + "]",           // not an email
                "[" + customerJson(1, "ann@example.com") + "," + customerJson(1, "bob@example.com") + "]",   // same id twice
                "[" + customerJson(1, "ann@example.com") + "," + customerJson(2, "ann@example.com") + "]",   // same email twice
        };
        for (String content : bad) {
            write("bad.json", content);
            StorageException e = assertThrows(StorageException.class, () -> new JsonStorage(path("bad.json")).load(),
                    "should have been refused: " + content);
            assertTrue(e.getMessage().contains("damaged"), content);
        }
    }

    // ---------- files that cannot be written or read ----------

    @Test
    void aFileThatCannotBeWrittenIsReported() throws Exception {
        write("blocker", "I am a file, not a folder");
        String impossible = path("blocker") + File.separator + "sub" + File.separator + "data.json";
        JsonStorage storage = new JsonStorage(impossible);

        StorageException e = assertThrows(StorageException.class, () -> storage.save(listOf()));

        assertTrue(e.getMessage().contains("could not be written"));
        assertNotNull(e.getCause());
    }

    @Test
    void aFolderInsteadOfAFileIsReported() {
        JsonStorage storage = new JsonStorage(folder.toString());
        assertThrows(StorageException.class, storage::load);
    }

    @Test
    void anEmptyPathIsAMistakeInOurCode() {
        assertThrows(IllegalArgumentException.class, () -> new JsonStorage(""));
        assertThrows(IllegalArgumentException.class, () -> new JsonStorage("   "));
        assertThrows(IllegalArgumentException.class, () -> new JsonStorage(null));
    }
}
