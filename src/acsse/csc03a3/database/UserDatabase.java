/**
 * This Class Stores Information About UserDatabase. {@link UserDatabase#UserDatabase}
 * @author Mr. A.M. Diepeveen, 221025168
 * @version Computer Science 3A Mini Project
 */

package acsse.csc03a3.database;

import java.io.*;
import java.nio.file.*;
import java.util.List;

public class UserDatabase 
{
    private static final String USER_FILE_PATH = "src/acsse/csc03a3/database/user_details.txt";

    public static void writeUser(String username, String password) 
    {
        String userRecord = username + "," + password + "\n";
        
        try 
        {
            Files.write(Paths.get(USER_FILE_PATH), userRecord.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } 
        catch(IOException e) 
        {
            e.printStackTrace();
        }
    }

    public static boolean validateUser(String username, String password) 
    {
        try 
        {
            List<String> lines = Files.readAllLines(Paths.get(USER_FILE_PATH));
            
            for(String line : lines) 
            {
                String[] credentials = line.split(",");
                
                if(credentials[0].equals(username) && credentials[1].equals(password)) 
                {
                    return true;
                }
                else
                {
                	System.out.println("User does not exist, please try again");
                }
            }
        } 
        catch(ArrayIndexOutOfBoundsException ex)
        {
        	System.out.println("User does not exist in system, please try again");
        }
        
        catch(IOException e) 
        {
            System.out.println("User does not exist");
        }
        
        return false;
    }
}
