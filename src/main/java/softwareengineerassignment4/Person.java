package softwareengineerassignment4;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
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

    public Person(String personID, String firstName, String lastName, String address, String birthdate) {
        this.personID = personID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.birthdate = birthdate;
    }

    public boolean addPerson() {
        if (!validatePersonID(personID) || !validateAddress(address) || !validateDate(birthdate)) {
            return false;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            writer.write(personID + "," + firstName + "," + lastName + "," + address + "," + birthdate);
            writer.newLine();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static Person fetchPersonById(String personID) {
        File inputFile = new File(FILE_NAME);
        if (!inputFile.exists()) {
            System.out.println("Data file not found.");
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                String[] data = currentLine.split(",", -1);
                if (data.length < 5) {
                    continue;
                }

                if (data[0].equals(personID)) {
                    String firstName = data[1];
                    String lastName = data[2];
                    String address = data[3];
                    String birthdate = data[4];

                    Person foundPerson = new Person(personID, firstName, lastName, address, birthdate);
                    // Optional: parse other fields if needed

                    return foundPerson;
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading data file: " + e.getMessage());
        }

        return null; // person not found
    }

    public static boolean updatePersonalDetails(
        String currentPersonID,
        String newPersonID,
        String newFirstName,
        String newLastName,
        String newAddress,
        String newBirthdate
    ) {
        // Create a temporary Person for validation methods
        Person tempPerson = new Person(currentPersonID, "", "", "", "");
        if (!tempPerson.validatePersonID(newPersonID)) {
            System.out.println("Invalid new Person ID.");
            return false;
        }
        if (!tempPerson.validateAddress(newAddress)) {
            System.out.println("Invalid new address. Must include 'Victoria' and 'Australia'.");
            return false;
        }
        if (!tempPerson.validateDate(newBirthdate)) {
            System.out.println("Invalid new birthdate format. Use dd-MM-yyyy.");
            return false;
        }

        // Fetch the existing person
        Person existingPerson = fetchPersonById(currentPersonID);
        if (existingPerson == null) {
            System.out.println("Person not found.");
            return false;
        }

        boolean birthdateChanging = !existingPerson.birthdate.equals(newBirthdate);
        int currentAge = existingPerson.getAge(existingPerson.birthdate);

        // Restrictions if birthdate changes
        if (birthdateChanging && (
            !existingPerson.firstName.equals(newFirstName) ||
            !existingPerson.lastName.equals(newLastName) ||
            !existingPerson.address.equals(newAddress)
        )) {
            System.out.println("Cannot change name or address while changing birthdate.");
            return false;
        }

        // Under 18 cannot change address
        if (currentAge < 18 && !existingPerson.address.equals(newAddress)) {
            System.out.println("Underage individuals cannot change address.");
            return false;
        }

        // Cannot change ID if original ID starts with even digit
        if (!newPersonID.equals(existingPerson.personID) &&
            Character.getNumericValue(existingPerson.personID.charAt(0)) % 2 == 0) {
            System.out.println("Cannot change ID if current ID starts with an even number.");
            return false;
        }

        try {
            File inputFile = new File(FILE_NAME);
            File tempFile = new File("temp_persons.txt");
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                String[] data = currentLine.split(",", -1);

                if (data[0].equals(currentPersonID)) {
                    // Rebuild extra fields string (preserve everything after 5th element)
                    StringBuilder extraFields = new StringBuilder();
                    for (int i = 5; i < data.length; i++) {
                        extraFields.append(",").append(data[i]);
                    }

                    // Write updated line preserving extra fields
                    writer.write(newPersonID + "," + newFirstName + "," + newLastName + "," +
                                newAddress + "," + newBirthdate + extraFields.toString());
                } else {
                    // Write unchanged line
                    writer.write(currentLine);
                }
                writer.newLine();
            }

            writer.close();
            reader.close();

            if (!inputFile.delete()) {
                System.out.println("Failed to delete original file.");
                return false;
            }
            if (!tempFile.renameTo(inputFile)) {
                System.out.println("Failed to rename temp file.");
                return false;
            }

            return true;
        } catch (IOException e) {
            System.out.println("Error updating file: " + e.getMessage());
            return false;
        }
    }


    public String addDemeritPoints(String offenseDate, int points) {
        if (!validateDate(offenseDate) || points < 1 || points > 6) return "Failed";

        try {
            Date date = new SimpleDateFormat("dd-MM-yyyy").parse(offenseDate);
            // accumulate points for the same date
            int prev = demeritPoints.getOrDefault(date, 0);
            demeritPoints.put(date, prev + points);
            int totalPoints = calculatePointsWithinTwoYears();

            int age = getAge(birthdate);
            if ((age < 21 && totalPoints > 6) || (age >= 21 && totalPoints > 12)) {
                isSuspended = true;
            } else {
                isSuspended = false;
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
                writer.write(personID + ",Demerit:" + points + ",Date:" + offenseDate);
                writer.newLine();
            }
            return "Success";
        } catch (ParseException | IOException e) {
            return "Failed";
        }
    }

    public boolean isSuspended() {
        return isSuspended;
    }

    public static boolean validatePersonID(String id) {
        if (id == null || id.length() != 10) return false;

        char first = id.charAt(0);
        char second = id.charAt(1);
        String middleFive = id.substring(2, 7);
        char ninth = id.charAt(8);
        char tenth = id.charAt(9);

        // First and second must be digits between 2 and 9
        if (!Character.isDigit(first) || !Character.isDigit(second)) return false;
        if (first < '2' || first > '9' || second < '2' || second > '9') return false;

        // 2 middle five must be alphanumeric or a special character
        String allowedSpecials = "@#$%^&*!()_+=-";
        int specialCount = 0;
        for (char c : middleFive.toCharArray()) {
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

    public static boolean validateAddress(String addr) {
        if (addr == null) return false;
        String[] parts = addr.split("\\|");
        return parts.length == 5 && parts[3].equalsIgnoreCase("Victoria") && parts[4].equalsIgnoreCase("Australia");
    }

    public static boolean validateDate(String date) {
        if (date == null) return false;
        try {
            new SimpleDateFormat("dd-MM-yyyy").parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private int getAge(String birthDateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            Date birthDate = sdf.parse(birthDateStr);
            Calendar birth = Calendar.getInstance();
            birth.setTime(birthDate);
            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) age--;
            return age;
        } catch (ParseException e) {
            return 0;
        }
    }

    private int calculatePointsWithinTwoYears() {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.YEAR, -2);
        Date twoYearsAgo = now.getTime();

        int total = 0;
        for (Map.Entry<Date, Integer> entry : demeritPoints.entrySet()) {
            if (!entry.getKey().before(twoYearsAgo)) {
                total += entry.getValue();
            }
        }
        return total;
    }

    public static void setFileName(String fileName) {
        FILE_NAME = fileName;
    }
}


