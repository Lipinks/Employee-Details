import com.microsoft.aad.msal4j.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.sql.*;

class Produce_Consume {
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
						return;
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (toAppend.length() > 0) {
            try (FileWriter writer = new FileWriter("D:\\Documents\\output.csv")) {
                writer.append(toAppend);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Data fetched successfully");
        }
		System.out.println(toAppend);
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

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute("COPY details FROM 'D:\\Documents\\output.csv' DELIMITER ',' CSV HEADER;");
            System.out.println("Data copied successfully from CSV to database.");
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

class Producer implements Runnable {
    private Produce_Consume p_q;

    public Producer(Produce_Consume p_q) {
        this.p_q = p_q;
        new Thread(this, "Producer").start();
    }

    public void run() {
        while (p_q.shouldRun) {
            try {
                p_q.produce();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Producer thread stopped.");
    }
}

class Consumer implements Runnable {
    private Produce_Consume p_q;

    public Consumer(Produce_Consume p_q) {
        this.p_q = p_q;
        new Thread(this, "Consumer").start();
    }

    public void run() {
        while (p_q.shouldRun) {
            try {
                p_q.consume();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Consumer thread stopped.");
    }
}

public class AzureADfetcher {
    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable task = () -> {
            Produce_Consume p_q = new Produce_Consume();
            try {
                p_q.accessToken = p_q.getAccessTokenByClientCredentialGrant().accessToken();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            new Producer(p_q);
            new Consumer(p_q);
            while (p_q.shouldRun) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        scheduler.scheduleAtFixedRate(task, 0, 30, TimeUnit.MINUTES);
    }
}
