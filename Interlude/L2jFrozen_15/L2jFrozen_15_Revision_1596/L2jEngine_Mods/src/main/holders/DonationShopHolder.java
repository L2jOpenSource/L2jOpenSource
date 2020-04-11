package main.holders;

public class DonationShopHolder
{
	String name;
	boolean allowMod;
	int priceId;
	int priceCount;
	
	public DonationShopHolder()
	{
		
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public boolean isAllowMod()
	{
		return allowMod;
	}
	
	public void setAllowMod(boolean allowMod)
	{
		this.allowMod = allowMod;
	}
	
	public int getPriceId()
	{
		return priceId;
	}
	
	public void setPriceId(int priceId)
	{
		this.priceId = priceId;
	}
	
	public int getPriceCount()
	{
		return priceCount;
	}
	
	public void setPriceCount(int priceCount)
	{
		this.priceCount = priceCount;
	}
}
