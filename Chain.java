package chain;
import java.util.*;
import java.security.*;
import java.io.*;

public class Chain 
{
	public static ArrayList<Block> updatedLocalChain = null;
	public static ArrayList<Block> queuedBlocks = null;
	public static HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();
	public static int difficulty = 5;										
	public static float minimumTransaction = 0.1f;
	public static int chainSize;
	
	//----------------------------------------------Genesis----------------------------------------------
	// Activates on initial launch of blockchain. Creates and mines genesis block.
	public static int addGenesisBlock(Block newBlock)
	{
		newBlock.mineBlock(difficulty);
		
		String blkPath = Utils.blkHandler();
		try
		{	
			String prevHash = Utils.stringToBinary(newBlock.previousHash);
			String hash = Utils.stringToBinary(newBlock.hash);
			int nonce = newBlock.nonce;
			String merkleRoot = Utils.stringToBinary(newBlock.merkleRoot);
			long timeStamp = newBlock.timeStamp;
			
			String transactionId;
			String sender;
			String recipient;
			Float value;
			String signature;
			String transactionOutputId;
			String UTXOId;
			String UTXORecipient;
			Float UTXOValue;
			String parentTransactionId;
			int UTXOCount;
			String UTXOData = "";
			String data = "";
			
			Transaction currentTransaction = newBlock.transactions.get(0);
			transactionId = Utils.stringToBinary(currentTransaction.transactionId);
			sender = Utils.stringToBinary(Utils.getStringFromKey(currentTransaction.sender));
			recipient = Utils.stringToBinary(Utils.getStringFromKey(currentTransaction.recipient));
			value = currentTransaction.value;
			signature = Utils.stringToBinary(Base64.getEncoder().encodeToString(currentTransaction.signature));
			
			transactionOutputId = null;
			for (TransactionOutput output: currentTransaction.outputs)
			{
				UTXOId = Utils.stringToBinary(output.id);
				UTXORecipient = Utils.stringToBinary(Utils.getStringFromKey(output.recipient));
				UTXOValue = output.value;
				parentTransactionId = Utils.stringToBinary(output.parentTransactionId);
				data += ";" + transactionId + ";" + sender + ";" + recipient + ";" + value + ";" + signature + ";" + transactionOutputId + ";" + UTXOId + ";" + UTXORecipient + ";" + UTXOValue + ";" + parentTransactionId;
			}
			
			UTXOCount = UTXOs.size();
			for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet())
			{
				TransactionOutput UTXO = item.getValue();
				UTXOData += ";" + Utils.stringToBinary(UTXO.id) + ";" + Utils.stringToBinary(Utils.getStringFromKey(UTXO.recipient)) + ";" + UTXO.value + ";" + Utils.stringToBinary(UTXO.parentTransactionId);
			}

			String blkData = prevHash + ";" + hash + ";" + nonce + ";" + merkleRoot + ";" + timeStamp + data + ";" + UTXOCount + UTXOData;
			FileWriter myWriter = new FileWriter(blkPath);
			myWriter.write(blkData);
			myWriter.close();
		}
		catch (Exception e) 
		{
			throw new RuntimeException(e);
		}
		
