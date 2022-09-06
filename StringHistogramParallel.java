import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

	/*This program calculates the frequency of each letter of a file using multithread programming*/

public class StringHistogramParallel {
	
    public static final int alphabetSize = 256;
	static int[] histogram;
	static Lock lock = new ReentrantLock();
	
    public static void main(String args[]) throws IOException {

        if (args.length != 2) {
		System.out.println("StringHistogram <file name> <number of threads>");
                System.exit(1);
        }
		
        String fileString = new String(Files.readAllBytes(Paths.get(args[0])));//, StandardCharsets.UTF_8);
        char[] text = new char[fileString.length()]; 
        int n = fileString.length();
        for (int i = 0; i < n; i++) { 
            text[i] = fileString.charAt(i); 
        }
		
		int numThreads = Integer.parseInt(args[1]);
        
		long startTime = System.currentTimeMillis();
		
		histogram = new int[alphabetSize];
		for(int i=0; i<alphabetSize; i++)
			histogram[i] = 0;
			
        HistogramThread[] threads= new HistogramThread[numThreads];
		for(int i = 0; i<numThreads; i++){
			threads[i] = new HistogramThread(i, text, n, numThreads);
			threads[i].start();
		}
		
		for(int i=0; i<numThreads; i++){
			try{
				threads[i].join();
			}catch(InterruptedException e){}
		}
		
		long endTime = System.currentTimeMillis();
        for (int i = 0; i < alphabetSize; i++) { 
            System.out.println((char)i+": "+histogram[i]);
        }
		System.out.println("Time: " + ((double) endTime - startTime));
    }
	
	static class HistogramThread extends Thread {
		private int id;
		private char[] text;
		private int myStart;
		private int myStop;
		private int n;
		private int[] localHistogram = new int[alphabetSize];

		public HistogramThread(int id, char[] text, int n, int numThreads){
			this.id = id;
			this.text = text;
			this.n = n;
			this.myStart = id*(n/numThreads);
			this.myStop = myStart + (n/numThreads);
			if(id == numThreads-1)
				myStop = n;
			for(int i = 0; i<alphabetSize; i++)
				localHistogram[i] = 0;
		}
		
		public void run(){
			for (int i = myStart; i < myStop; i++) {
                localHistogram[(int)text[i]] ++;
			}
			lock.lock();
			try{
				for(int i=0;i<alphabetSize;i++)
					histogram[i] += localHistogram[i];
			}finally{lock.unlock();}
			
		}
	}
	
}

