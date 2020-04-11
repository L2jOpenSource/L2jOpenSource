/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.actor.instance;

import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.instancemanager.TerritoryWarManager;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.SystemMessage;

public final class L2TerritoryWardInstance extends L2Attackable
{
	public L2TerritoryWardInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		
		disableCoreAI(true);
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (isInvul())
		{
			return false;
		}
		if ((getCastle() == null) || !getCastle().getZone().isActive())
		{
			return false;
		}
		
		final L2PcInstance actingPlayer = attacker.getActingPlayer();
		if (actingPlayer == null)
		{
			return false;
		}
		if (actingPlayer.getSiegeSide() == 0)
		{
			return false;
		}
		if (TerritoryWarManager.getInstance().isAllyField(actingPlayer, getCastle().getResidenceId()))
		{
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		
		if (getCastle() == null)
		{
			_log.warn("L2TerritoryWardInstance(" + getName() + ") spawned outside Castle Zone!");
		}
	}
	
	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		if ((skill != null) || !TerritoryWarManager.getInstance().isTWInProgress())
		{
			return;
		}
		
		final L2PcInstance actingPlayer = attacker.getActingPlayer();
		if (actingPlayer == null)
		{
			return;
		}
		if (actingPlayer.isCombatFlagEquipped())
		{
			return;
		}
		if (actingPlayer.getSiegeSide() == 0)
		{
			return;
		}
		if (getCastle() == null)
		{
			return;
		}
		if (TerritoryWarManager.getInstance().isAllyField(actingPlayer, getCastle().getResidenceId()))
		{
			return;
		}
		
		super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
	}
	
	@Override
	public void reduceCurrentHpByDOT(double i, L2Character attacker, L2Skill skill)
	{
		// wards can't be damaged by DOTs
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		// Kill the L2NpcInstance (the corpse disappeared after 7 seconds)
		if (!super.doDie(killer) || (getCastle() == null) || !TerritoryWarManager.getInstance().isTWInProgress())
		{
			return false;
		}
		
		if (killer instanceof L2PcInstance)
		{
			if ((((L2PcInstance) killer).getSiegeSide() > 0) && !((L2PcInstance) killer).isCombatFlagEquipped())
			{
				((L2PcInstance) killer).addItem("Pickup", getId() - 23012, 1, null, false);
			}
			else
			{
				TerritoryWarManager.getInstance().getTerritoryWard(getId() - 36491).spawnMe();
			}
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_S1_WARD_HAS_BEEN_DESTROYED_C2_HAS_THE_WARD);
			sm.addString(getName().replaceAll(" Ward", ""));
			sm.addPcName((L2PcInstance) killer);
			TerritoryWarManager.getInstance().announceToParticipants(sm, 0, 0);
		}
		else
		{
			TerritoryWarManager.getInstance().getTerritoryWard(getId() - 36491).spawnMe();
		}
		decayMe();
		return true;
	}
	
	@Override
	public void onForcedAttack(L2PcInstance player)
	{
		onAction(player);
	}
	
	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		if ((player == null) || !canTarget(player))
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
			if (isAutoAttackable(player) && (Math.abs(player.getZ() - getZ()) < 100))
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
