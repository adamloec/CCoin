package chain;
import java.security.*;
import java.util.ArrayList;

public class Transaction 
{
	public String transactionId;
	public PublicKey sender;
	public PublicKey recipient;
	public float value;
	public byte[] signature;
	public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
	public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
	public static int sequence = 0;
	
	public Transaction(PublicKey from, PublicKey to, float value, ArrayList<TransactionInput> inputs)
	{
		this.sender = from;
		this.recipient = to;
		this.value = value;
		this.inputs = inputs;
	}
	
	private String calculateHash()
	{
		sequence++;
		return Utils.applySha256(Utils.getStringFromKey(sender) + Utils.getStringFromKey(recipient) + Float.toString(value) + sequence);
	}
	
	public void generateSignature(PrivateKey privateKey)
	{
		String data = Utils.getStringFromKey(sender) + Utils.getStringFromKey(recipient) + Float.toString(value);
		signature = Utils.applyECDSA(privateKey, data);
	}
	
	public boolean verifySignature()
	{
		String data = Utils.getStringFromKey(sender) + Utils.getStringFromKey(recipient) + Float.toString(value);
		return Utils.verifyECDSASig(sender, data, signature);
	}
	
	public boolean processTransaction() 
	{
		
		if (verifySignature() == false) 
		{
			System.out.println("ERROR: Transaction signature failed to verify.");
			return false;
		}
		
		for (TransactionInput i : inputs) 
		{
			i.UTXO = Chain.UTXOs.get(i.transactionOutputId);
		}
		
		if (getInputsValue() < Chain.minimumTransaction) 
		{
			System.out.println("ERROR: Transaction input is too small: " + getInputsValue());
			return false;
		}
		
		float leftOver = getInputsValue() - value;
		transactionId = calculateHash();
		
		outputs.add(new TransactionOutput(this.recipient, value, transactionId));
		outputs.add(new TransactionOutput(this.sender, leftOver, transactionId));
				
		for (TransactionOutput o : outputs)
		{
			Chain.UTXOs.put(o.id, o);
		}
		
		for (TransactionInput i : inputs)
		{
			if (i.UTXO == null)
			{
				continue;
			}
			else
			{
				Chain.UTXOs.remove(i.UTXO.id);
			}
		}
		return true;
	}
	

	public float getInputsValue() 
	{
		float total = 0;
		for (TransactionInput i : inputs) 
		{
			if (i.UTXO == null) 
			{
				continue; 
			}
			else
			{
				total += i.UTXO.value;
			}
		}
		return total;
	}

	public float getOutputsValue() 
	{
		float total = 0;
		for (TransactionOutput o : outputs) 
		{
			total += o.value;
		}
		return total;
	}
}

class TransactionInput
{
	public String transactionOutputId;
	public TransactionOutput UTXO;
	
	public TransactionInput(String transactionOutputId)
	{
		this.transactionOutputId = transactionOutputId;
	}
}

class TransactionOutput
{
	public String id;
	public PublicKey recipient;
	public float value;
	public String parentTransactionId;
	
	public TransactionOutput(PublicKey recipient, float value, String parentTransactionId)
	{
		this.recipient = recipient;
		this.value = value;
		this.parentTransactionId = parentTransactionId;
		this.id = Utils.applySha256(Utils.getStringFromKey(recipient) + Float.toString(value) + parentTransactionId);
	}
	
	public boolean isMine(PublicKey publicKey)
	{
		return (Utils.getStringFromKey(publicKey).equals(Utils.getStringFromKey(recipient)));
	}
}