		return 1;
	}
	
	
	//----------------------------------------------Block Verification/Mine----------------------------------------------
	// Verifies/mines incoming block and adds it to the blockchain.
	public static int addBlock(Block newBlock)
	{
		newBlock.mineBlock(difficulty);		
		if (newBlock.transactions.size() < 10)
		{
			System.out.println("ERROR: Current block is not finished processing transactions. Minimum transaction count for block is 10.");
			return 0;
		}
		
		String blkPath = Utils.blkHandler();
		try
		{	
			String prevHash = Utils.stringToBinary(newBlock.previousHash);
			String hash = Utils.stringToBinary(newBlock.hash);
			int nonce = newBlock.nonce;
			String merkleRoot = Utils.stringToBinary(newBlock.merkleRoot);
			long timeStamp = newBlock.timeStamp;
			
			String transactionId;
			String sender;
			String recipient;
			Float value;
			String signature;
			int inputsCount;
			int outputsCount;
			String transactionOutputId = "";
			String inputs = "";
			String outputs = "";
			String UTXOId = "";
			String UTXORecipient = "";
			Float UTXOValue = 0f;
			String parentTransactionId;
			int UTXOCount;
			String UTXOData = "";
			String data = "";
			for (int i = 0; i < newBlock.transactions.size(); i++)
			{
				outputs = "";
				inputs = "";
				Transaction currentTransaction = newBlock.transactions.get(i);
				transactionId = Utils.stringToBinary(newBlock.transactions.get(i).transactionId);
				sender = Utils.stringToBinary(Utils.getStringFromKey(currentTransaction.sender));
				recipient = Utils.stringToBinary(Utils.getStringFromKey(currentTransaction.recipient));
				value = currentTransaction.value;
				signature = Utils.stringToBinary(Base64.getEncoder().encodeToString(currentTransaction.signature));
				inputsCount = currentTransaction.inputs.size();
				outputsCount = currentTransaction.outputs.size();
				
				for (TransactionInput input: currentTransaction.inputs)
				{
					transactionOutputId = Utils.stringToBinary(input.transactionOutputId);
					TransactionOutput tempUTXO = input.UTXO;
					UTXOId = Utils.stringToBinary(tempUTXO.id);
					UTXORecipient = Utils.stringToBinary(Utils.getStringFromKey(tempUTXO.recipient));
					UTXOValue = tempUTXO.value;
					parentTransactionId = Utils.stringToBinary(tempUTXO.parentTransactionId);
					
					inputs += ";" + transactionOutputId + ";" + UTXOId + ";" + UTXORecipient + ";" + UTXOValue + ";" + parentTransactionId;
				}
				
				for (TransactionOutput output: currentTransaction.outputs)
				{
					UTXOId = Utils.stringToBinary(output.id);
					UTXORecipient = Utils.stringToBinary(Utils.getStringFromKey(output.recipient));
					UTXOValue = output.value;
					parentTransactionId = Utils.stringToBinary(output.parentTransactionId);
					outputs += ";" + UTXOId + ";" + UTXORecipient + ";" + UTXOValue + ";" + parentTransactionId;
				}
				
				data += ";" + inputsCount + ";" + outputsCount + ";" + transactionId + ";" + sender + ";" + recipient + ";" + value + ";" + signature + inputs + outputs;
			}
			
			UTXOCount = UTXOs.size();
			for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet())
			{
				TransactionOutput UTXO = item.getValue();
				UTXOData += ";" + Utils.stringToBinary(UTXO.id) + ";" + Utils.stringToBinary(Utils.getStringFromKey(UTXO.recipient)) + ";" + UTXO.value + ";" + Utils.stringToBinary(UTXO.parentTransactionId);
			}
			
			String blkData = prevHash + ";" + hash + ";" + nonce + ";" + merkleRoot + ";" + timeStamp + data + ";" + UTXOCount + UTXOData;
			FileWriter myWriter = new FileWriter(blkPath);
			myWriter.write(blkData);
			myWriter.close();
		}
		catch (Exception e) 
		{
			throw new RuntimeException(e);
		}
		
		return 1;
	}
	
	
	//----------------------------------------------Updates Local Blockchain----------------------------------------------
	// Feeds local blockchain with verified blk.dat files to update the chain.
	public static ArrayList<Block> updateLocalChain()
	{
		try
		{
			ArrayList<Block> updatedLocalChain = new ArrayList<Block>();
			String blkFile;
			chainSize = Utils.dirSize("Blk");
			for (int i = 0; i < chainSize; i++)
			{
				String line = null;
				blkFile = "blk" + i;
				BufferedReader bReader = new BufferedReader(new FileReader("C:\\Users\\Adam Loeckle\\code\\CCoin\\src\\chain\\Blk\\" + blkFile + ".dat"));
				while ((line = bReader.readLine()) != null)
				{
					String[] values = line.split(";");
					
					String prevHash = Utils.binaryToString(values[0]);
					String hash = Utils.binaryToString(values[1]);
					int nonce = Integer.parseInt(values[2]);
					String merkleRoot = Utils.binaryToString(values[3]);
					long timeStamp = Long.parseLong(values[4]);
					ArrayList<Transaction> verifiedTransactions = new ArrayList<Transaction>();
					UTXOs = new HashMap<String, TransactionOutput>();
					
					// Genesis block rebuild
					if (i == 0)
					{
						String transactionId = Utils.binaryToString(values[5]);
						PublicKey sender = Utils.getPublicKeyFromString(Utils.binaryToString(values[6]));
						PublicKey recipient = Utils.getPublicKeyFromString(Utils.binaryToString(values[7]));
						Float value = Float.parseFloat(values[8]);
						byte[] signature = Base64.getDecoder().decode(Utils.binaryToString(values[9]));
						String transactionOutputId = null;
						String UTXOId = Utils.binaryToString(values[11]);
						PublicKey UTXORecipient = Utils.getPublicKeyFromString(Utils.binaryToString(values[12]));
						Float UTXOValue = Float.parseFloat(values[13]);
						String parentTransactionId = Utils.binaryToString(values[14]);
						
						int UTXOCount = Integer.parseInt(values[15]);
						for (int k = 0; k < UTXOCount; k++)
						{
							String tempUTXOId = Utils.binaryToString(values[16 + (k*4)]);
							PublicKey tempUTXORecipient = Utils.getPublicKeyFromString(Utils.binaryToString(values[17 + (k*4)]));
							float tempUTXOValue = Float.parseFloat(values[18 + (k*4)]);
							String tempUTXOParentTransactionId = Utils.binaryToString(values[19 + (k*4)]);
							TransactionOutput tempOutput = new TransactionOutput(tempUTXORecipient, tempUTXOValue, tempUTXOParentTransactionId);
							tempOutput.id = tempUTXOId;
							UTXOs.put(tempOutput.id, tempOutput);
						}
						
						// Conversion into Transaction
						ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
						ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
						TransactionInput in = new TransactionInput(transactionOutputId);
						TransactionOutput out = new TransactionOutput(UTXORecipient, UTXOValue, parentTransactionId);
						out.id = UTXOId;
						outputs.add(out);
						in.UTXO = out;
						inputs.add(in);
						Transaction newVerifiedTransaction = new Transaction(sender, recipient, value, inputs);
						newVerifiedTransaction.transactionId = transactionId;
						newVerifiedTransaction.signature = signature;
						newVerifiedTransaction.outputs = outputs;
						verifiedTransactions.add(newVerifiedTransaction);
					}
					else
					{
						// Generic block rebuild
						int inputsCount = 0;
						int inputsTotal = 0;
						int outputsCount = 0;
						int outputsTotal = 0;
						int j = 0;
						int jCode = 0;
						int UTXOjCode = 0;
						while (j < 10)
						{
							jCode = (j*7) + (inputsTotal*5) + (outputsTotal*4);
							inputsCount = Integer.parseInt(values[5 + jCode]);
							outputsCount = Integer.parseInt(values[6 + jCode]);
							
							String transactionId = Utils.binaryToString(values[7 + jCode]);
							PublicKey sender = Utils.getPublicKeyFromString(Utils.binaryToString(values[8 + jCode]));
							PublicKey recipient = Utils.getPublicKeyFromString(Utils.binaryToString(values[9 + jCode]));
							Float value = Float.parseFloat(values[10 + jCode]);
							byte[] signature = Base64.getDecoder().decode(Utils.binaryToString(values[11 + jCode]));
							
							
							ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
							for (int w = 0; w < inputsCount; w++)
							{
								TransactionInput in = new TransactionInput(Utils.binaryToString(values[12 + jCode + (w*5)]));
								TransactionOutput out = new TransactionOutput(Utils.getPublicKeyFromString(Utils.binaryToString(values[14 + jCode + (w*5)])), Float.parseFloat(values[15 + jCode + (w*5)]), Utils.binaryToString(values[16 + jCode + (w*5)]));
								out.id = Utils.binaryToString(values[13 + jCode + (w*5)]);
								in.UTXO = out;
								inputs.add(in);
							}
							inputsTotal += inputsCount;
							
							ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
							for (int l = 0; l < outputsCount; l++)
							{
								TransactionOutput out = new TransactionOutput(Utils.getPublicKeyFromString(Utils.binaryToString(values[18 + jCode + ((inputsCount - 1)*5) + (l*4)])), Float.parseFloat(values[19 + jCode + ((inputsCount - 1)*5) + (l*4)]), Utils.binaryToString(values[20 + jCode + ((inputsCount - 1)*5) + (l*4)]));
								out.id = Utils.binaryToString(values[17 + jCode + ((inputsCount - 1)*5) + (l*4)]);
								UTXOjCode = 20 + jCode + ((inputsCount - 1)*5) + (l*4) + 1;
								outputs.add(out);	
							}
							
							// Conversion into Transaction
							Transaction newVerifiedTransaction = new Transaction(sender, recipient, value, inputs);
							newVerifiedTransaction.transactionId = transactionId;
							newVerifiedTransaction.signature = signature;
							newVerifiedTransaction.outputs = outputs;
							verifiedTransactions.add(newVerifiedTransaction);
							
							outputsTotal += outputsCount;
							
							j++;
						}
						
						int UTXOCount = Integer.parseInt(values[UTXOjCode]);
						for (int k = 0; k < UTXOCount; k++)
						{
							String tempUTXOId = Utils.binaryToString(values[UTXOjCode + 1 + (k*4)]);
							PublicKey tempUTXORecipient = Utils.getPublicKeyFromString(Utils.binaryToString(values[UTXOjCode + 2 + (k*4)]));
							float tempUTXOValue = Float.parseFloat(values[UTXOjCode + 3 + (k*4)]);
							String tempUTXOParentTransactionId = Utils.binaryToString(values[UTXOjCode + 4 + (k*4)]);
							TransactionOutput tempOutput = new TransactionOutput(tempUTXORecipient, tempUTXOValue, tempUTXOParentTransactionId);
							tempOutput.id = tempUTXOId;
							UTXOs.put(tempOutput.id, tempOutput);
						}
						
					}
					Block newVerifiedBlock = new Block(prevHash);
					newVerifiedBlock.hash = hash;
					newVerifiedBlock.merkleRoot = merkleRoot;
					newVerifiedBlock.nonce = nonce;
					newVerifiedBlock.timeStamp = timeStamp;
					newVerifiedBlock.transactions = verifiedTransactions;
					updatedLocalChain.add(newVerifiedBlock);
				}
				
			}
			
			return updatedLocalChain;
			
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	
	//----------------------------------------------Chain Validation----------------------------------------------
	// Validates current local blockchain.
	public static Boolean checkValid()
	{
		Block currentBlock;
		Block previousBlock;
		String hashTarget = new String(new char[difficulty]).replace("\0", "0");
		HashMap<String, TransactionOutput> tempUTXOs = new HashMap<String, TransactionOutput>();
		tempUTXOs.put(updatedLocalChain.get(0).transactions.get(0).outputs.get(0).id, updatedLocalChain.get(0).transactions.get(0).outputs.get(0));
		
		for (int i = 1; i < updatedLocalChain.size(); i++)
		{
			currentBlock = updatedLocalChain.get(i);
			previousBlock = updatedLocalChain.get(i - 1);
			if (!currentBlock.hash.equals(currentBlock.calculateHash()))
			{
				System.out.println("ERROR: Current hashes are not equal.");
				return false;
			}
			
			if (!previousBlock.hash.equals(currentBlock.previousHash))
			{
				System.out.println("ERROR: Previous hashes are not equal.");
				return false;
			}
			
			if (!currentBlock.hash.substring(0, difficulty).equals(hashTarget))
			{
				System.out.println("ERROR: This block has not been mined.");
				return false;
			}
			
			TransactionOutput tempOutput;
			for (int t = 0; t < currentBlock.transactions.size(); t++) 
			{
				Transaction currentTransaction = currentBlock.transactions.get(t);
				
				if (!currentTransaction.verifySignature())
				{
					System.out.println("ERROR: Signature on Transaction(" + t + ") is invalid.");  
					return false; 																	
				}
				
				if (currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) 
				{
					System.out.println("ERROR: Inputs are not equal to outputs on Transaction(" + t + ").");
					return false; 
				}
				
				for (TransactionInput input: currentTransaction.inputs)
				{
					tempOutput = tempUTXOs.get(input.transactionOutputId);
					
					if (tempOutput == null)
					{
						System.out.println("Referenced input on Transaction(" + t + ") is missing.");
						return false;
					}
					
					if (input.UTXO.value != tempOutput.value)
					{
						System.out.println("Referenced input Transaction(" + t + ") value is invalid.");
						return false;
					}
					
					tempUTXOs.remove(input.transactionOutputId);
				}
				
				for (TransactionOutput output: currentTransaction.outputs)
				{
					tempUTXOs.put(output.id, output);
				}
				
				if (!Utils.getStringFromKey(currentTransaction.outputs.get(0).recipient).equals(Utils.getStringFromKey(currentTransaction.recipient))) 
				{
					System.out.println("ERROR: Transaction(" + t + ") output recipient is not who it should be.");
					return false;
				}
				
				if (!Utils.getStringFromKey(currentTransaction.outputs.get(1).recipient).equals(Utils.getStringFromKey(currentTransaction.sender))) 
				{
					System.out.println("ERROR: Transaction(" + t + ") output 'change' is not the sender.");
					return false;
				}	
			}
		}
		System.out.println("CONSOLE: Blockchain is valid.");
		return true;
	}
	
	
	//----------------------------------------------Previous Hash----------------------------------------------
	// Gets the previous hash of the block in the verified blk.dat storage.
	public static String getPreviousHash() throws IOException
	{
		String blkFile;
		chainSize = Utils.dirSize("Blk");
		String line = null;
		String hash = null;
		blkFile = "blk" + (chainSize - 1);
		try 
		{
			BufferedReader bReader = new BufferedReader(new FileReader("C:\\Users\\Adam Loeckle\\code\\CCoin\\src\\chain\\Blk\\" + blkFile + ".dat"));
			while ((line = bReader.readLine()) != null)
			{
				String[] values = line.split(";");
				hash = Utils.binaryToString(values[1]);
			}
		} 
		catch (FileNotFoundException e) 
		{
			throw new RuntimeException(e);
		}
		return hash;
	}
}















