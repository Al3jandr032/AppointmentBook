package GUI;

import Model.Appointment;
import Model.Patient;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PatientDetail {

    private JPanel main;
    private JPanel Rows;

    // patient
    private JFormattedTextField txtMRN;
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
    private JButton btnSavePatient;

    // appointment
    private JTextField txtAppointmentDateTime;
    private JTextField txtAppointmentDescription;
    private JSpinner txtAppointmentTime;
    private JSpinner txtAppointmentDate;
    private JButton btnSaveAppointment;
    private JButton btnDeletePatient;

    static JFrame frame = new JFrame("Patient Detail");

    private DefaultTableModel tblAppointmentsModel = new DefaultTableModel();
    private final Connection connection;
    private Patient patient;
    private AllPatients allPatients;

    private void populateAppointmentsTable() {
        PreparedStatement statement = null;
        for (int i = tblAppointmentsModel.getRowCount() - 1; i > -1; i--) {
            tblAppointmentsModel.removeRow(i);
        }
        try {
            String sql = "select * from appointments where patientId = ? order by DATEDIFF(DAY, appointmentDateTime, CURRENT_TIMESTAMP) desc";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, patient.getPatientId());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                tblAppointmentsModel.addRow(new Object[]{
                        resultSet.getString("appointmentDateTime"),
                        resultSet.getString("appointmentDescription"),
                        resultSet.getInt("appointmentId")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public PatientDetail(final Connection connection, AllPatients allPatients, Patient patient) {
        this.connection = connection;
        this.patient = patient;
        this.allPatients = allPatients;


        $$$setupUI$$$();
        if (patient.getPatientId() == -1) {
            btnSavePatient.setText("Add Patient");
            btnSaveAppointment.setText("Create Appointment");
            btnDeletePatient.setVisible(false);
            System.out.println("show new patient form");
        } else {
            btnSavePatient.setText("Update Patient");
            btnSaveAppointment.setText("Create Appointment");
            btnDeletePatient.setVisible(true);
            populateFormFields(patient);
            System.out.println("load patient " + patient.getPatientId());
        }

        setAppointmentColumns();
        populateAppointmentsTable();
        tblAppointments.removeColumn(tblAppointments.getColumnModel().getColumn(2));

        setTableStyle();
        showForm();
        setupListeners();


        txtEmailAddress.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2) {
                    String cmd = "cmd.exe /c start \"\" \"" + "mailto:" + txtEmailAddress.getText() + "?subject=Message from Team Java" + "\"";
                    try {
                        Runtime.getRuntime().exec(cmd);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    private void setupListeners() {
        btnSavePatient.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updatePatient();
            }
        });
        btnDeletePatient.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deletePatient();
            }
        });
        btnSaveAppointment.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveAppointment();
            }
        });
        tblAppointments.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                makeItSoEveryTypeOfClickSelectsARow(e);
                if (getSelectedAppointmentId() == -1) return;
                if (e.getButton() == 3) {
                    int response = JOptionPane.showConfirmDialog(null, "Do you want to delete the selected appointment?", "Delete Appointment?", JOptionPane.YES_NO_OPTION);
                    if (response == JOptionPane.YES_OPTION) {
                        deleteAppointment();
                    }
                } else {
                    appointmentRowClicked();
                }
            }
        });
    }

    /**
     * by default on left clicks select a row on jtable -- this makes it so right clicks do too .. !
     *
     * @param e
     */
    private void makeItSoEveryTypeOfClickSelectsARow(MouseEvent e) {
        Point p = e.getPoint();
        int rowNumber = tblAppointments.rowAtPoint(p);
        ListSelectionModel model = tblAppointments.getSelectionModel();
        model.setSelectionInterval(rowNumber, rowNumber);
    }

    private void deleteAppointment() {
        int appointmentId = getSelectedAppointmentId();
        if (appointmentId == -1) return;
        Appointment appointment = new Appointment(connection);
        appointment.get(appointmentId);
        System.out.println("deleting appointment " + appointment.getAppointmentId());
        appointment.delete();
        populateAppointmentsTable();
        allPatients.populatePatients();
    }

    private void deletePatient() {
        int response = JOptionPane.showConfirmDialog(
                null,
                "Are you sure you want to delete this patient and all of their appointments?",
                "Delete Patient?",
                JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            System.out.println("deleting patient " + patient.getPatientId());
            patient.delete();
            returnToAllPatients();
        }
    }

    private void returnToAllPatients() {
        allPatients.populatePatients();
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    private int getSelectedAppointmentId() {
        int selectedRow = tblAppointments.getSelectedRow();
        if (selectedRow == -1) return -1;
        return Integer.parseInt(tblAppointmentsModel.getValueAt(selectedRow, 2).toString());
    }

    private void appointmentRowClicked() {
        int appointmentId = getSelectedAppointmentId();
        System.out.println("selected appointment " + appointmentId);

        int selectedRow = tblAppointments.getSelectedRow();
        String appointmentDateTime = tblAppointmentsModel.getValueAt(selectedRow, 0).toString();
        String[] parts = appointmentDateTime.split(" ");
        Date date = null;
        Date time = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(parts[0]);
            time = new SimpleDateFormat("HH:mm:ss.0", Locale.ENGLISH).parse(parts[1]);
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
        txtAppointmentDate.setValue(date);
        txtAppointmentTime.setValue(time);
        txtAppointmentDescription.setText(tblAppointmentsModel.getValueAt(selectedRow, 1).toString());
        btnSaveAppointment.setText("Save Appointment");

    }

    private void showForm() {
        main.setBorder(new EmptyBorder(10, 10, 10, 10));
        main.setPreferredSize(new Dimension(700, 400));
        frame.setContentPane(main);
        frame.pack();
        frame.setVisible(true);
    }

    private void setTableStyle() {
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
    }

    private void setAppointmentColumns() {
        tblAppointmentsModel.addColumn("Date/Time");
        tblAppointmentsModel.addColumn("Description");
        tblAppointmentsModel.addColumn("Appointment Id");
        tblAppointments.setModel(tblAppointmentsModel);


    }

    private void populateFormFields(Patient patient) {
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
    }

    private void saveAppointment() {
        int patientId = patient.getPatientId();
        if (patientId == -1) {
            JOptionPane.showMessageDialog(frame, "Please save the patient before adding an appointment.");
            return;
        }

        int appointmentId = getSelectedAppointmentId();
        System.out.println("trying to save appointment " + appointmentId);

        // this isn't really the right way to do this .. but i'm a little pressed for time.
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(txtAppointmentDate, "MM/dd/yyyy");
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(txtAppointmentTime, "HH:mm:ss");

        String oldDate = dateEditor.getFormat().format(txtAppointmentDate.getValue());
        String oldTime = timeEditor.getFormat().format(txtAppointmentTime.getValue());
        String dateTime = null;

        try {
            dateTime = new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("MM/dd/yyyy").parse(oldDate)) + " " + oldTime;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Appointment appointment = new Appointment(connection);
        if (appointmentId == -1) {
            System.out.println("creating appointment for " + patientId);
            appointment.create(patientId, dateTime, txtAppointmentDescription.getText());
        } else {
            System.out.println("updating appointment " + appointmentId + " for " + patientId);
            appointment.get(appointmentId);
            appointment.setAppointmentDateTime(dateTime);
            appointment.setAppointmentDescription(txtAppointmentDescription.getText());
            appointment.update();
        }
        populateAppointmentsTable();
        allPatients.populatePatients();
    }

    private void updatePatient() {
        getData(patient);
        if (patient.getPatientId() != -1) {
            patient.update();
        } else {
            patient.create();
        }
        returnToAllPatients();
    }

    protected MaskFormatter createFormatter(String s) {
        MaskFormatter formatter = null;
        try {
            formatter = new MaskFormatter(s);
        } catch (ParseException exc) {
            System.err.println("formatter is bad: " + exc.getMessage());
            System.exit(-1);
        }
        return formatter;
    }

    private void createUIComponents() {
        txtAppointmentTime = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(txtAppointmentTime, "HH:mm:ss");
        txtAppointmentTime.setEditor(timeEditor);
        txtAppointmentTime.setValue(new Date());

        Date tomorrow = getTomorrow();
        txtAppointmentDate = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(txtAppointmentDate, "MM/dd/yyyy");
        txtAppointmentDate.setEditor(dateEditor);
        txtAppointmentDate.setValue(tomorrow);

        // txtMRN = new JFormattedTextField(createFormatter("#####"));
        // txtHomePhone = new JFormattedTextField(createFormatter("###-###-####"));
        // txtCellPhone = new JFormattedTextField(createFormatter("###-###-####"));
        txtMRN = new JFormattedTextField();
        txtHomePhone = new JFormattedTextField();
        txtCellPhone = new JFormattedTextField();

        txtEmailAddress = new JTextField();
        txtEmailAddress.setForeground(Color.blue);
    }

    private Date getTomorrow() {
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.DAY_OF_YEAR, 1);
        Date tomorrow = cal.getTime();
        System.out.println(tomorrow);
        return tomorrow;
    }

    public void getData(Patient data) {
        data.setMrn(Integer.parseInt(txtMRN.getText()));
        data.setFirstName(txtFirstName.getText());
        data.setLastName(txtLastName.getText());
        data.setAddress(txtAddress.getText());
        data.setCity(txtCity.getText());
        data.setState(txtState.getText());
        data.setZipcode(txtZipcode.getText());
        data.setHomePhone(txtHomePhone.getText());
        data.setCellPhone(txtCellPhone.getText());
        data.setEmailAddress(txtEmailAddress.getText());
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        main = new JPanel();
        main.setLayout(new BorderLayout(0, 0));
        main.setMinimumSize(new Dimension(477, 200));
        main.setPreferredSize(new Dimension(500, 400));
        final JLabel label1 = new JLabel();
        label1.setFont(new Font("SansSerif", Font.BOLD, 24));
        label1.setHorizontalAlignment(0);
        label1.setText("Patient Detail");
        main.add(label1, BorderLayout.NORTH);
        Rows = new JPanel();
        Rows.setLayout(new GridLayoutManager(12, 11, new Insets(0, 0, 0, 0), -1, -1));
        Rows.setPreferredSize(new Dimension(500, 400));
        main.add(Rows, BorderLayout.CENTER);
        final JLabel label2 = new JLabel();
        label2.setText("MRN");
        Rows.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        Rows.add(txtMRN, new GridConstraints(1, 3, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 30), new Dimension(150, -1), new Dimension(-1, 30), 0, false));
        txtFirstName = new JTextField();
        txtFirstName.setText("");
        Rows.add(txtFirstName, new GridConstraints(2, 3, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 30), new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("First Name");
        Rows.add(label3, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtLastName = new JTextField();
        Rows.add(txtLastName, new GridConstraints(3, 3, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 30), new Dimension(150, -1), null, 0, false));
        txtAddress = new JTextField();
        Rows.add(txtAddress, new GridConstraints(4, 3, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 30), new Dimension(150, -1), null, 0, false));
        txtCity = new JTextField();
        Rows.add(txtCity, new GridConstraints(5, 3, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 30), new Dimension(150, -1), null, 0, false));
        txtState = new JTextField();
        Rows.add(txtState, new GridConstraints(1, 8, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 30), new Dimension(150, -1), new Dimension(-1, 30), 0, false));
        txtZipcode = new JTextField();
        Rows.add(txtZipcode, new GridConstraints(2, 8, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 30), new Dimension(150, -1), new Dimension(-1, 30), 0, false));
        Rows.add(txtHomePhone, new GridConstraints(3, 8, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 30), new Dimension(150, -1), new Dimension(-1, 30), 0, false));
        Rows.add(txtCellPhone, new GridConstraints(4, 8, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 30), new Dimension(150, -1), new Dimension(-1, 30), 0, false));
        Rows.add(txtEmailAddress, new GridConstraints(5, 8, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 30), new Dimension(150, -1), new Dimension(-1, 30), 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("State");
        Rows.add(label4, new GridConstraints(1, 6, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("ZipCode");
        Rows.add(label5, new GridConstraints(2, 6, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Home Phone");
        Rows.add(label6, new GridConstraints(3, 6, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Cell Phone");
        Rows.add(label7, new GridConstraints(4, 6, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("E-mail Address");
        Rows.add(label8, new GridConstraints(5, 6, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Last Name");
        Rows.add(label9, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("Address");
        Rows.add(label10, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("City");
        Rows.add(label11, new GridConstraints(5, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnSavePatient = new JButton();
        btnSavePatient.setActionCommand("Update Patient");
        btnSavePatient.setText("Create Patient");
        Rows.add(btnSavePatient, new GridConstraints(10, 0, 1, 11, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtAppointmentDescription = new JTextField();
        Rows.add(txtAppointmentDescription, new GridConstraints(7, 8, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 30), new Dimension(150, -1), null, 0, false));
        final JLabel label12 = new JLabel();
        label12.setText("Date/Time:");
        Rows.add(label12, new GridConstraints(7, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label13 = new JLabel();
        label13.setText("Description:");
        Rows.add(label13, new GridConstraints(7, 6, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        Rows.add(scrollPane1, new GridConstraints(9, 0, 1, 11, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        tblAppointments = new JTable();
        scrollPane1.setViewportView(tblAppointments);
        final Spacer spacer1 = new Spacer();
        Rows.add(spacer1, new GridConstraints(6, 4, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        Rows.add(spacer2, new GridConstraints(6, 8, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        Rows.add(spacer3, new GridConstraints(8, 8, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        Rows.add(spacer4, new GridConstraints(8, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        Rows.add(txtAppointmentDate, new GridConstraints(7, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 30), null, null, 0, false));
        Rows.add(txtAppointmentTime, new GridConstraints(7, 4, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 30), null, null, 0, false));
        btnSaveAppointment = new JButton();
        btnSaveAppointment.setText("Save Appointment");
        Rows.add(btnSaveAppointment, new GridConstraints(7, 9, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        Rows.add(spacer5, new GridConstraints(0, 7, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        btnDeletePatient = new JButton();
        btnDeletePatient.setText("Delete Patient");
        btnDeletePatient.setVisible(false);
        Rows.add(btnDeletePatient, new GridConstraints(11, 9, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return main;
    }
}
