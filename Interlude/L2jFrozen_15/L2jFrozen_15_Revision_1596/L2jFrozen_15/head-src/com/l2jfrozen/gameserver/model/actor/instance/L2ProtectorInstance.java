package com.l2jfrozen.gameserver.model.actor.instance;

import java.util.concurrent.ScheduledFuture;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.network.serverpackets.CreatureSay;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * @author Ederik
 */
public class L2ProtectorInstance extends L2NpcInstance
{
	private ScheduledFuture<?> aiTask;
	
	private class ProtectorAI implements Runnable
	{
		private final L2ProtectorInstance caster;
		
		protected ProtectorAI(final L2ProtectorInstance caster)
		{
			this.caster = caster;
		}
		
		@Override
		public void run()
		{
			/**
			 * For each known player in range, cast sleep if pvpFlag != 0 or Karma >0 Skill use is just for buff animation
			 */
			for (final L2PcInstance player : getKnownList().getKnownPlayers().values())
			{
				if (player.getKarma() > 0 && Config.PROTECTOR_PLAYER_PK || player.getPvpFlag() != 0 && Config.PROTECTOR_PLAYER_PVP)
				{
					LOGGER.warn("player: " + player);
					handleCast(player, Config.PROTECTOR_SKILLID, Config.PROTECTOR_SKILLLEVEL);
				}
				final L2Summon activePet = player.getPet();
				
				if (activePet == null)
				{
					continue;
				}
				
				if (activePet.getKarma() > 0 && Config.PROTECTOR_PLAYER_PK || activePet.getPvpFlag() != 0 && Config.PROTECTOR_PLAYER_PVP)
				{
					LOGGER.warn("activePet: " + activePet);
					handleCastonPet(activePet, Config.PROTECTOR_SKILLID, Config.PROTECTOR_SKILLLEVEL);
				}
			}
		}
		
		// Cast for Player
		private boolean handleCast(final L2PcInstance player, final int skillId, final int skillLevel)
		{
			if (player.isGM() || player.isDead() || !player.isVisible() || !isInsideRadius(player, Config.PROTECTOR_RADIUS_ACTION, false, false))
			{
				return false;
			}
			
			L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
			
			if (player.getFirstEffect(skill) == null)
			{
				final int objId = caster.getObjectId();
				skill.getEffects(caster, player, false, false, false);
				broadcastPacket(new MagicSkillUser(caster, player, skillId, skillLevel, Config.PROTECTOR_SKILLTIME, 0));
				broadcastPacket(new CreatureSay(objId, 0, String.valueOf(getName()), Config.PROTECTOR_MESSAGE));
				
				skill = null;
				return true;
			}
			
			return false;
		}
		
		// Cast for pet
		private boolean handleCastonPet(final L2Summon player, final int skillId, final int skillLevel)
		{
			if (player.isDead() || !player.isVisible() || !isInsideRadius(player, Config.PROTECTOR_RADIUS_ACTION, false, false))
			{
				return false;
			}
			
			L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
			if (player.getFirstEffect(skill) == null)
			{
				final int objId = caster.getObjectId();
				skill.getEffects(caster, player, false, false, false);
				broadcastPacket(new MagicSkillUser(caster, player, skillId, skillLevel, Config.PROTECTOR_SKILLTIME, 0));
				broadcastPacket(new CreatureSay(objId, 0, String.valueOf(getName()), Config.PROTECTOR_MESSAGE));
				
				skill = null;
				return true;
			}
			
			return false;
		}
	}
	
	public L2ProtectorInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
		
		if (aiTask != null)
		{
			aiTask.cancel(true);
		}
		
		aiTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new ProtectorAI(this), 3000, 3000);
	}
	
	@Override
	public void deleteMe()
	{
		if (aiTask != null)
		{
			aiTask.cancel(true);
			aiTask = null;
		}
		
		super.deleteMe();
	}
	
	@Override
	public boolean isAutoAttackable(final L2Character attacker)
	{
		return false;
	}
}
