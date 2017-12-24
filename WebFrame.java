
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.table.DefaultTableModel;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;
/*
The WebFrame should contain the GUI, keep pointers to the main elements,
and manage the overall program flow.
 */
@SuppressWarnings("serial")
public class WebFrame extends JFrame

{

	static ExecutorService threadPool;
	static List<String> urls;
	static List<Component> btns = new ArrayList<Component>();
	//static int threadLimit = 30;     //for semaphore not initialized yet,correct value from instructions unclear
	static boolean inRunState;
	static ExecutorService singleThread;
	WebFrame frame;
	/*
    can use threadPool.getActiveCount() to determine number of running threads.
	 */

	public WebFrame()
	{
		threadPool = Executors.newCachedThreadPool();
		inRunState= false;
		frame = this;

	}

	public JButton addStopButton()
	{
		JButton stop = new JButton("Stop");
		stop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae)
			{
				killButtonVisibility("Stop");
				if (singleThread != null)
					singleThread.shutdownNow();
				else if (threadPool != null)
				{
					try{threadPool.shutdownNow();}
					catch(Exception e){
						System.out.println("Threadpool couldn't shutdown:" + e.getMessage());
					}
				}
			}

		});
		return stop;
	}

	public JButton addFetchButton(boolean concurrently)
	{
		JButton fetch;
		if (concurrently)
		{
			fetch = new JButton("Fetch Concurrently");
			fetch.addActionListener(new ActionListener()  {
				public void actionPerformed(ActionEvent ae)
				{
					killButtonVisibility("Fetch Concurrently");
					killButtonVisibility("Fetch");
					launchWorkers(true);

				}
			});
		}
		else
		{
			fetch = new JButton("Fetch");
			fetch.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{
					killButtonVisibility("Fetch Concurrently");
					killButtonVisibility("Fetch");
					launchWorkers(false);
					;
				}
			});
		}
		return fetch;
	}

	public void killButtonVisibility(String buttonName)
	{
		for(Component elt: btns)
		{
			if (((AbstractButton) elt).getText().charAt(0) == buttonName.charAt(0))
			{
				//resetButtonVisibility();
				elt.setVisible(false);
				elt.invalidate();
				frame.validate();
				frame.repaint();
			}
		}
	}

	public void resetButtonVisibility()
	{
		for (Component elt: btns)
		{
			elt.setVisible(true);
			elt.validate();
			frame.validate();
			frame.repaint();
		}
	}

	public void launchWorkers(boolean concurrently)
	{
		Runnable launch;

		if (!concurrently)
		{
			singleThread = Executors.newSingleThreadExecutor();
			//ThreadPoolExecutor s = (ThreadPoolExecutor)WebFrame.singleThread;
			launch = new Runnable() {
				public void run()
				{
					for (int i = 0; i < urls.size(); i++)
					{
						singleThread.execute(new WebWorker(urls.get(i), i, frame));
					}
					threadPool.shutdown();
					try {
						threadPool.awaitTermination(10 * 1000, TimeUnit.NANOSECONDS);//(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
		}

		else
		{
			launch = new Runnable() {
				public void run()
				{
					for (int i = 0; i < urls.size(); i++)
					{
						threadPool.execute(new WebWorker(urls.get(i), i, frame));
					}
					threadPool.shutdown();
					try{

						threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

					}catch(InterruptedException exc){
						exc.printStackTrace();
					}

				}
			};

		}
		new Thread(launch).start();
	}

	public static void main(String[] args)
	{
		WebFrame frame = new WebFrame();
		frame.setLayout(new FlowLayout());
		DefaultTableModel model = new DefaultTableModel(new String[] { "URL", "Status"}, 0);
		JTable table = new JTable(model);
		String url;
		urls = new ArrayList<String>();
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		String fileName = "";
		if (args.length == 1)
			fileName = args[0];
		else
			fileName = "http://www.sjsu.edu/cs/";
		try (FileReader fr = new FileReader(fileName);
				BufferedReader buffReader = new BufferedReader(fr);)
		{
			while ((url = buffReader.readLine()) != null)
				urls.add(url);
			//		urls.add("http://www.yahoo.com");
			//		urls.add("http://www.reddit.com/r/wtf");
			//		urls.add("http://www.espn.com");
			for (String elt: urls)
				model.addRow(new String[] { elt, "No Stats yet"});

		}catch (FileNotFoundException exception){
			exception.printStackTrace();
		}catch (IOException e){
			e.printStackTrace();
		}


		JScrollPane scrollpane = new JScrollPane(table);
		scrollpane.setPreferredSize(new Dimension(800,400));
		frame.add(scrollpane);
		JPanel p1 = new JPanel();
		JButton f1 = frame.addFetchButton(true);
		JButton f2 = frame.addFetchButton(false);
		JButton s = frame.addStopButton();
		p1.setLayout(new FlowLayout());
		p1.add(f1);
		btns.add(f1);
		p1.add(f2);
		btns.add(f2);
		p1.add(s);
		btns.add(s);
		frame.add(p1);


		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);


	}
}
