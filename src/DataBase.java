import java.sql.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class DataBase {
	
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/EmployeeDetails";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "Lipin@14062004";
	
    public static JSONArray getEmployeeDetails() {
		
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        JSONArray employeeArray = new JSONArray();
		
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM details");

            while (resultSet.next()) {
                JSONObject employee = new JSONObject();
                employee.put("employeeId", resultSet.getString("emp_id"));
                employee.put("displayNameName", resultSet.getString("name"));
                employee.put("description", resultSet.getString("description"));
                employee.put("mobilePhone", resultSet.getString("phone_no"));
                employee.put("userPrincipalName", resultSet.getString("e_mail"));
                employee.put("streetAddress", resultSet.getString("street"));
                
                employeeArray.put(employee);
            }

        } catch (SQLException e) {} 
        return employeeArray;  
    }

    public void uploadEmployeeDetails(String employeeId, String displayName, String description, String telephone, String email, String street) {	
        
        String query = "INSERT INTO details (emp_id, name, description, phone_no, e_mail, street) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = conn.prepareStatement(query);) {
            preparedStatement.setString(1, employeeId);
            preparedStatement.setString(2, displayName);
            preparedStatement.setString(3, description);
            preparedStatement.setString(4, telephone);
            preparedStatement.setString(5, email);
            preparedStatement.setString(6, street);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {}
    }
}