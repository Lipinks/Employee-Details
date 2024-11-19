import com.microsoft.aad.msal4j.*;
import org.json.JSONObject; // For working with JSON responses
import org.json.JSONArray;
import com.microsoft.aad.msal4j.IClientCredential;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class AddHandler {
	private static final  String CLIENT_ID = "14a090b3-c001-4bd0-b7c5-9b56c83423dd";
	private static final  String CLIENT_SECRET = "ukW8Q~VXw4EOhCBv7VBhxrDqS2sifZGyHzJAEcKn";
	private static final  String AUTHORITY = "https://login.microsoftonline.com/a7197baf-fcf2-464a-8082-3a29cab01cda";
	private static String accessToken;
	private static final Set<String> SCOPE = Collections.singleton("https://graph.microsoft.com/.default");
    public String uploadEmployeeDetails(String employeeId, String displayName, String description, 
                                         String telephone, String email, String street, String photo)										 
	{					 
        DataBase db = new DataBase();
        db.uploadEmployeeDetails(employeeId, displayName, description, telephone, email, street);
		String status = create_User(displayName,telephone,employeeId,description,street,photo);
		return status;
	}
	
	public static String create_User(String name,String phone_no,String emp_id, String description, String street,String photo) 
	{
	  Boolean success = false;
	  for(int tr=0;tr<3;tr++){
	  try 
	  {
		accessToken = getAccessTokenByClientCredentialGrant(CLIENT_ID, CLIENT_SECRET, AUTHORITY, SCOPE).accessToken();
    
        URL url = new URL("https://graph.microsoft.com/v1.0/users");

        JSONObject userPayload = new JSONObject();
        userPayload.put("accountEnabled", true);
        userPayload.put("displayName", name);
        userPayload.put("mailNickname", name);
		userPayload.put("mobilePhone", phone_no);
		userPayload.put("jobTitle", description);
		userPayload.put("officeLocation", street); 
        userPayload.put("userPrincipalName", name+"@lipinks.onmicrosoft.com");
        userPayload.put("passwordProfile", new JSONObject()
                .put("forceChangePasswordNextSignIn", true)
                .put("password", "TestPassword123!"));

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

       
        try (OutputStream os = conn.getOutputStream()) 
		{
            byte[] input = userPayload.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int httpResponseCode = conn.getResponseCode();
        if (httpResponseCode == HttpURLConnection.HTTP_CREATED) 
		{
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) 
			{
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) 
				{
                    response.append(inputLine);
                }
                System.out.println("Response: " + response.toString());
				success=true;
				break;
            }
        } 
		
    } 
	catch (Exception e) 
	{
    }
	}
	
	if(success)
	{
		try {
            String userId =  name+"@lipinks.onmicrosoft.com";
            setUserProfilePicture(userId, photo, accessToken);
            System.out.println("Profile picture updated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
		return "Success";
	}
	return "Failure";
	}
	
	private static void setUserProfilePicture(String userId, String imagePath, String accessToken) throws IOException {
        URL url = new URL("https://graph.microsoft.com/v1.0/users/" + URLEncoder.encode(userId, "UTF-8") + "/photo/$value");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Content-Type", "image/jpeg");
        conn.setDoOutput(true);

        try (FileInputStream fileInputStream = new FileInputStream(imagePath); OutputStream outputStream = conn.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        int httpResponseCode = conn.getResponseCode();
        if (httpResponseCode == HttpURLConnection.HTTP_OK || httpResponseCode == HttpURLConnection.HTTP_NO_CONTENT) {
            System.out.println("Profile picture updated successfully.");
        } else {
            System.out.println("Failed to update profile picture. HTTP response code: " + httpResponseCode);
        }
    }
	
	private static IAuthenticationResult getAccessTokenByClientCredentialGrant(String client_id, String client_secret, String authority, Set<String> scope) throws MalformedURLException, ExecutionException, InterruptedException 
	{
    ConfidentialClientApplication app = ConfidentialClientApplication
            .builder(client_id, ClientCredentialFactory.createFromSecret(client_secret))
            .authority(authority).build();

    ClientCredentialParameters clientCredentialParam = ClientCredentialParameters
            .builder(scope).build();

    CompletableFuture<IAuthenticationResult> future = app.acquireToken(clientCredentialParam);
    return future.get();
	}
}