package softwareengineerassignment4;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Person {
    private String personID;
    private String firstName;
    private String lastName;
    private String address;
    private String birthdate;
    private HashMap<Date, Integer> demeritPoints = new HashMap<>();
    private boolean isSuspended;

    private static String FILE_NAME = "persons.txt";

    // Constructor to initialize a Person object with given details
    public Person(String personID, String firstName, String lastName, String address, String birthdate) {
        this.personID = personID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.birthdate = birthdate;
    }

    /**
     * Adds the current person to the file after validation.
     * @return true if successfully added, false otherwise.
     */
    public boolean addPerson() {
        // Validate all required fields before writing
        if (!validatePersonID(personID) || !validateAddress(address) || !validateDate(birthdate)) {
            return false;
        }

        if (fetchPersonById(personID) != null) {
            return false;
        }

        // Append the person's data as a new line in the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            writer.write(personID + "," + firstName + "," + lastName + "," + address + "," + birthdate);
            writer.newLine();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Reads the file and returns a Person object matching the given personID.
     * @param personID The ID to search for.
     * @return Person object if found, otherwise null.
     */
    public static Person fetchPersonById(String personID) {
        File inputFile = new File(FILE_NAME);
        if (!inputFile.exists()) {
            return null;
        }

        // Read file line by line and parse fields
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                String[] data = currentLine.split(",", -1);
                if (data.length < 5) {
                    continue; // Skip malformed lines
                }

                if (data[0].equals(personID)) {
                    // Found matching person, return new Person object
                    String firstName = data[1];
                    String lastName = data[2];
                    String address = data[3];
                    String birthdate = data[4];

                    return new Person(personID, firstName, lastName, address, birthdate);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading data file: " + e.getMessage());
        }

        return null; // Person not found
    }

    /**
     * Updates personal details of a person identified by currentPersonID.
     * Enforces validation and business rules:
     * - Validates new ID, address, birthdate format
     * - Restricts changing name/address if birthdate is changed
     * - Under 18 cannot change address
     * - If original ID starts with even digit, ID cannot be changed
     * @return true if update is successful, false otherwise
     */
    public static boolean updatePersonalDetails(
        String currentPersonID,
        String newPersonID,
        String newFirstName,
        String newLastName,
        String newAddress,
        String newBirthdate
    ) {
        // Validate new input values using static methods
        if (!validatePersonID(newPersonID)) {
            return false;
        }
        if (!validateAddress(newAddress)) {
            return false;
        }
        if (!validateDate(newBirthdate)) {
            return false;
        }

        // Fetch existing person to check business logic constraints
        Person existingPerson = fetchPersonById(currentPersonID);
        if (existingPerson == null) {
            return false;
        }

        boolean birthdateChanging = !existingPerson.birthdate.equals(newBirthdate);
        int currentAge = existingPerson.getAge(existingPerson.birthdate);

        // If birthdate changes, name and address must remain unchanged
        if (birthdateChanging && (
            !existingPerson.firstName.equals(newFirstName) ||
            !existingPerson.lastName.equals(newLastName) ||
            !existingPerson.address.equals(newAddress)
        )) {
            return false;
        }

        // Under 18 cannot change address
        if (currentAge < 18 && !existingPerson.address.equals(newAddress)) {
            return false;
        }

        // If original ID starts with even digit, ID cannot be changed
        if (!newPersonID.equals(existingPerson.personID) &&
            Character.getNumericValue(existingPerson.personID.charAt(0)) % 2 == 0) {
            return false;
        }

        // Update file: create a temporary file with updated data
        try {
            File inputFile = new File(FILE_NAME);
            File tempFile = new File("temp_persons.txt");
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                String[] data = currentLine.split(",", -1);

                if (data[0].equals(currentPersonID)) {
                    // Preserve all fields after index 4 (e.g. demerit points) intact
                    StringBuilder extraFields = new StringBuilder();
                    for (int i = 5; i < data.length; i++) {
                        extraFields.append(",").append(data[i]);
                    }

                    // Write updated person info
                    writer.write(newPersonID + "," + newFirstName + "," + newLastName + "," +
                                newAddress + "," + newBirthdate + extraFields.toString());
                } else {
                    // Write unchanged line for other persons
                    writer.write(currentLine);
                }
                writer.newLine();
            }

            writer.close();
            reader.close();

            // Replace original file with updated temp file
            if (!inputFile.delete()) {
                return false;
            }
            if (!tempFile.renameTo(inputFile)) {
                return false;
            }

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Adds demerit points for an offense date.
     * Validates date and points range.
     * Accumulates points for offenses within the last 2 years.
     * Updates suspension status based on age and total points.
     * Appends demerit info to the file.
     * @param offenseDate Date string of offense in dd-MM-yyyy format.
     * @param points Number of points (1 to 6).
     * @return "Success" or "Failed"
     */
    public String addDemeritPoints(String offenseDate, int points) {
        // Validate offense date format and points range
        if (!validateDate(offenseDate) || points < 1 || points > 6) return "Failed";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            sdf.setLenient(false);  // Strict parsing
            Date offense = sdf.parse(offenseDate);

            // Store points, accumulating if offense already exists
            int prev = demeritPoints.getOrDefault(offense, 0);
            demeritPoints.put(offense, prev + points);

            // Recalculate suspension status based only on the past 2 years
            int totalPoints = calculatePointsWithinTwoYears();

            int age = getAge(birthdate);
            isSuspended = (age < 21 && totalPoints > 6) || (age >= 21 && totalPoints > 12);

            // Log the offense to the file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
                writer.write(personID + ",Demerit:" + points + ",Date:" + offenseDate);
                writer.newLine();
            }
            return "Success";
        } catch (ParseException | IOException e) {
            return "Failed";
        }
    }

    // Checks if person is suspended based on latest offense
    public boolean isSuspended() {
        return isSuspended;
    }

    /**
     * Validates personID string.
     * Rules:
     * - Exactly 10 chars
     * - First 2 chars digits 2-9
     * - Middle 6 chars (positions 3 - 8) contain at least 2 special chars from allowed set
     * - Last 2 chars uppercase letters
     * @param id The person ID string to validate
     * @return true if valid, false otherwise
     */
    public static boolean validatePersonID(String id) {
        if (id == null || id.length() != 10) return false;

        char first = id.charAt(0);
        char second = id.charAt(1);
        String middleSix = id.substring(2, 8);  // Positions 2-8 (6 characters)
        char ninth = id.charAt(8);
        char tenth = id.charAt(9);

        // First and second must be digits between 2 and 9
        if (!Character.isDigit(first) || !Character.isDigit(second)) return false;
        if (first < '2' || first > '9' || second < '2' || second > '9') return false;

        // Middle six must contain at least 2 allowed special characters
        String allowedSpecials = "@#$%^&*!()_+=-";
        int specialCount = 0;
        for (char c : middleSix.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                if (allowedSpecials.indexOf(c) == -1) return false;
                specialCount++;
            }
        }
        if (specialCount < 2) return false;

        // Last two must be uppercase letters
        if (!Character.isUpperCase(ninth) || !Character.isUpperCase(tenth)) return false;

        return true;
    }

    /**
     * Validates address format.
     * Rules:
     * - Must contain 5 parts separated by '|'
     * - Fourth part must be "Victoria"
     * - Fifth part must be "Australia"
     * @param addr Address string to validate
     * @return true if valid, false otherwise
     */
    public static boolean validateAddress(String addr) {
        if (addr == null) return false;
        String[] parts = addr.split("\\|");
        return parts.length == 5 && parts[3].equalsIgnoreCase("Victoria") && parts[4].equalsIgnoreCase("Australia");
    }

    /**
     * Validates date string format "dd-MM-yyyy"
     * @param date Date string to validate
     * @return true if valid date format, false otherwise
     */
    public static boolean validateDate(String date) {
        if (date == null) return false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            sdf.setLenient(false); // <-- strict checking
            sdf.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * Calculates age in years from birthdate string
     * @param birthDateStr Date string in "dd-MM-yyyy" format
     * @return Age in years, or 0 if parsing error
     */
    private int getAge(String birthDateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            Date birthDate = sdf.parse(birthDateStr);
            Calendar birth = Calendar.getInstance();
            birth.setTime(birthDate);
            Calendar today = Calendar.getInstance();

            int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);

            // Adjust if birthdate has not occurred yet this year
            if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) age--;
            return age;
        } catch (ParseException e) {
            return 0;
        }
    }

    private int getSuspensionThreshold() {
        int age = getAge(birthdate);
        return (age < 21) ? 6 : 12;
    }

    /**
     * Calculates total demerit points accumulated within the last 2 years
     * @return sum of points in the last 2 years
     */
    private int calculatePointsWithinTwoYears() {
        List<Date> offenseDates = new ArrayList<>(demeritPoints.keySet());
        Collections.sort(offenseDates);

        int maxPoints = 0;
        for (Date startDate : offenseDates) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            cal.add(Calendar.YEAR, 2);
            Date endDate = cal.getTime();

            int total = 0;
            for (Map.Entry<Date, Integer> entry : demeritPoints.entrySet()) {
                Date d = entry.getKey();
                if (!d.before(startDate) && !d.after(endDate)) {
                    total += entry.getValue();
                }
            }

            if (total > maxPoints) {
                maxPoints = total;
            }

            // Early exit if already enough to suspend
            if (maxPoints > getSuspensionThreshold()) break;
        }

        return maxPoints;
    }

    // Allows setting a custom file name for storage (useful for testing)
    public static void setFileName(String fileName) {
        FILE_NAME = fileName;
    }
}