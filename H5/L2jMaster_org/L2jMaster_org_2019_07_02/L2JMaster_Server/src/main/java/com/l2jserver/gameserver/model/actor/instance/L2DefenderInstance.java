/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.model.actor.instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.Config;
import com.l2jserver.gameserver.ai.CtrlIntention;
import com.l2jserver.gameserver.ai.L2CharacterAI;
import com.l2jserver.gameserver.ai.L2FortSiegeGuardAI;
import com.l2jserver.gameserver.ai.L2SiegeGuardAI;
import com.l2jserver.gameserver.ai.L2SpecialSiegeGuardAI;
import com.l2jserver.gameserver.enums.InstanceType;
import com.l2jserver.gameserver.instancemanager.CastleManager;
import com.l2jserver.gameserver.instancemanager.FortManager;
import com.l2jserver.gameserver.instancemanager.TerritoryWarManager;
import com.l2jserver.gameserver.model.actor.L2Attackable;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Playable;
import com.l2jserver.gameserver.model.actor.knownlist.DefenderKnownList;
import com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate;
import com.l2jserver.gameserver.model.entity.Castle;
import com.l2jserver.gameserver.model.entity.Fort;
import com.l2jserver.gameserver.model.entity.clanhall.SiegableHall;
import com.l2jserver.gameserver.network.serverpackets.ActionFailed;

public class L2DefenderInstance extends L2Attackable
{
	private static final Logger LOG = LoggerFactory.getLogger(L2DefenderInstance.class);
	private Castle _castle = null; // the castle which the instance should defend
	private Fort _fort = null; // the fortress which the instance should defend
	private SiegableHall _hall = null; // the siegable hall which the instance should defend
	
	/**
	 * Creates a defender.
	 * @param template the defender NPC template
	 */
	public L2DefenderInstance(L2NpcTemplate template)
	{
		super(template);
		setInstanceType(InstanceType.L2DefenderInstance);
	}
	
	@Override
	public DefenderKnownList getKnownList()
	{
		return (DefenderKnownList) super.getKnownList();
	}
	
	@Override
	public void initKnownList()
	{
		setKnownList(new DefenderKnownList(this));
	}
	
	@Override
	protected L2CharacterAI initAI()
	{
		if ((getConquerableHall() == null) && (getCastle(10000) == null))
		{
			return new L2FortSiegeGuardAI(this);
		}
		else if (getCastle(10000) != null)
		{
			return new L2SiegeGuardAI(this);
		}
		return new L2SpecialSiegeGuardAI(this);
	}
	
	/**
	 * Return True if a siege is in progress and the L2Character attacker isn't a Defender.
	 * @param attacker The L2Character that the L2SiegeGuardInstance try to attack
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		// Attackable during siege by all except defenders
		if (!(attacker instanceof L2Playable))
		{
			return false;
		}
		
		L2PcInstance player = attacker.getActingPlayer();
		
		// Check if siege is in progress
		if (((_fort != null) && _fort.getZone().isActive()) || ((_castle != null) && _castle.getZone().isActive()) || ((_hall != null) && _hall.getSiegeZone().isActive()))
		{
			int activeSiegeId = (_fort != null ? _fort.getResidenceId() : (_castle != null ? _castle.getResidenceId() : (_hall != null ? _hall.getId() : 0)));
			
			// Check if player is an enemy of this defender npc
			if ((player != null) && (((player.getSiegeState() == 2) && !player.isRegisteredOnThisSiegeField(activeSiegeId)) || ((player.getSiegeState() == 1) && !TerritoryWarManager.getInstance().isAllyField(player, activeSiegeId)) || (player.getSiegeState() == 0)))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
	
	/**
	 * This method forces guard to return to home location previously set
	 */
	@Override
	public void returnHome()
	{
		if (getWalkSpeed() <= 0)
		{
			return;
		}
		if (getSpawn() == null)
		{
			return;
		}
		if (!isInsideRadius(getSpawn(), 40, false, false))
		{
			if (Config.DEBUG)
			{
				LOG.debug("{} moving home", getObjectId());
			}
			setisReturningToSpawnPoint(true);
			clearAggroList();
			
			if (hasAI())
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, getSpawn().getLocation());
			}
		}
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		
		_fort = FortManager.getInstance().getFort(getX(), getY(), getZ());
		_castle = CastleManager.getInstance().getCastle(getX(), getY(), getZ());
		_hall = getConquerableHall();
		if ((_fort == null) && (_castle == null) && (_hall == null))
		{
			LOG.warn("L2DefenderInstance spawned outside of Fortress, Castle or Siegable hall Zone! NpcId: {} x={} y={} z={}", getId(), getX(), getY(), getZ());
		}
	}
	
	/**
	 * Custom onAction behaviour. Note that super() is not called because guards need extra check to see if a player should interact or ATTACK them when clicked.
	 */
	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		if (!canTarget(player))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			if (Config.DEBUG)
			{
				LOG.debug("new target selected: {}", getObjectId());
			}
			
			// Set the target of the L2PcInstance player
			player.setTarget(this);
		}
		else if (interact)
		{
			if (isAutoAttackable(player) && !isAlikeDead())
			{
				if (Math.abs(player.getZ() - getZ()) < 600) // this max heigth difference might need some tweaking
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
				}
			}
			if (!isAutoAttackable(player))
			{
				if (!canInteract(player))
				{
					// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void addDamageHate(L2Character attacker, int damage, long aggro)
	{
		if (attacker == null)
		{
			return;
		}
		
		if (!(attacker instanceof L2DefenderInstance))
		{
			if ((damage == 0) && (aggro <= 1) && (attacker instanceof L2Playable))
			{
				L2PcInstance player = attacker.getActingPlayer();
				// Check if siege is in progress
				if (((_fort != null) && _fort.getZone().isActive()) || ((_castle != null) && _castle.getZone().isActive()) || ((_hall != null) && _hall.getSiegeZone().isActive()))
				{
					int activeSiegeId = (_fort != null ? _fort.getResidenceId() : (_castle != null ? _castle.getResidenceId() : (_hall != null ? _hall.getId() : 0)));
					if ((player != null) && (((player.getSiegeState() == 2) && player.isRegisteredOnThisSiegeField(activeSiegeId)) || ((player.getSiegeState() == 1) && TerritoryWarManager.getInstance().isAllyField(player, activeSiegeId))))
					{
						return;
					}
				}
			}
			super.addDamageHate(attacker, damage, aggro);
		}
	}
}