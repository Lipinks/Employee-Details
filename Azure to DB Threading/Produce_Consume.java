import com.microsoft.aad.msal4j.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.sql.*;


public class Produce_Consume {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/EmployeeDetails";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "Lipin@14062004";
    private static final String CLIENT_ID = "14a090b3-c001-4bd0-b7c5-9b56c83423dd";
    private static final String CLIENT_SECRET = "ukW8Q~VXw4EOhCBv7VBhxrDqS2sifZGyHzJAEcKn";
    private static final String AUTHORITY = "https://login.microsoftonline.com/a7197baf-fcf2-464a-8082-3a29cab01cda";
    private static final Set<String> SCOPE = Collections.singleton("https://graph.microsoft.com/.default");
    private boolean produced = false;
    boolean shouldRun = true; // Single flag to control thread execution
    String accessToken;
	int tot=1;
    String url = "https://graph.microsoft.com/v1.0/users";

    public synchronized void produce() {
        while (produced) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } 

        StringBuilder toAppend = new StringBuilder();
        try {
			int j=0;
			while (url != null && j<100)
			{
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Accept", "application/json");

            int httpResponseCode = conn.getResponseCode();
            if (httpResponseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray users = jsonResponse.getJSONArray("value");
                    if (users.isEmpty() || !jsonResponse.has("@odata.nextLink")) {
                        shouldRun = false;
                        System.out.println("No more data available. Stopping producer.");
						produced=true;
                    }

                    for (int i = 0; i < users.length(); i++) {
                        JSONObject user = users.getJSONObject(i);
                        toAppend.append(user.getString("id")).append(',')
                                .append(user.getString("displayName")).append(',')
                                .append(user.optString("jobTitle")).append(',')
                                .append(user.optString("mobilePhone")).append(',')
                                .append(user.optString("userPrincipalName")).append(',')
                                .append(user.optString("officeLocation")).append('\n');
                    }

                    url = jsonResponse.getString("@odata.nextLink");
                }
            }
			j++;
        }} catch (Exception e) {
            e.printStackTrace();
		}
		System.out.println("Total fetched : "+(tot*10000));
		tot++;
        if (toAppend.length() > 0) {
            try (FileWriter writer = new FileWriter("D:\\Documents\\output.csv")) {
                writer.append(toAppend);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("50000 Data fetched and wrote successfully");
			//System.out.println(toAppend);
        }
		//System.out.println(toAppend);
        produced = true;
        notify();
    }

    public synchronized void consume() {
        while (!produced) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
		String upsertQuery = "INSERT INTO details (emp_id, name, description, phone_no, e_mail, street) SELECT emp_id, name, description, phone_no, e_mail, street FROM temp_details ON CONFLICT (emp_id) DO UPDATE SET name = EXCLUDED.name, description = EXCLUDED.description, phone_no = EXCLUDED.phone_no, e_mail = EXCLUDED.e_mail, street = EXCLUDED.street;";
			
		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
		 Statement stmt = conn.createStatement())
		{
			stmt.execute("CREATE TEMP TABLE temp_details AS TABLE details WITH NO DATA;");

			stmt.execute("COPY temp_details FROM 'D:\\Documents\\output.csv' DELIMITER ',' CSV;");
			stmt.execute(upsertQuery);

			stmt.execute("DROP TABLE temp_details;");
			System.out.println("Data copied successfully from CSV to database.");
			//done=false;
		} catch (Exception e) {
			e.printStackTrace();
		}

        produced = false;
        notify();
    }

    public IAuthenticationResult getAccessTokenByClientCredentialGrant() throws MalformedURLException, ExecutionException, InterruptedException {
        ConfidentialClientApplication app = ConfidentialClientApplication
                .builder(CLIENT_ID, ClientCredentialFactory.createFromSecret(CLIENT_SECRET))
                .authority(AUTHORITY).build();

        ClientCredentialParameters clientCredentialParam = ClientCredentialParameters.builder(SCOPE).build();
        CompletableFuture<IAuthenticationResult> future = app.acquireToken(clientCredentialParam);
        return future.get();
    }

}