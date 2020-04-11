package com.l2jfrozen.gameserver.network.clientpackets;

import java.nio.BufferUnderflowException;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.position.L2CharPosition;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.EnchantResult;
import com.l2jfrozen.gameserver.network.serverpackets.StopMove;
import com.l2jfrozen.gameserver.thread.TaskPriority;
import com.l2jfrozen.gameserver.util.IllegalPlayerAction;
import com.l2jfrozen.gameserver.util.Util;

@SuppressWarnings("unused")
public class MoveBackwardToLocation extends L2GameClientPacket
{
	private int targetX, targetY, targetZ, originX, originY, originZ, moveMovement;
	private int curX, curY, curZ; // for geodata
	
	public TaskPriority getPriority()
	{
		return TaskPriority.PR_HIGH;
	}
	
	@Override
	protected void readImpl()
	{
		targetX = readD();
		targetY = readD();
		targetZ = readD();
		originX = readD();
		originY = readD();
		originZ = readD();
		
		try
		{
			moveMovement = readD(); // is 0 if cursor keys are used 1 if mouse is used
		}
		catch (final BufferUnderflowException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			// Ignore for now
			if (Config.L2WALKER_PROTEC)
			{
				final L2PcInstance activeChar = getClient().getActiveChar();
				activeChar.sendPacket(SystemMessageId.HACKING_TOOL);
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " trying to use L2Walker!", IllegalPlayerAction.PUNISH_KICK);
			}
		}
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		// Move flood protection
		if (!getClient().getFloodProtectors().getMoveAction().tryPerformAction("MoveBackwardToLocation"))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Like L2OFF movements prohibited when char is sitting
		if (activeChar.isSitting())
		{
			getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Like L2OFF movements prohibited when char is teleporting
		if (activeChar.isTeleporting())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Like L2OFF the enchant window will close
		if (activeChar.getActiveEnchantItem() != null)
		{
			activeChar.sendPacket(new EnchantResult(0));
			activeChar.setActiveEnchantItem(null);
		}
		
		if (targetX == originX && targetY == originY && targetZ == originZ)
		{
			activeChar.sendPacket(new StopMove(activeChar));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		/*
		 * // Correcting targetZ from floor level to head level (?) // Client is giving floor level as targetZ but that floor level doesn't // match our current geodata and teleport coords as good as head level! // L2J uses floor, not head level as char coordinates. This is some // sort of incompatibility
		 * fix. // Validate position packets sends head level. targetZ += activeChar.getTemplate().collisionHeight;
		 */
		
		curX = activeChar.getX();
		curY = activeChar.getY();
		curZ = activeChar.getZ();
		
		if (activeChar.getTeleMode() > 0)
		{
			if (activeChar.getTeleMode() == 1)
			{
				activeChar.setTeleMode(0);
			}
			
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			activeChar.teleToLocation(targetX, targetY, targetZ, false);
			return;
		}
		
		if (moveMovement == 0 && !Config.ALLOW_USE_CURSOR_FOR_WALK)
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			final double dx = targetX - curX;
			final double dy = targetY - curY;
			
			// Can't move if character is confused, or trying to move a huge distance
			if (activeChar.isOutOfControl() || dx * dx + dy * dy > 98010000) // 9900*9900
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// This is to avoid exploit with Hit + Fast movement
			if ((activeChar.isMoving() && activeChar.isAttackingNow()))
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(targetX, targetY, targetZ, 0));
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 01 MoveBackwardToLoc";
	}
}