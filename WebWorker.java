
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.*;
import java.util.Observable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;

public class WebWorker extends Thread
{
	/*
  This is the core web/download i/o code...*/
	String urlString;
	int rowNumber;
	WebFrame frame;
	static Semaphore permit;
	static List<String> urlResults;

	public WebWorker(String url, int rowNumber, WebFrame frame)
	{
		this.rowNumber = rowNumber;
		this.frame = frame;
		urlString = url;
		urlResults = new ArrayList<String>();
		//permit = new Semaphore(WebFrame.threadLimit);	//not sure of number of semaphore limits
	}

	public void run() {
		System.out.println("Fetching...." + urlString);
		InputStream input = null;
		StringBuilder contents = null;
		try {
			//permit.acquireUninterruptibly();
			URL url = new URL(urlString);
			URLConnection connection = url.openConnection();
			// Set connect() to throw an IOException
			// if connection does not succeed in this many msecs.
			connection.setConnectTimeout(5000);
			connection.connect();
			input = connection.getInputStream();
			BufferedReader reader  = new BufferedReader(new InputStreamReader(input));
			char[] array = new char[1000];
			int len;
			contents = new StringBuilder(1000);
			while ((len = reader.read(array, 0, array.length)) > 0) {
				System.out.println("Fetching...." + urlString + len);
				contents.append(array, 0, len);
				Thread.sleep(10);
			}
			downLoad(contents.toString());
				//permit.release();

		}
		// Otherwise control jumps to a catch...
		catch(MalformedURLException ignored){
			System.out.println("Exception: " + ignored.toString());
		}
		catch(InterruptedException exception){
			// YOUR CODE HERE
			// deal with interruption
			/*
				if interrupted, that means the 'stop' button was pressed.
				use the reference to the frame to showFetchButtons and change state
			 */

			//if worker running, stop button is clickable, change so only fetch buttons are visible
			if (frame != null)
				{
					frame.killButtonVisibility("Stop");
					frame.resetButtonVisibility();
				}
			System.out.println("Button's visibility changed");

		}
		catch(IOException ignored) {
			System.out.println("Exception: " + ignored.toString());
		}
		// "finally" clause, to close the input stream
		// in any case
		finally {
			try{
				if (input != null) input.close();
			}catch(IOException ignored) {}
		}


	}

	public void downLoad(String urlResult)
	{
		urlResults.add(urlResult);

		System.out.println("task completed, in download\n");

		/*
		-WebWorker should have a download() method, called from its run(),
		that tries to download the content of the url. The download() may or may not succeed for any number of reasons, it will probably take between a fraction of a second and a couple seconds to run

		-> https://alvinalexander.com/java/edu/pj/pj010011
		 */
	}


}



