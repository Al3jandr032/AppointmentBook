package GUI;

import Database.Importer;
import Model.Patient;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
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
    private JButton btnImport;
    static JFrame frame = new JFrame("All Patients");
    final private Connection connection;

    private DefaultTableModel tblPatientsModel = new DefaultTableModel();

    private AllPatients allPatients;

    public AllPatients(final Connection connection) {
        this.connection = connection;
        this.allPatients = this;

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
        mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        mainPanel.setPreferredSize(new Dimension(1000, 400));
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        addPButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Patient patient = new Patient(connection);
                PatientDetail patientDetail = new PatientDetail(connection, allPatients, patient);
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
                PatientDetail patientDetail = new PatientDetail(connection, allPatients, patient);
            }
        });
        btnImport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Importer importer = new Importer(connection);
                if (importer.displayPrompt()) {
                    populatePatients();
                }
            }
        });
    }

    public void populatePatients() {
        String search = searchBox.getText().trim();
        PreparedStatement statement = null;
        for (int i = tblPatientsModel.getRowCount() - 1; i > -1; i--) {
            tblPatientsModel.removeRow(i);
        }
        try {
            String sql = "select patients.*, \n" +
                    "convert(varchar, nearest_appointment_info.appointmentDateTime, 100) as appointmentDateTime, \n" +
                    "nearest_appointment_info.appointmentDescription\n" +
                    "from patients\n" +
                    "\n" +
                    "/* join to find the nearest appointment date */\n" +
                    "left outer join (\n" +
                    "\tselect patientId, min(appointmentDateTime) as appointmentDateTime\n" +
                    "\tfrom appointments\n" +
                    "\twhere appointmentDateTime > CURRENT_TIMESTAMP\n" +
                    "\tgroup by patientId\n" +
                    ") as nearest_appointment\n" +
                    "on patients.patientId = nearest_appointment.patientId\n" +
                    "\n" +
                    "/* join to find the nearest actual appointment */\n" +
                    "left outer join appointments as nearest_appointment_info\n" +
                    "on patients.patientId = nearest_appointment_info.patientId\n" +
                    "and nearest_appointment_info.appointmentDateTime = nearest_appointment.appointmentDateTime " +

                    "where patients.firstName like '%" + search + "%'" +
                    "or lastName like '%" + search + "%'" +
                    "or homePhone like '%" + search + "%'" +
                    "or cellPhone like '%" + search + "%'" +
                    "or emailAddress like '%" + search + "%'" +
                    "or nearest_appointment_info.appointmentDescription like '%" + search + "%'" +
                    " order by DATEDIFF(DAY, nearest_appointment_info.appointmentDateTime, CURRENT_TIMESTAMP) desc, nearest_appointment_info.appointmentDateTime asc";

            /* String sql = "select patients.*, convert(varchar, appointments.appointmentDateTime, 100) as appointmentDateTime, appointments.appointmentDescription\n" +
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
                    "or  emailAddress like '%" + search + "%'" +
                    " order by DATEDIFF(DAY, appointmentDateTime, CURRENT_TIMESTAMP) desc";     */
            statement = connection.prepareStatement(sql);

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

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setMinimumSize(new Dimension(650, 76));
        mainPanel.setPreferredSize(new Dimension(750, 469));
        scrollPane = new JScrollPane();
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(scrollPane, gbc);
        scrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        tblPatients = new JTable();
        tblPatients.setFillsViewportHeight(true);
        tblPatients.setIntercellSpacing(new Dimension(4, 4));
        tblPatients.setRowMargin(4);
        scrollPane.setViewportView(tblPatients);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(3, 3, 3, 3), -1, -1));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(panel1, gbc);
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        searchBox = new JTextField();
        panel1.add(searchBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Search by name, phone, email :");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        addPButton = new JButton();
        addPButton.setText("Add Patient");
        panel1.add(addPButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnImport = new JButton();
        btnImport.setText("Import from CSV");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(btnImport, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}