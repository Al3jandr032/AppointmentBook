import Database.*;
import GUI.*;
import Model.*;

import javax.swing.*;
import java.sql.Connection;

public class Main {
    private static void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        setLookAndFeel();

        // connect to database when application is first launched
        Database database = new Database("jdbc:sqlserver://localhost:1433;instanceName=sqlexpress", "user", "pass");
        Connection connection = database.connect();

        // test retrieval of patient from database
        Patient patient = new Patient(connection, 4);
        System.out.println(patient.getZipcode());

        // test creation of new patient into database
        Patient newPatient = new Patient(connection,
                123, "Chad", "Burke", "x", "Gilford", "NH", "03249",
                "555-555-5555", "555-555-5555", "x@gmail.com");
        System.out.println(newPatient.getPatientId());

        // test of import csv method
        Importer importer = new Importer();
        importer.importCsv("d");

        // check to see if the database exists and that it has data in it
        if(!AppointmentBook.exists()) {
            // if data does not exist then ask if they want to import a csv
            importer.displayPrompt();
        }

        // load AllPatients form
        AllPatients allPatients = new AllPatients();
    }
}
