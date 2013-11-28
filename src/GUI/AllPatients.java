package GUI;

import Model.Patient;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AllPatients {

    private JTable tblPatients;
    private JPanel mainPanel;
    private JScrollPane scrollPane;
    private JTextField searchBox;
    private JButton addPButton;
    static JFrame frame = new JFrame("All Patients");
    final private Connection connection;

    private DefaultTableModel tblPatientsModel = new DefaultTableModel();

    public AllPatients(final Connection connection) {
        this.connection = connection;

        tblPatientsModel.addColumn("First Name");
        tblPatientsModel.addColumn("Last Name");
        tblPatientsModel.addColumn("City");
        tblPatientsModel.addColumn("State");
        tblPatientsModel.addColumn("Phone");
        tblPatientsModel.addColumn("Email Address");
        tblPatientsModel.addColumn("Next Appointment");
        tblPatientsModel.addColumn("Appointment Description");
        tblPatientsModel.addColumn("Patient ID");
        tblPatients.setModel(tblPatientsModel);
        populatePatients();

        // a little hodge .. remove the patients id column after populating it .. we don't want it in the view
        tblPatients.removeColumn(tblPatients.getColumnModel().getColumn(8));

        // style cell renderer .. add some padding
        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
            Border padding = BorderFactory.createEmptyBorder(5, 5, 5, 5);
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createCompoundBorder(getBorder(), padding));
                return this;
            }
        };

        tblPatients.getColumnModel().getColumn(0).setCellRenderer(r);
        tblPatients.getColumnModel().getColumn(1).setCellRenderer(r);

        // set row height
        tblPatients.setRowHeight(tblPatients.getRowHeight() + 8);
        tblPatients.setRowMargin(3);

        // set selection color
        tblPatients.setSelectionBackground(new Color(Integer.parseInt("A1CDEC", 16)));

        // allow sorting
        tblPatients.setAutoCreateRowSorter(true);

        // open ui
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        addPButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            Patient patient = new Patient(connection);
            PatientDetail patientDetail = new PatientDetail(connection, patient);
            }
        });

        searchBox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                populatePatients();
            }
        });

        tblPatients.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                int selectedRow = tblPatients.getSelectedRow();
                int patientId = Integer.parseInt(tblPatientsModel.getValueAt(selectedRow, 8).toString());
                Patient patient = new Patient(connection);
                patient.get(patientId);
                PatientDetail patientDetail = new PatientDetail(connection, patient);
            }
        });
    }

    private void populatePatients() {
        String search = searchBox.getText().trim();
        PreparedStatement statement = null;
        for (int i = tblPatientsModel.getRowCount() - 1; i > -1; i--) {
            tblPatientsModel.removeRow(i);
        }
        try {
            String query = "select patients.*, appointments.appointmentDateTime, appointments.appointmentDescription\n" +
                    "from patients \n" +
                    "left outer join (\n" +
                    "select * from (\n" +
                    "    select\n" +
                    "        t.patientId,\n" +
                    "        t.appointmentDateTime,\n" +
                    "        t.appointmentDescription,\n" +
                    "        t.appointmentId,\n" +
                    "        row_number() over(partition by t.patientId order by t.appointmentDateTime asc) as rn\n" +
                    "    from\n" +
                    "        appointments t\n" +
                    "\twhere t.appointmentDateTime > CURRENT_TIMESTAMP\n" +
                    ") tt\n" +
                    "where tt.rn = 1\n" +
                    ") as appointments\n" +
                    "on patients.patientId = appointments.appointmentId " +
                    "where firstName like '%" + search + "%'" +
                    "or lastName like '%" + search + "%'" +
                    "or homePhone like '%" + search + "%'" +
                    "or cellPhone like '%" + search + "%'" +
                    "or  emailAddress like '%" + search + "%'";
            statement = connection.prepareStatement(query);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                tblPatientsModel.addRow(new Object[]{
                        resultSet.getString("firstName"),
                        resultSet.getString("lastName"),
                        resultSet.getString("city"),
                        resultSet.getString("state"),
                        resultSet.getString("homePhone"),
                        resultSet.getString("emailAddress"),
                        resultSet.getString("appointmentDateTime"),
                        resultSet.getString("appointmentDescription"),
                        resultSet.getString("patientId")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}