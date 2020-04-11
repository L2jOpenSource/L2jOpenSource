package main.holders.objects;

import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;

import main.data.memory.ObjectData;

/**
 * @author fissban
 */
public class ItemHolder extends ObjectHolder
{
	private L2ItemInstance item;
	/** Weather in Milliseconds where the item will be deleted */
	private int interval = 0;
	private int duration = 0;
	
	public ItemHolder(L2ItemInstance item)
	{
		super(item);
	}
	
	@Override
	public L2ItemInstance getInstance()
	{
		return (L2ItemInstance) super.getInstance();
	}
	
	public void setDropperObjectId(int dropperId)
	{
		if (dropperId > 0)
		{
			setWorldId(ObjectData.get(ObjectHolder.class, dropperId).getWorldId());
		}
	}
	
	/**
	 * Returns <b>true</b> if the item will be deleted in a certain time.
	 * @return
	 */
	public boolean isTemporalItem()
	{
		return interval > 0;
	}
	
	public int getInterval()
	{
		return interval;
	}
	
	/**
	 * Remaining time in seconds remaining to the item before being erased.
	 * @return
	 */
	public int getDuration()
	{
		return duration;
	}
	
	public void decreaseDuration()
	{
		duration--;
	}
	
	/**
	 * The time in which the item in Milliseconds will be deleted will be defined.
	 * @param time
	 */
	public void setDuration(long start, long end)
	{
		// init duration
		duration = 600;
		// create interval for decrease duration
		interval = (int) ((end - start) / 1000 / 600);
		
		long actualTime = System.currentTimeMillis() - 1000;
		
		// set actual duration
		boolean deleteItem = false;
		while ((start < actualTime) && !deleteItem)
		{
			start += interval;
			duration--;
			
			if (duration <= 0)
			{
				deleteItem = true;
			}
		}
	}
	
	public boolean isSellable()
	{
		return getDuration() > 0 ? false : getInstance().getItem().isSellable();
	}
	
	public boolean isDropable()
	{
		return getDuration() > 0 ? false : getInstance().getItem().isDropable();
	}
	
	public boolean isDestroyable()
	{
		return getDuration() > 0 ? false : getInstance().getItem().isDestroyable();
	}
	
	// public boolean isTradable()
	// {
	// return getDuration() > 0 ? false : getInstance().getItem().isTradable();
	// }
	
	// public boolean isDepositable()
	// {
	// return getDuration() > 0 ? false : getInstance().getItem().isDepositable();
	// }
	
	public int getWeight()
	{
		return getDuration() > 0 ? 0 : getInstance().getItem().getWeight();
	}
}
