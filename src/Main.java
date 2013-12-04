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
        Database database = new Database("jdbc:sqlserver://plc245:1433;instanceName=sqlexpress", "user", "pass");
        Connection connection = database.connect();

        // test creation of new patient into database
        /* Patient newPatient = new Patient(connection);
        newPatient.create(123, "Chad", "Burke", "x", "Gilford", "NH", "03249",
                "555-555-5555", "555-555-5555", "x@gmail.com");
        System.out.println(newPatient.getPatientId()); */

        // does database exist?
        // if not : create it
        // if !AppointmentBook.exists
        // AppointmentBook.createDatabase



        // does patients table contain data?
        // if not : prompt for csv import
        // if !PatientsTable.hasData
        // Importer importer = new Importer(connection);
        // importer.importCsv("d");

        // AllPatients will accept a 'Connection' object as a parameter
        AllPatients allPatients = new AllPatients(connection);

        // test retrieval of patient from database
        Patient patient = new Patient(connection);
        patient.get(5);
        System.out.println(patient.getFirstName());
        patient.setFirstName("Ethan");
        patient.update();

        // PatientDetail will accept a 'Patient' object as a parameter
        // PatientDetail patientDetail = new PatientDetail(connection, patient);
    }
}
