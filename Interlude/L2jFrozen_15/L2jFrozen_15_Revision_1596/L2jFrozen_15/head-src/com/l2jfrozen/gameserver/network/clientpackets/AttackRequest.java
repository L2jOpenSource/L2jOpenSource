package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jfrozen.gameserver.model.entity.event.CTF;
import com.l2jfrozen.gameserver.model.entity.event.DM;
import com.l2jfrozen.gameserver.model.entity.event.TvT;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;

public final class AttackRequest extends L2GameClientPacket
{
	private int objectId;
	private int originX;
	private int originY;
	private int originZ;
	private int attackId;
	
	@Override
	protected void readImpl()
	{
		objectId = readD();
		originX = readD();
		originY = readD();
		originZ = readD();
		attackId = readC(); // 0 for simple click - 1 for shift-click
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (System.currentTimeMillis() - activeChar.getLastAttackPacket() < 500)
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		activeChar.setLastAttackPacket();
		
		// avoid using expensive operations if not needed
		final L2Object target;
		
		if (activeChar.getTargetId() == objectId)
		{
			target = activeChar.getTarget();
		}
		else
		{
			target = L2World.getInstance().findObject(objectId);
		}
		
		if (target == null)
		{
			return;
		}
		
		// Like L2OFF
		if (activeChar.isAttackingNow() && activeChar.isMoving())
		{
			// If target is not attackable, send a Server->Client packet ActionFailed
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Players can't attack objects in the other instances except from multiverse
		if (target.getInstanceId() != activeChar.getInstanceId() && activeChar.getInstanceId() != -1)
		{
			return;
		}
		
		// Only GMs can directly attack invisible characters
		if (target instanceof L2PcInstance && ((L2PcInstance) target).getAppearance().isInvisible() && !activeChar.isGM())
		{
			return;
		}
		
		// During teleport phase, players cant do any attack
		if ((TvT.isTeleport() && activeChar.inEventTvT) || (CTF.isTeleport() && activeChar.inEventCTF) || (DM.is_teleport() && activeChar.inEventDM))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// No attacks to same team in Event
		if (TvT.isStarted())
		{
			if (target instanceof L2PcInstance)
			{
				if ((activeChar.inEventTvT && ((L2PcInstance) target).inEventTvT) && activeChar.teamNameTvT.equals(((L2PcInstance) target).teamNameTvT))
				{
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
			else if (target instanceof L2SummonInstance)
			{
				if ((activeChar.inEventTvT && ((L2SummonInstance) target).getOwner().inEventTvT) && activeChar.teamNameTvT.equals(((L2SummonInstance) target).getOwner().teamNameTvT))
				{
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}
		
		// No attacks to same team in Event
		if (CTF.isStarted())
		{
			if (target instanceof L2PcInstance)
			{
				if ((activeChar.inEventCTF && ((L2PcInstance) target).inEventCTF) && activeChar.teamNameCTF.equals(((L2PcInstance) target).teamNameCTF))
				{
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
			else if (target instanceof L2SummonInstance)
			{
				if ((activeChar.inEventCTF && ((L2SummonInstance) target).getOwner().inEventCTF) && activeChar.teamNameCTF.equals(((L2SummonInstance) target).getOwner().teamNameCTF))
				{
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}
		
		if (activeChar.getTarget() != target)
		{
			target.onAction(activeChar);
		}
		else
		{
			if ((target.getObjectId() != activeChar.getObjectId()) && activeChar.getPrivateStoreType() == 0
			/* && activeChar.getActiveRequester() ==null */)
			{
				target.onForcedAttack(activeChar);
			}
			else
			{
				sendPacket(ActionFailed.STATIC_PACKET);
			}
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
	
	public int getAttackId()
	{
		return attackId;
	}
	
	public void setAttackId(int attackId)
	{
		this.attackId = attackId;
	}
	
	@Override
	public String getType()
	{
		return "[C] 0A AttackRequest";
	}
}