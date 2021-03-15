package chain;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.*;

public class Wallet
{
	public PrivateKey privateKey;
	public PublicKey publicKey;
	public HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();
	
	public Wallet()
	{
		generateKeyPair();
	}
	
	public void generateKeyPair()
	{
		try
		{
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
			keyGen.initialize(ecSpec, new SecureRandom());
			
			KeyPair keyPair = keyGen.generateKeyPair();
			privateKey = keyPair.getPrivate();
			publicKey = keyPair.getPublic();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public float getBalance()
	{
		float total = 0;
        for (Map.Entry<String, TransactionOutput> item: Chain.UTXOs.entrySet())
        {
        	TransactionOutput UTXO = item.getValue();
            if (UTXO.isMine(publicKey))
            {
            	UTXOs.put(UTXO.id, UTXO);
            	total += UTXO.value;
            }
        }
		return total;
	}

	public Transaction sendFunds(PublicKey _recipient, float value) 
	{
		if (getBalance() < value)
		{ 
			System.out.println("ERROR: Not Enough funds to send transaction.");
			return null;
		}
    
		ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
    
		float total = 0;
		for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet())
		{
			TransactionOutput UTXO = item.getValue();
			total += UTXO.value;
			inputs.add(new TransactionInput(UTXO.id));
			if (total > value)
			{
				break;
			}
		}
		
		Transaction newTransaction = new Transaction(publicKey, _recipient, value, inputs);
		newTransaction.generateSignature(privateKey);
		
		for (TransactionInput input: inputs)
		{
			UTXOs.remove(input.transactionOutputId);
		}
		return newTransaction;
	}
}
