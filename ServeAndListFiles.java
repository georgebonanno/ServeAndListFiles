import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.Socket;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServeAndListFiles {

	private ExecutorService es = Executors.newCachedThreadPool();
	private final ServerSocket serverSocket;
	private final File dirToListFiles;
	private volatile boolean running;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private Runnable tcpListener = new Runnable() {
		public void run() {
			while (running) {
				String requestUrl =null;
				long start = 0;
				try (Socket sock = serverSocket.accept()) {
					start = System.currentTimeMillis();
					try (BufferedReader reader = 
							new BufferedReader(new InputStreamReader(sock.getInputStream()))) {
						String line = "";
						while ((line = reader.readLine()) != null) {
							if (requestUrl == null && !line.isEmpty()) {
								requestUrl = line;
							} else if (requestUrl != null && line.isEmpty()) {
								break;
							}
						}

						try (BufferedWriter writer = 
							new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()))) {
							writer.write("HTTP/1.1 200 OK\nAw man!");
						}
					}
				}catch (IOException e) {
					if (running) {
						new Exception("error while reading from socket",e).printStackTrace();
						running = false;
					} 
				}
				long stop = System.currentTimeMillis();
				System.out.printf("%s|%s|%sms%n",sdf.format(new Date()),requestUrl,(stop-start));
			} 
		}
	};

	private Runnable dirListener = new Runnable() {
		public void run() {
			while (running) {
				dirToListFiles.listFiles();		
			}
		}
	};

	public ServeAndListFiles(short port,File dirToListFiles) throws IOException {
		System.out.println("starting ServeAndListFiles...");
		this.serverSocket = new ServerSocket(port);
		this.dirToListFiles = dirToListFiles;
		running = false;
	}


	private static void printUsage() {
		System.err.println("usage: <port number to listen to> <directory to listen to>");
	}

	public void start() throws IOException {
		if (!running) {
			running=true;
			es.submit(dirListener);
			es.submit(tcpListener);
		}
	}

	public void stop() throws IOException {
		running = false;	
		serverSocket.close();
		es.shutdown();
	}

	public static void main(String [] args) throws Exception {
		boolean valid = 
			(args != null && args.length == 2 && args[0].matches("\\d+"));
		ServeAndListFiles serveAndListFiles = null;
		if (valid) {
			try {
				File dirToList = new File(args[1]);
				if (dirToList.isDirectory()) {
					serveAndListFiles = 
						new ServeAndListFiles(Short.parseShort(args[0]),dirToList);
				} else {
					System.err.println("directory does not exist: "+args[1]);
				}
			} catch (NumberFormatException e) {
				System.err.println("port number out of range: "+args[0]);
			}
			if (serveAndListFiles == null) {
				throw new IllegalStateException("listener not initialised (tbazwar xi haga)");
			}
			serveAndListFiles.start();
			System.err.println("press any key to exit...");
			new InputStreamReader(System.in).read();
			System.err.println("closing....");
			serveAndListFiles.stop();
			System.err.println("lest man");
		} else {
			printUsage();
		}
		
	}
}