package main.holders.objects;

import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

import main.data.memory.ObjectData;

/**
 * @author fissban
 */
public class ObjectHolder
{
	// object
	private L2Object obj = null;
	// world
	private int worldId = 0;
	
	public ObjectHolder(L2Object obj)
	{
		this.obj = obj;
	}
	
	/**
	 * ObjectId
	 * @return int
	 */
	public int getObjectId()
	{
		return obj.getObjectId();
	}
	
	public L2Object getInstance()
	{
		return obj;
	}
	
	public void setWorldId(int id)
	{
		if (worldId == id)
		{
			return;
		}
		
		worldId = id;
		
		if (getInstance() == null)
		{
			return;
		}
		
		// remove from old world
		removeDifferentWorldObjects();
		
		if ((this instanceof PlayerHolder) && (((L2PcInstance) getInstance()).getPet() != null))
		{
			ObjectData.get(ObjectHolder.class, ((L2PcInstance) getInstance()).getPet()).setWorldId(id);
		}
		
		if (!(this instanceof ItemHolder) && getInstance().isVisible() && !getInstance().getKnownList().getKnownObjects().isEmpty())
		{
			getInstance().decayMe();
			getInstance().spawnMe();
		}
	}
	
	public int getWorldId()
	{
		return worldId;
	}
	
	public void removeDifferentWorldObjects()
	{
		for (L2Object obj : getInstance().getKnownList().getKnownObjects().values())
		{
			if (isDifferentWorld(obj))
			{
				ObjectHolder ch = ObjectData.get(ObjectHolder.class, obj);
				
				if (ch == null)
				{
					continue;
				}
				
				// remove known objects
				ch.getInstance().getKnownList().removeKnownObject(getInstance());
				// remove known objects
				getInstance().getKnownList().removeKnownObject(getInstance());
			}
		}
	}
	
	public boolean isDifferentWorld(L2Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		
		ObjectHolder oh = ObjectData.get(ObjectHolder.class, obj);
		
		if (oh == null)
		{
			return false;
		}
		
		if (oh.getWorldId() == getWorldId())
		{
			return false;
		}
		
		return true;
	}
}
