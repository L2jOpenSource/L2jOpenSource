package main.holders;

/**
 * @author fissban
 */
public class AuctionItemHolder
{
	private int key;
	private final int ownerId;
	private final int itemObjId;
	private final int itemId;
	private final int itemCount;
	private final int itemEnchantLevel;
	private final int itemPriceCount;
	private final int itemPriceId;
	
	/**
	 * @param key
	 * @param ownerId
	 * @param itemObjId
	 * @param itemId
	 * @param itemCount
	 * @param itemEnchantLevel
	 * @param itemPriceCount
	 * @param itemPriceId
	 */
	public AuctionItemHolder(String auction)
	{
		String[] auctions = auction.split(" ");
		key = Integer.parseInt(auctions[0]);
		ownerId = Integer.parseInt(auctions[1]);
		itemObjId = Integer.parseInt(auctions[2]);
		itemId = Integer.parseInt(auctions[3]);
		itemCount = Integer.parseInt(auctions[4]);
		itemEnchantLevel = Integer.parseInt(auctions[5]);
		itemPriceCount = Integer.parseInt(auctions[6]);
		itemPriceId = Integer.parseInt(auctions[7]);
	}
	
	/**
	 * @param key
	 * @param ownerId
	 * @param itemObjId
	 * @param itemId
	 * @param itemCount
	 * @param itemEnchantLevel
	 * @param itemPriceCount
	 * @param itemPriceId
	 */
	public AuctionItemHolder(int key, int ownerId, int itemObjId, int itemId, int itemCount, int itemEnchantLevel, int itemPriceCount, int itemPriceId)
	{
		this.key = key;
		this.ownerId = ownerId;
		this.itemObjId = itemObjId;
		this.itemId = itemId;
		this.itemCount = itemCount;
		this.itemEnchantLevel = itemEnchantLevel;
		this.itemPriceCount = itemPriceCount;
		this.itemPriceId = itemPriceId;
	}
	
	public int getkey()
	{
		return key;
	}
	
	public void setKey(int key)
	{
		this.key = key;
	}
	
	public int getOwnerId()
	{
		return ownerId;
	}
	
	public int getItemObjId()
	{
		return itemObjId;
	}
	
	public int getItemId()
	{
		return itemId;
	}
	
	public int getItemEnchantLevel()
	{
		return itemEnchantLevel;
	}
	
	public int getItemPriceCount()
	{
		return itemPriceCount;
	}
	
	public int getItemPriceId()
	{
		return itemPriceId;
	}
	
	public int getItemCount()
	{
		return itemCount;
	}
	
	@Override
	public String toString()
	{
		return key + " " + ownerId + " " + itemObjId + " " + itemId + " " + itemCount + " " + itemEnchantLevel + " " + itemPriceCount + " " + itemPriceId;
	}
}
