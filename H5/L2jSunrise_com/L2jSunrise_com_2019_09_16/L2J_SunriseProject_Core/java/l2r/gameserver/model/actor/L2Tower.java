/*
 * Copyright (C) L2J Sunrise
 * This file is part of L2J Sunrise.
 */
package l2r.gameserver.model.actor;

import l2r.gameserver.GeoData;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.network.serverpackets.ActionFailed;

/**
 * This class is a super-class for L2ControlTowerInstance and L2FlameTowerInstance.
 * @author Zoey76
 */
public abstract class L2Tower extends L2Npc
{
	/**
	 * Creates an abstract Tower.
	 * @param template the tower template
	 */
	public L2Tower(L2NpcTemplate template)
	{
		super(template);
		setIsInvul(false);
	}
	
	@Override
	public boolean canBeAttacked()
	{
		// Attackable during siege by attacker only
		return ((getCastle() != null) && (getCastle().getResidenceId() > 0) && getCastle().getSiege().isInProgress());
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		// Attackable during siege by attacker only
		return ((attacker != null) && attacker.isPlayer() && (getCastle() != null) && (getCastle().getResidenceId() > 0) && getCastle().getSiege().isInProgress() && getCastle().getSiege().checkIsAttacker(((L2PcInstance) attacker).getClan()));
	}
	
	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		if (!canTarget(player))
		{
			return;
		}
		
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
		}
		else if (interact)
		{
			if (isAutoAttackable(player) && (Math.abs(player.getZ() - getZ()) < 100) && GeoData.getInstance().canSeeTarget(player, this))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void onForcedAttack(L2PcInstance player)
	{
		onAction(player);
	}
}
