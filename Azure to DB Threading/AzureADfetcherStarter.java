import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class AzureADfetcherStarter implements ServletContextListener {
    boolean is_first=true;
    private ScheduledExecutorService scheduler;
    Url_storage nxt_url = new Url_storage();
	
    public void delete_files() 
	{
        String directoryPath ="D:\\Documents\\azure_users";
        File directory = new File(directoryPath);
        for (File file : directory.listFiles()){
            file.delete();}
	}

    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Starting AzureADFetcher...");
	scheduler = Executors.newScheduledThreadPool(2);
        scheduler.scheduleAtFixedRate(() -> {
            try {
		delete_files();
                System.out.println("Running AzureADFetcher task...");
                Produce_Consume p_q;
		p_q=new Produce_Consume(nxt_url);
                p_q.accessToken = p_q.getAccessTokenByClientCredentialGrant().accessToken();
                new Producer(p_q);
                new Consumer(p_q);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 10, TimeUnit.MINUTES);
    }

   
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Stopping AzureADFetcher...");
	if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
}
