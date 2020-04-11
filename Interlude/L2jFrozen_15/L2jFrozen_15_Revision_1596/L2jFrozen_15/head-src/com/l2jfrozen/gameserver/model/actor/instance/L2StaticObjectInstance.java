package com.l2jfrozen.gameserver.model.actor.instance;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.cache.HtmCache;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.knownlist.NullKnownList;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.ShowTownMap;

/**
 * @author GODSON ROX!.
 */
public class L2StaticObjectInstance extends L2Object
{
	private static Logger LOGGER = Logger.getLogger(L2StaticObjectInstance.class);
	
	/** The interaction distance of the L2StaticObjectInstance. */
	public static final int INTERACTION_DISTANCE = 150;
	private int staticObjectId;
	private int type = -1; // 0 - map signs, 1 - throne , 2 - arena signs
	private int x;
	private int y;
	private String texture;
	
	/**
	 * Gets the static object id.
	 * @return Returns the StaticObjectId.
	 */
	public int getStaticObjectId()
	{
		return staticObjectId;
	}
	
	/**
	 * Sets the static object id.
	 * @param StaticObjectId the new static object id
	 */
	public void setStaticObjectId(final int StaticObjectId)
	{
		staticObjectId = StaticObjectId;
	}
	
	/**
	 * Instantiates a new l2 static object instance.
	 * @param objectId the object id
	 */
	public L2StaticObjectInstance(final int objectId)
	{
		super(objectId);
		setKnownList(new NullKnownList(this));
	}
	
	/**
	 * Gets the type.
	 * @return the type
	 */
	public int getType()
	{
		return type;
	}
	
	/**
	 * Sets the type.
	 * @param type the new type
	 */
	public void setType(final int type)
	{
		this.type = type;
	}
	
	/**
	 * Sets the map.
	 * @param texture the texture
	 * @param x       the x
	 * @param y       the y
	 */
	public void setMap(final String texture, final int x, final int y)
	{
		this.texture = "town_map." + texture;
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Gets the map x.
	 * @return the map x
	 */
	private int getMapX()
	{
		return x;
	}
	
	/**
	 * Gets the map y.
	 * @return the map y
	 */
	private int getMapY()
	{
		return y;
	}
	
	/**
	 * this is called when a player interacts with this NPC.
	 * @param player the player
	 */
	@Override
	public void onAction(final L2PcInstance player)
	{
		if (type < 0)
		{
			LOGGER.info("L2StaticObjectInstance: StaticObject with invalid type! StaticObjectId: " + getStaticObjectId());
		}
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));
		}
		else
		{
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
			my = null;
			
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if (!player.isInsideRadius(this, INTERACTION_DISTANCE, false, false))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				
				// Send a Server->Client packet ActionFailed (target is out of interaction range) to the L2PcInstance player
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
			{
				if (type == 2)
				{
					String filename = "data/html/signboard.htm";
					String content = HtmCache.getInstance().getHtm(filename);
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					
					if (content == null)
					{
						html.setHtml("<html><body>Signboard is missing:<br>" + filename + "</body></html>");
					}
					else
					{
						html.setHtml(content);
					}
					
					player.sendPacket(html);
					player.sendPacket(ActionFailed.STATIC_PACKET);
					html = null;
					filename = null;
					content = null;
				}
				else if (type == 0)
				{
					player.sendPacket(new ShowTownMap(texture, getMapX(), getMapY()));
				}
				
				// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
	
	@Override
	public boolean isAutoAttackable(final L2Character attacker)
	{
		return false;
	}
}
