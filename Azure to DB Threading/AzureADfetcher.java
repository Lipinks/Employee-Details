import com.microsoft.aad.msal4j.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.sql.*;


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
			/*
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
			*/
        }
        System.out.println("Producer thread stopped.");
		return;
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
			/*
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
			*/
        }
        System.out.println("Consumer thread stopped.");
		return;
    }
}
/*
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
			/*
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
*/