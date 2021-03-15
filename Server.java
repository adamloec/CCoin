package chain;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Set;

public class Server extends Thread 
{
	private ServerSocket serverSocket;
	private Set<ServerThread> serverThreads = new HashSet<ServerThread>();
	
	public Server(String nodeNumber) throws IOException
	{
		serverSocket = new ServerSocket(Integer.parseInt(nodeNumber));
	}
	
	public void run()
	{
		try
		{
			while (true)
			{
				ServerThread serverThread = new ServerThread(serverSocket.accept(), this);
				serverThreads.add(serverThread);
				serverThread.start();
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	void sendData(String data)
	{
		try
		{
			serverThreads.forEach(t -> t.getPrintWriter().println(data));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public Set<ServerThread> getServerThreads()
	{
		return serverThreads;
	}
}
