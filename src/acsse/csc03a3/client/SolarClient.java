/**
 * This Class Stores Information About SolarClient. {@link SolarClient#SolarClient}
 * @author Mr. A.M. Diepeveen, 221025168
 * @version Computer Science 3A Mini Project
 */

package acsse.csc03a3.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SolarClient extends Application
{
	
	//Client socket connection
	private Socket clientSocket = null;
	
	//Byte streams
	private OutputStream outputStream = null;
	private InputStream inputStream = null;
	
	//Text streams
	private PrintWriter printWriter = null;
	private BufferedReader bufferedReader = null;
	
	//Binary streams
	private DataOutputStream dataOutputStream = null;
	private DataInputStream dataInputStream = null;
	
	//Object streams
	private ObjectOutputStream objectOutputStream = null;
	private ObjectInputStream objectInputStream = null;
		
    public static void main(String[] args)
    {
    	//Launch the JavaFX application
    	launch(args);
    }

	@Override
	public void start(Stage primaryStage) throws Exception 
	{
		
		primaryStage.setOnCloseRequest(event -> 
		{
	        System.out.println("Closing window and cleaning up resources...");
	        closeStreams();
	    });
		
		Runtime.getRuntime().addShutdownHook(new Thread(() -> 
		{
	        System.out.println("JVM shutdown detected, cleaning up resources...");
	        closeStreams();
	    }));
		
		//Set the logging level for PDFBox
        Logger.getLogger("org.apache.pdfbox").setLevel(Level.SEVERE);
		
		//Create the pane, scene and stage
		SolarClientHandler root  = new SolarClientHandler();
		
		Scene scene = new Scene(root, 1030, 580);
		
		//Utilise CSS style sheet for graphical user interface
		scene.getStylesheets().add(getClass().getResource("/acsse/csc03a3/style/style.css").toExternalForm());
		
		primaryStage.setScene(scene);
		primaryStage.setTitle("SolarChain");
		primaryStage.show();
	}
	
	public void closeStreams() 
	{
	    try 
	    {
	       
			if(clientSocket != null && !clientSocket.isClosed()) 
			{
	            clientSocket.close();
	            System.out.println("Socket closed");
	        }
			if(outputStream != null) 
	        {
				outputStream.close();
	            System.out.println("OutputStream closed");
	        }
	        if(inputStream != null) 
	        {
	        	inputStream.close();
	            System.out.println("InputStream closed");
	        }
	        if(printWriter != null) 
	        {
	            printWriter.close();
	            System.out.println("PrintWriter closed");
	        }
	        if(bufferedReader != null) 
	        {
	            bufferedReader.close();
	            System.out.println("BufferedReader closed");
	        }
	        if(dataOutputStream != null) 
	        {
	        	dataOutputStream.close();
	            System.out.println("DataOutputStream closed");
	        }
	        if(dataInputStream != null) 
	        {
	            bufferedReader.close();
	            System.out.println("DataInputStream closed");
	        }
	        if(objectOutputStream != null) 
	        {
	        	objectOutputStream.close();
	            System.out.println("ObjectOutputStream closed");
	        }
	        if(objectInputStream != null) 
	        {
	        	objectInputStream.close();
	            System.out.println("ObjectInputStream closed");
	        }
	        
	    } 
	    catch 
	    (IOException e) 
	    {
	        System.err.println("Error while closing resources: " + e.getMessage());
	    }
	}
}