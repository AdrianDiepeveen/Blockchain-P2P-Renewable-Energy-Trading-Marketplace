/**
 * This Class Stores Information About SolarEnergyDataAPI. {@link SolarEnergyDataAPI#SolarEnergyDataAPI}
 * @author Mr. A.M. Diepeveen, 221025168
 * @version Computer Science 3A Mini Project
 */

package acsse.csc03a3.api;

import acsse.csc03a3.client.SolarEnergyData;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class SolarEnergyDataAPI 
{

	//API key
    private static final String API_KEY = "4BVlqH4dUzB1ZMe4lPOTaQjmq955AcscjdtFRod3";

    public static SolarEnergyData fetchSolarEnergyData(double systemCapacity, int moduleType, double losses, int arrayType, double tilt, double azimuth, double lat, double lon) 
    {
    	DecimalFormat decimalFormat = new DecimalFormat("#.##");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        
        //Explicitly set decimal separator
        symbols.setDecimalSeparator('.');
        decimalFormat.setDecimalFormatSymbols(symbols);

        String parameters = String.format("api_key=%s&system_capacity=%s&module_type=%d&losses=%s&array_type=%d&tilt=%s&azimuth=%s&lat=%s&lon=%s",
            API_KEY,
            decimalFormat.format(systemCapacity),
            moduleType,
            decimalFormat.format(losses),
            arrayType,
            decimalFormat.format(tilt),
            decimalFormat.format(azimuth),
            decimalFormat.format(lat),
            decimalFormat.format(lon));

        String baseUrl = "https://developer.nrel.gov/api/pvwatts/v6.json?" + parameters;

        try 
        {
            URL url = new URL(baseUrl);
            
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            
            while((inputLine = in.readLine()) != null) 
            {
                content.append(inputLine);
            }
            in.close();

            JSONObject jsonResponse = new JSONObject(content.toString());
            
            double acAnnual = jsonResponse.getJSONObject("outputs").getDouble("ac_annual");
            return new SolarEnergyData("Data ID", acAnnual);

        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            return null;
        }       
    }
}