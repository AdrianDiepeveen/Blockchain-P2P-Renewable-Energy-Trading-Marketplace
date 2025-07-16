/**
 * This Class Stores Information About BlockchainHandlerCertificates. {@link BlockchainHandlerCertificates#BlockchainHandlerCertificates}
 * @author Mr. A.M. Diepeveen, 221025168
 * @version Computer Science 3A Mini Project
 */

package acsse.csc03a3.blockchain;
import acsse.csc03a3.Block;
import acsse.csc03a3.Blockchain;
import acsse.csc03a3.Transaction;
import acsse.csc03a3.client.CertificateData;
import acsse.csc03a3.database.BlockchainDatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockchainHandlerCertificates 
{
	
	private Blockchain<CertificateData> certificateBlockchain;
	
	//Stakeholder management
	private Map<String, Integer> stakeholders; 
	
	private BlockchainDatabase blockchainDatabase;

	public BlockchainHandlerCertificates() 
	{
      this.certificateBlockchain = new Blockchain<>();
      this.stakeholders = new HashMap<>();
      this.blockchainDatabase = new BlockchainDatabase();
	}
 
  
	public void addToQueueCertificates(CertificateData data, String sender, String receiver) 
	{
		Transaction<CertificateData> transaction = new Transaction<>(sender, receiver, data);
	    List<Transaction<CertificateData>> transactions = new ArrayList<>();
	   
	    transactions.add(transaction);
	    blockchainDatabase.writeToQueueCertificates(transactions.toString() + "\n");
	}
  
	/*
	 * Adds a transaction for solar energy data to the blockchain
	 * This transaction includes the sender, receiver, and the energy data encapsulated within it
	 * @param data The solar energy data encapsulating energy produced, timestamp, and identifier
	 * @param sender The sender's identifier in the blockchain network
	 * @param receiver The receiver's identifier in the blockchain network
	 */
	public void addCertificateData(CertificateData data, String sender, String receiver) 
	{
  	
		if(!hasStakeholders()) 
		{
			System.out.println("Registering a stakeholder.");
			registerStake("defaultNode", 100);
		}
  	
	    Transaction<CertificateData> transaction = new Transaction<>(sender, receiver, data);
	    List<Transaction<CertificateData>> transactions = new ArrayList<>();
	    transactions.add(transaction);
	    certificateBlockchain.addBlock(transactions);
	    
	    blockchainDatabase.writeToFileCertificates(transactions.toString() + "\n");
	    blockchainDatabase.overwriteFileCertificates(getBlockchainAsString());
	}
  
    /*
     * Adds a transaction for solar energy data to the blockchain
     * This transaction includes the sender, receiver, and the energy data encapsulated within it
     * @param data The solar energy data encapsulating energy produced, timestamp, and identifier
     * @param sender The sender's identifier in the blockchain network
     * @param receiver The receiver's identifier in the blockchain network
     */
	public void addCertificateTransactionAndBlockToBlockchain(Transaction<CertificateData> transaction) 
	{
  	
		if(!hasStakeholders()) 
		{
          System.out.println("Registering a stakeholder.");
          registerStake("defaultNode", 100);
		}
  	
		List<Transaction<CertificateData>> transactions = new ArrayList<>();
		transactions.add(transaction);
	}

	/*
	 * Retrieves all block hashes from the blockchain
	 * @return a list of hashes of all blocks in the blockchain
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
      List<CertificateData> allData = getAllCertificateData();
      List<CertificateData> updatedData = new ArrayList<>();

      for(CertificateData data : allData) 
      {
          double currentProduced = data.getPriceMwH();
          
          if(currentProduced >= quantity) 
          {
              data.setPriceMwH(currentProduced - quantity);
              updatedData.add(data);
              break;
          }
      }
      rebuildBlockchain(updatedData);
  }

  
  private void rebuildBlockchain(List<CertificateData> updatedData)
  {
      certificateBlockchain = new Blockchain<>();
      
      for(CertificateData data : updatedData) 
      {
          addCertificateData(data, "System", "Blockchain");
      }
  }
  
  /*
   * Registers the stake for a node within the blockchain, necessary for the PoS consensus mechanism
   * @param nodeAddress The address or identifier of the node
   * @param stake The stake amount
   */
  public void registerStake(String nodeAddress, int stake) 
  {
	  certificateBlockchain.registerStake(nodeAddress, stake);
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
      return certificateBlockchain.toString();
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
  

  public List<CertificateData> getAllCertificateData() 
  {
      List<CertificateData> allSolarData = new ArrayList<>();
      String blockchainString = getBlockchainAsString();

      //Parse the blockchain string to extract SolarData
      String[] blocks = blockchainString.split("\n");
      
      for (String blockString : blocks) 
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
    
                  CertificateData certificateData = deserialiseSolarEnergyData(dataString);
                  
                  if(certificateData != null) 
                  {
                      allSolarData.add(certificateData);
                  }
              }
          }
      }

      return allSolarData;
  }
  
  public List<Block<CertificateData>> getAllBlockCertificateData() 
  {
	  List<Block<CertificateData>> allBlockCertificateData = new ArrayList<>();
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
  
                  CertificateData certificateData = deserialiseSolarEnergyData(dataString);
                  
                  if(certificateData != null) 
                  {
                	  allBlockCertificateData.addAll((Collection<? extends Block<CertificateData>>) certificateData);
                  }
              }
          }
      }

      return allBlockCertificateData;
  }
  
  private CertificateData deserialiseSolarEnergyData(String dataString) 
  {
      //Split the dataString into individual fields
      String[] fields = dataString.split(",");

      //Ensure that the dataString has the expected number of fields
      if(fields.length != 8) 
      {
          //If the format is incorrect return null 
          return null;
      }

      //Extract each field from the array
      int certitifcateID = Integer.parseInt(fields[0]);
      String transactionType = fields[1];
      String renewableEnergyType = fields[2];
      double priceMwH = Double.parseDouble(fields[3]);
      double carbonEmissionsOffset = Double.parseDouble(fields[4]);
      double totalPriceTraded = Double.parseDouble(fields[5]);
      String certificateFile = fields[6];
      String dateTime = fields[7];

      //Construct a SolarData object using the extracted fields
      CertificateData certificateData = new CertificateData(certitifcateID, transactionType, renewableEnergyType, priceMwH, carbonEmissionsOffset, totalPriceTraded, certificateFile, dateTime);

      //Return the constructed SolarData object
      return certificateData;
  }
}