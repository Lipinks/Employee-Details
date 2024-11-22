import java.util.concurrent.*;

class Producer implements Runnable 
{
    private Produce_Consume p_q;

    public Producer(Produce_Consume p_q) 
	{
        this.p_q = p_q;
        new Thread(this, "Producer").start();
    }

    public void run() 
	{
        while (p_q.shouldRun) 
		{
            p_q.produce();
        }
        System.out.println("Producer thread stopped.");
		return;
    }
}
