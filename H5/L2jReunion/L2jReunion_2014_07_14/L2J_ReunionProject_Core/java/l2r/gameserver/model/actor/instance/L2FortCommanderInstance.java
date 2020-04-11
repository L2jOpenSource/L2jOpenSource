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

import javolution.util.FastList;
import l2r.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.enums.InstanceType;
import l2r.gameserver.instancemanager.FortSiegeManager;
import l2r.gameserver.model.FortSiegeSpawn;
import l2r.gameserver.model.L2Spawn;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.network.NpcStringId;
import l2r.gameserver.network.clientpackets.Say2;
import l2r.gameserver.network.serverpackets.NpcSay;

public class L2FortCommanderInstance extends L2DefenderInstance
{
	
	private boolean _canTalk;
	
	public L2FortCommanderInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2FortCommanderInstance);
		_canTalk = true;
	}
	
	/**
	 * Return True if a siege is in progress and the L2Character attacker isn't a Defender.
	 * @param attacker The L2Character that the L2CommanderInstance try to attack
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if ((attacker == null) || !(attacker instanceof L2PcInstance))
		{
			return false;
		}
		
		boolean isFort = ((getFort() != null) && (getFort().getResidenceId() > 0) && getFort().getSiege().isInProgress() && !getFort().getSiege().checkIsDefender(((L2PcInstance) attacker).getClan()));
		
		// Attackable during siege by all except defenders
		return (isFort);
	}
	
	@Override
	public void addDamageHate(L2Character attacker, int damage, int aggro)
	{
		if (attacker == null)
		{
			return;
		}
		
		if (!(attacker instanceof L2FortCommanderInstance))
		{
			super.addDamageHate(attacker, damage, aggro);
		}
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		if (getFort().getSiege().isInProgress())
		{
			getFort().getSiege().killedCommander(this);
			
		}
		
		return true;
	}
	
	/**
	 * This method forces guard to return to home location previously set
	 */
	@Override
	public void returnHome()
	{
		if (!isInsideRadius(getSpawn(), 200, false, false))
		{
			if (Config.DEBUG)
			{
				_log.info(getObjectId() + ": moving home");
			}
			setisReturningToSpawnPoint(true);
			clearAggroList();
			
			if (hasAI())
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(getSpawn().getX(), getSpawn().getY(), getSpawn().getZ(), 0));
			}
		}
	}
	
	@Override
	public final void addDamage(L2Character attacker, int damage, L2Skill skill)
	{
		L2Spawn spawn = getSpawn();
		if ((spawn != null) && canTalk())
		{
			FastList<FortSiegeSpawn> commanders = FortSiegeManager.getInstance().getCommanderSpawnList(getFort().getResidenceId());
			for (FortSiegeSpawn spawn2 : commanders)
			{
				if (spawn2.getId() == spawn.getId())
				{
					NpcStringId npcString = null;
					switch (spawn2.getMessageId())
					{
						case 1:
							npcString = NpcStringId.ATTACKING_THE_ENEMYS_REINFORCEMENTS_IS_NECESSARY_TIME_TO_DIE;
							break;
						case 2:
							if (attacker instanceof L2Summon)
							{
								attacker = ((L2Summon) attacker).getOwner();
							}
							npcString = NpcStringId.EVERYONE_CONCENTRATE_YOUR_ATTACKS_ON_S1_SHOW_THE_ENEMY_YOUR_RESOLVE;
							break;
						case 3:
							npcString = NpcStringId.SPIRIT_OF_FIRE_UNLEASH_YOUR_POWER_BURN_THE_ENEMY;
							break;
					}
					if (npcString != null)
					{
						NpcSay ns = new NpcSay(getObjectId(), Say2.NPC_SHOUT, getId(), npcString);
						if (npcString.getParamCount() == 1)
						{
							ns.addStringParameter(attacker.getName());
						}
						
						broadcastPacket(ns);
						setCanTalk(false);
						ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTalkTask(), 10000);
					}
				}
			}
		}
		super.addDamage(attacker, damage, skill);
	}
	
	private class ScheduleTalkTask implements Runnable
	{
		
		public ScheduleTalkTask()
		{
		}
		
		@Override
		public void run()
		{
			setCanTalk(true);
		}
	}
	
	void setCanTalk(boolean val)
	{
		_canTalk = val;
	}
	
	private boolean canTalk()
	{
		return _canTalk;
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
}
