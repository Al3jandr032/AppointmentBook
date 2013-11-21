package Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: nhti
 * Date: 11/20/13
 * Time: 8:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class PatientsTable {

    public static boolean hasData(Connection connection){

        PreparedStatement statement = null;

        String query = "select * from AppointmentBook.dbo.patients";

        int recordCount = 0;
        try {
            statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                recordCount++;
            }


        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return (recordCount==0) ?  false : true;
    }
}