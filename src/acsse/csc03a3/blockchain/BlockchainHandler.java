/**
 * This Class Stores Information About BlockchainHandler. {@link BlockchainHandler#BlockchainHandler}
 * @author Mr. A.M. Diepeveen, 221025168
 * @version Computer Science 3A Mini Project
 */

package acsse.csc03a3.blockchain;

import acsse.csc03a3.Block;
import acsse.csc03a3.Blockchain;
import acsse.csc03a3.Transaction;
import acsse.csc03a3.client.CertificateData;
import acsse.csc03a3.client.SolarEnergyData;
import acsse.csc03a3.database.BlockchainDatabase;
import acsse.csc03a3.queue.CertificateDataQueue;
import acsse.csc03a3.queue.SolarEnergyDataQueue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockchainHandler 
{
 
	private Blockchain<SolarEnergyData> blockchain;
	
	//Stakeholder management
	private Map<String, Integer> stakeholders;
	
	private BlockchainDatabase blockchainDatabase;
	
	private SolarEnergyDataQueue<Transaction<SolarEnergyData>> solarEnergyQueue;
	
	private CertificateDataQueue<Transaction<CertificateData>> certificateQueue;
	
	public BlockchainHandler() 
	{
      this.blockchain = new Blockchain<>();
      this.stakeholders = new HashMap<>();
      this.blockchainDatabase = new BlockchainDatabase();
      this.solarEnergyQueue = new SolarEnergyDataQueue<>();
	}

    /*
     * Adds a transaction for solar energy data to the blockchain
     * This transaction includes the sender, receiver, and the energy data encapsulated within it
     * @param data The solar energy data encapsulating energy produced, timestamp, and identifier
     * @param sender The sender's identifier in the blockchain network
     * @param receiver The receiver's identifier in the blockchain network
     */
	public void addSolarEnergyData(SolarEnergyData data, String sender, String receiver) 
	{
		if(!hasStakeholders()) 
		{
			System.out.println("Registering a stakeholder.");
			registerStake("defaultNode", 100);
		}
  	  
		Transaction<SolarEnergyData> transaction = new Transaction<>(sender, receiver, data);
	    List<Transaction<SolarEnergyData>> transactions = new ArrayList<>();
	   
	    transactions.add(transaction);
	    blockchain.addBlock(transactions);
	    blockchainDatabase.writeToFileTransactions(transactions.toString() + "\n");
	    blockchainDatabase.overwriteFile(getBlockchainAsString());    
	}
	
	public void addToQueue(SolarEnergyData data, String sender, String receiver) 
	{
        int nextIndex = blockchainDatabase.getLastQueueIndex(BlockchainDatabase.SOLAR_ENERGY_QUEUE_FILE_PATH) + 1;
        
        Transaction<SolarEnergyData> transaction = new Transaction<>(sender, receiver, data);
        
        String indexedTransaction = nextIndex + ": " + transaction.toString();
        
        blockchainDatabase.writeToQueue(indexedTransaction + "\n");
    }

    public void addToQueueCertificates(CertificateData data, String sender, String receiver) 
    {
        int nextIndex = blockchainDatabase.getLastQueueIndex(BlockchainDatabase.CERTIFICATE_QUEUE_FILE_PATH) + 1;
        
        Transaction<CertificateData> transaction = new Transaction<>(sender, receiver, data);
        
        String indexedTransaction = nextIndex + ": " + transaction.toString();
        
        blockchainDatabase.writeToQueueCertificates(indexedTransaction + "\n");
    }
  
    /*
     * Adds a transaction for solar energy data to the blockchain
     * This transaction includes the sender, receiver, and the energy data encapsulated within it
     * @param data The solar energy data encapsulating energy produced, timestamp, and identifier
     * @param sender The sender's identifier in the blockchain network
     * @param receiver The receiver's identifier in the blockchain network
     */
	public void addSolarTransactionAndBlockToBlockchain(Transaction<SolarEnergyData> transaction) 
	{
		if(!hasStakeholders()) 
		{
          System.out.println("Registering a stakeholder.");
          registerStake("defaultNode", 100);
		}

	     List<Transaction<SolarEnergyData>> transactions = new ArrayList<>();
	     transactions.add(transaction);
	     blockchain.addBlock(transactions);
	}
	
	public void addSolarTransactionToQueue(Transaction<SolarEnergyData> transaction) 
	{
	     List<Transaction<SolarEnergyData>> transactions = new ArrayList<>();
	     transactions.add(transaction);
	     solarEnergyQueue.enqueue(transactions);
	}
	
	public void addCertificateTransactionToQueue(Transaction<CertificateData> transaction) 
	{
	     List<Transaction<CertificateData>> transactions = new ArrayList<>();
	     transactions.add(transaction);
	     certificateQueue.enqueue(transactions);
	}

    /**
     * Retrieves all block hashes from the blockchain.
     * @return a list of hashes of all blocks in the blockchain.
     */
	public List<String> getBlockHashes() 
	{
		List<String> hashes = new ArrayList<>();
		String blockchainString = this.getBlockchainAsString();
      
		String[] lines = blockchainString.split("\n");
		
		for(String line : lines) 
		{
          if(line.contains("hash=")) 
          {
              int startIndex = line.indexOf("hash='") + 6;
              int endIndex = line.indexOf("'", startIndex);
              hashes.add(line.substring(startIndex, endIndex));
          }
		}
		
		return hashes;
	}
 
	public void removeSolarEnergyData(double quantity) 
	{
		List<SolarEnergyData> allData = getAllSolarEnergyData();
		List<SolarEnergyData> updatedData = new ArrayList<>();

		for(SolarEnergyData data : allData) 
		{
			double currentProduced = data.getEnergyProduced();
			
			if(currentProduced >= quantity) 
			{
              data.setEnergyProduced(currentProduced - quantity);
              updatedData.add(data);
              break;
			}
		}

		rebuildBlockchain(updatedData);
	}

  
	private void rebuildBlockchain(List<SolarEnergyData> updatedData) 
	{
		blockchain = new Blockchain<>();
		
		for(SolarEnergyData data : updatedData) 
		{
			addSolarEnergyData(data, "System", "Blockchain");
		}
	}
  
    /**
     * Registers the stake for a node within the blockchain, necessary for the PoS consensus mechanism
     * @param nodeAddress The address or identifier of the node
     * @param stake The stake amount, which affects the node's likelihood of being chosen to forge the next block
     */
	public void registerStake(String nodeAddress, int stake) 
	{
		blockchain.registerStake(nodeAddress, stake);
	}

	public boolean hasStakeholders() 
	{
      return !this.stakeholders.isEmpty();
	}

    /*
     * Retrieves a string representation of the entire blockchain
     * @return A string representation of the blockchain
     */
	public String getBlockchainAsString() 
	{
		return blockchain.toString();
	}
  
    /*
     * Parses the blockchain string representation and reconstructs the blockchain
     * @param blockchainString The entire string representation of the blockchain
     * @return 
     */
	public String parseBlockchainFromString() 
	{
		String blockchainString = getBlockchainAsString();
		String regex = "Block\\{previousHash='([^']+)', transactions=\\[([^\\]]*)\\], hash='([^']+)', nonce='([^']*)'\\}";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(blockchainString);

		StringBuilder parsedBlockchain = new StringBuilder();
		
		while(matcher.find()) 
		{
			String previousHash = matcher.group(1);
			String transactionsString = matcher.group(2);
			String hash = matcher.group(3);
			String nonce = matcher.group(4);

			parsedBlockchain.append("Block:\n");
			parsedBlockchain.append("Previous Hash: ").append(previousHash).append("\n");
			parsedBlockchain.append("Transactions: ").append(transactionsString).append("\n");
          	parsedBlockchain.append("Hash: ").append(hash).append("\n");
          	parsedBlockchain.append("Nonce: ").append(nonce).append("\n\n");
		}

		return parsedBlockchain.toString();
	}
 
	public List<SolarEnergyData> getAllSolarEnergyData() 
	{
		List<SolarEnergyData> allSolarData = new ArrayList<>();
		String blockchainString = getBlockchainAsString();

		//Parse the blockchain string to extract SolarData
		String[] blocks = blockchainString.split("\n");
		
		for(String blockString : blocks) 
		{
			//Parse each block string
			String[] transactions = blockString.split(";");
          
			for(String transactionString : transactions) 
			{
				//Parse each transaction string
				String[] parts = transactionString.split(",");
              
				if(parts.length == 3) 
				{ 
					String dataString = parts[2];
              
					SolarEnergyData solarData = deserialiseSolarEnergyData(dataString);
                  
					if(solarData != null) 
					{
						allSolarData.add(solarData);
					}
				}
			}
		}

		return allSolarData;
	}
  
	public List<Block<SolarEnergyData>> getAllBlockSolarEnergyData() 
	{
	  List<Block<SolarEnergyData>> allBlockSolarData = new ArrayList<>();
	  
      String blockchainString = getBlockchainAsString();

      //Parse the blockchain string to extract SolarData
      String[] blocks = blockchainString.split("\n");
      
      for(String blockString : blocks) 
      {
          //Parse each block string
          String[] transactions = blockString.split(";"); 
          
          for(String transactionString : transactions) 
          {
              //Parse each transaction string
              String[] parts = transactionString.split(",");
              
              if(parts.length == 3) 
              { 
                  String dataString = parts[2];
            
                  SolarEnergyData solarData = deserialiseSolarEnergyData(dataString);
                  
                  if(solarData != null) 
                  {
                	  allBlockSolarData.addAll((Collection<? extends Block<SolarEnergyData>>) solarData);
                  }
              }
          }
      }

      return allBlockSolarData;
  }
  
  private SolarEnergyData deserialiseSolarEnergyData(String dataString) 
  {
      //Split the dataString into individual fields
      String[] fields = dataString.split(",");

      //Ensure that the dataString has the expected number of fields
      if(fields.length != 7) 
      {
          //If the format is incorrect, return null
          return null;
      }

      //Extract each field from the array
      int solarEnergyID = Integer.parseInt(fields[0]);
      String transactionType = fields[1];
      double quantitySolarEnergyTraded = Double.parseDouble(fields[2]);
      double priceKwH = Double.parseDouble(fields[3]);
      double totalPriceTraded = Double.parseDouble(fields[4]);
      String dateTime = fields[5];

      //Construct a SolarData object using the extracted fields
      SolarEnergyData solarData = new SolarEnergyData(solarEnergyID, transactionType, quantitySolarEnergyTraded, priceKwH, totalPriceTraded, dateTime);

      //Return the constructed SolarData object
      return solarData;
  }
  
  /*
   * Validates the integrity and order of the blockchain, ensuring that no data has been tampered with
   * @return true if the blockchain is valid, false otherwise
   */
  public boolean validateBlockchain() 
  {
      return blockchain.isChainValid();
  }
  
  public Set<String> getSellerIds() 
  {
      Set<String> sellerIds = new HashSet<>();
      String blockchainString = getBlockchainAsString();

      //Parse the blockchain string to extract Seller IDs from Transactions
      String[] blocks = blockchainString.split("\n");
      
      for(String blockString : blocks) 
      {
          String[] transactions = blockString.split(";");
          
          for(String transaction : transactions) 
          {
              String[] parts = transaction.split(",");
              
              if(parts.length > 1) 
              {
                  String senderPart = parts[0];
                  String[] senderInfo = senderPart.split("=");
                  
                  if(senderInfo.length > 1) 
                  {
                      String sellerId = senderInfo[1].replace("'", "").trim();
                      
                      if(!sellerId.isEmpty()) 
                      {
                          sellerIds.add(sellerId);
                      }
                  }
              }
          }
      }
      return sellerIds;
  } 
}