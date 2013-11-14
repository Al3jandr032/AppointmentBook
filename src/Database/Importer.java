package Database;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Importer {
    public void displayPrompt() {
        int response = JOptionPane.showConfirmDialog(
                null,
                "The patient database is empty, would you like to import a .csv file?",
                "Database Empty",
                JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            final JFrame frame = new JFrame();
            FileDialog fd = new FileDialog(frame, "Select .csv for import", FileDialog.LOAD);
            fd.setFile("*.csv");
            fd.setVisible(true);
            String filename = fd.getDirectory() + "\\" + fd.getFile();
            if(fd.getFile() == null) {
                JOptionPane.showMessageDialog(null, "Loading interface anyway.");
            } else {
                importCsv(filename);
            }
        }
    }
    public void importCsv(String filename) {
        filename = "patients.csv";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            String line = br.readLine();
            while (line != null) {
                line = br.readLine();
                if(line == null) continue;

                line = line.replace("'", "''");
                String[] arr = line.split(",");
                String sql = String.format("" +
                        "insert into AppointmentBook.dbo.patients " +
                        "(firstName, lastName, streetAddress, city, state, zipcode)" +
                        "values('%s','%s','%s','%s','%s','%s');", arr[0],arr[1],arr[2],arr[3],arr[4],arr[5]);
                System.out.println(sql);
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
