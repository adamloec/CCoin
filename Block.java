package chain;
import java.util.*;

public class Block 
{
	public String hash;
	public String previousHash;
	public String merkleRoot;
	public ArrayList<Transaction> transactions = new ArrayList<Transaction>();
	public long timeStamp;
	public int nonce;

	public Block(String previousHash)
	{
		this.previousHash = previousHash;
		this.timeStamp = new Date().getTime();
		this.hash = calculateHash();
	}
	
	public String calculateHash()
	{
		String calculatedhash = Utils.applySha256(previousHash + Long.toString(timeStamp) + Integer.toString(nonce) + merkleRoot);
		return calculatedhash;
	}
	
	// When block is successfully mined it is broadcast to all nodes with a timestamp, node with first timestamp gets mined reward assuming it was successful
	public void mineBlock(int difficulty)
	{
		merkleRoot = Utils.getMerkleRoot(transactions);
		String target = Utils.getDifficultyString(difficulty);
		while (!hash.substring(0, difficulty).equals(target))
		{
			nonce++;
			hash = calculateHash();
		}
		System.out.println("CONSOLE: Block successfully mined. Hash: " + hash);
	}
	
	// Transaction is broadcast across all nodes for approval before block insertion
	public boolean addTransaction(Transaction transaction) 
	{
		if(transaction == null)
		{
			return false;
		}
		
		if((previousHash != "0")) 
		{
			if((transaction.processTransaction() != true)) 
			{
				System.out.println("ERROR: Transaction failed to process.");
				return false;
			}
		}
		
		transactions.add(transaction);
		System.out.println("CONSOLE: Transaction successfully added to block.");
		return true;
		
	}
}
