/**
 * This Class Stores Information About SolarServer. {@link SolarServer#SolarServer}
 * @author Mr. A.M. Diepeveen, 221025168
 * @version Computer Science 3A Mini Project
 */

package acsse.csc03a3.server;

import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class SolarServer implements Runnable 
{
    private ServerSocket serverSocket;
    boolean isReady;
    private boolean isRunning;
    public static int serverPort;

    public SolarServer(int port) 
	{
		try 
		{
			serverSocket = new ServerSocket(port);
			System.out.println("Server created on port: " + port);
			
			isReady = true;
			
			//While true loop
			while(isReady)
			{
				System.out.println("Ready to accept clients...");
				
				Socket clientConnection = serverSocket.accept();
				
				SolarServerHandler serverHandler = new SolarServerHandler(clientConnection);
				
				Thread thread = new Thread(serverHandler);

				thread.start();
			}
		} 
		catch(BindException e) 
		{
			System.out.println("Please clear the console because server is already running");
		}
		catch(IOException e) 
		{
			e.printStackTrace();
		}
		
	}

    @Override
    public void run() 
    {
        isRunning = true;
        
        System.out.println("Server started successfully on port: " + serverPort);
        
        try 
        {
            while(isRunning) 
            {
                System.out.println("Waiting to accept clients...");
                
                Socket clientSocket = serverSocket.accept();
                
                if(clientSocket != null) 
                {
	                SolarServerHandler solarServerHandler = new SolarServerHandler(clientSocket);
                    Thread thread = new Thread(solarServerHandler);
                    thread.start();
                }
            }
        } 
        catch(IOException e) 
        {
            if(!isRunning) 
            {
                System.out.println("Server stopped");
            } 
            else 
            {
                System.out.println("Error accepting client connection");
                e.printStackTrace();
            }
        }
    }

    public void stopServer() 
    {
        isRunning = false;
        
        try 
        {
            if(serverSocket != null) 
            {
                serverSocket.close();
            }
        } 
        catch(IOException e) 
        {
            System.out.println("Error closing server");
            e.printStackTrace();
        }
    }
    
    public static void main(String[] argv)
    {
    	SolarServer solarServer = new SolarServer(8888);    	
    	
    	//Add shutdown hook if user abruptly closes application
        Runtime.getRuntime().addShutdownHook(new Thread(() -> 
        {
            clearFileContents("src/acsse/csc03a3/database/queue_solar_energy.txt");
            clearFileContents("src/acsse/csc03a3/database/queue_certificates.txt");
        }));

    }
    
    public static void clearFileContents(String filePath) 
    {
        try (PrintWriter writer = new PrintWriter(new File(filePath))) 
        {
            writer.print("");
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }
}
