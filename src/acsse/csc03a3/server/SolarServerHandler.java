/**
 * This Class Stores Information About SolarServerHandler. {@link SolarServerHandler#SolarServerHandler}
 * @author Mr. A.M. Diepeveen, 221025168
 * @version Computer Science 3A Mini Project
 */

package acsse.csc03a3.server;

import acsse.csc03a3.client.CertificateData;
import acsse.csc03a3.client.SolarEnergyData;
import acsse.csc03a3.queue.LinkedListBasedQueue;
import java.net.Socket;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

//Implement runnable interface for multi-threaded server
public class SolarServerHandler implements Runnable
{
	//Private member variables
	private Socket connectionToClient;
	
	//Byte streams
	private OutputStream outputStream = null;
	private InputStream inputStream = null;

	//Text streams
	private PrintWriter printWriter = null;
	private BufferedReader bufferedReader = null;
	
	//Binary streams
	private DataOutputStream dataOutputStream = null;
	private DataInputStream dataInputStream = null;
    
    private SolarEnergyData currentTransaction = null;
    private CertificateData currentTransactionCertificate = null;
	private ArrayList<String> fileList = new ArrayList<>();
	
	//Only process requests if the User is logged in
	private boolean processing;

	private boolean transactionInProgress = false;
   
	private static LinkedListBasedQueue<SolarEnergyData> solarEnergyQueue;
	private static LinkedListBasedQueue<CertificateData> certificateQueue;
	
    public SolarServerHandler(Socket newConnectionToClient)
    {	
    	
    	solarEnergyQueue = new LinkedListBasedQueue<>();
    	certificateQueue = new LinkedListBasedQueue<>();
    	
    	//Set up socket connection
    	this.connectionToClient = newConnectionToClient;
    	
    	//Set up steams
    	try 
    	{   		
    		outputStream = connectionToClient.getOutputStream();
    		inputStream = connectionToClient.getInputStream();
    		
    		printWriter = new PrintWriter(outputStream);
    		bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
    		
    		dataOutputStream = new DataOutputStream(outputStream);
    		dataInputStream = new DataInputStream(inputStream);
		} 
    	catch(IOException e) 
    	{
			e.printStackTrace();
    	}
    }
    
    //Helper method to send messages to client
    private void sendMessage(String message)
    {
    	printWriter.println(message);
    	printWriter.flush();
    }
    
	// Helper method to initialize or update fileList (mock example)
	private void initialiseOrUpdateFileList() 
	{
	     File folder = new File("data/sellerFiles");
	     File[] listOfFiles = folder.listFiles();
	     
	     if(listOfFiles != null) 
	     {
	         fileList.clear();
	         for(File file : listOfFiles) 
	         {
	             if(file.isFile() && file.getName().endsWith(".pdf")) 
	             {
	                 fileList.add(file.getName());
	             }
	         }
	     }
	 }
    
    public void run()
    {
    	System.out.println("Start Processing Commands");
    	
    	processing =true;
    	
    	try 
    	{
    		while(processing) 
    		{             
                String requestLine = null;
                try
                {
                	//Must 1st. Receive Command From Client Because Client Initiates Conversation:
    	    		requestLine = bufferedReader.readLine();
    	    		System.out.println("Received requestLine: " + requestLine);
                }
                catch(IOException ex)
                {
                	System.out.println("Application Exited");
                }
    			
	    		
	    		if(requestLine == null) 
	    		{
	                System.out.println("Client closed connection or sent null request.");
	                break;  // Exit the processing loop if the client connection is closed
	            }
	    		
	    		String command = "";
	    		StringTokenizer stringTokenizer = new StringTokenizer(requestLine);
	    		try
	    		{
	    			command = stringTokenizer.nextToken();
	    		}
	    		catch(Exception ex)
	    		{
	    			System.out.println("Retry Sending Requests");
	    		}
	    		
	    		if (command.equals("SELL_CERTIFICATE")) 
				{		
					handleUploadCommand(stringTokenizer);                
				}
	    		
	    		else if(command.equals("BUY_CERTIFICATE"))
				{
					handlePurchaseCommand(stringTokenizer);
				}

				else if(command.equals("GET_SOLAR_TRANSACTIONS")) 
				{
					sendSolarTransactions();	
				}
				
				else if(command.equals("GET_SOLAR_BLOCKCHAIN")) 
				{
					sendSolarBlockchain();		
				}
				
				else if(command.equals("GET_CERTIFICATE_TRANSACTIONS")) 
				{
					sendCertificateTransactions();	
				}
				
				else if(command.equals("GET_CERTIFICATE_BLOCKCHAIN")) 
				{
					sendCertificateBlockchain();		
				}
		
				else if(command.equals("GET_QUEUE_SOLAR_ENERGY"))
				{	
					sendQueueState();
	            } 
			
				else if(command.equals("GET_QUEUE_CERTIFICATE"))
				{
					sendQueueStateCertificate();
	            } 

				else
				{
					sendMessage("500 Invalid Command");
				}
			}
		} 	
	    finally 
	    {
	       closeResources();
	        
	       if(transactionInProgress) 
	       {
	           removeTransactionFromQueue();
	           removeTransactionFromQueueCertificate();
	       }
	   }
    }
    
    private void closeResources() 
    {
        try 
        {
        	if(outputStream != null) outputStream.close();
            if(inputStream != null) inputStream.close();
            if(printWriter != null) printWriter.close();
            if(bufferedReader != null) bufferedReader.close();
            if(dataOutputStream != null) dataOutputStream.close();
            if(dataInputStream != null) dataInputStream.close();
            if(connectionToClient != null) connectionToClient.close();
            
            processing = false;
            System.out.println("Connections closed successfully.");
        } 
        catch(IOException ex) 
        {
            System.out.println("Error closing resources: " + ex.getMessage());
        }
    }
    
