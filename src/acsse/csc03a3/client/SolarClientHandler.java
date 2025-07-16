/**
 * This Class Stores Information About SolarClientHandler. {@link SolarClientHandler#SolarClientHandler}
 * @author Mr. A.M. Diepeveen, 221025168
 * @version Computer Science 3A Mini Project
 */

package acsse.csc03a3.client;
import acsse.csc03a3.api.SolarEnergyDataAPI;
import acsse.csc03a3.Block;
import acsse.csc03a3.Transaction;
import acsse.csc03a3.database.UserDatabase;
import acsse.csc03a3.queue.CertificateDataQueue;
import acsse.csc03a3.queue.SolarEnergyDataQueue;
import acsse.csc03a3.database.BlockchainDatabase;
import acsse.csc03a3.blockchain.BlockchainHandler;
import acsse.csc03a3.blockchain.BlockchainHandlerCertificates;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.view.View;

public class SolarClientHandler extends BorderPane
{
	//Client socket connection
	private Socket clientSocket = null;
	
	//Byte streams
	private OutputStream outputStream = null;
	private InputStream inputStream = null;
 
    Transaction<SolarEnergyData> currentTransactionProcessing;
    Transaction<CertificateData> currentTransactionProcessingCertificate;
	
	//Text streams
	private PrintWriter printWriter = null;
	private BufferedReader bufferedReader = null;
	
	//Binary streams
	private DataOutputStream dataOutputStream = null;
	private DataInputStream dataInputStream = null;
	
	private double pricePerKwh;
	private double quantitySell;
	private double sellingPrice;
	private int solarEnergyID;
	private double quantityBuy;
	private double purchasePrice;
	private String dateTime;
	private SolarEnergyData solarEnergySold;
	private SolarEnergyData solarEnergyPurchased;
    
	private int certificateID;
	private double pricePerMwh;
	private double carbonEmissionsOffset;
    private CertificateData certificateSold;
    private CertificateData certificatePurchased;

    private TextField quantityField = new TextField();

	ObservableList<String> fileList; 
	private MenuBar menuBar;
	private BorderPane root;
	
	private static BlockchainHandler blockchainHandler;
	private static BlockchainHandlerCertificates blockchainHandlerCertificate;
	private static BlockchainDatabase blockchainDatabase;
	private double totalSolarEnergyGenerated = 0;
	private double totalCarbonEmissionsOffset= 0;
	
	//Global variable to store company name
	private String companyName;  

	private String FILE_REC_FROM_SERVER;
	
	private SolarEnergyDataQueue<Transaction<SolarEnergyData>> solarEnergyDataQueue;
	private CertificateDataQueue<Transaction<CertificateData>> certificateDataQueue;
	
	//Client socket connection
	public SolarClientHandler() 
	{
		this.menuBar = createMenuBar();
            
		blockchainHandler = new BlockchainHandler();
		blockchainHandlerCertificate = new BlockchainHandlerCertificates();
		blockchainDatabase = new BlockchainDatabase();
		this.root = new BorderPane();
		
		//Set the menu bar at the top of the BorderPane
		this.setTop(menuBar);  
		this.setCenter(root);
		setupInitialPage();
		
		this.solarEnergyDataQueue = new SolarEnergyDataQueue<>();	
		this.certificateDataQueue = new CertificateDataQueue<>();	
    }
	
	//Establish connection and set up streams
    private void connect(String host, int port)
    {
    	try 
    	{
    		//Set up socket connection
			clientSocket = new Socket(host, port);

			outputStream = clientSocket.getOutputStream();
			inputStream = clientSocket.getInputStream();
			
			bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			printWriter = new PrintWriter(outputStream);
			
			dataInputStream = new DataInputStream(inputStream);
			dataOutputStream = new DataOutputStream(outputStream);
	
			System.out.println("Client Connected To Server And Streams Created Successfully On Port: " + 8888);
    	} 
    	catch(IOException e) 
    	{	
    		e.printStackTrace();
    		Platform.runLater(() -> 
    		{
    			Alert alert = null;
    			alert.setAlertType(Alert.AlertType.ERROR);
    			alert.setContentText("Failed to connect to the server.");
    			alert.show();
    		});
		}	 	
    }
    
    /*
	 * Method to send commands to server
	 */
	private void sendCommand(String command) 
	{	
		printWriter.println(command);
		printWriter.flush();
		
	}
    
	/*
	 * Method to read responses from server
	 */
	private String readResponse()
	{
		String response = "";
		
		try 
		{
			response = bufferedReader.readLine();
			
			System.out.println("Response From Server: " + response);
		} 
		catch(IOException e) 
		{
			System.out.println("Reconnecting");
			reconnect();
		}
		
		return response;
		
	}
    
