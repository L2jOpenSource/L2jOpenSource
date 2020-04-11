package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public final class Action extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(Action.class);
	private int objectId;
	private int originX;
	private int originY;
	private int originZ;
	private int actionId;
	
	@Override
	protected void readImpl()
	{
		objectId = readD(); // Target object Identifier
		originX = (readD());
		originY = (readD());
		originZ = (readD());
		actionId = readC(); // Action identifier : 0-Simple click, 1-Shift click
	}
	
	@Override
	protected void runImpl()
	{
		if (Config.DEBUG)
		{
			LOGGER.debug("DEBUG " + getType() + ": ActionId: " + actionId + " , ObjectID: " + objectId);
		}
		
		// Get the current L2PcInstance of the player
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		if (activeChar.inObserverMode())
		{
			getClient().sendPacket(new SystemMessage(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE));
			getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2Object obj;
		
		if (activeChar.getTargetId() == objectId)
		{
			obj = activeChar.getTarget();
		}
		else
		{
			obj = L2World.getInstance().findObject(objectId);
		}
		
		// If object requested does not exist
		// pressing e.g. pickup many times quickly would get you here
		if (obj == null)
		{
			getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Players can't interact with objects in the other instances except from multiverse
		if (obj.getInstanceId() != activeChar.getInstanceId() && activeChar.getInstanceId() != -1)
		{
			getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Only GMs can directly interact with invisible characters
		if (obj instanceof L2PcInstance && (((L2PcInstance) obj).getAppearance().isInvisible()) && !activeChar.isGM())
		{
			getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// reset old Moving task
		if (activeChar.isMovingTaskDefined())
		{
			activeChar.setMovingTaskDefined(false);
		}
		
		// Check if the target is valid, if the player haven't a shop or isn't the requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...)
		if (activeChar.getPrivateStoreType() == 0/* && activeChar.getActiveRequester() == null */)
		{
			switch (actionId)
			{
				case 0:
					obj.onAction(activeChar);
					break;
				case 1:
					if (obj instanceof L2Character && ((L2Character) obj).isAlikeDead())
					{
						obj.onAction(activeChar);
					}
					else
					{
						obj.onActionShift(getClient());
					}
					break;
				default:
					// Invalid action detected (probably client cheating), LOGGER this
					LOGGER.warn("Character: " + activeChar.getName() + " requested invalid action: " + actionId);
					getClient().sendPacket(ActionFailed.STATIC_PACKET);
					break;
			}
		}
		else
		{
			getClient().sendPacket(ActionFailed.STATIC_PACKET); // Actions prohibited when in trade
		}
	}
	
	public int getOriginX()
	{
		return originX;
	}
	
	public void setOriginX(int originX)
	{
		this.originX = originX;
	}
	
	public int getOriginY()
	{
		return originY;
	}
	
	public void setOriginY(int originY)
	{
		this.originY = originY;
	}
	
	public int getOriginZ()
	{
		return originZ;
	}
	
	public void setOriginZ(int originZ)
	{
		this.originZ = originZ;
	}
	
	@Override
	public String getType()
	{
		return "[C] 04 Action";
	}
}