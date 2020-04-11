package com.l2jfrozen.gameserver.model.actor.instance;

import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.managers.SiegeManager;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2SiegeClan;
import com.l2jfrozen.gameserver.model.entity.siege.Siege;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.ValidateLocation;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

public class L2SiegeFlagInstance extends L2NpcInstance
{
	private final L2PcInstance siegePlayer;
	private final Siege siege;
	
	public L2SiegeFlagInstance(final L2PcInstance player, final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
		
		siegePlayer = player;
		siege = SiegeManager.getInstance().getSiege(siegePlayer.getX(), siegePlayer.getY(), siegePlayer.getZ());
		if (siegePlayer.getClan() == null || siege == null)
		{
			deleteMe();
		}
		else
		{
			L2SiegeClan sc = siege.getAttackerClan(siegePlayer.getClan());
			if (sc == null)
			{
				deleteMe();
			}
			else
			{
				sc.addFlag(this);
			}
			sc = null;
		}
	}
	
	@Override
	public boolean isAttackable()
	{
		// Attackable during siege by attacker only
		return getCastle() != null && getCastle().getCastleId() > 0 && getCastle().getSiege().getIsInProgress();
	}
	
	@Override
	public boolean isAutoAttackable(final L2Character attacker)
	{
		// Attackable during siege by attacker only
		return attacker != null && attacker instanceof L2PcInstance && getCastle() != null && getCastle().getCastleId() > 0 && getCastle().getSiege().getIsInProgress();
	}
	
	@Override
	public boolean doDie(final L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		L2SiegeClan sc = siege.getAttackerClan(siegePlayer.getClan());
		if (sc != null)
		{
			sc.removeFlag(this);
		}
		sc = null;
		
		return true;
	}
	
	@Override
	public void onForcedAttack(final L2PcInstance player)
	{
		onAction(player);
	}
	
	@Override
	public void onAction(final L2PcInstance player)
	{
		if (player == null || !canTarget(player))
		{
			return;
		}
		
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);
			my = null;
			
			// Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			player.sendPacket(su);
			su = null;
			
			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			if (isAutoAttackable(player) && Math.abs(player.getZ() - getZ()) < 100)
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			}
			else
			{
				// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
}
