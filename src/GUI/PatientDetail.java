package GUI;

import Model.Appointment;
import Model.Patient;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PatientDetail {

    private JPanel main;
    private JPanel Rows;

    // patient
    private JTextField txtMRN;
    private JTextField txtFirstName;
    private JTextField txtLastName;
    private JTextField txtAddress;
    private JTextField txtCity;
    private JTextField txtState;
    private JTextField txtZipcode;
    private JTextField txtHomePhone;
    private JTextField txtCellPhone;
    private JTextField txtEmailAddress;
    private JTable tblAppointments;
    private JButton btnCreateAppointment;
    private JButton btnCreatePatient;

    // appointment
    private JTextField txtAppointmentDateTime;
    private JTextField txtAppointmentDescription;
    private JLabel lblDateTime;
    private JLabel lblDescription;

    static JFrame frame = new JFrame("Patient Detail");

    private DefaultTableModel tblAppointmentsModel = new DefaultTableModel();
    private final Connection connection;
    private Patient patient;

    private void populateAppointments() {
        PreparedStatement statement = null;
        for (int i = tblAppointmentsModel.getRowCount() - 1; i > -1; i--) {
            tblAppointmentsModel.removeRow(i);
        }
        try {
            String sql = "select * from appointments where patientId = ?";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, patient.getPatientId());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                tblAppointmentsModel.addRow(new Object[]{
                        resultSet.getString("appointmentDateTime"),
                        resultSet.getString("appointmentDescription")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public PatientDetail(final Connection connection, Patient patient) {
        this.connection = connection;
        this.patient = patient;

        // map the passed patient to the text fields
        txtMRN.setText(Integer.toString(patient.getMrn()));
        txtFirstName.setText(patient.getFirstName());
        txtLastName.setText(patient.getLastName());
        txtAddress.setText(patient.getAddress());
        txtCity.setText(patient.getCity());
        txtState.setText(patient.getState());
        txtZipcode.setText(patient.getZipcode());
        txtHomePhone.setText(patient.getHomePhone());
        txtCellPhone.setText(patient.getCellPhone());
        txtEmailAddress.setText(patient.getEmailAddress());

        // Table code to display appointments tied to the current patient in PatientDetail
        // set dynamic columns
        tblAppointmentsModel.addColumn("Date/Time");
        tblAppointmentsModel.addColumn("Description");
        tblAppointments.setModel(tblAppointmentsModel);

        //sets dateTime and txtAppointmentDescription
        Appointment appointment = new Appointment(connection);
        appointment.get(1);
        txtAppointmentDescription.setText(appointment.getAppointmentDescription());
        txtAppointmentDateTime.setText((appointment.getAppointmentDateTime()));

        populateAppointments();

        // add rows to table
        //tblAppointmentsModel.addRow(new Object[]{"12/12/12 12:12", "Burke"});
        //tblAppointmentsModel.addRow(new Object[]{"11/11/11 11:11", "Burke"});
        //tblAppointmentsModel.addRow(new Object[]{"10/10/10 10:10", "Burke"});

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

        tblAppointments.getColumnModel().getColumn(0).setCellRenderer(r);
        tblAppointments.getColumnModel().getColumn(1).setCellRenderer(r);

        // set row height
        tblAppointments.setRowHeight(tblAppointments.getRowHeight() + 8);
        tblAppointments.setRowMargin(3);

        // set selection color
        tblAppointments.setSelectionBackground(new Color(Integer.parseInt("A1CDEC", 16)));

        // allow sorting
        tblAppointments.setAutoCreateRowSorter(true);

        // open ui
        frame.setContentPane(main);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        btnCreatePatient.addActionListener(new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent e) {
               updatePatient();
            }
        });

        tblAppointments.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                int selectedRow = tblAppointments.getSelectedRow();
                txtAppointmentDateTime.setText(tblAppointmentsModel.getValueAt(selectedRow, 0).toString());
                txtAppointmentDescription.setText(tblAppointmentsModel.getValueAt(selectedRow, 1).toString());
            }
        });
    }

    private void updatePatient() {
        patient.update();
    }


}
