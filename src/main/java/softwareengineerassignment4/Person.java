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

    private static final String FILE_NAME = "persons.txt";

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

    public boolean updatePersonalDetails(String newFirstName, String newLastName, String newAddress, String newBirthdate) {
        if (!validatePersonID(personID) || !validateAddress(address) || !validateDate(birthdate)) return false;

        boolean isUnder18 = getAge(birthdate) < 18;
        boolean birthdateChanging = !birthdate.equals(newBirthdate);

        if (isUnder18 && !address.equals(newAddress)) return false;
        if (birthdateChanging && (!firstName.equals(newFirstName) || !lastName.equals(newLastName) || !address.equals(newAddress))) return false;
        if (Character.getNumericValue(personID.charAt(0)) % 2 == 0 && !personID.equals(this.personID)) return false;

        try {
            File inputFile = new File(FILE_NAME);
            File tempFile = new File("Person.txt");
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                String[] data = currentLine.split(",");
                if (data[0].equals(personID)) {
                    writer.write(personID + "," + newFirstName + "," + newLastName + "," + newAddress + "," + newBirthdate);
                } else {
                    writer.write(currentLine);
                }
                writer.newLine();
            }
            writer.close();
            reader.close();
            inputFile.delete();
            tempFile.renameTo(inputFile);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public String addDemeritPoints(String offenseDate, int points) {
        if (!validateDate(offenseDate) || points < 1 || points > 6) return "Failed";

        try {
            Date date = new SimpleDateFormat("dd-MM-yyyy").parse(offenseDate);
            demeritPoints.put(date, points);
            int totalPoints = calculatePointsWithinTwoYears();

            int age = getAge(birthdate);
            if ((age < 21 && totalPoints > 6) || (age >= 21 && totalPoints > 12)) {
                isSuspended = true;
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

    private boolean validatePersonID(String id) {
        if (id.length() != 10) return false;

        char first = id.charAt(0);
        char second = id.charAt(1);
        String middleSix = id.substring(2, 8);
        char ninth = id.charAt(8);
        char tenth = id.charAt(9);

        // First and second must be digits between 2 and 9
        if (!Character.isDigit(first) || !Character.isDigit(second)) return false;
        if (first < '2' || first > '9' || second < '2' || second > '9') return false;

        // Middle six must be alphanumeric or a special character
        String allowedSpecials = "@#$%^&*!()_+=-";
        for (char c : middleSix.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && allowedSpecials.indexOf(c) == -1) {
                return false;
            }
        }

        // Last two must be uppercase letters
        if (!Character.isUpperCase(ninth) || !Character.isUpperCase(tenth)) return false;

        return true;
    }


    private boolean validateAddress(String addr) {
        String[] parts = addr.split("\\|");
        return parts.length == 5 && parts[3].equalsIgnoreCase("Victoria");
    }

    private boolean validateDate(String date) {
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
        Calendar twoYearsAgo = Calendar.getInstance();
        twoYearsAgo.add(Calendar.YEAR, -2);
        int total = 0;
        for (Map.Entry<Date, Integer> entry : demeritPoints.entrySet()) {
            if (!entry.getKey().before(twoYearsAgo.getTime())) {
                total += entry.getValue();
            }
        }
        return total;
    }
}


