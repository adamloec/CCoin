package chain;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;
import javax.json.Json;
import javax.json.JsonObject;

public class NodeThread extends Thread
{
	private BufferedReader bReader;
	private DataOutputStream out;
	private DataInputStream in;
	public NodeThread(Socket socket) throws IOException
	{
		bReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
	}
	
	public void run()
	{
		boolean flag = true;
		while (flag)
		{
			try
			{
				JsonObject obj = Json.createReader(bReader).readObject();
				if (obj.containsKey("blk"))
				{
					System.out.println("CONSOLE: Downloading and verifying newly mined block.");
				}
				
				if (obj.containsKey("chain"))
				{
					System.out.println("CONSOLE: Downloading new blockchain.");
				}
				
				if (obj.containsKey("nodes"))
				{
					System.out.println("CONSOLE: Updating nodes list");
				}
				
				if (obj.containsKey(""))
				{
					
				}
			}
			catch (Exception e)
			{
				flag = false;
				interrupt();
			}
		}
	}
}
