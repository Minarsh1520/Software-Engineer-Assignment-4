package softwareengineerassignment4;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
//workflow testing
public class PersonTest {
    private static final String TEST_FILE = "test_persons.txt";
    // CORRECTED: 10-character valid ID (positions: 2-7 are 6 characters)
    private static final String VALID_ID = "56@a!b#cXY"; 
    private static final String VALID_ADDRESS = "123|Main St|Melbourne|Victoria|Australia";
    private static final String VALID_BIRTHDATE = "15-05-1990";
    private static final String VALID_FIRST_NAME = "John";
    private static final String VALID_LAST_NAME = "Doe";

    @BeforeEach
    public void setUp() {
        Person.setFileName(TEST_FILE);
        new File(TEST_FILE).delete();
    }

    @AfterEach
    public void tearDown() {
        new File(TEST_FILE).delete();
    }

    private Person createValidPerson() {
        return new Person(VALID_ID, VALID_FIRST_NAME, VALID_LAST_NAME, VALID_ADDRESS, VALID_BIRTHDATE);
    }

    private String getDateOffset(int years) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, years);
        return new SimpleDateFormat("dd-MM-yyyy").format(cal.getTime());
    }

    // ==================== addPerson Tests ====================
    @Test
    public void addPerson_validInput_returnsTrue() {
        Person p = createValidPerson();
        assertTrue(p.addPerson());
    }

    @Test
    public void addPerson_invalidPersonID_returnsFalse() {
        Person p = new Person("12ab!cdEF", VALID_FIRST_NAME, VALID_LAST_NAME, VALID_ADDRESS, VALID_BIRTHDATE);
        assertFalse(p.addPerson());
    }

    @Test
    public void addPerson_invalidAddress_returnsFalse() {
        String invalidAddress = "123|Main St|Sydney|NSW|Australia";
        Person p = new Person(VALID_ID, VALID_FIRST_NAME, VALID_LAST_NAME, invalidAddress, VALID_BIRTHDATE);
        assertFalse(p.addPerson());
    }

    @Test
    public void addPerson_invalidBirthdate_returnsFalse() {
        Person p = new Person(VALID_ID, VALID_FIRST_NAME, VALID_LAST_NAME, VALID_ADDRESS, "1990/05/15");
        assertFalse(p.addPerson());
    }

    @Test
    public void addPerson_shortPersonID_returnsFalse() {
        Person p = new Person("56@a!b#", VALID_FIRST_NAME, VALID_LAST_NAME, VALID_ADDRESS, VALID_BIRTHDATE);
        assertFalse(p.addPerson());
    }

    // ================= updatePersonalDetails Tests =================
    @Test
    public void updatePersonalDetails_birthdayAndNameChange_returnsFalse() {
        createValidPerson().addPerson();
        String newBirthdate = "16-05-2000";
        assertFalse(Person.updatePersonalDetails(VALID_ID, VALID_ID, "Alice", VALID_LAST_NAME, VALID_ADDRESS, newBirthdate));
    }

    @Test
    public void updatePersonalDetails_validAddressUpdate_returnsTrue() {
        // Adult person (25 years old)
        String adultBirthdate = getDateOffset(-25);
        Person p = new Person(VALID_ID, VALID_FIRST_NAME, VALID_LAST_NAME, VALID_ADDRESS, adultBirthdate);
        p.addPerson();
        
        String newAddress = "456|New St|Melbourne|Victoria|Australia";
        assertTrue(Person.updatePersonalDetails(VALID_ID, VALID_ID, VALID_FIRST_NAME, VALID_LAST_NAME, newAddress, adultBirthdate));
    }

    @Test
    public void updatePersonalDetails_addressUpdateMinor_returnsFalse() {
        // Minor (15 years old)
        String minorBirthdate = getDateOffset(-15);
        Person p = new Person(VALID_ID, VALID_FIRST_NAME, VALID_LAST_NAME, VALID_ADDRESS, minorBirthdate);
        p.addPerson();
        
        String newAddress = "456|New St|Melbourne|Victoria|Australia";
        assertFalse(Person.updatePersonalDetails(VALID_ID, VALID_ID, VALID_FIRST_NAME, VALID_LAST_NAME, newAddress, minorBirthdate));
    }

    @Test
    public void updatePersonalDetails_idChangeBlocked_returnsFalse() {
        // ID starting with even digit (2)
        String evenId = "23a$b%cDE";
        Person p = new Person(evenId, VALID_FIRST_NAME, VALID_LAST_NAME, VALID_ADDRESS, VALID_BIRTHDATE);
        p.addPerson();
        
        String newId = "33x*y!zAB";
        assertFalse(Person.updatePersonalDetails(evenId, newId, VALID_FIRST_NAME, VALID_LAST_NAME, VALID_ADDRESS, VALID_BIRTHDATE));
    }

    @Test
    public void updatePersonalDetails_validNameUpdate_returnsTrue() {
        createValidPerson().addPerson();
        assertTrue(Person.updatePersonalDetails(VALID_ID, VALID_ID, VALID_FIRST_NAME, "Smith", VALID_ADDRESS, VALID_BIRTHDATE));
    }

    // ================= addDemeritPoints Tests =================
    @Test
    public void addDemeritPoints_validInput_returnsSuccess() {
        Person p = createValidPerson();
        p.addPerson();
        assertEquals("Success", p.addDemeritPoints("01-01-2025", 4));
    }

    @Test
    public void addDemeritPoints_invalidDate_returnsFailed() {
        Person p = createValidPerson();
        p.addPerson();
        assertEquals("Failed", p.addDemeritPoints("2025/01/01", 4));
    }

    @Test
    public void addDemeritPoints_pointsZero_returnsFailed() {
        Person p = createValidPerson();
        p.addPerson();
        assertEquals("Failed", p.addDemeritPoints("01-01-2025", 0));
    }

    @Test
    public void addDemeritPoints_pointsSeven_returnsFailed() {
        Person p = createValidPerson();
        p.addPerson();
        assertEquals("Failed", p.addDemeritPoints("01-01-2025", 7));
    }

    @Test
    public void addDemeritPoints_suspensionTriggered_returnsSuccessAndSuspends() {
        // 20-year-old person
        String birthdate = getDateOffset(-20);
        Person p = new Person(VALID_ID, VALID_FIRST_NAME, VALID_LAST_NAME, VALID_ADDRESS, birthdate);
        p.addPerson();
        
        // Add 4 points (total=4)
        p.addDemeritPoints(getDateOffset(0), 4);
        
        // Add 3 points (total=7) - should trigger suspension
        assertEquals("Success", p.addDemeritPoints(getDateOffset(0), 3));
        assertTrue(p.isSuspended());
    }
}