	/*
     * Sets up the menu bar for the application
     * @return MenuBar that contains all the navigation options
     */
    private MenuBar createMenuBar() 
    {
    	MenuBar menuBar = new MenuBar();
    	
        Menu connectMenu = new Menu("Connect Solar Panels");
        MenuItem connectItem = new MenuItem("Connect Solar Panels Page");
        connectItem.setDisable(true);  //Initially disabled
        connectItem.setOnAction(e -> connectSolarPanelsPage());
        
        connectMenu.getItems().addAll(connectItem);
        
        Menu dashboardMenu = new Menu("Dashboard");
        MenuItem dashboardItem = new MenuItem("Dashboard Page");
        dashboardItem.setDisable(true);  //Initially disabled
        dashboardItem.setOnAction(e -> dashboardPage());
        
        dashboardMenu.getItems().addAll(dashboardItem);
        
        Menu registerTraderMenu = new Menu("Register As Trader");
        MenuItem registerTraderItem = new MenuItem("Register As Trader Page");
        registerTraderItem.setDisable(true);  //Initially disabled
        registerTraderItem.setOnAction(e -> registerTraderPage());
        
        registerTraderMenu.getItems().addAll(registerTraderItem);
       
        Menu solarEnergyMenu = new Menu("Solar Energy");
        MenuItem solarTransactionItem = new MenuItem("Solar Energy Transactions");
        solarTransactionItem.setDisable(true);  //Initially disabled
        solarTransactionItem.setOnAction(e -> 
        {
			try
			{
				solarTransactionPage();
			} 
			catch(IOException e1) 
			{
				e1.printStackTrace();
			}
		});
        
        MenuItem solarBlockchainItem = new MenuItem("Solar Energy Blockchain");
        solarBlockchainItem.setDisable(true);  //Initially disabled
        solarBlockchainItem.setOnAction(e -> solarBlockchainPage());
        
        MenuItem buyItem = new MenuItem("Buy Solar Energy");
        buyItem.setDisable(true);  //Initially disabled
        buyItem.setOnAction(e -> buySolarEnergyPage());
        
        MenuItem sellItem = new MenuItem("Sell Solar Energy");
        sellItem.setDisable(true);  //Initially disabled
        sellItem.setOnAction(e -> sellSolarEnergyPage());
        
        MenuItem solarBlockchainVisualiserItem = new MenuItem("Solar Energy Blockchain Visualiser");
        solarBlockchainVisualiserItem.setDisable(true);  //Initially disabled
        solarBlockchainVisualiserItem.setOnAction(e -> solarBlockchainVisualiserPage());
        
        solarEnergyMenu.getItems().addAll(solarTransactionItem, solarBlockchainItem, sellItem, buyItem, solarBlockchainVisualiserItem);
        
        Menu renewableEnergyCertificatesMenu = new Menu("Renewable Energy Certificates");   
        MenuItem certificateTransactionItem = new MenuItem("Renewable Energy Certificates Transactions");
        certificateTransactionItem.setDisable(true);  //Initially disabled
        certificateTransactionItem.setOnAction(e -> certificatesTransactionPage());
        
        MenuItem certificateBlockchainItem = new MenuItem("Renewable Energy Certificates Blockchain");
        certificateBlockchainItem.setDisable(true);  //Initially disabled
        certificateBlockchainItem.setOnAction(e -> certificatesBlockchainPage());
        
        MenuItem sellCertificatesItem = new MenuItem("Sell Certificates");
        sellCertificatesItem.setDisable(true);  //Initially disabled
        sellCertificatesItem.setOnAction(e -> sellCertificatesPage());
        
        MenuItem buyCertificatesItem = new MenuItem("Buy Certificates");
        buyCertificatesItem.setDisable(true);  //Initially disabled
        buyCertificatesItem.setOnAction(e -> buyCertificatesPage());
        
        MenuItem certificateBlockchainVisualiserItem = new MenuItem("Renewable Energy Certificates Blockchain Visualiser");
        certificateBlockchainVisualiserItem.setDisable(true);  //Initially disabled
        certificateBlockchainVisualiserItem.setOnAction(e -> certificatesBlockchainVisualiserPage());
        
        renewableEnergyCertificatesMenu.getItems().addAll(certificateTransactionItem, certificateBlockchainItem, sellCertificatesItem, buyCertificatesItem, certificateBlockchainVisualiserItem);
	       
        Menu reportingMenu = new Menu("Reporting");
        MenuItem solarTransactionsReportingItem = new MenuItem("Solar Energy Transactions Reporting");
        solarTransactionsReportingItem.setDisable(true);  //Initially disabled
        solarTransactionsReportingItem.setOnAction(e -> solarTransactionsReportingPage());
        
        MenuItem solarBlockchainReportingItem = new MenuItem("Solar Energy Blockchain Reporting");
        solarBlockchainReportingItem.setDisable(true);  //Initially disabled
        solarBlockchainReportingItem.setOnAction(e -> solarBlockchainReportingPage());
        
        MenuItem certificateTransactionsReportingItem = new MenuItem("Certificate Transactions Reporting");
        certificateTransactionsReportingItem.setDisable(true);  //Initially disabled
        certificateTransactionsReportingItem.setOnAction(e -> certificateTransactionsReportingPage());
        
        MenuItem certificateBlockchainReportingItem = new MenuItem("Certificate Blockchain Reporting");
        certificateBlockchainReportingItem.setDisable(true);  //Initially disabled
        certificateBlockchainReportingItem.setOnAction(e -> certificateBlockchainReportingPage());
        
        reportingMenu.getItems().addAll(solarTransactionsReportingItem, solarBlockchainReportingItem, certificateTransactionsReportingItem, certificateBlockchainReportingItem);
        
        Menu accountMenu = new Menu("Account");
        MenuItem signUpItem = new MenuItem("Sign Up");
        signUpItem.setOnAction(e -> signUpPage());
        
        MenuItem logInItem = new MenuItem("Log In");
        logInItem.setOnAction(e -> logInPage());
        
        MenuItem logOutItem = new MenuItem("Log Out");
        logOutItem.setDisable(true);  //Initially disabled
        logOutItem.setOnAction(e -> 
        {
        		clearFileContents("src/acsse/csc03a3/database/queue_solar_energy.txt");
        		clearFileContents("src/acsse/csc03a3/database/queue_certificates.txt");
        		closeResources();
        		System.exit(0);
        });
         
        accountMenu.getItems().addAll(signUpItem, logInItem, logOutItem);
        
        menuBar.getMenus().addAll(connectMenu, dashboardMenu, registerTraderMenu, solarEnergyMenu, renewableEnergyCertificatesMenu, reportingMenu, accountMenu);
        
        return menuBar;
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
            if(clientSocket != null) clientSocket.close();
            
            System.out.println("Connections closed successfully.");
        } 
        catch(IOException ex) 
        {
            System.out.println("Error closing resources: " + ex.getMessage());
        }
    }
    public static void clearFileContents(String filePath) 
    {
        try(PrintWriter writer = new PrintWriter(new File(filePath))) 
        {
            writer.print("");
        } 
        catch(IOException e) 
        {
            e.printStackTrace();
        }
    }
    
    /*
     * Sets up the initial page that is displayed when the application is launched
     */
    private void setupInitialPage() 
    {
        //Load the welcome page image
        Image welcomeImage = new Image("file:src/acsse/csc03a3/style/WelcomePage.jpg");
        
        if(welcomeImage.isError()) 
        {
            System.out.println("Error loading image: " + welcomeImage.getException());
            return;
        }

        ImageView imageView = new ImageView(welcomeImage);

        imageView.setFitWidth(1080); 
        imageView.setFitHeight(580);  
        imageView.setPreserveRatio(true);

        //Rectangle for the welcome text
        Rectangle rectangle = new Rectangle(300, 100); 
        rectangle.setFill(Color.LIGHTGREEN);
        rectangle.setOpacity(1);

        //Label with the welcome text
        Label welcomeLabel = new Label("Welcome to SolarChain");
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        welcomeLabel.setTextFill(Color.BLACK);

        //Stack the label on top of the rectangle
        StackPane labelStack = new StackPane();
        labelStack.getChildren().addAll(rectangle, welcomeLabel);
        //Center the text within the rectangle
        labelStack.setAlignment(Pos.CENTER); 

        //Layout container for the image and add the text overlay
        StackPane layout = new StackPane();
        layout.getChildren().addAll(imageView, labelStack);
        layout.setAlignment(Pos.CENTER);

        //Set the layout as the center of the root pane
        root.setCenter(layout);
    }

    private void signUpPage() 
    {
    	VBox pageContent = new VBox(10);
        pageContent.setPadding(new Insets(10));
       
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        
        //Image placeholder for sign up page
        ImageView imageView = new ImageView(new Image("file:src/acsse/csc03a3/style/SolarEnergyBannerImage.jpg"));
        imageView.setFitWidth(400); 
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
      
        Button signUpButton = new Button("Sign Up");
        signUpButton.getStyleClass().add("button"); //Apply the CSS class
        
	    signUpButton.setOnAction(e -> 
	    {
	    	if(usernameField.getText().isEmpty() || passwordField.getText().isEmpty()) 
	    	{
	            showAlert("Note", "Both fields are required.", Alert.AlertType.ERROR);
	        } 
	    	else 
	    	{
	            UserDatabase.writeUser(usernameField.getText(), passwordField.getText());
	            companyName = usernameField.getText();

	            connect("localhost", 8888);
	            showAlert("Success", "User registered successfully", Alert.AlertType.INFORMATION);
	            enableMenuAfterSignUpOrSignIn();
	            switchToConnectSolarPanelsMenu();
	        }
	    });

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(signUpButton, 1, 2);

        layout.getChildren().addAll(imageView, grid);
        
        pageContent.getChildren().addAll(new Label("Sign Up"), layout);
        root.setCenter(pageContent);
    }
    
    private void showAlert(String title, String message, Alert.AlertType alertType) 
    {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void logInPage() 
    {
    	VBox pageContent = new VBox(10);
        pageContent.setPadding(new Insets(10));
        
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        //Image placeholder for sign in page
        ImageView imageView = new ImageView(new Image("file:src/acsse/csc03a3/style/SolarEnergyBannerImage.jpg"));
        imageView.setFitWidth(400); 
        imageView.setFitHeight(200);
        //imageView.setPreserveRatio(true);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        
        Button signInButton = new Button("Log In");
        signInButton.getStyleClass().add("button"); // Apply the CSS class
        
        signInButton.setOnAction(e -> 
        { 	
        	if(UserDatabase.validateUser(usernameField.getText(), passwordField.getText())) 
        	{	        	
	        	companyName = usernameField.getText();
		        
	        	connect("localhost", 8888);
	        	
	        	showAlert("Login successful!");
	            enableMenuAfterSignUpOrSignIn();
	            switchToConnectSolarPanelsMenu();
	        } 
	        else 
	        {
	            showAlert("Invalid username or password");
	        }
	    });

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(signInButton, 1, 2);

        layout.getChildren().addAll(imageView, grid);
        
        pageContent.getChildren().addAll(new Label("Log In"), layout);
        root.setCenter(pageContent);
    }
    
    /*
     * Loads the page for connecting solar panels
     */
    private void connectSolarPanelsPage() 
    {
    	VBox pageContent = new VBox(10);
        pageContent.setPadding(new Insets(10));

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));

        //New company name label and text field
        Label companyNameLabel = new Label("Private Company Name:");
        TextField companyNameField = new TextField();
        companyNameField.setText(companyName);
        companyNameField.setPromptText("Enter your company name");
        grid.add(companyNameLabel, 0, 0);
        grid.add(companyNameField, 1, 0);

        addConnectionFields(grid);

        //Label for results which handleFetchData will update
        Label resultLabel = new Label();

        //Register button
        Button connectButton = new Button("Connect Solar Panels");
        connectButton.getStyleClass().add("button"); //Apply the CSS class
        connectButton.setOnAction(e -> 
        {
            companyName = companyNameField.getText();
            handleFetchData(grid, resultLabel);
            
            //Rebuild the solar energy blockchain across states
            sendCommand("GET_SOLAR_TRANSACTIONS");
        	
            FILE_REC_FROM_SERVER = readResponse();
            System.out.println(FILE_REC_FROM_SERVER);
            ArrayList<Transaction<SolarEnergyData>> solarTransactions = rebuildBlockchainSolar(new File(FILE_REC_FROM_SERVER));

            //Rebuild the certificates blockchain across states
            sendCommand("GET_CERTIFICATE_TRANSACTIONS");

            FILE_REC_FROM_SERVER = readResponse();
            System.out.println(FILE_REC_FROM_SERVER);
            ArrayList<Transaction<CertificateData>> certificateTransactions = rebuildBlockchainCertificates(new File(FILE_REC_FROM_SERVER));
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("System Connected To Solar Panels And Blockchains Rebuilt Successfully");
            alert.setHeaderText(null);
            alert.setContentText("System Connected To Solar Panels And Blockchains Rebuilt Successfully");
            alert.showAndWait();
           
            switchToDashboardMenu();
        });
        grid.add(connectButton, 1, 7);

        pageContent.getChildren().addAll(new Label("Connect Solar Panels Page"), grid);
        root.setCenter(pageContent);  
    }
    
    private void addConnectionFields(GridPane grid) 
	{
	    grid.add(new Label("System Capacity (kW):"), 0, 1);
	    TextField systemCapacityField = new TextField();
	    systemCapacityField.setPromptText("Enter system capacity in kW:");
	    grid.add(systemCapacityField, 1, 1);
	
		grid.add(new Label("Module Type (0=Standard, 1=Premium, 2=Thin film):"), 0, 2);
		ComboBox<Integer> moduleTypeField = new ComboBox<>();
		moduleTypeField.getItems().addAll(0, 1, 2);
		moduleTypeField.setPromptText("Select module type");
		grid.add(moduleTypeField, 1, 2);
		
		grid.add(new Label("System Losses (%):"), 0, 3);
		TextField lossesField = new TextField();
		lossesField.setPromptText("Enter system losses (%):");
		grid.add(lossesField, 1, 3);
		
		grid.add(new Label("Array Type (0=Fixed - Open Rack, 1=Fixed - Roof Mounted, 2=1-Axis, 3=1-Axis Backtracking, 4=2-Axis):"), 0, 4);
		ComboBox<Integer> arrayTypeField = new ComboBox<>();
		arrayTypeField.getItems().addAll(0, 1, 2, 3, 4);
		arrayTypeField.setPromptText("Select array type");
		grid.add(arrayTypeField, 1, 4);
		
		grid.add(new Label("Tilt (degrees):"), 0, 5);
		TextField tiltField = new TextField();
		tiltField.setPromptText("Enter tilt (degrees):");
		grid.add(tiltField, 1, 5);
		
		grid.add(new Label("Azimuth (degrees):"), 0, 6);
		TextField azimuthField = new TextField();
		azimuthField.setPromptText("Enter azimuth (degrees):");
	    grid.add(azimuthField, 1, 6);
	}
	
    //Fetch data from solar panel energy generation API
	private void handleFetchData(GridPane grid, Label resultLabel) 
	{
	    try 
	    {
		    String companyName = ((TextField) grid.getChildren().get(1)).getText();
		    double systemCapacity = Double.parseDouble(((TextField) grid.getChildren().get(3)).getText());
		   
		    int moduleType = ((ComboBox<Integer>) grid.getChildren().get(5)).getSelectionModel().getSelectedItem();
		    double losses = Double.parseDouble(((TextField) grid.getChildren().get(7)).getText());
		    int arrayType = ((ComboBox<Integer>) grid.getChildren().get(9)).getSelectionModel().getSelectedItem();
		    
		    double tilt = Double.parseDouble(((TextField) grid.getChildren().get(11)).getText());
		    double azimuth = Double.parseDouble(((TextField) grid.getChildren().get(13)).getText());
		    
		    //Fetch data from API
		    SolarEnergyData solarEnergyData = SolarEnergyDataAPI.fetchSolarEnergyData(
		        systemCapacity, moduleType, losses, arrayType, tilt, azimuth, 40.7128, -74.0060);
		
		    if(solarEnergyData != null) 
		    {
		        totalSolarEnergyGenerated = solarEnergyData.getEnergyProduced();
		        resultLabel.setText("Annual Energy Produced: " + totalSolarEnergyGenerated);
		    } 
		    else 
		    {
		        resultLabel.setText("Could not fetch solar energy data.");
		    }
		} 
	    catch(NumberFormatException ex) 
	    {
		    resultLabel.setText("Invalid input. Please enter correct numerical values.");
		}
	    catch(NullPointerException ex) 
	    {
		    resultLabel.setText("Please make sure all fields are correctly filled.");
	    }
	}
    
    //Rebuild the solar energy blockchain across states
    public static ArrayList<Transaction<SolarEnergyData>> rebuildBlockchainSolar(File file) 
    {
	    ArrayList<Transaction<SolarEnergyData>> transactions = new ArrayList<>();
	
	    try (Scanner scanner = new Scanner(file))
	    {
	        while(scanner.hasNextLine()) 
	        {
	            String line = scanner.nextLine();
	            Transaction<SolarEnergyData> transaction = parseTransactions(line);
	            
	            if(transaction != null) 
	            {
	                transactions.add(transaction);
	                blockchainHandler.addSolarTransactionAndBlockToBlockchain(transaction);
	            }
	        }
	        System.out.println(blockchainHandler.getBlockchainAsString());
	    } 
	    catch(FileNotFoundException e) 
	    {
	        System.err.println("File not found: " + file);
	    } 
	    catch(Exception e) 
	    {
	        e.printStackTrace();
	    }
	
	    return transactions;
    }
    
	//Rebuild the certificate blockchain across states
	public static ArrayList<Transaction<CertificateData>> rebuildBlockchainCertificates(File file) 
	{
	    ArrayList<Transaction<CertificateData>> transactions = new ArrayList<>();
	
	    try(Scanner scanner = new Scanner(file)) 
	    {
	        while(scanner.hasNextLine()) 
	        {
	            String line = scanner.nextLine();
	            Transaction<CertificateData> transaction = parseTransactionsCertificates(line);
	            
	            if(transaction != null) 
	            {
	                transactions.add(transaction);
	                blockchainHandlerCertificate.addCertificateTransactionAndBlockToBlockchain(transaction);
	            }
	        }
	        
	        System.out.println(blockchainHandlerCertificate.getBlockchainAsString());
	    }
	    catch(FileNotFoundException e) 
	    {
	        System.err.println("File not found: " + file);
	    } 
	    catch(Exception e) 
	    {
	        e.printStackTrace();
	    }
	
	    return transactions;
	}

    private void dashboardPage() 
    {
    	VBox pageContent = new VBox(10);
        pageContent.setPadding(new Insets(10));
	   
	    VBox layout = new VBox(10);
	    layout.setAlignment(Pos.CENTER);
	    layout.setPadding(new Insets(20));
	  
	    //Setup the chart components
	    CategoryAxis xAxis = new CategoryAxis();
	    xAxis.setLabel("Time");

	    NumberAxis yAxis = new NumberAxis();
	    yAxis.setLabel("Energy (kWh)");

	    BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
	    barChart.setTitle("Solar Energy Generation");

	    //Label to display solar energy balance
	    Label balanceLabel = new Label("Current Solar Energy Balance: " + totalSolarEnergyGenerated + " kWh");
	    balanceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

	    //Button to calculate energy
	    Button calculateButton = new Button("Calculate Solar Energy Balance");
	    calculateButton.setOnAction(e -> 
	    {
	        double energy = Math.random() * 100;
	        totalSolarEnergyGenerated += energy;
	        balanceLabel.setText("Current Solar Energy Balance: " + totalSolarEnergyGenerated + " kWh"); // Update the label
	        updateBarChart(barChart, "Current", energy);
	    	
	    });
	   
	    layout.getChildren().addAll(balanceLabel, calculateButton, barChart);
	    
	    pageContent.getChildren().addAll(new Label("Dashboard"), layout);
        root.setCenter(pageContent);
    }
    
    private void updateBarChart(BarChart<String, Number> barChart, String category, double value) 
    {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>(category, value));
        barChart.getData().clear();
        barChart.getData().add(series);
    }

    private void registerTraderPage() 
    {
    	VBox pageContent = new VBox(10);
        pageContent.setPadding(new Insets(10));

	    VBox layout = new VBox(10);
	    layout.setAlignment(Pos.CENTER);
	    layout.setPadding(new Insets(20));
	    
	    Label registerLabel = new Label("Register as official solar energy and certificates trader");
	    layout.getChildren().add(registerLabel);
        Label companyRegNrLabel = new Label("Company Registration Number:");
        TextField companyRegNrField = new TextField();
      
        HBox companyRegNrBox = new HBox(10, companyRegNrLabel, companyRegNrField);
        companyRegNrBox.setAlignment(Pos.CENTER);
        layout.getChildren().add(companyRegNrBox);

        Label dateLabel = new Label("Date of Incorporation:");
        TextField dateField = new TextField();
      	        
        HBox dateBox = new HBox(10, dateLabel, dateField);
        dateBox.setAlignment(Pos.CENTER);
        layout.getChildren().add(dateBox);
        
        //Button to calculate energy
	    Button registerButton = new Button("Register As Solar Energy And Certificates Trader");
	    registerButton.setOnAction(e -> 
	    {	
	    	try 
	    	{
				blockchainDatabase.registerCompany(companyName);
			} 
	    	catch(IOException e1) 
	    	{
				e1.printStackTrace();
			}
	    
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Successfully Registered As Trader");
            alert.show();
	    });

	    layout.getChildren().add(registerButton);
	  
	    pageContent.getChildren().addAll(new Label("Register As Trader"), layout);
        root.setCenter(pageContent);   
    }
   
    private void sellSolarEnergyPage() 
    {
        VBox pageContent = new VBox(10);
        pageContent.setPadding(new Insets(10));

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        //Display the balance button and text field
        Button calculateBalanceButton = new Button("Calculate Solar Energy Balance");
        calculateBalanceButton.getStyleClass().add("button"); //Apply the CSS class

        TextField balanceDisplay = new TextField();
        balanceDisplay.setEditable(false);
        calculateBalanceButton.setOnAction(e -> balanceDisplay.setText(String.valueOf(totalSolarEnergyGenerated) + " kWh"));
        layout.getChildren().add(calculateBalanceButton);
        layout.getChildren().add(balanceDisplay);

        Label sellLabel = new Label("Enter details to sell solar energy:");
        layout.getChildren().add(sellLabel);

        Label priceLabel = new Label("Price per kWh (R):");
        TextField priceField = new TextField("0.0");
        priceField.setPromptText("Enter price per kWh");

        HBox priceBox = new HBox(10, priceLabel, priceField);
        priceBox.setAlignment(Pos.CENTER);
        layout.getChildren().add(priceBox);

        Label quantityLabel = new Label("Quantity (kWh):");
        TextField quantityField = new TextField("0.0");
        quantityField.setPromptText("Enter quantity to sell");

        HBox quantityBox = new HBox(10, quantityLabel, quantityField);
        quantityBox.setAlignment(Pos.CENTER);
        layout.getChildren().add(quantityBox);

        //Button to display the calculated selling price
        Button calculatePriceButton = new Button("Display Selling Price");
        calculatePriceButton.getStyleClass().add("button"); //Apply the CSS class

        TextField sellingPriceDisplay = new TextField();
        sellingPriceDisplay.setEditable(false);

        calculatePriceButton.setOnAction(e -> 
        {    
            pricePerKwh = Double.parseDouble(priceField.getText());
            quantitySell = Double.parseDouble(quantityField.getText());
            sellingPrice = pricePerKwh * quantitySell;
            sellingPriceDisplay.setText(String.format("%.2f", sellingPrice) + " R");
        });

        layout.getChildren().add(calculatePriceButton);
        layout.getChildren().add(sellingPriceDisplay);

        //Send command to receive state of solar energy queue of transactions
        sendCommand("GET_QUEUE_SOLAR_ENERGY");
    	
        //Read response of state of solar energy queue of transactions
    	String QUEUE_FILE_REC_FROM_SERVER = readResponse();
    	
        Button processQueueButton = new Button("Place Order Onto Queue");
        processQueueButton.setOnAction(e -> 
        {
        	int solarEnergyID = getMaxTransactionIdSolarEnergy() + 1;

            String transactionType = "Sell";
          
            String dateTime = "2024";
            
            quantitySell = Double.parseDouble(quantityField.getText());

            solarEnergySold = new SolarEnergyData(solarEnergyID, transactionType, quantitySell, pricePerKwh, sellingPrice, dateTime);
        	
        	rebuildQueue(new File(QUEUE_FILE_REC_FROM_SERVER));
            blockchainHandler.addToQueue(solarEnergySold, companyName, "Market");
            updateQueue(QUEUE_FILE_REC_FROM_SERVER, solarEnergyDataQueue);

            currentTransactionProcessing = new Transaction<>(companyName, "Market", solarEnergySold);

            int queuePosition = getQueuePosition(QUEUE_FILE_REC_FROM_SERVER);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Order Processed On Queue");
            alert.setHeaderText("Successfully Added To Queue");
            
            alert.setContentText("Your Order Has Now Been Processed And Is Ready To Be Sent To The Blockchain: Click Sell Energy To Confirm Order");
            alert.showAndWait();
        });

        layout.getChildren().add(processQueueButton);

        Button cancelOrderButton = new Button("Cancel Order");
        
        cancelOrderButton.setOnAction(e -> 
        {
        	try 
        	{
        		//Remove the order from the queue
				dequeueFromQueue(QUEUE_FILE_REC_FROM_SERVER, solarEnergyID);
			} 
        	catch(IOException e1) 
        	{
				e1.printStackTrace();
			}
        	
        	//Update the state of the queue
        	updateQueue(QUEUE_FILE_REC_FROM_SERVER, solarEnergyDataQueue);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Alert");
            alert.setHeaderText("Order Details");
            alert.setContentText("Order cancelled");
            alert.showAndWait();
            switchToDashboardMenu();
        });
        
        layout.getChildren().add(cancelOrderButton);

        Button sellButton = new Button("Sell Energy");
        sellButton.getStyleClass().add("button"); // Apply the CSS class

        sellButton.setOnAction(e -> 
        {
        	//Block validation occurs by confirming whether company is registered solar energy trader
        	if(blockchainDatabase.companyNameExists(companyName) 
                    && currentTransactionProcessing != null)               
        	{
        		registerStakeholder();
                blockchainHandler.addSolarEnergyData(solarEnergySold, companyName, "Market");

                balanceDisplay.setText(String.valueOf(totalSolarEnergyGenerated) + " kWh");

                //Show invoice alert
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Invoice");
                alert.setHeaderText("Transaction Details");
                String content = String.format("Selling price per kWh:R %.2f\nQuantity sold: %.2f kWh\nAvailable solar energy balance: %.2f kWh\n\nBlockchain updated successfully.", pricePerKwh, quantitySell, totalSolarEnergyGenerated);
                alert.setContentText(content);
                alert.showAndWait();
                blockchainHandler.getBlockchainAsString();

                totalSolarEnergyGenerated -= quantitySell;
                
                try 
                {
					dequeueFromQueue(QUEUE_FILE_REC_FROM_SERVER, solarEnergyID);
                } 
                catch(IOException e1) 
                {
					e1.printStackTrace();
				}
                
                updateQueue(QUEUE_FILE_REC_FROM_SERVER, solarEnergyDataQueue);
            } 
        	//If the block is not validated, cannot proceed to add block to blockchain
            else if(!blockchainDatabase.companyNameExists(companyName)) 
            {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Alert");
                alert.setHeaderText("Block Not Validated");
                String content = String.format("Block Cannot Be Validated and Added To Blockchain Since Not Registered Solar Trader");
                alert.setContentText(content);
                alert.showAndWait();
            } 
            else 
            {          	
            	int queueIndex = getQueuePosition(QUEUE_FILE_REC_FROM_SERVER);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Alert");
                alert.setHeaderText("Order Details");
                String content = String.format("Please Wait And Try Again Later, You Are Still In The Queue");
              
                alert.setContentText(content);
                alert.showAndWait();
            }
        });
        layout.getChildren().add(sellButton);

        pageContent.getChildren().addAll(new Label("Sell Solar Energy Page"), layout);
        root.setCenter(pageContent);
    }
    
    public static ArrayList<Transaction<SolarEnergyData>> rebuildQueue(File file) 
    {
        ArrayList<Transaction<SolarEnergyData>> transactions = new ArrayList<>();

        try(Scanner scanner = new Scanner(file)) 
        {
            while(scanner.hasNextLine()) 
            {
                String line = scanner.nextLine();
                
                Transaction<SolarEnergyData> transaction = parseTransactions(line);
                if(transaction != null)
                {
                    transactions.add(transaction);
                    blockchainHandler.addSolarTransactionToQueue(transaction);
                }
            } 
        } 
        catch(FileNotFoundException e) 
        {
            System.err.println("File not found: " + file);
        } 
        catch(Exception e) 
        {
            e.printStackTrace();
        }
        
        return transactions;
    }
    
    public static void updateQueue(String filePath, SolarEnergyDataQueue<Transaction<SolarEnergyData>> queue) 
    {
        try(BufferedReader br = new BufferedReader(new FileReader(filePath))) 
        {
            String line;
            
            while((line = br.readLine()) != null) 
            {
                Transaction<SolarEnergyData> transaction = parseTransaction(line);
                
                if(transaction != null) 
                {
                    queue.enqueue(transaction);
                }
            }
        } 
        catch(IOException e) 
        {
            e.printStackTrace();
        }
    }

    private static Transaction<SolarEnergyData> parseTransaction(String line) 
    {
        Pattern pattern = Pattern.compile("Transaction\\{sender='([^']*)', receiver='([^']*)', data=SolarEnergyData\\{ID: (\\d+), Transaction Type: ([^,]*), Quantity Solar Energy Traded: ([^,]*), Price Per KwH: ([^,]*), Total Price Traded: ([^,]*), Date and Time: ([^}]*)\\}, timestamp=(\\d+)\\}");
        
        Matcher matcher = pattern.matcher(line);
        
        if(matcher.find()) 
        {
            String sender = matcher.group(1);
            String receiver = matcher.group(2);
            int id = Integer.parseInt(matcher.group(3));
            String transactionType = matcher.group(4);
            double quantity = Double.parseDouble(matcher.group(5));
            double pricePerKwH = Double.parseDouble(matcher.group(6));
            double totalPrice = Double.parseDouble(matcher.group(7));
            String dateTime = matcher.group(8);
            long timestamp = Long.parseLong(matcher.group(9));

            SolarEnergyData data = new SolarEnergyData(id, transactionType, quantity, pricePerKwH, totalPrice, dateTime);
            
            return new Transaction<>(sender, receiver, data);
        }
        return null;
    }

    public int getQueuePosition(String filePath) 
    {
        return blockchainDatabase.getLastQueueIndex(filePath);
    }
    
    /*
     * Removes the transaction from the queue text file that matches the specified solarEnergyID
     * @param filePath the path to the queue text file
     * @param solarEnergyID the ID of the SolarEnergyData to remove
     * @throws IOException if an error occurs
     */
    public static void dequeueFromQueue(String filePath, int solarEnergyID) throws IOException 
    {
        Path tempFile = Files.createTempFile("queue", ".tmp");
        clearFileContents(filePath);
        
        try(BufferedReader reader = new BufferedReader(new FileReader(filePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile.toFile()))) 
        {

            String line;
            
            while((line = reader.readLine()) != null) 
            {
                if(!line.contains("ID: " + solarEnergyID + ",")) 
                {
                    writer.write(line);
                    writer.newLine();
                }
            }
        }
        
        clearFileContents(filePath);
        Files.move(tempFile, Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
    }
    
    /*
     * Loads the page for buying solar energy
     */
    private void buySolarEnergyPage() 
    {
        VBox pageContent = new VBox(10);
        pageContent.setPadding(new Insets(10));
        
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        //Retrieves the blockchain hashes for selection
        ComboBox<String> blockchainHashComboBox = new ComboBox<>();
        List<String> blockHashes = blockchainHandler.getBlockHashes();
        blockchainHashComboBox.setItems(FXCollections.observableArrayList(blockHashes));
        blockchainHashComboBox.setPromptText("Select a block hash");

        layout.getChildren().addAll(new Label("Select Block Hash:"), blockchainHashComboBox);

        Label priceLabel = new Label("Price per kWh (R):");
        TextField priceField = new TextField("0.0");
        priceField.setPromptText("Enter price per kWh");
        layout.getChildren().addAll(priceLabel, priceField);

        Label quantityLabel = new Label("Quantity (kWh):");
        TextField quantityField = new TextField("0.0");
        quantityField.setPromptText("Enter quantity to buy");
        layout.getChildren().addAll(quantityLabel, quantityField);
        
        sendCommand("GET_QUEUE_SOLAR_ENERGY");
        
        String QUEUE_FILE_REC_FROM_SERVER = readResponse();
        
        Button processQueueButton = new Button("Place Order Onto Queue");
        
        processQueueButton.setOnAction(e -> 
        {      	
        	solarEnergyID = getMaxTransactionIdSolarEnergy() + 1;
            String transactionType = "Buy";
            pricePerKwh = Double.parseDouble(priceField.getText());  
            quantityBuy = Double.parseDouble(quantityField.getText());  
            purchasePrice = pricePerKwh * quantityBuy;
            String dateTime = "Date";

            solarEnergyPurchased = new SolarEnergyData(solarEnergyID, transactionType, quantityBuy, pricePerKwh, purchasePrice, dateTime);
        	rebuildQueue(new File(QUEUE_FILE_REC_FROM_SERVER));
            blockchainHandler.addToQueue(solarEnergyPurchased, "Market", companyName);
            updateQueue(QUEUE_FILE_REC_FROM_SERVER, solarEnergyDataQueue);

            currentTransactionProcessing = new Transaction<>("Market", companyName, solarEnergyPurchased);

            int queuePosition = getQueuePosition(QUEUE_FILE_REC_FROM_SERVER);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Order Processed On Queue");
            alert.setHeaderText("Successfully Added To Queue");
            
            alert.setContentText("Your Order Has Now Been Processed And Is Ready To Be Sent To The Blockchain: Click Buy Energy To Confirm Order");
            alert.showAndWait();
        });

        layout.getChildren().add(processQueueButton);
        
        Button cancelOrderButton = new Button("Cancel Order");
        cancelOrderButton.setOnAction(e -> 
        {       
        	try 
        	{
        		dequeueFromQueue(QUEUE_FILE_REC_FROM_SERVER, solarEnergyID);
        	} 
        	catch(IOException e1) 
        	{             
        		e1.printStackTrace();
        	}
        	
        	updateQueue(QUEUE_FILE_REC_FROM_SERVER, solarEnergyDataQueue);
        	
        	Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Alert");
            alert.setHeaderText("Order Details");
            
            String content = String.format("Order cancelled");
            alert.setContentText(content);
            alert.showAndWait();
            switchToDashboardMenu();
        });
        layout.getChildren().add(cancelOrderButton);
        
        Button buyButton = new Button("Buy Energy");
        buyButton.getStyleClass().add("button"); //Apply the CSS class
        
        buyButton.setOnAction(e -> 
        {
        	String selectedHash = blockchainHashComboBox.getSelectionModel().getSelectedItem();
        	
        	//Block validation
        	if(blockchainDatabase.companyNameExists(companyName) 
                    && currentTransactionProcessing != null) 
            { 
            	 registerStakeholder();
                 blockchainHandler.addSolarEnergyData(solarEnergyPurchased, companyName, "Market");
                 
                 //Show invoice alert
                 Alert alert = new Alert(Alert.AlertType.INFORMATION);
                 alert.setTitle("Invoice");
                 alert.setHeaderText("Transaction Details");
                 String content = String.format("Purchase price per kWh:R %.2f\nQuantity purchased: %.2f kWh\nAvailable solar energy balance: %.2f kWh\n\nBlockchain updated successfully.", pricePerKwh, quantityBuy, totalSolarEnergyGenerated);
                 alert.setContentText(content);
                 alert.showAndWait();
                 blockchainHandler.getBlockchainAsString();
                 
                 totalSolarEnergyGenerated += quantityBuy;
                 
                 try 
                 {
                     dequeueFromQueue(QUEUE_FILE_REC_FROM_SERVER, solarEnergyID);
                 } 
                 catch(IOException e1) 
                 {
                     e1.printStackTrace();
                 }
                 
                 updateQueue(QUEUE_FILE_REC_FROM_SERVER, solarEnergyDataQueue);
                    
            }
        	//Block not validated
            else if(!blockchainDatabase.companyNameExists(companyName))
            {
                 Alert alert = new Alert(Alert.AlertType.INFORMATION);
                 alert.setTitle("Alert");
                 alert.setHeaderText("Block Not Validated");
                 
                 String content = String.format("Block Cannot Be Validated and Vetted Since Not Registered Solar Trader");
                 alert.setContentText(content);
                 alert.showAndWait();
            }           
            else
            {
            	
            	 int queuePosition = getQueuePosition(QUEUE_FILE_REC_FROM_SERVER);
            	 Alert alert = new Alert(Alert.AlertType.INFORMATION);
            	 alert.setTitle("Alert");
            	 alert.setHeaderText("Order Details");         
            	 String content = String.format("Please Wait And Try Again Later, You Are Still In The Queue");
            	
            	 alert.setContentText(content);
            	 alert.showAndWait(); 
            } 
        });
        
        layout.getChildren().add(buyButton);

        pageContent.getChildren().addAll(new Label("Buy Solar Energy Page"), layout);
        root.setCenter(pageContent);
    }
   
    /**
     * Loads the transactions history page
     */
    private void sellCertificatesPage() 
    {	
    	VBox pageContent = new VBox(0);
        pageContent.setPadding(new Insets(0));
        
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(16));
        
        //Display the balance button and text field
        Button calculateBalanceButton = new Button("Calculate Carbon Emissions Offset Balance");
        calculateBalanceButton.getStyleClass().add("button"); // Apply the CSS class
        
        totalCarbonEmissionsOffset = 1;
        
        TextField balanceDisplay = new TextField();
        balanceDisplay.setEditable(false);
        calculateBalanceButton.setOnAction(e -> 
        {
        	balanceDisplay.setText(totalCarbonEmissionsOffset + " Tons");      
        });
        
        layout.getChildren().add(calculateBalanceButton);
        layout.getChildren().add(balanceDisplay);
        
        Label typeLabel = new Label("Select Renewable Energy Certificate Type");
        ComboBox<String> typeComboBox = new ComboBox<>();
        String selectedType = typeComboBox.getSelectionModel().getSelectedItem();
        typeComboBox.getItems().addAll("Solar Energy", "Wind Energy");
        typeComboBox.setPromptText("Select Energy Type");
              
        HBox typeBox = new HBox(10, typeLabel, typeComboBox);
        typeBox.setAlignment(Pos.CENTER);

        layout.getChildren().add(typeBox);
        
        Label sellLabel = new Label("Enter details to sell renewable energy certificates:");
        layout.getChildren().add(sellLabel);

        Label priceLabel = new Label("Price Per MWh (R):");
        
        TextField priceField = new TextField("0.0");
        priceField.setPromptText("Enter Price Per MWh");
        layout.getChildren().add(priceLabel);
        layout.getChildren().add(priceField);
        
        Label emissionsOffsetLabel = new Label("Tons of CO2 Not Emitted:");
        TextField emissionsOffsetField = new TextField("0.0");
        
        emissionsOffsetField.setPromptText("Enter Tons of CO2 Not Emitted");
        layout.getChildren().add(emissionsOffsetLabel);
        
        //Button to display the calculated selling price
        Button calculatePriceButton = new Button("Display Selling Price");
        calculatePriceButton.getStyleClass().add("button"); // Apply the CSS class
        
        TextField sellingPriceDisplay = new TextField();
        sellingPriceDisplay.setEditable(false);
        calculatePriceButton.setOnAction(e -> 
        {        	
        	double pricePerMwh = Double.parseDouble(priceField.getText()); 
        	double carbonEmissionsOffset = totalCarbonEmissionsOffset;
            double sellingPrice = pricePerMwh * carbonEmissionsOffset;
            sellingPriceDisplay.setText(String.format("%.2f", sellingPrice) + " R");
        });
        
        layout.getChildren().add(calculatePriceButton);
        layout.getChildren().add(sellingPriceDisplay);
        
        TextField fileField = new TextField("Certificate Name");
        fileField.setPromptText("Select a PDF certificate to sell");
        
        //ComboBox for file selection
        ComboBox<String> fileComboBox = new ComboBox<>();
        fileComboBox.setPromptText("Select a PDF certificate to sell");
        //Populate ComboBox with files from the folder
        updateFileComboBox(fileComboBox); 
		String fileName = fileComboBox.getSelectionModel().getSelectedItem();
		
		//Event listener to display PDF when selected from ComboBox
	    fileComboBox.setOnAction(e -> 
	    {
	        String fileNameToDisplay = fileComboBox.getSelectionModel().getSelectedItem();
	        
	        if(fileNameToDisplay != null) 
	        {
	            File file = new File("src/acsse/csc03a3/database/purchasedCertificates/" + fileNameToDisplay);
	            
	            displayPdf(file);
	        }
	    });
        		
        fileField.setText(fileName);
        layout.getChildren().add(fileComboBox);

	    sendCommand("GET_QUEUE_CERTIFICATE");
	    
	    String QUEUE_FILE_REC_FROM_SERVER = readResponse();
	    
        Button processQueueButton = new Button("Place Order Onto Queue");
        processQueueButton.setOnAction(e -> 
        {
        	
        	int certificateID = getMaxTransactionIdCertificates() + 1;       	
        	String transactionType = "Sell";        	
        	String renewableEnergyType = typeComboBox.getSelectionModel().getSelectedItem();     	
        	double pricePerMwh = Double.parseDouble(priceField.getText());           
            double carbonEmissionsOffset = totalCarbonEmissionsOffset;
        
    		sellingPrice = pricePerMwh * carbonEmissionsOffset;
    	  
    	    dateTime = "2024";
    	    
    	    String fileNameToSell = "RenewableEnergyCertificate.pdf";
    	
    	    certificateSold = new CertificateData(certificateID, transactionType, renewableEnergyType, pricePerMwh, carbonEmissionsOffset, sellingPrice, fileNameToSell, dateTime);
        	
    	    rebuildQueueCertificate(new File(QUEUE_FILE_REC_FROM_SERVER));
            blockchainHandlerCertificate.addToQueueCertificates(certificateSold, companyName, "Market");
            updateQueueCertificate(QUEUE_FILE_REC_FROM_SERVER, certificateDataQueue);
            
            currentTransactionProcessingCertificate = new Transaction<>(companyName, "Market", certificateSold);
            
            int queuePosition = getQueuePosition(QUEUE_FILE_REC_FROM_SERVER);

	        Alert alert = new Alert(Alert.AlertType.INFORMATION);
	        alert.setTitle("Order Processed On Queue");
	        alert.setHeaderText("Successfully Added To Queue");
	        
	        alert.setContentText("Your Order Has Now Been Processed And Is Ready To Be Sent To The Blockchain: Click Sell Certificate To Confirm Order");
	        alert.showAndWait();    
        });

        layout.getChildren().add(processQueueButton);
        
        Button cancelOrderButton = new Button("Cancel Order");
        cancelOrderButton.setOnAction(e -> 
        {         
			  try 
		      {
		          dequeueFromQueueCertificate(QUEUE_FILE_REC_FROM_SERVER, certificateID);
		      } 
		      catch(IOException e1) 
		      {
		          e1.printStackTrace();
		      }
		
		      updateQueueCertificate(QUEUE_FILE_REC_FROM_SERVER, certificateDataQueue);
			
		      Alert alert = new Alert(Alert.AlertType.INFORMATION);
		      alert.setTitle("Alert");
		      alert.setHeaderText("Order Details");
		        
		      String content = String.format("Order cancelled");
		      alert.setContentText(content);
		      alert.showAndWait();
		      switchToDashboardMenu();
        });
        layout.getChildren().add(cancelOrderButton);

        Button sellButton = new Button("Sell Certificate");
        sellButton.getStyleClass().add("button"); // Apply the CSS class
        
        sellButton.setOnAction(e -> 
        {
        	//Block validation
        	if(blockchainDatabase.companyNameExists(companyName) 
                    && currentTransactionProcessingCertificate != null)            
            {
            	registerStakeholder();

                blockchainHandlerCertificate.addCertificateData(certificateSold, companyName, "Market");
                
                balanceDisplay.setText(String.valueOf(totalCarbonEmissionsOffset) + " Tons");

                String fileNameToDisplay = fileComboBox.getSelectionModel().getSelectedItem();
                File file = new File("src/acsse/csc03a3/database/purchasedCertificates/" + fileNameToDisplay);
	            
                //Upload the file
                uploadFile(file);
	            
                double pricePerMwh = Double.parseDouble(priceField.getText());           
                double carbonEmissionsOffset = totalCarbonEmissionsOffset;
                //Show invoice alert
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Invoice");
                alert.setHeaderText("Transaction Details");
                String content = String.format("Selling price per mWh:R %.2f\nQuantity sold: %.2f mWh\nAvailable carbon emissions offset balance: %.2f Tons\n\nBlockchain updated successfully.", pricePerMwh, carbonEmissionsOffset, totalCarbonEmissionsOffset);
                alert.setContentText(content);
                alert.showAndWait();
                blockchainHandlerCertificate.getBlockchainAsString();
                
                totalCarbonEmissionsOffset -= carbonEmissionsOffset;
                
                try 
                {
                    dequeueFromQueueCertificate(QUEUE_FILE_REC_FROM_SERVER, certificateID);
                } 
                catch(IOException e1) 
                {
                    e1.printStackTrace();
                }
                
                	updateQueueCertificate(QUEUE_FILE_REC_FROM_SERVER, certificateDataQueue);
                    
                }
        		//Block not validated
                else if(!blockchainDatabase.companyNameExists(companyName))
                {
                     Alert alert = new Alert(Alert.AlertType.INFORMATION);
                     alert.setTitle("Alert");
                     alert.setHeaderText("Block Not Validated");
                     
                     String content = String.format("Block Cannot Be Validated and Vetted Since Not Registered Solar Trader");
                     alert.setContentText(content);
                     alert.showAndWait();
                }           
                else
                {
                	 int queueIndex = getQueuePosition(QUEUE_FILE_REC_FROM_SERVER);
                	
                	 Alert alert = new Alert(Alert.AlertType.INFORMATION);
	                 alert.setTitle("Alert");
	                 alert.setHeaderText("Order Details");
	                 
	                 String content = String.format("Please Wait And Try Again Later, You Are Still In The Queue");
	               
	                 alert.setContentText(content);
	                 alert.showAndWait();
                } 
        });
        
        layout.getChildren().add(sellButton);
  
        pageContent.getChildren().addAll(new Label("Sell Solar Energy Page"), layout);
        root.setCenter(pageContent);
    }
    
    private void updateFileComboBox(ComboBox<String> fileComboBox) 
    {
        File folder = new File("src/acsse/csc03a3/database/purchasedCertificates");
        
        File[] listOfFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));

        if(listOfFiles != null) 
        {
            for (File file : listOfFiles) 
            {
                if(file.isFile()) 
                {
                    fileComboBox.getItems().add(file.getName());
                }
            }
        }
    }
    
    private void displayPdf(File file) 
    {
    	try(PDDocument document = PDDocument.load(file)) 
    	{
    		PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);

            Image image = SwingFXUtils.toFXImage(bim, null);
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(600);
            imageView.setPreserveRatio(true);

            StackPane pane = new StackPane(imageView);
            Scene scene = new Scene(pane, 600, 500);
            Stage stage = new Stage();
            stage.setTitle("PDF Viewer");
            stage.setScene(scene);
            stage.show();
        } 
    	catch(IOException e) 
    	{
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Could not load PDF file.");
            alert.showAndWait();
        }
    }
      
    private void uploadFile(File file) 
    {
        try(FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) 
        {
            //Send the upload command with file name and size
            String command = "SELL_CERTIFICATE " + file.getName() + " " + file.length();
            sendCommand(command);

            byte[] buffer = new byte[4096];
            int bytesRead;

            while((bytesRead = bis.read(buffer)) != -1) 
            {
                dataOutputStream.write(buffer, 0, bytesRead);
                dataOutputStream.flush();
            }

            //After sending the file, read the server's response
            String serverResponse = readResponse();
            handleServerResponse(serverResponse, file);

        } 
        catch(IOException e) 
        {
            showAlert("Error: Unable to upload file. " + e.getMessage());
        }
    }

    private void handleServerResponse(String response, File file) 
    {
        if(response.startsWith("200")) 
        {
            showAlert("Success: " + response.substring(4));
            saveFileWithDifferentName(file); // Save file with a different name
        } 
        else 
        {
            showAlert("Error: " + response + " Please Retry");
            reconnect();
        }
    }

    private void saveFileWithDifferentName(File file) {
        String newFileName = "sold_" + file.getName();
        File newFile = new File("src/acsse/csc03a3/database/soldCertificates/" + newFileName);

        try 
        {
            Files.createDirectories(newFile.getParentFile().toPath());
            Files.copy(file.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File saved with new name: " + newFileName);
        } 
        catch(IOException e) 
        {
            System.err.println("Failed to save the file with a new name: " + file.getName());
            e.printStackTrace();
        }
    }
    
    private void reconnect() 
    {
        closeResources();
		connect("localhost", 8888);
    }
    
    public static ArrayList<Transaction<CertificateData>> rebuildQueueCertificate(File file) 
    {
        ArrayList<Transaction<CertificateData>> transactions = new ArrayList<>();

        try(Scanner scanner = new Scanner(file)) 
        {
            while(scanner.hasNextLine()) 
            {
                String line = scanner.nextLine();
                
                Transaction<CertificateData> transaction = parseTransactionsCertificates(line);
                if(transaction != null)
                {
                    transactions.add(transaction);
                    blockchainHandler.addCertificateTransactionToQueue(transaction);
                }
            }
          
        } 
        catch(FileNotFoundException e) 
        {
            System.err.println("File not found: " + file);
        } 
        catch(Exception e) 
        {
            e.printStackTrace();
        }
        
        return transactions;
    }

    public static void updateQueueCertificate(String filePath, CertificateDataQueue<Transaction<CertificateData>> queue) 
    {
        try(BufferedReader br = new BufferedReader(new FileReader(filePath))) 
        {
            String line;
            
            while((line = br.readLine()) != null) 
            {
                Transaction<CertificateData> transaction = parseTransactionsCertificates(line);
                
                if(transaction != null) 
                {
                    queue.enqueue(transaction);
                }
            }
        } 
        catch(IOException e) 
        {
            e.printStackTrace();
        }
    }


    

    /*
     * Removes the transaction from the queue text file that matches the specified solarEnergyID
     * @param filePath the path to the queue text file
     * @param solarEnergyID the ID of the SolarEnergyData to remove
     * @throws IOException if an I/O error occurs
     */
    public static void dequeueFromQueueCertificate(String filePath, int certificateID) throws IOException 
    {
    	clearFileContents(filePath);
        Path tempFile = Files.createTempFile("queue", ".tmp");

        try(BufferedReader reader = new BufferedReader(new FileReader(filePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile.toFile()))) 
        {

            String line;
            
            while((line = reader.readLine()) != null) 
            {
                if(!line.contains("CID: " + certificateID + ",")) 
                {
                    writer.write(line);
                    writer.newLine();
                }
            }
        }

        Files.move(tempFile, Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
    }    

    /*
     * Loads the transactions history page
     */
    private void buyCertificatesPage() 
    {
    	
    	VBox pageContent = new VBox(10);
        pageContent.setPadding(new Insets(10));
        
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        
        ComboBox<String> comboBox = new ComboBox<>();
        
        try(Stream<Path> files = Files.list(Paths.get("src/acsse/csc03a3/database/soldCertificates"))) 
        {
            List<String> fileNames = files.filter(path -> path.toString().endsWith(".pdf"))
                                          .map(path -> path.getFileName().toString())
                                          .collect(Collectors.toList());
            comboBox.setItems(FXCollections.observableArrayList(fileNames));
        } 
        catch(IOException e1) 
        {
			e1.printStackTrace();
		}

        comboBox.setOnAction(event -> 
        {
        	String selectedFileName = comboBox.getSelectionModel().getSelectedItem();
	        String directoryPath = "src/acsse/csc03a3/database/soldCertificates/";
	        File pdfFile = new File(directoryPath + selectedFileName);
        
            //Display the PDF file to user
            displayPdf(pdfFile);
        });
        
        layout.getChildren().add(comboBox);

        Label typeLabel = new Label("Select Renewable Energy Certificate Type");
        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("Solar Energy", "Wind Energy");
        typeComboBox.setPromptText("Select Energy Type");

        layout.getChildren().addAll(typeLabel, typeComboBox);

        Label priceLabel = new Label("Price per MWh (R):");
        TextField priceField = new TextField("0.0");
        priceField.setPromptText("Enter price per MWh");
        layout.getChildren().addAll(priceLabel, priceField);

        Label emissionsOffsetLabel = new Label("Carbon Emissions Offset (Tons):");
        TextField emissionsOffsetField = new TextField("0.0");
        quantityField.setPromptText("Enter carbon emissions offset to buy");
        layout.getChildren().addAll(emissionsOffsetLabel, emissionsOffsetField);
 
		String selectedFileName = comboBox.getSelectionModel().getSelectedItem();
	    
        sendCommand("GET_QUEUE_CERTIFICATE");
        
        String QUEUE_FILE_REC_FROM_SERVER = readResponse();
         
        Button processQueueButton = new Button("Place Order Onto Queue");
        processQueueButton.setOnAction(e -> 
        {
            int certificateID = getMaxTransactionIdCertificates() + 1;       	
        	String transactionType = "Buy";        	
        	String renewableEnergyType = "Solar";       	
        	Double pricePerMwh = Double.parseDouble(priceField.getText());           
            Double carbonEmissionsOffset = Double.parseDouble(emissionsOffsetField.getText());       
    		Double purchasePrice = pricePerMwh * carbonEmissionsOffset;    	     	    
    		String selectedFileNameToRetrieve = comboBox.getSelectionModel().getSelectedItem(); 	    
    	    String dateTimeBuy = "Date";
            
            certificatePurchased = new CertificateData(certificateID, transactionType, renewableEnergyType, pricePerMwh, carbonEmissionsOffset, purchasePrice, selectedFileNameToRetrieve, dateTimeBuy);
        	
            rebuildQueueCertificate(new File(QUEUE_FILE_REC_FROM_SERVER));
            blockchainHandlerCertificate.addToQueueCertificates(certificatePurchased, "Market", companyName);
            updateQueueCertificate(QUEUE_FILE_REC_FROM_SERVER, certificateDataQueue);
        	
            currentTransactionProcessingCertificate = new Transaction<>(companyName, "Market", certificatePurchased);
            
            int queuePosition = getQueuePosition(QUEUE_FILE_REC_FROM_SERVER);

	        Alert alert = new Alert(Alert.AlertType.INFORMATION);
	        alert.setTitle("Order Processed On Queue");
	        
	        alert.setHeaderText("Successfully Added To Queue");
	        alert.setContentText("Your Order Has Now Been Processed And Is Ready To Be Sent To The Blockchain: Click Buy Certificate To Confirm Order");
	        alert.showAndWait();
        	
        });
        
        layout.getChildren().add(processQueueButton);

        Button buyButton = new Button("Buy Certificate");
        buyButton.getStyleClass().add("button"); //Apply the CSS class
        
        Button cancelOrderButton = new Button("Cancel Order");
        cancelOrderButton.setOnAction(e -> 
        {                   	
              sendCommand("BUY_CERTIFICATE " + selectedFileName);
              File purchasedFile = receiveFile();
            
        	  try 
              {
                  dequeueFromQueueCertificate(QUEUE_FILE_REC_FROM_SERVER, certificateID);
              } 
              catch(IOException e1) 
              {
                  e1.printStackTrace();
              }

              updateQueueCertificate(QUEUE_FILE_REC_FROM_SERVER, certificateDataQueue);
        	
              Alert alert = new Alert(Alert.AlertType.INFORMATION);
              alert.setTitle("Alert");
              alert.setHeaderText("Order Details");
                
              String content = String.format("Order cancelled");
              alert.setContentText(content);
              alert.showAndWait();
              switchToDashboardMenu();
        });
        layout.getChildren().add(cancelOrderButton);

        buyButton.setOnAction(e -> 
        {   
        	//Block validation
        	if(blockchainDatabase.companyNameExists(companyName) 
                    && currentTransactionProcessingCertificate != null)              
            {            
	            registerStakeholder();

                blockchainHandlerCertificate.addCertificateData(certificatePurchased, "Market", companyName);

	            String selectedFileNameToRetrieve = comboBox.getSelectionModel().getSelectedItem();
	            Path pathToFile = Paths.get("src/acsse/csc03a3/database/soldCertificates/" + selectedFileNameToRetrieve);
	            
	            String newFileName = "purchased_" + selectedFileNameToRetrieve;
	            Path newPath = Paths.get("src/acsse/csc03a3/database/purchasedCertificates/" + newFileName);

	            try 
	            {
	                Files.copy(pathToFile, newPath, StandardCopyOption.REPLACE_EXISTING);
	                System.out.println("File saved with new name: " + newFileName);
	            } 
	            catch(IOException e1) 
	            {
	                e1.printStackTrace();
	            }
	          
	            Double pricePerMwh = Double.parseDouble(priceField.getText());           
	            Double carbonEmissionsOffset = Double.parseDouble(emissionsOffsetField.getText());
	            //Show invoice alert
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Invoice");
                alert.setHeaderText("Transaction Details");
                String content = String.format("Purchase price per mWh:R %.2f\nQuantity purchased: %.2f mWh\nAvailable carbon emissions offset balance: %.2f Tons\n\nBlockchain updated successfully.", pricePerMwh, carbonEmissionsOffset, totalCarbonEmissionsOffset);
                alert.setContentText(content);
                alert.showAndWait();
                blockchainHandlerCertificate.getBlockchainAsString();
                
                totalCarbonEmissionsOffset += carbonEmissionsOffset;
               
	            updateQueueCertificate(QUEUE_FILE_REC_FROM_SERVER, certificateDataQueue);              
            }
        	//Block not validated
        	else if(!blockchainDatabase.companyNameExists(companyName))
            {
                 Alert alert = new Alert(Alert.AlertType.INFORMATION);
                 alert.setTitle("Alert");
                 alert.setHeaderText("Block Not Validated");
                 
                 String content = String.format("Block Cannot Be Validated and Vetted Since Not Registered Solar Trader");
                 alert.setContentText(content);
                 alert.showAndWait();
            }           
            else
            {
            	 int queueIndex = getQueuePosition(QUEUE_FILE_REC_FROM_SERVER);
            	
            	 Alert alert = new Alert(Alert.AlertType.INFORMATION);
                 alert.setTitle("Alert");
                 alert.setHeaderText("Order Details");
                 
                 String content = String.format("Please Wait And Try Again Later, You Are Still In The Queue");
                 alert.setContentText(content);
                 alert.showAndWait();
            } 
            
        });
        layout.getChildren().add(buyButton);

        pageContent.getChildren().addAll(new Label("Buy Certificates Page"), layout);
        root.setCenter(pageContent);
    }

    /*
     * Method to receive file from server
     */
    public File receiveFile() 
    {
    	try 
    	{
    		String response = bufferedReader.readLine();
    		
            if(response.startsWith("200")) 
            {
                int fileSize = Integer.parseInt(response.split(" ")[1]);
                File file = new File("downloaded_certificate.pdf");
                
                try(FileOutputStream fos = new FileOutputStream(file);
                     BufferedOutputStream bos = new BufferedOutputStream(fos)) 
                {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    
                    while(fileSize > 0 && (bytesRead = dataInputStream.read(buffer, 0, Math.min(buffer.length, fileSize))) != -1) 
                    {
                        bos.write(buffer, 0, bytesRead);
                        fileSize -= bytesRead;
                    }
                }
                return file;
            }
        } 
    	catch(IOException e) 
    	{
            e.printStackTrace();
        }
        return null;
    }

    private void solarTransactionPage() throws IOException 
    {
    	VBox pageContent = new VBox(10);
        pageContent.setPadding(new Insets(10));

        //Create the TableView for transactions
        TableView<Transaction<SolarEnergyData>> table = new TableView<>();
        table.setEditable(true);

        //Define columns for transaction details
        TableColumn<Transaction<SolarEnergyData>, String> senderCol = new TableColumn<>("Sender");
        senderCol.setCellValueFactory(new PropertyValueFactory<>("sender"));

        TableColumn<Transaction<SolarEnergyData>, String> receiverCol = new TableColumn<>("Receiver");
        receiverCol.setCellValueFactory(new PropertyValueFactory<>("receiver"));

        TableColumn<Transaction<SolarEnergyData>, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getData().getSolarEnergyID()));

        TableColumn<Transaction<SolarEnergyData>, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getData().getTransactionType()));

        TableColumn<Transaction<SolarEnergyData>, Double> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getData().getQuantitySolarEnergyTraded()));

        TableColumn<Transaction<SolarEnergyData>, Double> priceCol = new TableColumn<>("Price/kWh");
        priceCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getData().getPriceKwH()));

        TableColumn<Transaction<SolarEnergyData>, Double> totalCol = new TableColumn<>("Total Price");
        totalCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getData().getTotalPriceTraded()));

        TableColumn<Transaction<SolarEnergyData>, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getData().getDateTime()));

        //Add columns to table
        table.getColumns().addAll(senderCol, receiverCol, idCol, typeCol, quantityCol, priceCol, totalCol, dateCol);
        
        //Send command to server to retrieve solar energy transactions
        sendCommand("GET_SOLAR_TRANSACTIONS");
  	  	
        //Populate table with solar energy transactions retrieved from server
  	  	FILE_REC_FROM_SERVER = readResponse();
		ObservableList<Transaction<SolarEnergyData>> transactions = FXCollections.observableArrayList(readTransactions(new File(FILE_REC_FROM_SERVER)));
	  	
		table.setItems(transactions);
		table.refresh();
	
        //Search Field and Button
        TextField searchField = new TextField();
        searchField.setPromptText("Enter buyer or seller to search");
        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> searchTransactions(table, searchField.getText()));

        HBox searchBox = new HBox(10, searchField, searchButton);
        searchBox.setAlignment(Pos.CENTER);

        pageContent.getChildren().addAll(new Label("Solar Energy Transactions"), table, searchBox);
        root.setCenter(pageContent);
    }
    
    private void searchTransactions(TableView<Transaction<SolarEnergyData>> table, String searchTerm) 
    {
        String searchLower = searchTerm.toLowerCase(); 
        boolean found = false;
        
        for(Transaction<SolarEnergyData> transaction : table.getItems()) 
        {
            //Check whether the sender or receiver contains the search term
            if(transaction.getSender().toLowerCase().contains(searchLower) ||
                transaction.getReceiver().toLowerCase().contains(searchLower)) 
            {
                showSearchAlert("Transaction Found", "Details: " + transactionDetailsToString(transaction));
                found = true;
                break;
            }
        }
        if(!found) 
        {
            showSearchAlert("Search Result", "No transaction found with buyer or seller name: " + searchTerm);
        }
    }
    
    private void searchBlockchain(TableView<Block<SolarEnergyData>> table, String searchTerm) 
    {
        String searchLower = searchTerm.toLowerCase(); 
        boolean found = false;
        
        for(Block<SolarEnergyData> block : table.getItems()) 
        {
            //Check whether the sender or receiver contains the search term
            if(block.getHash().toLowerCase().contains(searchLower)) 
            {
                showSearchAlert("Block Found", "Details: " + blockDetailsToString(block));
                found = true;
                break;
            }
        }
        if(!found) 
        {
            showSearchAlert("Search Result", "No block found with hash: " + searchTerm);
        }
    }

    private String transactionDetailsToString(Transaction<SolarEnergyData> transaction) 
    {
    	return String.format("Sender: %s\nReceiver: %s\nID: %d\nType: %s\nQuantity: %.2f\nPrice/kWh: %.2f\nTotal Price: %.2f\nDate & Time: %s",
                             transaction.getSender(), transaction.getReceiver(),
                             transaction.getData().getSolarEnergyID(), transaction.getData().getTransactionType(),
                             transaction.getData().getQuantitySolarEnergyTraded(), transaction.getData().getPriceKwH(),
                             transaction.getData().getTotalPriceTraded(), transaction.getData().getDateTime());
    }
    
    private String blockDetailsToString(Block<SolarEnergyData> block) 
    {
    	return String.format("Hash: %s\nPreviousHash: %s\nNonce: %s\n",
                             block.getHash(), block.getPreviousHash(), block.getNonce());
    }
    
    private void showSearchAlert(String title, String content) 
    {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
 
    private void solarBlockchainPage() {
        VBox pageContent = new VBox(10);
        pageContent.setPadding(new Insets(10));

        TableView<Block<SolarEnergyData>> blockchainTable = new TableView<>();
        TableColumn<Block<SolarEnergyData>, String> prevHashCol = new TableColumn<>("Previous Hash");
        prevHashCol.setCellValueFactory(new PropertyValueFactory<>("previousHash"));

        TableColumn<Block<SolarEnergyData>, String> transactionsCol = new TableColumn<>("Transactions");
        transactionsCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTransactions().toString()));

        TableColumn<Block<SolarEnergyData>, String> hashCol = new TableColumn<>("Hash");
        hashCol.setCellValueFactory(new PropertyValueFactory<>("hash"));

        TableColumn<Block<SolarEnergyData>, Integer> nonceCol = new TableColumn<>("Nonce");
        nonceCol.setCellValueFactory(cellData -> new SimpleObjectProperty<Integer>((int) cellData.getValue().getNonce()));

        blockchainTable.getColumns().addAll(prevHashCol, transactionsCol, hashCol, nonceCol);

        sendCommand("GET_SOLAR_BLOCKCHAIN");

        FILE_REC_FROM_SERVER = readResponse();
        ObservableList<Block<SolarEnergyData>> blocks = FXCollections.observableArrayList(readBlockchain(new File(FILE_REC_FROM_SERVER)));
        blockchainTable.setItems(blocks);
        blockchainTable.refresh();
        
        //Search Field and Button
        TextField searchField = new TextField();
        searchField.setPromptText("Enter hash to search");
        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> searchBlockchain(blockchainTable, searchField.getText()));

        HBox searchBox = new HBox(10, searchField, searchButton);
        searchBox.setAlignment(Pos.CENTER);

        pageContent.getChildren().addAll(new Label("Solar Energy Blockchain"), blockchainTable, searchBox);
        root.setCenter(pageContent);
    }
    
    public static Transaction<SolarEnergyData> parseTransactions(String transactionLine) 
    {
        String transactionRegex = "Transaction\\{sender='(.*?)', receiver='(.*?)', data=SolarEnergyData\\{ID: (\\d+), Transaction Type: (.*?), Quantity Solar Energy Traded: (.*?), Price Per KwH: (.*?), Total Price Traded: (.*?), Date and Time: (.*?)\\}, timestamp=(\\d+)\\}";
        Pattern transactionPattern = Pattern.compile(transactionRegex);
        Matcher transactionMatcher = transactionPattern.matcher(transactionLine);

        if(transactionMatcher.find()) 
        {
            String sender = transactionMatcher.group(1);
            String receiver = transactionMatcher.group(2);
            int ID = Integer.parseInt(transactionMatcher.group(3));
            String transactionType = transactionMatcher.group(4);
            double quantityTraded = Double.parseDouble(transactionMatcher.group(5));
            double pricePerKwH = Double.parseDouble(transactionMatcher.group(6));
            double totalPrice = Double.parseDouble(transactionMatcher.group(7));
            String dateTime = transactionMatcher.group(8);
            long timestamp = Long.parseLong(transactionMatcher.group(9));

            SolarEnergyData solarEnergyData = new SolarEnergyData(ID, transactionType, quantityTraded, pricePerKwH, totalPrice, dateTime);
            return new Transaction<>(sender, receiver, solarEnergyData);
        }
        
        return null;
    }

    public static ArrayList<Transaction<SolarEnergyData>> readTransactions(File file) 
    {
        ArrayList<Transaction<SolarEnergyData>> transactions = new ArrayList<>();

        try(Scanner scanner = new Scanner(file)) 
        {
            while(scanner.hasNextLine()) 
            {
                String line = scanner.nextLine();
                Transaction<SolarEnergyData> transaction = parseTransactions(line);
                if(transaction != null) 
                {
                    transactions.add(transaction);
                }
            }
            System.out.println(blockchainHandler.getBlockchainAsString());
        } 
        catch(FileNotFoundException e) 
        {
            System.err.println("File not found: " + file);
        }catch(Exception e) 
        {
            e.printStackTrace();
        }

        return transactions;
    }

    private Block<SolarEnergyData> parseBlockchain(String blockString) 
    {
        String blockRegex = "Block\\{previousHash='([^']+)', transactions=\\[(.*?)\\], hash='([^']+)', nonce=(-?\\d+)\\}";

        Pattern blockPattern = Pattern.compile(blockRegex);
        Matcher matcher = blockPattern.matcher(blockString);

        if(matcher.find()) 
        {
            String previousHash = matcher.group(1);
            String transactionsString = matcher.group(2);
            String hash = matcher.group(3);
            int nonce = Integer.parseInt(matcher.group(4));

            List<Transaction<SolarEnergyData>> transactions = new ArrayList<>();
            
            if(!transactionsString.isEmpty() && !transactionsString.equals("[]")) 
            {
                String[] transactionEntries = transactionsString.split("Transaction\\{");
                
                for(String entry : transactionEntries) 
                {
                    if(!entry.isEmpty()) 
                    {
                        entry = "Transaction{" + entry;
                        if(entry.endsWith(","))
                    	{
                    		entry = entry.substring(0, entry.length() - 1);
                    	}
                        
                        Transaction<SolarEnergyData> transaction = parseTransactions(entry.trim());
                        
                        if(transaction != null) 
                        {
                            transactions.add(transaction);
                        }
                    }
                }
            }

            Block<SolarEnergyData> block = new Block<>(previousHash, transactions);
            
            //Explicitly set the nonce of the block
            block.setNonce(nonce);
            
            return block;
        }
        
        return null;
    }

    private List<Block<SolarEnergyData>> readBlockchain(File file) 
    {
        List<Block<SolarEnergyData>> blocks = new ArrayList<>();
        try(Scanner scanner = new Scanner(file)) 
        {
            while(scanner.hasNextLine()) 
            {
                String line = scanner.nextLine();
                Block<SolarEnergyData> block = parseBlockchain(line);
                
                if(block != null) 
                {
                    blocks.add(block);
                }
            }
        }
        catch(FileNotFoundException e) 
        {
            System.err.println("File not found: " + file);
            e.printStackTrace();
        } 
        catch(Exception e) 
        {
            System.err.println("Error reading blockchain data: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println(blocks); 
        
        return blocks;
    }

    private void solarBlockchainVisualiserPage() 
    {
        VBox pageContent = new VBox(10);
        pageContent.setPadding(new Insets(10));

        //Create the graph using GraphStream
        org.graphstream.graph.Graph graph = createSolarGraphFromBlockchain();

        //Set up GraphStream viewer
        FxViewer viewer = new FxViewer(graph, FxViewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        viewer.enableAutoLayout();
        View view = viewer.addDefaultView(false);

        //Create a JavaFX container and add the GraphStream view to it
        StackPane graphPane = new StackPane();
        graphPane.getChildren().add((javafx.scene.Node) view);

        pageContent.getChildren().add(graphPane);
        root.setCenter(pageContent);
    }

    private org.graphstream.graph.Graph createSolarGraphFromBlockchain() 
    {
        org.graphstream.graph.Graph graph = new SingleGraph("BlockchainGraph");

        graph.setAttribute("ui.stylesheet", "node { fill-color: lightgreen; size: 20px; text-alignment: under; text-background-mode: plain; text-background-color: white; text-size: 12; } edge { fill-color: lightgreen; size: 2px; arrow-shape: arrow; arrow-size: 10px, 5px; }");

        sendCommand("GET_SOLAR_BLOCKCHAIN");
        
        FILE_REC_FROM_SERVER = readResponse();
        List<Block<SolarEnergyData>> blocks = readBlockchain(new File(FILE_REC_FROM_SERVER));

        int gap = 10;

        for(int i = 0; i < blocks.size(); i++) 
        {
            Block<SolarEnergyData> block = blocks.get(i);
            String nodeId = block.getHash();
            String previousNodeId = block.getPreviousHash();

            //Add current block node
            if(graph.getNode(nodeId) == null) 
            {
                org.graphstream.graph.Node node = graph.addNode(nodeId);
                node.setAttribute("ui.label", nodeId);
                //Set the position of the node in a straight line along the x-axis
                node.setAttribute("xyz", i * gap, 0, 0);
            }

            //Add previous block node if does not exist
            if (!previousNodeId.isEmpty() && graph.getNode(previousNodeId) == null) 
            {
                org.graphstream.graph.Node prevNode = graph.addNode(previousNodeId);
                prevNode.setAttribute("ui.label", previousNodeId);
            }

            //Add edge between previous block and current block
            if(!previousNodeId.isEmpty()) 
            {
                try 
                {
                    graph.addEdge(previousNodeId + "-" + nodeId, previousNodeId, nodeId, true)
                         .setAttribute("ui.style", "arrow-shape: arrow; arrow-size: 10px, 5px; fill-color: lightgreen;");
                } 
                catch(ElementNotFoundException e) 
                {
                    System.err.println("Error adding edge between nodes: " + previousNodeId + " and " + nodeId);
                }
            }
        }

        return graph;
    }


    
    /*
     * Loads the transactions history page
     */
    private void certificatesTransactionPage() 
    {
    	VBox pageContent = new VBox(10);
        pageContent.setPadding(new Insets(10));

        //Create the TableView for transactions
        TableView<Transaction<CertificateData>> table = new TableView<>();
        table.setEditable(true);

        //Define columns for transaction details
        TableColumn<Transaction<CertificateData>, String> senderCol = new TableColumn<>("Sender");
        senderCol.setCellValueFactory(new PropertyValueFactory<>("sender"));

        TableColumn<Transaction<CertificateData>, String> receiverCol = new TableColumn<>("Receiver");
        receiverCol.setCellValueFactory(new PropertyValueFactory<>("receiver"));

        //Assuming the SolarEnergyData has these fields
        TableColumn<Transaction<CertificateData>, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getData().getCertificateID()));

        TableColumn<Transaction<CertificateData>, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getData().getTransactionType()));

        TableColumn<Transaction<CertificateData>, String> renewableEnergyTypeCol = new TableColumn<>("Renewable Energy Type");
        renewableEnergyTypeCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getData().getRenewableEnergyType()));
        
        TableColumn<Transaction<CertificateData>, Double> priceCol = new TableColumn<>("Price/MWh");
        priceCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getData().getPriceMwH()));
        
        TableColumn<Transaction<CertificateData>, Double> emissionsOffsetCol = new TableColumn<>("Carbon Emissions Offset");
        emissionsOffsetCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getData().getCarbonEmissionsOffset()));
        
        TableColumn<Transaction<CertificateData>, Double> totalPriceTradedCol = new TableColumn<>("Total Price Traded");
        totalPriceTradedCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getData().getTotalPriceTraded()));
        
        TableColumn<Transaction<CertificateData>, String> certificateFileCol = new TableColumn<>("Certificate File");
        certificateFileCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getData().getCertificateFile()));

        TableColumn<Transaction<CertificateData>, String> dateCol = new TableColumn<>("Date & Time");
        dateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getData().getDateTime()));

        //Add columns to table
        table.getColumns().addAll(senderCol, receiverCol, idCol, typeCol, renewableEnergyTypeCol, priceCol, emissionsOffsetCol, totalPriceTradedCol, certificateFileCol, dateCol);

        sendCommand("GET_CERTIFICATE_TRANSACTIONS");
  	  	
		FILE_REC_FROM_SERVER = readResponse();
        ObservableList<Transaction<CertificateData>> transactions = FXCollections.observableArrayList(readTransactionsCertificates(new File(FILE_REC_FROM_SERVER)));
        table.setItems(transactions);
        table.refresh();
        
        //Search Field and Button
        TextField searchField = new TextField();
        searchField.setPromptText("Enter buyer or seller to search");
        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> searchTransactionsCertificates(table, searchField.getText()));

        HBox searchBox = new HBox(10, searchField, searchButton);
        searchBox.setAlignment(Pos.CENTER);
        
        pageContent.getChildren().addAll(new Label("Renewable Energy Certificates Blockchain"), table, searchBox);
        root.setCenter(pageContent);
    }
    
    private void searchTransactionsCertificates(TableView<Transaction<CertificateData>> table, String searchTerm) 
    {
        String searchLower = searchTerm.toLowerCase(); 
        boolean found = false;
        
        for(Transaction<CertificateData> transaction : table.getItems()) 
        {
            //Check whether the sender or receiver contains the search term
            if(transaction.getSender().toLowerCase().contains(searchLower) ||
                transaction.getReceiver().toLowerCase().contains(searchLower)) 
            {
                showSearchAlert("Transaction Found", "Details: " + transactionDetailsToStringCertificates(transaction));
                found = true;
                break;
            }
        }
        if(!found) 
        {
            showSearchAlert("Search Result", "No transaction found with buyer or seller name: " + searchTerm);
        }
    }
    
    private String transactionDetailsToStringCertificates(Transaction<CertificateData> transaction) 
    {
    	return String.format("Sender: %s\nReceiver: %s\nID: %d\nType: %s\nRenewable Energy Type: %s\nPrice per MWh: %.2f\nCarbon Emissions Offset: %.2f\nTotal Price: %.2f\nCertificate File: %s\nDate & Time: %s",
                transaction.getSender(), 
                transaction.getReceiver(),
                transaction.getData().getCertificateID(), 
                transaction.getData().getTransactionType(),
                transaction.getData().getRenewableEnergyType(), 
                transaction.getData().getPriceMwH(),
                transaction.getData().getCarbonEmissionsOffset(), 
                transaction.getData().getTotalPriceTraded(), 
                transaction.getData().getCertificateFile(), 
                transaction.getData().getDateTime());
    }

    private void certificatesBlockchainPage() 
    {
        VBox pageContent = new VBox(10);
        pageContent.setPadding(new Insets(10));

        TableView<Block<CertificateData>> blockchainTable = new TableView<>();
        TableColumn<Block<CertificateData>, String> prevHashCol = new TableColumn<>("Previous Hash");
        prevHashCol.setCellValueFactory(new PropertyValueFactory<>("previousHash"));

        TableColumn<Block<CertificateData>, String> transactionsCol = new TableColumn<>("Transactions");
        transactionsCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTransactions().toString()));

        TableColumn<Block<CertificateData>, String> hashCol = new TableColumn<>("Hash");
        hashCol.setCellValueFactory(new PropertyValueFactory<>("hash"));

        TableColumn<Block<CertificateData>, Integer> nonceCol = new TableColumn<>("Nonce");
        nonceCol.setCellValueFactory(cellData -> new SimpleObjectProperty<Integer>((int) cellData.getValue().getNonce()));

        blockchainTable.getColumns().addAll(prevHashCol, transactionsCol, hashCol, nonceCol);

        sendCommand("GET_CERTIFICATE_BLOCKCHAIN");
        FILE_REC_FROM_SERVER = readResponse();
        
        ObservableList<Block<CertificateData>> blocks = FXCollections.observableArrayList(readBlockchainCertificates(new File(FILE_REC_FROM_SERVER)));
        blockchainTable.setItems(blocks);
        blockchainTable.refresh();
        
        //Search Field and Button
        TextField searchField = new TextField();
        searchField.setPromptText("Enter hash to search");
        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> searchBlockchainCertificates(blockchainTable, searchField.getText()));

        HBox searchBox = new HBox(10, searchField, searchButton);
        searchBox.setAlignment(Pos.CENTER);

        pageContent.getChildren().addAll(new Label("Solar Energy Transactions"), blockchainTable, searchBox);
        root.setCenter(pageContent);
    }
    
    private void searchBlockchainCertificates(TableView<Block<CertificateData>> table, String searchTerm) 
    {
        String searchLower = searchTerm.toLowerCase(); 
        boolean found = false;
        
        for(Block<CertificateData> block : table.getItems()) 
        {
            //Check whether the sender or receiver contains the search term
            if(block.getHash().toLowerCase().contains(searchLower)) 
            {
                showSearchAlert("Block Found", "Details: " + blockDetailsToStringCertificates(block));
                found = true;
                break;
            }
        }
        if(!found) 
        {
            showSearchAlert("Search Result", "No block found with hash: " + searchTerm);
        }
    }
    
    private String blockDetailsToStringCertificates(Block<CertificateData> block) 
    {
    	return String.format("Hash: %s\nPreviousHash: %s\nNonce: %s\n",
                             block.getHash(), block.getPreviousHash(), block.getNonce());
    }
   
    public static Transaction<CertificateData> parseTransactionsCertificates(String transactionLine) 
    {
        String transactionRegex = "Transaction\\{sender='(.*?)', receiver='(.*?)', data=CertificateData\\{CID: (\\d+), Transaction Type: (.*?), Renewable Energy Type: (.*?), Price Per MwH: (.*?), Carbon Emissions Offset: (.*?), Total Price Traded: (.*?), Certificate File: (.*?), Date and Time: (.*?)\\}, timestamp=(\\d+)\\}";
        Pattern transactionPattern = Pattern.compile(transactionRegex);
        Matcher transactionMatcher = transactionPattern.matcher(transactionLine);

        if(transactionMatcher.find()) 
        {
            String sender = transactionMatcher.group(1);
            String receiver = transactionMatcher.group(2);
            int ID = Integer.parseInt(transactionMatcher.group(3));
            String transactionType = transactionMatcher.group(4);
            String renewableEnergyType = transactionMatcher.group(5);
            double pricePerMwH = Double.parseDouble(transactionMatcher.group(6));
            double emissionsOffset = Double.parseDouble(transactionMatcher.group(7));
            double totalPriceTraded = Double.parseDouble(transactionMatcher.group(8));
            String certificateFile = transactionMatcher.group(9);
            String dateTime = transactionMatcher.group(10);
            long timestamp = Long.parseLong(transactionMatcher.group(11));

            CertificateData certificateData = new CertificateData(ID, transactionType, renewableEnergyType, pricePerMwH, emissionsOffset, totalPriceTraded, certificateFile, dateTime);
            return new Transaction<>(sender, receiver, certificateData);
        }
        
        return null;
    }

    public static ArrayList<Transaction<CertificateData>> readTransactionsCertificates(File file) 
    {
        ArrayList<Transaction<CertificateData>> transactions = new ArrayList<>();

        try(Scanner scanner = new Scanner(file)) 
        {
            while(scanner.hasNextLine()) 
            {
                String line = scanner.nextLine();
                Transaction<CertificateData> transaction = parseTransactionsCertificates(line);
                
                if(transaction != null) 
                {
                    transactions.add(transaction);
                }
            }
            
            System.out.println(blockchainHandlerCertificate.getBlockchainAsString());
        } 
        catch(FileNotFoundException e) 
        {
            System.err.println("File not found: " + file);
        }catch(Exception e) 
        {
            e.printStackTrace();
        }

        return transactions;
    }

    private Block<CertificateData> parseBlockchainCertificates(String blockString) 
    {
        String blockRegex = "Block\\{previousHash='([^']+)', transactions=\\[(.*?)\\], hash='([^']+)', nonce=(-?\\d+)\\}";

        Pattern blockPattern = Pattern.compile(blockRegex);
        Matcher matcher = blockPattern.matcher(blockString);

        if(matcher.find()) 
        {
            String previousHash = matcher.group(1);
            String transactionsString = matcher.group(2);
            String hash = matcher.group(3);
            int nonce = Integer.parseInt(matcher.group(4));

            List<Transaction<CertificateData>> transactions = new ArrayList<>();
            if(!transactionsString.isEmpty() && !transactionsString.equals("[]")) 
            {             
                String[] transactionEntries = transactionsString.split("Transaction\\{");
                
                for(String entry : transactionEntries) 
                {
                    if(!entry.isEmpty()) 
                    {
                        entry = "Transaction{" + entry;
                        
                        if(entry.endsWith(","))
                    	{
                    		entry = entry.substring(0, entry.length() - 1);                    	
                    	}
                        
                        Transaction<CertificateData> transaction = parseTransactionsCertificates(entry.trim());
                        
                        if(transaction != null) 
                        {
                            transactions.add(transaction);
                        }
                    }
                }
            }

            Block<CertificateData> block = new Block<>(previousHash, transactions);
            
            //Explicitly set the nonce of the block
            block.setNonce(nonce); 
            
            return block;
        }
        return null;
    }

    private List<Block<CertificateData>> readBlockchainCertificates(File file) 
    {
        List<Block<CertificateData>> blocks = new ArrayList<>();
        
        try(Scanner scanner = new Scanner(file)) 
        {
            while(scanner.hasNextLine()) 
            {
                String line = scanner.nextLine();
                
                Block<CertificateData> block = parseBlockchainCertificates(line);
                
                if(block != null) 
                {
                    blocks.add(block);
                }
            }
        } 
        catch(FileNotFoundException e) 
        {
            System.err.println("File not found: " + file);
            e.printStackTrace();
        }catch(Exception e) 
        {
            System.err.println("Error reading blockchain data: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println(blocks);  
        return blocks;
    }

    private void certificatesBlockchainVisualiserPage() 
    {
        VBox pageContent = new VBox(10);
        pageContent.setPadding(new Insets(10));

        //Create the graph using GraphStream
        org.graphstream.graph.Graph graph = createCertificatesGraphFromBlockchain();

        //Set up GraphStream viewer
        FxViewer viewer = new FxViewer(graph, FxViewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        viewer.enableAutoLayout();
        View view = viewer.addDefaultView(false);

        //Create a JavaFX container and add the GraphStream view to it
        StackPane graphPane = new StackPane();
        graphPane.getChildren().add((javafx.scene.Node) view);

        pageContent.getChildren().add(graphPane);
        root.setCenter(pageContent);
    }

    private org.graphstream.graph.Graph createCertificatesGraphFromBlockchain() 
    {
        org.graphstream.graph.Graph graph = new SingleGraph("BlockchainGraph");

        graph.setAttribute("ui.stylesheet", "node { fill-color: lightgreen; size: 20px; text-alignment: under; text-background-mode: plain; text-background-color: white; text-size: 12; } edge { fill-color: lightgreen; size: 2px; arrow-shape: arrow; arrow-size: 10px, 5px; }");

        sendCommand("GET_CERTIFICATE_BLOCKCHAIN");
        
        FILE_REC_FROM_SERVER = readResponse();
        List<Block<CertificateData>> blocks = readBlockchainCertificates(new File(FILE_REC_FROM_SERVER));

        int gap = 10;

        for(int i = 0; i < blocks.size(); i++) 
        {
            Block<CertificateData> block = blocks.get(i);
            String nodeId = block.getHash();
            String previousNodeId = block.getPreviousHash();

            //Add current block node
            if(graph.getNode(nodeId) == null) 
            {
                org.graphstream.graph.Node node = graph.addNode(nodeId);
                node.setAttribute("ui.label", nodeId);
                //Set the position of the node in a straight line along the x-axis
                node.setAttribute("xyz", i * gap, 0, 0);
            }

            //Add previous block node if does not exist
            if(!previousNodeId.isEmpty() && graph.getNode(previousNodeId) == null) 
            {
                org.graphstream.graph.Node prevNode = graph.addNode(previousNodeId);
                prevNode.setAttribute("ui.label", previousNodeId);
            }

            //Add edge between previous block and current block
            if(!previousNodeId.isEmpty())
            {
                try 
                {
                    graph.addEdge(previousNodeId + "-" + nodeId, previousNodeId, nodeId, true)
                         .setAttribute("ui.style", "arrow-shape: arrow; arrow-size: 10px, 5px; fill-color: lightgreen;");
                } 
                catch(ElementNotFoundException e) 
                {
                    System.err.println("Error adding edge between nodes: " + previousNodeId + " and " + nodeId);
                }
            }
        }

        return graph;
    }
    
    public void solarTransactionsReportingPage() 
    {
        VBox pageContent = new VBox(10);
        pageContent.setPadding(new Insets(10));
        
        sendCommand("GET_SOLAR_TRANSACTIONS");

		FILE_REC_FROM_SERVER = readResponse();
		System.out.println(FILE_REC_FROM_SERVER);
		ArrayList<Transaction<SolarEnergyData>> transactions = readTransactions(new File(FILE_REC_FROM_SERVER));

        //Set up the bar chart
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Year");  // Adjust label as needed

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Number of Transactions");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

        //Load data into the bar chart
        try(Stream<String> stream = Files.lines(Paths.get(FILE_REC_FROM_SERVER))) 
        {
            long numberOfTransactions = stream.count();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.getData().add(new XYChart.Data<>("2024", numberOfTransactions));
            barChart.getData().add(series);
      
            barChart.setTitle("Transactions per Year: " + numberOfTransactions);
        } 
        catch(IOException e) 
        {
            e.printStackTrace();
        }
       
        pageContent.getChildren().addAll(new Label("Solar Energy Transactions Reporting"), barChart);
        root.setCenter(pageContent); 
    }
    
    public void solarBlockchainReportingPage() 
    {
        VBox pageContent = new VBox(10);
        pageContent.setPadding(new Insets(10));
        
        sendCommand("GET_SOLAR_BLOCKCHAIN");
    	
        FILE_REC_FROM_SERVER = readResponse();
        List<Block<SolarEnergyData>> block = readBlockchain(new File(FILE_REC_FROM_SERVER));

        //Set up the bar chart
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Year");  // Adjust label as needed

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Number of Blocks");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        
        //Load data into the bar chart
        try(Stream<String> stream = Files.lines(Paths.get(FILE_REC_FROM_SERVER))) 
        {
            long numberOfBlocks = stream.count();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.getData().add(new XYChart.Data<>("2024", numberOfBlocks));
            barChart.getData().add(series);
                     
            barChart.setTitle("Transactions per Year: " + numberOfBlocks);
        } 
        catch(IOException e) 
        {
            e.printStackTrace();
        }
       
        pageContent.getChildren().addAll(new Label("Solar Energy Blockchain Reporting"), barChart);
        root.setCenter(pageContent);  // Assuming 'root' is a BorderPane
    }
    
    public void certificateTransactionsReportingPage() 
    {
        VBox pageContent = new VBox(10);
        pageContent.setPadding(new Insets(10));
        
        sendCommand("GET_CERTIFICATE_TRANSACTIONS");
    	  
		FILE_REC_FROM_SERVER = readResponse();
		System.out.println(FILE_REC_FROM_SERVER);
		ArrayList<Transaction<CertificateData>> transactions = readTransactionsCertificates(new File(FILE_REC_FROM_SERVER));

        //Set up the bar chart
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Year");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Number of Transactions");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        

        //Load data into the bar chart
        try(Stream<String> stream = Files.lines(Paths.get(FILE_REC_FROM_SERVER))) 
        {
            long numberOfTransactions = stream.count();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.getData().add(new XYChart.Data<>("2024", numberOfTransactions));
            barChart.getData().add(series);
  
            barChart.setTitle("Transactions per Year: " + numberOfTransactions);
        } 
        catch(IOException e) 
        {
            e.printStackTrace();
        }
       
        pageContent.getChildren().addAll(new Label("Certificate Transactions Reporting"), barChart);
        root.setCenter(pageContent); 
    }
    
    public void certificateBlockchainReportingPage() 
    {
        VBox pageContent = new VBox(10);
        pageContent.setPadding(new Insets(10));
      
        sendCommand("GET_CERTIFICATE_BLOCKCHAIN");
    	
        FILE_REC_FROM_SERVER = readResponse();
        List<Block<CertificateData>> block = readBlockchainCertificates(new File(FILE_REC_FROM_SERVER));

        //Set up the bar chart
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Year");  // Adjust label as needed

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Number of Blocks");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        
        //Load data into the bar chart
        try (Stream<String> stream = Files.lines(Paths.get(FILE_REC_FROM_SERVER))) 
        {
            long numberOfBlocks = stream.count();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.getData().add(new XYChart.Data<>("2024", numberOfBlocks)); 
            barChart.getData().add(series);
                       
            barChart.setTitle("Transactions per Year: " + numberOfBlocks);
        } 
        catch(IOException e) 
        {
            e.printStackTrace();
        }
       
        pageContent.getChildren().addAll(new Label("Solar Energy Blockchain Reporting"), barChart);
        root.setCenter(pageContent);  
    }
    


	public BorderPane getRoot() 
	{
	    return root;
	}

	/*
	 * Method to enable menu items after sign up or sign in
	 */
	private void enableMenuAfterSignUpOrSignIn() {
	    for (Menu menu : menuBar.getMenus()) {
	        for (MenuItem item : menu.getItems()) {
	            item.setDisable(false);
	        }
	    }
	    
	    //Manually disable Sign Up and Log In after successful sign up or sign in
	    findMenuItem("Sign Up").setDisable(true);
	    findMenuItem("Log In").setDisable(true);
	}


	private MenuItem findMenuItem(String text) 
	{
	    for(Menu menu : menuBar.getMenus()) 
	    {
	        for(MenuItem item : menu.getItems()) 
	        {
	            if(item.getText().equals(text)) 
	            {
	                return item;
	            }
	        }
	    }
	    
	    return null; 
	}
	
	private void switchToConnectSolarPanelsMenu() 
	{
	    for(Menu menu : menuBar.getMenus()) 
	    {
	        if (menu.getText().equals("Connect Solar Panels")) {
	        	
	            //Iterate over the menu items to find Connect Solar Panels
	            for (MenuItem menuItem : menu.getItems()) 
	            {
	                if(menuItem.getText().equals("Connect Solar Panels Page")) 
	                {
	                    //If the menu item is found
	                    Platform.runLater(() -> menuItem.fire());
	                    break;
	                }
	            }
	            
	            break;
	        }
	    }
	}
	
	private void switchToDashboardMenu() 
	{
	    for(Menu menu : menuBar.getMenus()) 
	    {
	        if(menu.getText().equals("Dashboard")) 
	        {
	            //Iterate over the menu items to find Connect Solar Panels
	            for(MenuItem menuItem : menu.getItems()) 
	            {
	                if(menuItem.getText().equals("Dashboard Page")) 
	                {
	                    //If the menu item is found
	                    Platform.runLater(() -> menuItem.fire());
	                    break;
	                }
	            }
	            
	            break;
	        }
	    }
	}

	private void registerStakeholder() 
	{
		if(!blockchainHandler.hasStakeholders()) 
		{
			blockchainHandler.registerStake("node1", 100);
			System.out.println("Stakeholder 'node1' registered with stake 100.");
	    }
	}
	
	private double parseDoubleSafely(String input) 
	{
	    try 
	    {
	        return Double.parseDouble(input);
	    } 
	    catch(NumberFormatException e) 
	    {
	        System.err.println("Invalid input for parsing as double: " + input);
	        return 0.0;
	    }
	}
	
	private int getMaxTransactionIdSolarEnergy() 
	{
	    int maxId = 0;
	    try 
	    {
	    	sendCommand("GET_SOLAR_TRANSACTIONS");
	
			FILE_REC_FROM_SERVER = readResponse();
	    	
	        File file = new File(FILE_REC_FROM_SERVER);
	        Scanner scanner = new Scanner(file);
	        
	        while(scanner.hasNextLine()) 
	        {
	            String line = scanner.nextLine();
	            
	            if(line.contains("Transaction")) 
	            {
		            Matcher m = Pattern.compile("ID: (\\d+)").matcher(line);
		            
	                if(m.find()) 
	                {
	                    int id = Integer.parseInt(m.group(1));
	                    
	                    if(id > maxId) 
	                    {
	                        maxId = id;
	                    }
	                }
	            }
	        }
	        
	        scanner.close();
	    }catch 
	    (FileNotFoundException e) 
	    {
	        e.printStackTrace();
	    }
	    
	    return maxId;
	}
	
	private int getMaxTransactionIdCertificates() 
	{
	    int maxId = 0;
	    
	    try 
	    {
	    	
	    	sendCommand("GET_CERTIFICATE_TRANSACTIONS");

			FILE_REC_FROM_SERVER = readResponse();
	        File file = new File(FILE_REC_FROM_SERVER);
	        Scanner scanner = new Scanner(file);
	        
	        while(scanner.hasNextLine()) 
	        {
	            String line = scanner.nextLine();
	            
	            if(line.contains("Transaction")) 
	            {
		            Matcher m = Pattern.compile("CID: (\\d+)").matcher(line);
		            
	                if(m.find()) 
	                {
	                    int id = Integer.parseInt(m.group(1));
	                    
	                    if(id > maxId) 
	                    {
	                        maxId = id;
	                    }
	                }
	            }
	        }
	        
	        scanner.close();
	    } 
	    catch(FileNotFoundException e) 
	    {
	        e.printStackTrace();
	    }
	    
	    return maxId;
	}
	
	private void showAlert(String message) 
	{
	    Alert alert = new Alert(Alert.AlertType.INFORMATION);
	    alert.setContentText(message);
	    alert.showAndWait();
	}
}

