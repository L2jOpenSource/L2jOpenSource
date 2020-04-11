package net.sf.l2j.gameserver.model.actor.cast;

import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.enums.SiegeSide;
import net.sf.l2j.gameserver.enums.skills.SkillTargetType;
import net.sf.l2j.gameserver.enums.skills.SkillType;
import net.sf.l2j.gameserver.model.WorldRegion;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.L2Skill;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillSummon;

/**
 * This class groups all cast data related to a {@link Player}.
 */
public class PlayerCast extends CreatureCast
{
	public PlayerCast(Creature creature)
	{
		super(creature);
	}
	
	@Override
	public void doCast(L2Skill skill)
	{
		super.doCast(skill);
		
		((Player) _creature).clearRecentFakeDeath();
	}
	
	@Override
	protected boolean checkDoCastConditions(L2Skill skill)
	{
		if (!super.checkDoCastConditions(skill))
			return false;
		
		final Player player = (Player) _creature;
		
		// Can't summon multiple servitors.
		if (skill.getSkillType() == SkillType.SUMMON)
		{
			if (!((L2SkillSummon) skill).isCubic() && (player.getSummon() != null || player.isMounted()))
			{
				player.sendPacket(SystemMessageId.SUMMON_ONLY_ONE);
				return false;
			}
		}
		// Can't use ressurect skills on siege if you are defender and control towers is not alive, if you are attacker and flag isn't spawned or if you aren't part of that siege.
		else if (skill.getSkillType() == SkillType.RESURRECT)
		{
			final Siege siege = CastleManager.getInstance().getActiveSiege(player);
			if (siege != null)
			{
				if (player.getClan() == null)
				{
					player.sendPacket(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE);
					return false;
				}
				
				final SiegeSide side = siege.getSide(player.getClan());
				if (side == SiegeSide.DEFENDER || side == SiegeSide.OWNER)
				{
					if (siege.getControlTowerCount() == 0)
					{
						player.sendPacket(SystemMessageId.TOWER_DESTROYED_NO_RESURRECTION);
						return false;
					}
				}
				else if (side == SiegeSide.ATTACKER)
				{
					if (player.getClan().getFlag() == null)
					{
						player.sendPacket(SystemMessageId.NO_RESURRECTION_WITHOUT_BASE_CAMP);
						return false;
					}
				}
				else
				{
					player.sendPacket(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE);
					return false;
				}
			}
		}
		// Can't casting signets on peace zone.
		else if (skill.getSkillType() == SkillType.SIGNET || skill.getSkillType() == SkillType.SIGNET_CASTTIME)
		{
			final WorldRegion region = player.getRegion();
			if (region == null)
				return false;
			
			if (!region.checkEffectRangeInsidePeaceZone(skill, (skill.getTargetType() == SkillTargetType.GROUND) ? player.getCurrentSkillWorldPosition() : player.getPosition()))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill));
				return false;
			}
		}
		
		// Can't use Hero and resurrect skills during Olympiad
		if (player.isInOlympiadMode() && (skill.isHeroSkill() || skill.getSkillType() == SkillType.RESURRECT))
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return false;
		}
		
		// Check if the spell uses charges
		if (skill.getMaxCharges() == 0 && player.getCharges() < skill.getNumCharges())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill));
			return false;
		}
		
		return true;
	}
}