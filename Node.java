package chain;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.json.Json;

import com.dosse.upnp.UPnP;

public class Node 
{
	public static Server serverThread;
	// Creates self node, if new IP address update the IP.
	public static void main(String[] args) throws Exception
	{
		// User port input
		Scanner scanner = new Scanner(System.in);
		System.out.println("CONSOLE: Welcome to the Com Network.");
		System.out.println("CONSOLE: Enter port number (UPnP): ");
		int newPort = scanner.nextInt();
		boolean ans = UPnP.openPortTCP(newPort);
		System.out.println("CONSOLE: Port opened: " + ans);
		
		// Collects users current public IP address
		URL myIp = new URL("http://checkip.amazonaws.com");
		BufferedReader in = new BufferedReader(new InputStreamReader(myIp.openStream()));
		String newIp = in.readLine();
		
		// Checks to see if user.dat file exists, compares new IP/port to old saved IP/port, updates user.dat file
		String oldIp = null;
		String oldPort = null;
		if (Utils.userHandler(newPort))
		{
			String line = null;
			String newLine = null;
			BufferedReader bReader = new BufferedReader(new FileReader("C:\\Users\\adaml\\OneDrive\\CCoin\\src\\chain\\user.dat"));
			while ((line = bReader.readLine()) != null)
			{
				String[] values = line.split(";");
				String[] oldIpPort = values[2].split(":");
				oldIp = oldIpPort[0];
				oldPort = oldIpPort[1];
				
				if (!oldIp.equals(newIp))
				{
					if (Integer.parseInt(oldPort) != newPort)
					{
						values[2] = newIp + newPort;
					}
					else
					{
						values[2] = newIp + newPort;
					}
				}
				else
				{
					if (Integer.parseInt(oldPort) != newPort)
					{
						values[2] = newIp + newPort;
					}
				}
				
				for (int i = 0; i < values.length; i++)
				{
					newLine += i + ";";
				}
				FileWriter myWriter = new FileWriter("C:\\Users\\adaml\\OneDrive\\CCoin\\src\\chain\\user.dat");
				myWriter.write(newLine);
				myWriter.close();
			}	
		}
		
		// Checks to see if nodes.dat file exists, compares new IP/port to old IP/port in node list, updates nodes.dat file
		File dir = new File("C:\\Users\\adaml\\OneDrive\\CCoin\\src\\chain\\Network\\nodes.dat");
		if (!dir.createNewFile())
		{
			String line = null;
			BufferedReader bReader = new BufferedReader(new FileReader("C:\\Users\\adaml\\OneDrive\\CCoin\\src\\chain\\Network\\nodes.dat"));
			while ((line = bReader.readLine()) != null)
			{
				String[] values = line.split(";");
				String newValues = null;
				Boolean flag = false;
				for (int i = 0; i < values.length; i++)
				{
					if (values[i].equals(oldIp + ":" + oldPort))
					{
						values[i] = newIp + ":" + newPort;
						flag = true;
					}
					newValues += values[i] + ";";
				}
				
				if (flag == false)
				{
					newValues += newIp + ":" + oldPort;
				}
				
				FileWriter myWriter = new FileWriter("C:\\Users\\adaml\\OneDrive\\CCoin\\src\\chain\\Network\\nodes.dat");
				myWriter.write(newValues);
				myWriter.close();
			}
		}
		else
		{
			// Requests and recieves updated node list from core node (http ip, adamsinter.net)
			try (BufferedInputStream coreNodes = new BufferedInputStream(new URL("http://adamsinter.net/nodes.dat").openStream()))
			{
				FileOutputStream fileOutputStream = new FileOutputStream("C:\\Users\\adaml\\OneDrive\\CCoin\\src\\chain\\Network\\nodes.dat");
				byte[] dataBuffer = new byte[1024];
				int bytesRead;
				while ((bytesRead = coreNodes.read(dataBuffer, 0, 1024)) != -1) 
				{
			        fileOutputStream.write(dataBuffer, 0, bytesRead);
			    }
				String myIpPort = (newIp + ":" + newPort);
				FileWriter myWriter = new FileWriter("C:\\Users\\adaml\\OneDrive\\CCoin\\src\\chain\\Network\\nodes.dat", true);
				myWriter.write(myIpPort);
				myWriter.close();
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		
		serverThread = new Server(String.valueOf(newPort));
		serverThread.start();
		new Node().updateListenToPeers(serverThread);
		
		// Main program starts, uses the above serverthread object for the communicate parameter
		
		
		
	}
	
	public void updateListenToPeers(Server serverThread) throws Exception
	{
		String line = null;
		String[] values = null;
		BufferedReader bReader = new BufferedReader(new FileReader("C:\\Users\\adaml\\OneDrive\\CCoin\\src\\chain\\Network\\nodes.dat"));
		while ((line = bReader.readLine()) != null)
		{
			values = line.split(";");
		}
		
		for (int i = 0; i < values.length; i++)
		{
			String[] address = values[i].split(":");
			Socket socket = null;
			try
			{
				socket = new Socket(address[0], Integer.parseInt(address[1]));
				new NodeThread(socket).start();
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
	}
	
	public void communicate(Server serverThread, String header, String data)
	{
		try
		{
			System.out.println("CONSOLE: Sending " + header + " data to nodes.");
			StringWriter stringWriter = new StringWriter();
			Json.createWriter(stringWriter).writeObject(Json.createObjectBuilder().add(header, header).add("data", data).build());
			serverThread.sendData(stringWriter.toString());
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

}
