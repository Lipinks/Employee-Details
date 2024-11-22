import com.microsoft.aad.msal4j.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.sql.*;

public class Produce_Consume 
{
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/EmployeeDetails";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "Lipin@14062004";
    private static final String CLIENT_ID = "14a090b3-c001-4bd0-b7c5-9b56c83423dd";
    private static final String CLIENT_SECRET = "ukW8Q~VXw4EOhCBv7VBhxrDqS2sifZGyHzJAEcKn";
    private static final String AUTHORITY = "https://login.microsoftonline.com/a7197baf-fcf2-464a-8082-3a29cab01cda";
    private static final Set<String> SCOPE = Collections.singleton("https://graph.microsoft.com/.default");
    volatile boolean shouldRun = true;
    volatile boolean is_last = false;
    volatile Queue<JSONArray> queue = new LinkedList<>();
    int count_users=0;
    private volatile int current_csv=1;
    String accessToken;
    String url;
	Url_storage url_backup;
	
	
	public Produce_Consume(Url_storage nxt_link)
	{
		this.url_backup=nxt_link;
		if(nxt_link.next_url==null)
		{
			this.url = "https://graph.microsoft.com/v1.0/users/delta";
		}
		else
		{
			this.url=nxt_link.next_url;
		}
	}
	
    public void produce() 
	{   
        try {
			while (url != null)
			{
				HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Authorization", "Bearer " + accessToken);
				conn.setRequestProperty("Accept", "application/json");
				StringBuilder response = new StringBuilder();
				int httpResponseCode = conn.getResponseCode();
				if (httpResponseCode == HttpURLConnection.HTTP_OK) 
				{
					try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) 
					{
						String inputLine;
						
						while ((inputLine = in.readLine()) != null) 
						{
							response.append(inputLine);
						}
						
						JSONObject jsonResponse = new JSONObject(response.toString());
						
						JSONArray users = jsonResponse.getJSONArray("value");
						if(users!=null)
						{queue.add(users);}
						
						if (jsonResponse.has("@odata.nextLink")) 
						{
							url = jsonResponse.getString("@odata.nextLink");
						} 
						else
						{
							is_last = true;
							url_backup.next_url = jsonResponse.getString("@odata.deltaLink");
							System.out.println("Delta link updated for next sync.");
							url = null;
						}
						System.out.println("Data added to Queue.");
					}
				}
			}
			}catch (Exception e) {}
    }
	
	
    public void consume() 
	{
		if(queue.size()>0 || is_last)
		{
			JSONArray users=queue.poll();
			if(users!=null){
			System.out.println("Data consumed from Queue.");
			StringBuilder toAppend = new StringBuilder();
			for (int i = 0; i < users.length(); i++) 
			{
				JSONObject user = users.getJSONObject(i);
				if(user!=null){
				toAppend.append(user.getString("id")).append(',')
						.append(user.getString("displayName")).append(',')
						.append(user.optString("jobTitle")).append(',')
						.append(user.optString("mobilePhone")).append(',')
						.append(user.optString("userPrincipalName")).append(',')
				.append(user.optString("officeLocation")).append('\n');}
			}
			try (FileWriter writer = new FileWriter("D:\\Documents\\azure_users\\output"+current_csv+".csv",true)) {
                writer.append(toAppend);
			} catch (IOException e) {e.printStackTrace();}
			count_users++;}
			if(count_users==50 || is_last)
			{
				current_csv++;
				System.out.println("Going to update DB");
				String upsertQuery = "INSERT INTO details (emp_id, name, description, phone_no, e_mail, street) SELECT emp_id, name, description, phone_no, e_mail, street FROM temp_details ON CONFLICT (emp_id) DO UPDATE SET name = EXCLUDED.name, description = EXCLUDED.description, phone_no = EXCLUDED.phone_no, e_mail = EXCLUDED.e_mail, street = EXCLUDED.street;";
				try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
				 Statement stmt = conn.createStatement())
				{
					stmt.execute("CREATE TEMP TABLE temp_details AS TABLE details WITH NO DATA;");
					stmt.execute("COPY temp_details FROM 'D:\\Documents\\azure_users\\output"+(current_csv-1)+".csv' DELIMITER ',' CSV;");
					stmt.execute(upsertQuery);
					stmt.execute("DROP TABLE temp_details;");
					System.out.println("Data copied successfully from CSV to database.");
				} catch (Exception e) {e.printStackTrace();}
				count_users=0;
				if(is_last){shouldRun=false;}
			}
		}
	}
	
    public IAuthenticationResult getAccessTokenByClientCredentialGrant() 
			throws MalformedURLException, ExecutionException, InterruptedException 
	{
        ConfidentialClientApplication app = ConfidentialClientApplication
                .builder(CLIENT_ID, ClientCredentialFactory.createFromSecret(CLIENT_SECRET))
                .authority(AUTHORITY).build();
        ClientCredentialParameters clientCredentialParam = ClientCredentialParameters.builder(SCOPE).build();
        CompletableFuture<IAuthenticationResult> future = app.acquireToken(clientCredentialParam);
        return future.get();
    }

}
