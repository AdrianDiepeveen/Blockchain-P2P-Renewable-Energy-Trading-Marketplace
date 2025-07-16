/**
 * This Class Stores Information About BlockchainDatabase. {@link BlockchainDatabase#BlockchainDatabase}
 * @author Mr. A.M. Diepeveen, 221025168
 * @version Computer Science 3A Mini Project
 */

package acsse.csc03a3.database;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class BlockchainDatabase 
{
    private static final String TRANSACTIONS_FILE_PATH = "src/acsse/csc03a3/database/transaction_database.txt";
    private static final String BLOCKCHAIN_FILE_PATH = "src/acsse/csc03a3/database/blockchain_database.txt";
    
    private static final String CERTIFICATES_TRANSACTIONS_FILE_PATH = "src/acsse/csc03a3/database/certificates_transaction_database.txt";
    private static final String CERTIFICATES_BLOCKCHAIN_FILE_PATH = "src/acsse/csc03a3/database/certificates_blockchain_database.txt";
    public static final String SOLAR_ENERGY_QUEUE_FILE_PATH = "src/acsse/csc03a3/database/queue_solar_energy.txt";
    public static final String CERTIFICATE_QUEUE_FILE_PATH = "src/acsse/csc03a3/database/queue_certificates.txt";
    
    private static final String REGISTERED_TRADERS_FILE_PATH = "src/acsse/csc03a3/database/registered_solar_traders.txt";

    private int solarEnergyQueueCounter = 1; 
    private int certificateQueueCounter = 1;
    
    public void writeToFileTransactions(String content) 
    {
        try 
        {
            Files.write(Paths.get(TRANSACTIONS_FILE_PATH), content.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } 
        catch(IOException e) 
        {
            e.printStackTrace();
        }
    }
    
    public synchronized void writeToQueue(String content) 
    {
        try
        {
            content = solarEnergyQueueCounter + ": " + content;
            solarEnergyQueueCounter++;
            Files.write(Paths.get(SOLAR_ENERGY_QUEUE_FILE_PATH), content.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } 
        catch(IOException e) 
        {
            e.printStackTrace();
        }
    }
    
    public synchronized void writeToQueueCertificates(String content) 
    {
        try 
        {
            content = certificateQueueCounter + ": " + content;
            certificateQueueCounter++;
            Files.write(Paths.get(CERTIFICATE_QUEUE_FILE_PATH), content.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } 
        catch(IOException e) 
        {
            e.printStackTrace();
        }
    }
    
    public int getLastQueueIndex(String filePath) 
    {
        try(BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) 
        {
            String lastLine = null;
            String currentLine;
            
            while((currentLine = reader.readLine()) != null) 
            {
                lastLine = currentLine;
            }
            if(lastLine != null && !lastLine.isEmpty()) 
            {
                //Extract the index from the last line
                String[] parts = lastLine.split(":", 2);
                
                if(parts.length > 0 && parts[0].trim().matches("\\d+")) 
                {
                    return Integer.parseInt(parts[0].trim());
                }
            }
        } 
        catch(IOException e) 
        {
            e.printStackTrace();
        }
        
        //Return 0 if the file is empty
        return 0; 
    }

    /*
     * Writes content to the file, overwriting any existing content
     * @param content The string content to write to the file
     */
    public void overwriteFile(String content) 
    {
        try 
        {
            Files.write(Paths.get(BLOCKCHAIN_FILE_PATH), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } 
        catch(IOException e) 
        {
            e.printStackTrace();
        }
    }
    
    public void writeToFileCertificates(String content) 
    {
        try 
        {
            Files.write(Paths.get(CERTIFICATES_TRANSACTIONS_FILE_PATH), content.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } 
        catch(IOException e) 
        {
            e.printStackTrace();
        }
    }
    
    /*
     * Writes content to the file, overwriting any existing content
     * @param content The string content to write to the file
     */
    public void overwriteFileCertificates(String content) 
    {
        try 
        {
            Files.write(Paths.get(CERTIFICATES_BLOCKCHAIN_FILE_PATH), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } 
        catch(IOException e) 
        {
            e.printStackTrace();
        }
    }

    public List<String> readFromFile() 
    {
        try 
        {
            return Files.readAllLines(Paths.get(TRANSACTIONS_FILE_PATH));
        } 
        catch(IOException e) 
        {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /*
     * Checks if the specified companyName exists in the companies file
     * @param companyName The name of the company to check
     * @return true if the companyName exists, false otherwise
     */
    public boolean companyNameExists(String companyName)
    {
        try
        {
            List<String> lines = Files.readAllLines(Paths.get(REGISTERED_TRADERS_FILE_PATH));
            return lines.stream().anyMatch(line -> line.trim().equalsIgnoreCase(companyName));
        } 
        catch(IOException e) 
        {
            System.err.println("Error reading from the file: " + e.getMessage());
            return false;
        }
    }
    
    /*
     * Appends the given company name to the registered traders file
     * @param companyName The company name to register
     * @throws IOException If an I/O error occurs writing to the file
     */
    public void registerCompany(String companyName) throws IOException 
    {
        String content = companyName + System.lineSeparator();
        Files.write(Paths.get(REGISTERED_TRADERS_FILE_PATH), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}