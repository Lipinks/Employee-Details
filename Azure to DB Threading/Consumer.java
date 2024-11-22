import java.util.concurrent.*;
class Consumer implements Runnable 
{
    private Produce_Consume p_q;

    public Consumer(Produce_Consume p_q) 
	{
        this.p_q = p_q;
        new Thread(this, "Consumer").start();
    }

    public void run() 
	{
        while (p_q.shouldRun) 
		{
            p_q.consume();
        }
        System.out.println("Consumer thread stopped.");
		return;
    }
}
