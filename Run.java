// Decentralized blockchain based cryptocurrency.
// Author - Adam
//
//
//
// TODO:
// Create nodes and Integrate network protocols for non-localized access/communication (Time stamped data for orginization and ensure same chain: Send/recieve queuedTransactions list, send/recieve queuedBlocks, send/recieve updated chain files)
// Consensus algorithm for longest chain superiority
// Miner rewards (10 coins per miner), focus on single user mining (no groups, yet)
// CUDA implementation for faster mining through core app?
// Difficulty fluctuation algorithm

package chain;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import com.dosse.upnp.UPnP;

import java.io.*;

public class Run 
{
	public static Wallet nodeWallet = new Wallet();
	public static String ip;
	
	public static void main(String[] args)
	{
		try
		{
			File userFile = new File("C:\\Users\\Adam Loeckle\\code\\CCoin\\src\\chain\\user.dat");
			if (userFile.exists())
			{
				nodeWallet.publicKey = Utils.userPub();
				nodeWallet.privateKey = Utils.userPrv();
				ip = Utils.userIp();
			}
			else
			{
				Utils.userHandler();
				nodeWallet.publicKey = Utils.userPub();
				nodeWallet.privateKey = Utils.userPrv();
				ip = Utils.userIp();
			}
			
			boolean continuer = true;
			Scanner scanner = null;
			int choice = 0;
			
			while (continuer)
			{
				System.out.println("Welcome: " + ip);
				System.out.println("Public Key: " + Utils.getStringFromKey(Utils.userPub()));
				System.out.println("Online nodes: ");
				System.out.println("1) Perform transaction");
				System.out.println("2) About");
				System.out.println("3) Test Driver");
				
				scanner = new Scanner(System.in);
	            choice = scanner.nextInt();
	            
	            if (choice == 1)
	            {
	            	
	            }
	            
	            if (choice == 2)
	            {
	            	System.out.println("Decentralized blockchain based cryptocurrency built entirely in Java.");
	            	System.out.println("Author - Adam");
	            }
	            
	            if (choice == 3)
	            {
	            	Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	            	Wallet walletA = new Wallet();
	            	Wallet walletB = new Wallet();
		        	File genesis = new File("C:\\Users\\Adam Loeckle\\code\\CCoin\\src\\chain\\Blk\\blk0.dat");
		        	boolean genesisMined = genesis.exists();
		        	Block genesisBlock;
		        	Block newBlock = null;
		        	String prevHash = "";
		        	
		        	boolean testContinuer = true;
	            	while (testContinuer)
	            	{
		            	System.out.println("-------------------------------------------------------");
		            	System.out.println("Your Wallet Balance: " + nodeWallet.getBalance());
		            	System.out.println("WalletA Balance: " + walletA.getBalance());
		            	System.out.println("WalletB Balance: " + walletB.getBalance());
		            	System.out.println("-------------------------------------------------------");
		            	System.out.println("1) Create and mine genesis");
		            	System.out.println("2) Add transaction (You (10) -> A)");
		            	System.out.println("3) Add transaction (A (10) -> B)");
		            	System.out.println("4) Add transaction (B (10) -> A)");
		            	System.out.println("5) Mine block");
		            	System.out.println("6) Verify blockchain");
		            	System.out.println("-------------------------------------------------------");
		            	scanner = new Scanner(System.in);
		            	int testChoice = scanner.nextInt();
		            	
		            	if (testChoice == 1)
		            	{
		            		if (!genesisMined)
		            		{
		            			Wallet coinbase = new Wallet();
		            			Transaction genesisTransaction = new Transaction(coinbase.publicKey, nodeWallet.publicKey, 1000000f, null);
			            		genesisTransaction.generateSignature(coinbase.privateKey); 
			            		genesisTransaction.transactionId = "0";
			            		genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.recipient, genesisTransaction.value, genesisTransaction.transactionId));
			            		Chain.UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); 
			            		
			            		System.out.println("CONSOLE: Genesis.");
			            		genesisBlock = new Block("0");
			            		genesisBlock.addTransaction(genesisTransaction);
			            		Chain.addGenesisBlock(genesisBlock);
			            		prevHash = genesisBlock.hash;
			            		genesisMined = true;
		            		}
		            		else
		            		{
		            			System.out.println("ERROR: Genesis already verified. Please select another test choice.");
		            		}
		            	}
		            	
		            	if (testChoice == 2)
		            	{
		            		if (!genesisMined)
		            		{
		            			System.out.println("ERROR: Please create and mine genesis block before adding transaction data.");
		            		}
		            		
		            		else if (newBlock != null)
		            		{
		            			if (newBlock.transactions.size() < 10)
			            		{
			            			newBlock.addTransaction(nodeWallet.sendFunds(walletA.publicKey, 10f));
			            		}
		            			else
		            			{
		            				System.out.println("ERROR: Block has reached maximum transactions. Please mine the current block.");
		            			}
		            		}
		            		
		            		else if (newBlock == null)
		            		{
		            			prevHash = Chain.getPreviousHash();
		            			newBlock = new Block(prevHash);
		            			newBlock.addTransaction(nodeWallet.sendFunds(walletA.publicKey, 10f));
		            		}
		            	}
		            	
		            	if (testChoice == 3)
		            	{
		            		if (!genesisMined)
		            		{
		            			System.out.println("ERROR: Please create and mine genesis block before adding transaction data.");
		            		}
		            		
		            		else if (newBlock != null)
		            		{
		            			if (newBlock.transactions.size() < 10)
			            		{
			            			newBlock.addTransaction(walletA.sendFunds(walletB.publicKey, 20f));
			            		}
		            			else
		            			{
		            				System.out.println("ERROR: Block has reached maximum transactions. Please mine the current block.");
		            			}
		            		}
		            		
		            		else if (newBlock == null)
		            		{
		            			prevHash = Chain.getPreviousHash();
		            			newBlock = new Block(prevHash);
		            			newBlock.addTransaction(walletA.sendFunds(walletB.publicKey, 10f));
		            		}
		            	}
		            	
		            	if (testChoice == 4)
		            	{
		            		if (!genesisMined)
		            		{
		            			System.out.println("ERROR: Please create and mine genesis block before adding transaction data.");
		            		}
		            				
		            		else if (newBlock != null)
		            		{
		            			if (newBlock.transactions.size() < 10)
			            		{
			            			newBlock.addTransaction(walletB.sendFunds(walletA.publicKey, 10f));
			            		}
		            			else
		            			{
		            				System.out.println("ERROR: Block has reached maximum transactions (10). Please mine the current block.");
		            			}
		            		}
		            		
		            		else if (newBlock == null)
		            		{
		            			prevHash = Chain.getPreviousHash();
		            			newBlock = new Block(prevHash);
		            			newBlock.addTransaction(walletB.sendFunds(walletA.publicKey, 10f));
		            		}
		            	}
		            	
		            	if (testChoice == 5)
		            	{
		            		if (newBlock != null)
		            		{
		            			if (newBlock.transactions.size() != 10)
			            		{
			            			System.out.println("ERROR: Block has not reached maximum transactions (10). Please continue adding transactions.");
			            		}
		            			else
		            			{
		            				Chain.addBlock(newBlock);
			            			newBlock = null;
			            			Chain.updatedLocalChain = Chain.updateLocalChain();
		            			}
		            		}
		            		else
		            		{
		            			System.out.println("ERROR: Please add transactions to create a new block.");
		            		}
		            	}
		            	
		            	if (testChoice == 6)
		            	{
		            		Chain.checkValid();
		            	}
	            	}
	            }
	            
	            else
	            {
	            	System.out.println("ERROR: Please enter a valid choice.");
	            }
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
