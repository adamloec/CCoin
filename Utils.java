package chain;
import java.util.stream.Stream;

import javax.crypto.spec.SecretKeySpec;

import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.io.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utils 
{
	public static String userPath = "C:\\Users\\adaml\\OneDrive\\CCoin\\src\\chain\\user.dat";
	
	//----------------------------------------------Core Utilities----------------------------------------------
	public static int num = 0;
	public static String applySha256(String input)
	{
		try
		{
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(input.getBytes("UTF-8"));
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < hash.length; i++)
			{
				String hex = Integer.toHexString(0xff & hash[i]);
				if (hex.length() == 1)
				{
					hexString.append("0");
				}
				else
				{
					hexString.append(hex);
				}
			}
			return hexString.toString();
			
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static byte[] applyECDSA(PrivateKey privateKey, String input)
	{
		Signature dsa;
		byte[] output = new byte[0];
		try
		{
			dsa = Signature.getInstance("SHA256withECDSA");
			dsa.initSign(privateKey);
			byte[] strByte = input.getBytes();
			dsa.update(strByte);
			byte[] realSig = dsa.sign();
			output = realSig;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		return output;
	}
	
	public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature)
	{
		try
		{
			Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA");
			ecdsaVerify.initVerify(publicKey);
			ecdsaVerify.update(data.getBytes());
			return ecdsaVerify.verify(signature);
		}
		catch (Exception e)
		{
			e.getStackTrace()[0].getLineNumber();
			return false;
		}
	}
	
	public static String getMerkleRoot(ArrayList<Transaction> transactions) 
	{
		int count = transactions.size();
		ArrayList<String> previousTreeLayer = new ArrayList<String>();
		
		for(Transaction transaction : transactions) 
		{
			previousTreeLayer.add(transaction.transactionId);
		}
		
		ArrayList<String> treeLayer = previousTreeLayer;
		while(count > 1) 
		{
			treeLayer = new ArrayList<String>();
			for(int i=1; i < previousTreeLayer.size(); i++) 
			{
				treeLayer.add(applySha256(previousTreeLayer.get(i - 1) + previousTreeLayer.get(i)));
			}
			count = treeLayer.size();
			previousTreeLayer = treeLayer;
		}
		String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
		return merkleRoot;
	}
	
	public static String getDifficultyString(int difficulty)
	{
		return new String(new char[difficulty]).replace("\0", "0");
	}
	
	
	//----------------------------------------------Helper Utilities----------------------------------------------
	public static String getStringFromKey(Key key)
	{
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}
	
	public static PublicKey getPublicKeyFromString(String key) throws InvalidKeySpecException
	{
		EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(key));
		KeyFactory kf = null;
		PublicKey publicKey = null;
		try 
		{
			kf = KeyFactory.getInstance("EC");
			publicKey = kf.generatePublic(publicKeySpec);
			return publicKey;
		} 
		catch (NoSuchAlgorithmException e) 
		{
			throw new RuntimeException(e);
		}
	}
	
	public static PrivateKey getPrivateKeyFromString(String key) throws InvalidKeySpecException
	{
		EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(key));
		KeyFactory kf = null;
		PrivateKey privateKey = null;
		try 
		{
			kf = KeyFactory.getInstance("EC");
			privateKey = kf.generatePrivate(privateKeySpec);
			return privateKey;
		} 
		catch (NoSuchAlgorithmException e) 
		{
			throw new RuntimeException(e);
		}
	}
	
	public static String stringToBinary(String data)
	{
		StringBuilder result = new StringBuilder();
		char[] chars = data.toCharArray();
		for (char aChar : chars)
		{
			result.append(String.format("%8s", Integer.toBinaryString(aChar)).replaceAll(" ", "0"));
		}
		return result.toString();
	}
	
	public static String binaryToString(String data)
	{
		StringBuilder stringBuilder = new StringBuilder();
	    int charCode;
	    for (int i = 0; i < data.length(); i += 8) 
	    {
	        charCode = Integer.parseInt(data.substring(i, i + 8), 2);
	        String returnChar = Character.toString((char) charCode);
	        stringBuilder.append(returnChar);
	    }
	    return stringBuilder.toString();
	}
	
	
	//----------------------------------------------File Utilities----------------------------------------------
	public static String blkHandler()
	{
		try
		{
			int currentChainSize = dirSize("Blk");
			String blk = "blk" + currentChainSize;
			File dir = new File("C:\\Users\\Adam Loeckle\\code\\CCoin\\src\\chain\\Blk\\" + blk + ".dat");
			if (dir.createNewFile())
			{
				System.out.println("CONSOLE: Block file " + dir + " successfully created.");
				return dir.toString();
			}
			else
			{
				System.out.println("ERROR: Block file unsuccessfully created.");
				return null;
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static Boolean userHandler(int portNumber)
	{
		try
		{
			String ip = null;
			File dir = new File(userPath);
			if (dir.createNewFile())
			{
				String publicKey = Utils.stringToBinary(Utils.getStringFromKey(Run.nodeWallet.publicKey));
				String privateKey = Utils.stringToBinary(Utils.getStringFromKey(Run.nodeWallet.privateKey));
				URL myIp = new URL("http://checkip.amazonaws.com");
				BufferedReader in = new BufferedReader(new InputStreamReader(myIp.openStream()));
				ip = in.readLine();
				
				String userData = publicKey + ";" + privateKey + ";" + ip + ":" + portNumber;
				FileWriter myWriter = new FileWriter(userPath);
				myWriter.write(userData);
				myWriter.close();
				return false;
			}
			return true;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static PublicKey userPub() throws IOException
	{
		String line = null;
		BufferedReader bReader = new BufferedReader(new FileReader("C:\\Users\\adaml\\OneDrive\\CCoin\\src\\chain\\user.dat"));
		while ((line = bReader.readLine()) != null)
		{
			String[] values = line.split(";");
			try 
			{
				return getPublicKeyFromString(binaryToString(values[0]));
			} 
			catch (InvalidKeySpecException e) 
			{
				throw new RuntimeException(e);
			}
		}
		return null;
	}
	
	public static PrivateKey userPrv() throws IOException
	{
		String line = null;
		BufferedReader bReader = new BufferedReader(new FileReader("C:\\Users\\adaml\\OneDrive\\CCoin\\src\\chain\\user.dat"));
		while ((line = bReader.readLine()) != null)
		{
			String[] values = line.split(";");
			try 
			{
				return getPrivateKeyFromString(binaryToString(values[1]));
			} 
			catch (InvalidKeySpecException e) 
			{
				throw new RuntimeException(e);
			}
		}
		return null;
	}
	
	public static String userIp() throws IOException
	{
		String line = null;
		BufferedReader bReader = new BufferedReader(new FileReader("C:\\Users\\adaml\\OneDrive\\CCoin\\src\\chain\\user.dat"));
		while ((line = bReader.readLine()) != null)
		{
			String[] values = line.split(";");
			return values[2];
		}
		return null;
	}
	
	public static int dirSize(String dirName)
	{
		try (Stream<Path> files = Files.list(Paths.get("C:\\Users\\adaml\\OneDrive\\CCoin\\src\\chain" + dirName)))
		{
			return (int) files.count();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static long fileSize(String path)
	{
		File file = new File(path);
		return file.length();
	}
}













