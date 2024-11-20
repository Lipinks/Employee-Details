import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AzureADfetcherStarter implements ServletContextListener {

    private ScheduledExecutorService scheduler;

   
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Starting AzureADFetcher...");
	scheduler = Executors.newScheduledThreadPool(2);
        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("Running AzureADFetcher task...");
                Produce_Consume p_q = new Produce_Consume();
                p_q.accessToken = p_q.getAccessTokenByClientCredentialGrant().accessToken();
                new Producer(p_q);
                new Consumer(p_q);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 30, TimeUnit.MINUTES);
    }

   
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Stopping AzureADFetcher...");
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
}
