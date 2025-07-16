/**
 * This Class Stores Information About SolarEnergyData. {@link SolarEnergyData#SolarEnergyData}
 * @author Mr. A.M. Diepeveen, 221025168
 * @version Computer Science 3A Mini Project
 */

package acsse.csc03a3.client;

import java.io.Serializable;

public class SolarEnergyData implements Serializable
{
	private static final long serialVersionUID = 1L;
	private int solarEnergyID;
	private String transactionType;
	private double quantitySolarEnergyTraded;
	private double priceKwH;
	private double totalPriceTraded;
	private String dateTime;
	private double energyProduced;
	private String location;
	private double energyConsumed;
	private double quantity;

	public SolarEnergyData(int solarEnergyID, String transactionType, double quantitySolarEnergyTraded, double priceKwH, double totalPriceTraded, String dateTime) 
	{
	      this.solarEnergyID = solarEnergyID;
	      this.transactionType = transactionType;
	      this.quantitySolarEnergyTraded = quantitySolarEnergyTraded;
	      this.priceKwH = priceKwH;
	      this.totalPriceTraded = totalPriceTraded;
	      this.dateTime = dateTime;
	}
	  
	public SolarEnergyData(String transactionType, double quantity) 
	{
	  this.transactionType = transactionType;
	  this.quantity = quantity;
	}
  
    /*
     * @return the solarEnergyID
 	 */
	public int getSolarEnergyID() 
	{
		return solarEnergyID;
	}
	
	/*
	 * @return the transactionType
	 */
	public String getTransactionType() 
	{
		return transactionType;
	}
	
	/*
	 * @return the quantitySolarEnergyTraded
	 */
	public double getQuantitySolarEnergyTraded() 
	{
		return quantitySolarEnergyTraded;
	}
	
	/*
	 * @return the priceKwH
	 */
	public double getPriceKwH() 
	{
		return priceKwH;
	}
		
	/*
	 * @return the totalPriceTraded
	 */
	public double getTotalPriceTraded() 
	{
		return totalPriceTraded;
	}
		
	/*
	 * @return the dateTime
	 */
	public String getDateTime() 
	{
		return dateTime;
	}
	
	/*
	 * @return the energyProduced
	 */
	public double getEnergyProduced()
	{
		return energyProduced;
	}
	
	/*
	 * @return the location
	 */
	public String getLocation() 
	{
		return location;
	}
	
	/*
	 * @return the energyConsumed
	 */
	public double getEnergyConsumed() 
	{
		return energyConsumed;
	}

	/*
	 * @return the quantity
	 */
	public double getQuantity() 
	{
		return quantity;
	}
		
	/*
	 * @param solarEnergyID the solarEnergyID to set
	 */
	public void setSolarEnergyID(int solarEnergyID) 
	{
		this.solarEnergyID = solarEnergyID;
	}
		
	/*
	 * @param transactionType the transactionType to set
	 */
	public void setTransactionType(String transactionType) 
	{
		this.transactionType = transactionType;
	}

	/*
	 * @param quantitySolarEnergyTraded the quantitySolarEnergyTraded to set
	 */
	public void setQuantitySolarEnergyTraded(double quantitySolarEnergyTraded) 
	{
		this.quantitySolarEnergyTraded = quantitySolarEnergyTraded;
	}
	
	/*
	 * @param priceKwH the priceKwH to set
	 */
	public void setPriceKwH(double priceKwH) 
	{
		this.priceKwH = priceKwH;
	}
	
	/*
	 * @param totalPriceTraded the totalPriceTraded to set
	 */
	public void setTotalPriceTraded(double totalPriceTraded) 
	{
		this.totalPriceTraded = totalPriceTraded;
	}
	
	/*
	 * @param dateTime the dateTime to set
	 */
	public void setDateTime(String dateTime) 
	{
		this.dateTime = dateTime;
	}
	
	/*
	 * @param energyProduced the energyProduced to set
	 */
	public void setEnergyProduced(double energyProduced) 
	{
		this.energyProduced = energyProduced;
	}
	
	/*
	 * @param location the location to set
	 */
	public void setLocation(String location) 
	{
		this.location = location;
	}
	
	/*
	 * @param energyConsumed the energyConsumed to set
	 */
	public void setEnergyConsumed(double energyConsumed) 
	{
		this.energyConsumed = energyConsumed;
	}
	
	/*
	 * @param quantity the quantity to set
	 */
	public void setQuantity(double quantity) 
	{
		this.quantity = quantity;
	}	
	
	@Override
	public String toString() 
	{
		  return "SolarEnergyData{" +
		         "ID: " + solarEnergyID +
		         ", Transaction Type: " + transactionType +
		         ", Quantity Solar Energy Traded: " + quantitySolarEnergyTraded +
		         ", Price Per KwH: " + priceKwH +
		         ", Total Price Traded: " + totalPriceTraded +
		         ", Date and Time: " + dateTime +
		         '}';
	}
}
