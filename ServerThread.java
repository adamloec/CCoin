package chain;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerThread extends Thread
{
	private Server serverThread;
	private Socket socket;
	private PrintWriter printWriter;
	
	public ServerThread(Socket socket, Server serverThread)
	{
		this.serverThread = serverThread;
		this.socket = socket;
	}
	
	public void run()
	{
		try
		{
			BufferedReader bReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			this.printWriter = new PrintWriter(socket.getOutputStream(), true);
			while (true)
			{
				serverThread.sendData(bReader.readLine());
			}
		}
		catch (Exception e)
		{
			serverThread.getServerThreads().remove(this);
		}
	}
	
	public PrintWriter getPrintWriter()
	{
		return printWriter;
	}
}
