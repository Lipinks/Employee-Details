import org.json.JSONArray;

public class ViewHandler {

    public JSONArray getEmployeeDetailsFromDB() {
        return DataBase.getEmployeeDetails();
    }
}