package softwareengineerassignment4;

public class MainApp {
    public static void main(String[] args) {
        // Clear the file before tests (overwrite)
        try (java.io.PrintWriter pw = new java.io.PrintWriter("persons.txt")) {
            // empty the file
        } catch (Exception e) {
            System.out.println("Could not clear persons.txt before tests");
        }

        System.out.println("=== Add Person Tests ===");

        // Test 1: Valid add (should succeed)
        Person p1 = new Person("23ab12@#XY", "John", "Doe", "123|Street|Suburb|Victoria|Australia", "15-05-1990");
        System.out.println("Add valid person: " + (p1.addPerson() ? "Success" : "Fail"));

        // Test 2: Invalid personID (fail)
        Person p2 = new Person("11ab12@#xy", "Jane", "Doe", "123|Street|Suburb|Victoria|Australia", "15-05-1990");
        System.out.println("Add person invalid ID: " + (p2.addPerson() ? "Success" : "Fail") + " (expected Fail)");

        // Test 3: Invalid address (fail)
        Person p3 = new Person("24ab12@#XY", "Mike", "Smith", "123|Street|Suburb|NSW|Australia", "15-05-1990");
        System.out.println("Add person invalid address: " + (p3.addPerson() ? "Success" : "Fail") + " (expected Fail)");

        // Test 4: Invalid birthdate (fail)
        Person p4 = new Person("24ab12@#XY", "Anna", "Smith", "123|Street|Suburb|Victoria|Australia", "31-02-1990");
        System.out.println("Add person invalid birthdate: " + (p4.addPerson() ? "Success" : "Fail") + " (expected Fail)");

        System.out.println("\n=== Fetch Person Tests ===");
        Person fetched = Person.fetchPersonById("23ab12@#XY");
        System.out.println(fetched != null ? "Fetch existing person: Success" : "Fetch existing person: Fail" + " (expected Success)");
        Person notFound = Person.fetchPersonById("0000000000");
        System.out.println(notFound == null ? "Fetch non-existing person: Success" : "Fetch non-existing person: Fail" + " (expected Success)");

        System.out.println("\n=== Update Person Tests ===");

        // Setup - add a valid person to update
        Person p5 = new Person("24ab12@#XZ", "Alice", "Jones", "123|Street|Suburb|Victoria|Australia", "01-01-2005");
        p5.addPerson();

        // Test 5: Valid update (change name, no birthdate change, under 18 so no address change)
        boolean res1 = Person.updatePersonalDetails(
            "24ab12@#XZ",
            "24ab12@#XZ",
            "AliceUpdated",
            "JonesUpdated",
            "123|Street|Suburb|Victoria|Australia",  // same address (required because under 18)
            "01-01-2005"
        );
        System.out.println("Update name with no birthdate change (under 18, same address): " + (res1 ? "Success" : "Fail") + " (expected Success)");

        // Test 6: Fail update under 18 with address change
        boolean res2 = Person.updatePersonalDetails(
            "24ab12@#XZ",
            "24ab12@#XZ",
            "AliceUpdated",
            "JonesUpdated",
            "456|NewStreet|NewSuburb|Victoria|Australia", // address changed
            "01-01-2005"
        );
        System.out.println("Update address change under 18: " + (!res2 ? "Fail (correct)" : "Unexpected Success") + " (expected Fail)");

        // Test 7: Fail update changing birthdate with other changes
        boolean res3 = Person.updatePersonalDetails(
            "24ab12@#XZ",
            "24ab12@#XZ",
            "AliceUpdated",
            "JonesUpdated",
            "123|Street|Suburb|Victoria|Australia",
            "02-02-2004"
        );
        System.out.println("Change birthdate and other fields: " + (!res3 ? "Fail (correct)" : "Unexpected Success"));

        // Test 8: Fail update changing ID if first digit even
        Person p6 = new Person("24ab12@#YY", "Bob", "Brown", "123|Street|Suburb|Victoria|Australia", "15-06-1990");
        p6.addPerson();

        boolean res4 = Person.updatePersonalDetails(
            "24ab12@#YY",
            "34ab12@#ZZ", // change ID from '2' (even) to '3'
            "BobUpdated",
            "BrownUpdated",
            "123|Street|Suburb|Victoria|Australia",
            "15-06-1990"
        );
        System.out.println("Update ID to odd first digit (should fail): " + (!res4 ? "Fail (correct)" : "Unexpected Success"));

        // Test 9: Valid update changing ID if first digit odd
        Person p7 = new Person("35ab12@#YY", "Carol", "White", "123|Street|Suburb|Victoria|Australia", "15-06-1990");
        p7.addPerson();

        boolean res5 = Person.updatePersonalDetails(
            "35ab12@#YY",
            "45ab12@#ZZ", // change ID from '3' (odd) to '4'
            "CarolUpdated",
            "WhiteUpdated",
            "123|Street|Suburb|Victoria|Australia",
            "15-06-1990"
        );
        System.out.println("Update ID to even first digit (should succeed): " + (res5 ? "Success" : "Fail"));

        System.out.println("\n=== Add Demerit Points Tests ===");
        Person p8 = new Person("25ab12@#ZZ", "Dan", "Green", "123|Street|Suburb|Victoria|Australia", "01-01-2000");
        p8.addPerson();

        // Valid demerit add
        System.out.println("Add valid demerit points: " + (p8.addDemeritPoints("01-01-2024", 3).equals("Success") ? "Success" : "Fail"));

        // Invalid date format
        System.out.println("Add demerit points invalid date: " + (p8.addDemeritPoints("31-02-2024", 3).equals("Failed") ? "Fail (correct)" : "Unexpected Success"));

        // Invalid points (too high)
        System.out.println("Add demerit points invalid points: " + (p8.addDemeritPoints("01-01-2024", 10).equals("Failed") ? "Fail (correct)" : "Unexpected Success"));

        // Suspension check
        p8.addDemeritPoints("01-01-2023", 4);
        p8.addDemeritPoints("01-01-2024", 6);
        System.out.println("Person suspended after points over limit (age 24): " + (p8.isSuspended() ? "Suspended" : "Not Suspended"));

        Person p9 = new Person("26ab12@#ZZ", "Eve", "Black", "123|Street|Suburb|Victoria|Australia", "01-01-2005");
        p9.addPerson();
        p9.addDemeritPoints("01-01-2024", 4);
        p9.addDemeritPoints("01-01-2024", 4);
        System.out.println("Person suspended after points over limit (age 19): " + (p9.isSuspended() ? "Suspended" : "Not Suspended"));

        Person p10 = new Person("27ab12@#ZZ", "Eve", "Black", "123|Street|Suburb|Victoria|Australia", "01-01-1990");
        p10.addPerson();

        // Adding offenses not within any 2-year window
        System.out.println("Add valid demerit points: " + p10.addDemeritPoints("01-01-2018", 5));  // total: 5
        System.out.println("Add valid demerit points: " + p10.addDemeritPoints("01-01-2021", 4));  // total: 9 (but 2018-2021 > 2 years)
        System.out.println("Add valid demerit points: " + p10.addDemeritPoints("01-01-2024", 5));  // total: 14 (but max in 2-year span = 9)

        System.out.println("Is Suspended? (should print fail) " + p10.isSuspended());   // Should print: false
    }
}
