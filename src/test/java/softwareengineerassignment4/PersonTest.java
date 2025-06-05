package softwareengineerassignment4;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

public class PersonTest {

    private Person validPerson;
    private final String validID = "56@a!b#CD";
    private final String validAddress = "123|Main|St|Melbourne|Victoria";
    private final String validBirthdate = "15-05-1990";

    @TempDir
    static Path tempDir;

    private static Path filePath;

    @BeforeEach
    void setup() throws IOException {
        filePath = tempDir.resolve("persons.txt");
        // Only create file if it doesn't exist
        if (!Files.exists(filePath)) {
            Files.createFile(filePath);
        }
        overrideFileNameInPerson(filePath.toString());

        validPerson = new Person(validID, "John", "Doe", validAddress, validBirthdate);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up after each test
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
    }

    private void overrideFileNameInPerson(String newPath) {
        Person.setFileName(newPath);
    }

    @Test
    void testAddPersonWithValidData() {
        assertTrue(validPerson.addPerson());
        assertTrue(fileContains(validID));
    }

    @Test
    void testAddPersonWithInvalidID() {
        Person p = new Person("11abcdEFGH", "A", "B", validAddress, validBirthdate);
        assertFalse(p.addPerson());
    }

    @Test
    void testAddPersonWithInvalidAddress() {
        Person p = new Person(validID, "A", "B", "1|Fake|Road|Sydney|NSW", validBirthdate);
        assertFalse(p.addPerson());
    }

    @Test
    void testUpdatePersonalDetailsSuccess() {
        validPerson.addPerson();
        boolean result = validPerson.updatePersonalDetails("Johnny", "Doe", validAddress, validBirthdate);
        assertTrue(result);
    }

    @Test
    void testUpdatePersonalDetailsInvalidForUnder18AddressChange() {
        String birthdateUnder18 = LocalDate.now().minusYears(17).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        Person minor = new Person(validID, "Kid", "Test", validAddress, birthdateUnder18);
        minor.addPerson();
        boolean result = minor.updatePersonalDetails("Kid", "Test", "123|New|St|Melbourne|Victoria", birthdateUnder18);
        assertFalse(result);
    }

    @Test
    void testUpdatePersonalDetailsInvalidBirthdateChangeRules() {
        Person p = new Person(validID, "A", "B", validAddress, validBirthdate);
        p.addPerson();
        boolean result = p.updatePersonalDetails("C", "D", validAddress, "01-01-1995");
        assertFalse(result);
    }

    @Test
    void testAddDemeritPointsSuccessAndSuspension() {
        validPerson.addPerson();

        assertEquals("Success", validPerson.addDemeritPoints("01-01-2023", 4));
        assertFalse(validPerson.isSuspended());

        assertEquals("Success", validPerson.addDemeritPoints("01-01-2024", 9));
        assertTrue(validPerson.isSuspended());
    }

    @Test
    void testAddDemeritPointsInvalidDate() {
        assertEquals("Failed", validPerson.addDemeritPoints("2024/06/20", 2));
    }

    @Test
    void testAddDemeritPointsInvalidPoints() {
        assertEquals("Failed", validPerson.addDemeritPoints("20-06-2024", 10));
    }

    @Test
    void testValidatePersonID_Reflection() throws Exception {
        Method method = Person.class.getDeclaredMethod("validatePersonID", String.class);
        method.setAccessible(true);
        assertTrue((Boolean) method.invoke(null, "56@a!b#CD"));
        assertFalse((Boolean) method.invoke(null, "12abcdEF"));
    }

    @Test
    void testValidateAddress_Reflection() throws Exception {
        Method method = Person.class.getDeclaredMethod("validateAddress", String.class);
        method.setAccessible(true);
        assertTrue((Boolean) method.invoke(null, "123|Main|St|Melbourne|Victoria"));
        assertFalse((Boolean) method.invoke(null, "1|Fake|Road|Sydney|NSW"));
    }

    @Test
    void testValidateDate_Reflection() throws Exception {
        Method method = Person.class.getDeclaredMethod("validateDate", String.class);
        method.setAccessible(true);
        assertTrue((Boolean) method.invoke(null, "15-05-1990"));
        assertFalse((Boolean) method.invoke(null, "1990/05/15"));
    }

    private boolean fileContains(String personID) {
        try {
            return Files.lines(filePath).anyMatch(line -> line.contains(personID));
        } catch (IOException e) {
            return false;
        }
    }
}