    private void removeTransactionFromQueue() 
    {
        synchronized (solarEnergyQueue) 
        {
            Iterator<SolarEnergyData> iterator = solarEnergyQueue.iterator();
            
            while(iterator.hasNext()) 
            {
                SolarEnergyData transaction = iterator.next();
                
                if(transaction.equals(currentTransaction)) 
                {
                    iterator.remove();
                    System.out.println("Transaction removed from queue: " + transaction);
                    break;
                }
            }
        }
    }
    
    private void removeTransactionFromQueueCertificate() 
    {
        synchronized (certificateQueue) {
            Iterator<CertificateData> iterator = certificateQueue.iterator();
            while (iterator.hasNext()) {
                CertificateData transaction = iterator.next();
                if (transaction.equals(currentTransactionCertificate)) {
                    iterator.remove();
                    System.out.println("Transaction removed from queue: " + transaction);
                    break;
                }
            }
        }
    }
    
    private void sendQueueState()  
    {
    	String QUEUE_FILE_PATH = "src/acsse/csc03a3/database/queue_solar_energy.txt";
    	
    	sendMessage(QUEUE_FILE_PATH);	
    }
    
    void sendQueueStateCertificate()  
    {
    	String QUEUE_FILE_PATH = "src/acsse/csc03a3/database/queue_certificates.txt";
    	
    	sendMessage(QUEUE_FILE_PATH);	
    }
    
    public void sendSolarTransactions()
    {
    	String TRANSACTION_FILE_PATH = "src/acsse/csc03a3/database/transaction_database.txt";
    	
    	sendMessage(TRANSACTION_FILE_PATH);
    }
    
    public void sendSolarBlockchain()
    {
    	String BLOCKCHAIN_FILE_PATH = "src/acsse/csc03a3/database/blockchain_database.txt";
    	
    	sendMessage(BLOCKCHAIN_FILE_PATH);
    }
    
    public void sendCertificateTransactions()
    {
    	String CERTIFICATE_TRANSACTION_FILE_PATH = "src/acsse/csc03a3/database/certificates_transaction_database.txt";
    	
    	sendMessage(CERTIFICATE_TRANSACTION_FILE_PATH);
    }
    
    public void sendCertificateBlockchain()
    {
    	String CERTIFICATE_BLOCKCHAIN_FILE_PATH = "src/acsse/csc03a3/database/certificates_blockchain_database.txt";
    	
    	sendMessage(CERTIFICATE_BLOCKCHAIN_FILE_PATH);
    }
    
    private void handleUploadCommand(StringTokenizer stringTokenizer) 
    {
        try 
        {
            String fileName = stringTokenizer.nextToken();
            
            long fileSize = Long.parseLong(stringTokenizer.nextToken());

            if(!fileName.endsWith(".pdf")) 
            {
                sendMessage("400 Bad Request - Only PDF files are accepted");
                return;
            }

            File directory = new File("src/acsse/csc03a3/database/storedCertificates");
            
            if(!directory.exists()) 
            {
                directory.mkdirs();
            }

            String newFileName = "uploaded_" + fileName;
            File fileToSave = new File(directory, newFileName);

            try(FileOutputStream fos = new FileOutputStream(fileToSave);
                 BufferedOutputStream bos = new BufferedOutputStream(fos)) 
            {
                byte[] buffer = new byte[4096];
                int bytesRead;
                long remaining = fileSize;

                while(remaining > 0 && (bytesRead = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) 
                {
                    bos.write(buffer, 0, bytesRead);
                    remaining -= bytesRead;
                }

                if(remaining == 0) 
                {
                    sendMessage("200 OK - File uploaded successfully");
                } 
                else 
                {
                    fileToSave.delete();
                    sendMessage("400 Bad Request - Incomplete file received");
                }
            } 
            catch(IOException ex) 
            {
                sendMessage("500 Internal Server Error - Failed to save file");
                fileToSave.delete();
            }
        } 
        catch(NumberFormatException ex) 
        {
            sendMessage("400 Bad Request - File size must be a valid number");
        }
    }
    
    private void handlePurchaseCommand(StringTokenizer stringTokenizer)
    {
    	 try 
		 {
			 int fileId = Integer.parseInt(stringTokenizer.nextToken());
	         initialiseOrUpdateFileList(); // Ensure the fileList is updated
	            
	         if(fileId < 0 || fileId >= fileList.size()) 
	         {
	        	 sendMessage("404 File Not Found");
	        	 return;
	         }
	         String fileName = fileList.get(fileId); // Retrieve the file name by ID from fileList

	         File pdfFile = new File("data/sellerFiles/" + fileName);
	         if(pdfFile.exists()) 
	         {
	        	 long fileSize = pdfFile.length();
	        	 sendMessage("200 " + fileSize + " bytes");

	        	 try(FileInputStream fileInputStream = new FileInputStream(pdfFile)) 
	        	 {
	        		 byte[] buffer = new byte[1024];
	        		 int bytesRead;
                    
	        		 while((bytesRead = fileInputStream.read(buffer)) != -1) 
	        		 {
	        			 dataOutputStream.write(buffer, 0, bytesRead);
	        			 dataOutputStream.flush();
	        		 }
	        	 }
                
	        	 System.out.println("File sent to client: " + fileName);
	         } 
	         else 
	         {
	        	 sendMessage("404 File Not Found");
	         }
    	 } 
		 catch(NumberFormatException e) 
		 {
			 sendMessage("400 Bad Request - Invalid file ID format");
		 } 
		 catch(IOException e) 
		 {
			 sendMessage("500 Internal Server Error - File transmission error");
			 e.printStackTrace();
		 }
    }
}