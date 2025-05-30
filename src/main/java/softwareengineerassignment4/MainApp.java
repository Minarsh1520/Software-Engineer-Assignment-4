package softwareengineerassignment4;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class MainApp {
    
    public static void main(String[] args) {
        // Test 1: Add person with valid data
        Person p1 = new Person("56s_@x#FAB", "John", "Doe", "32|Highland|Street|Melbourne|Victoria|Australia", "15-11-2000");
        boolean added = p1.addPerson();
        System.out.println("Add Person (valid): " + (added ? "Success" : "Failed"));

        // Check if person ID exists in file
        if (checkFileContainsID("56s_@x#FAB")) {
            System.out.println("Check File Contains ID (p1): Found");
        } else {
            System.out.println("Check File Contains ID (p1): Not Found");
        }

        // Test 2: Update personal details
        boolean updated = p1.updatePersonalDetails("Johnny", "Doe", "32|Highland|Street|Melbourne|Victoria|Australia", "15-11-2000");
        System.out.println("Update Details: " + (updated ? "Success" : "Failed"));

        // Test 3: Add valid demerit points
        String result1 = p1.addDemeritPoints("15-03-2023", 3);
        System.out.println("Add Demerit Points (valid): " + result1);

        // Test 4: Add more demerit points to test suspension
        String result2 = p1.addDemeritPoints("01-05-2024", 4);
        System.out.println("Add Demerit Points (check suspension): " + result2);

        // Test 5: Add person with invalid ID and address
        Person p2 = new Person("11abcdEFGH", "Alice", "Smith", "10|Queen|Ave|Sydney|NSW|Australia", "01-01-1990");
        boolean addedInvalid = p2.addPerson();
        System.out.println("Add Person (invalid ID/address): " + (addedInvalid ? "Success" : "Failed"));

        // Ensure invalid ID is not in file
        if (checkFileContainsID("11abcdEFGH")) {
            System.out.println("Check File Contains ID (p2): Found (unexpected)");
        } else {
            System.out.println("Check File Contains ID (p2): Not Found (expected)");
        }

        // Test 6: Add demerit points with invalid point count
        String result3 = p1.addDemeritPoints("20-06-2024", 10);
        System.out.println("Add Demerit Points (invalid points): " + result3);

        // Test 7: Add demerit points with invalid date format
        String result4 = p1.addDemeritPoints("2024-06-20", 2);
        System.out.println("Add Demerit Points (invalid date): " + result4);
    }

    // Helper method to check if person ID exists in the text file
    private static boolean checkFileContainsID(String id) {
        try (BufferedReader reader = new BufferedReader(new FileReader("persons.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(id)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
        return false;
    }

}
