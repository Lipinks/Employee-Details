import java.io.*;
import java.util.*;
import java.util.concurrent.*;

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
        }
        System.out.println("Consumer thread stopped.");
	return;
    }
}
