package Model;
import java.sql.*;

public class Patient {

    private int patientId = -1;
    private int mrn;
    private String firstName, lastName, address, city, state, zipcode, homePhone, cellPhone, emailAddress;
    private Connection connection;

    // get existing patient
    public Patient(Connection connection) {
        this.connection = connection;
    }

    // get from database
    public void get(int patientId) {
        PreparedStatement statement = null;
        try {
            String query = "select * from AppointmentBook.dbo.patients where patientId = ?";
            statement = connection.prepareStatement(query);
            statement.setInt(1, patientId);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();

            // map database values to object
            setPatientId(resultSet.getInt("patientId"));
            setMrn(resultSet.getInt("MRN"));
            setFirstName(resultSet.getString("firstName"));
            setLastName(resultSet.getString("lastName"));
            setAddress(resultSet.getString("streetAddress"));
            setCity(resultSet.getString("city"));
            setState(resultSet.getString("state"));
            setZipcode(resultSet.getString("zipcode"));
            setHomePhone(resultSet.getString("homePhone"));
            setCellPhone(resultSet.getString("cellPhone"));
            setEmailAddress(resultSet.getString("emailAddress"));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // insert into database
    public void create(int mrn, String firstName,
                       String lastName, String address, String city, String state, String zipcode,
                       String homePhone, String cellPhone, String emailAddress) {
        this.mrn = mrn;
        this.zipcode = zipcode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.city = city;
        this.state = state;
        this.homePhone = homePhone;
        this.cellPhone = cellPhone;
        this.emailAddress = emailAddress;
        this.insert();
    }

    // update existing patient into database
    public void update() {
        if(getPatientId() == -1) {
            throw new IllegalArgumentException("Trying to update with no patientId.");
        }
        try {
            PreparedStatement statement = null;
            String sql = "update patients " +
                    "set MRN = ?, firstName = ?, lastName = ?, streetAddress = ?, " +
                    "city = ?, state = ?, zipcode = ?, homePhone = ?, cellPhone = ?, emailAddress = ? " +
                    "where patientId = ?";
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, getMrn());
            statement.setString(2, getFirstName());
            statement.setString(3, getLastName());
            statement.setString(4, getAddress());
            statement.setString(5, getCity());
            statement.setString(6, getState());
            statement.setString(7, getZipcode());
            statement.setString(8, getHomePhone());
            statement.setString(9, getCellPhone());
            statement.setString(10, getEmailAddress());
            statement.setInt(11, getPatientId());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insert() {
        PreparedStatement statement = null;
        try {
            String sql = "INSERT INTO patients" +
                    "([MRN],[firstName],[lastName],[streetAddress],[city],[state],[zipcode],[homePhone],[cellPhone],[emailAddress])" +
                    "VALUES(?,?,?,?,?,?,?,?,?,?)";
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, getMrn());
            statement.setString(2, getFirstName());
            statement.setString(3, getLastName());
            statement.setString(4, getAddress());
            statement.setString(5, getCity());
            statement.setString(6, getState());
            statement.setString(7, getZipcode());
            statement.setString(8, getHomePhone());
            statement.setString(9, getCellPhone());
            statement.setString(10, getEmailAddress());
            statement.execute();
            // get identity value
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                patientId = generatedKeys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void delete() {

    }

    public int getPatientId() {
        return patientId;
    }

    // don't allow outside access to patientId
    private void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getMrn() {
        return mrn;
    }

    public void setMrn(int mrn) {
        this.mrn = mrn;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getHomePhone() {
        return homePhone;
    }

    public void setHomePhone(String homePhone) {
        this.homePhone = homePhone;
    }

    public String getCellPhone() {
        return cellPhone;
    }

    public void setCellPhone(String cellPhone) {
        this.cellPhone = cellPhone;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

}