/**
 * This Class Stores Information About CertificateData. {@link CertificateData#CertificateData}
 * @author Mr. A.M. Diepeveen, 221025168
 * @version Computer Science 3A Mini Project
 */

package acsse.csc03a3.client;

import java.io.Serializable;

public class CertificateData implements Serializable 
{
	private static final long serialVersionUID = 1L;
	private int certificateID;
	private String transactionType;
	private String renewableEnergyType;
	private double priceMwH;
	private double carbonEmissionsOffset;
	private double totalPriceTraded;
	private String certificateFile;
	private String dateTime;
	
	public CertificateData(int certificateID, String transactionType, String renewableEnergyType, double priceMwH, double carbonEmissionsOffset, double totalPriceTraded, String certificateFile, String dateTime) 
	{
	      this.certificateID = certificateID;
	      this.transactionType = transactionType;
	      this.renewableEnergyType = renewableEnergyType;
	      this.priceMwH = priceMwH;
	      this.carbonEmissionsOffset = carbonEmissionsOffset;
	      this.totalPriceTraded = totalPriceTraded;
	      this.certificateFile = certificateFile;
	      this.dateTime = dateTime;
	}
	  
	/*
	 * @return the certificateID
	 */
	public int getCertificateID() 
	{
		return certificateID;
	}
	
	/*
	 * @return the transactionType
	 */
	public String getTransactionType() 
	{
		return transactionType;
	}
	
	/*
	 * @return the renewableEnergyType
	 */
	public String getRenewableEnergyType() 
	{
		return renewableEnergyType;
	}

	/*
	 * @return the totalPriceMwH
	 */
	public double getPriceMwH() 
	{
		return priceMwH;
	}	

	/*
	 * @return the carbonEmissionsOffset
	 */
	public double getCarbonEmissionsOffset() 
	{
		return carbonEmissionsOffset;
	}
	
	/*
	 * @return the totalPriceMwH
	 */
	public double getTotalPriceTraded() 
	{
		return totalPriceTraded;
	}

	/*
	 * @return the certificateFile
	 */
	public String getCertificateFile() 
	{
		return certificateFile;
	}

	/*
	 * @return the dateTime
	 */
	public String getDateTime() 
	{
		return dateTime;
	}

	/*
	 * @param certificateID the certificateID to set
	 */
	public void setCertificateID(int certificateID) 
	{
		this.certificateID = certificateID;
	}

	/*
	 * @param transactionType the transactionType to set
	 */
	public void setTransactionType(String transactionType) 
	{
		this.transactionType = transactionType;
	}

	/*
	 * @param renewableEnergyType the renewableEnergyType to set
	 */
	public void setRenewableEnergyType(String renewableEnergyType) 
	{
		this.renewableEnergyType = renewableEnergyType;
	}
	
	/*
	 * @param totalPriceMwH the totalPriceMwH to set
	 */
	public void setPriceMwH(double priceMwH) 
	{
		this.priceMwH = priceMwH;
	}

	/*
	 * @param carbonEmissionsOffset the carbonEmissionsOffset to set
	 */
	public void setCarbonEmissionsOffset(double carbonEmissionsOffset) 
	{
		this.carbonEmissionsOffset = carbonEmissionsOffset;
	}
	
	/*
	 * @param carbonEmissionsOffset the carbonEmissionsOffset to set
	 */
	public void setTotalPriceTraded(double totalPriceTraded) 
	{
		this.totalPriceTraded = totalPriceTraded;
	}

	/*
	 * @param certificateFile the certificateFile to set
	 */
	public void setCertificateFile(String certificateFile) 
	{
		this.certificateFile = certificateFile;
	}

	/*
	 * @param dateTime the dateTime to set
	 */
	public void setDateTime(String dateTime) 
	{
		this.dateTime = dateTime;
	}

	@Override
	public String toString() 
	{
		return "CertificateData{" +
             "CID: " + certificateID +
             ", Transaction Type: " + transactionType +
             ", Renewable Energy Type: " + renewableEnergyType +
             ", Price Per MwH: " + priceMwH +
             ", Carbon Emissions Offset: " + carbonEmissionsOffset +         
             ", Total Price Traded: " + totalPriceTraded +
             ", Certificate File: " + certificateFile +    
             ", Date and Time: " + dateTime +
             '}';
	}
}