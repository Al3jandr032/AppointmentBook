package Database;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Importer {

    private Connection connection;
    public Importer(Connection connection) {
        this.connection = connection;
    }

    public boolean displayPrompt() {
        final JFrame frame = new JFrame();
        FileDialog fd = new FileDialog(frame, "Select .csv for import", FileDialog.LOAD);
        fd.setFile("*.csv");
        fd.setVisible(true);
        String filename = fd.getDirectory() + "\\" + fd.getFile();
        if(fd.getFile() == null) {
            JOptionPane.showMessageDialog(null, "You didn't select anything.");
            return false;
        } else {
            importCsv(filename);
            return true;
        }
    }

    public void importCsv(String filename) {

        boolean purgeRecords = false;
        String[] options = new String[] {"Remove all records before import", "Leave existing records in place"};
        int response = JOptionPane.showOptionDialog(null, "Do you want to clear all existing records before importing?", "Remove all records?", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if(response == 0) {
            purgeRecords = true;
        }

        BufferedReader br = null;
        PreparedStatement statement = null;
        String sql = null;
        try {
            br = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {

            if(purgeRecords) {
                try {
                        sql = String.format("delete from AppointmentBook.dbo.appointments");
                        System.out.println(sql);
                        statement = connection.prepareStatement(sql);
                        statement.execute();

                        sql = String.format("delete from AppointmentBook.dbo.patients");
                        System.out.println(sql);
                        statement = connection.prepareStatement(sql);
                        statement.execute();

                        sql = String.format("DBCC CHECKIDENT ('AppointmentBook.dbo.patients',RESEED, 0)");
                        System.out.println(sql);
                        statement = connection.prepareStatement(sql);
                        statement.execute();

                        sql = String.format("DBCC CHECKIDENT ('AppointmentBook.dbo.appointments',RESEED, 0)");
                        System.out.println(sql);
                        statement = connection.prepareStatement(sql);
                        statement.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }

            // read through csv, import patients
            String line = br.readLine();
            while (line != null) {
                line = br.readLine();
                if(line == null) continue;

                line = line.replace("'", "''");
                String[] arr = line.split(",");
                sql = String.format("" +
                        "insert into AppointmentBook.dbo.patients " +
                        "(firstName, lastName, address, city, state, zipcode, homePhone, cellPhone, emailAddress)" +
                        "values('%s','%s','%s','%s','%s','%s','%s','%s','%s');", arr[0],arr[1],arr[2],arr[3],arr[4],arr[5],arr[6],arr[7],arr[8]);
                System.out.println(sql);

                try {
                    statement = connection.prepareStatement(sql);
                    statement.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }

            // create fake appointments
            try {

                sql = String.format("update patients set mrn = patientId");
                System.out.println(sql);
                statement = connection.prepareStatement(sql);
                statement.execute();

                sql = String.format("" +
                        "insert into AppointmentBook.dbo.appointments " +
                        "(patientId, appointmentDateTime, appointmentDescription)" +
                        "values('1','2013-12-07 09:30:00','broken leg');");
                System.out.println(sql);
                statement = connection.prepareStatement(sql);
                statement.execute();

                sql = String.format("" +
                        "insert into AppointmentBook.dbo.appointments " +
                        "(patientId, appointmentDateTime, appointmentDescription)" +
                        "values('2','2013-12-07 10:30:00','broken arm');");
                System.out.println(sql);
                statement = connection.prepareStatement(sql);
                statement.execute();

                sql = String.format("" +
                        "insert into AppointmentBook.dbo.appointments " +
                        "(patientId, appointmentDateTime, appointmentDescription)" +
                        "values('3','2013-12-07 11:00:00','vomit.  everywhere.');");
                System.out.println(sql);
                statement = connection.prepareStatement(sql);
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
