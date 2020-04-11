package net.sf.l2j.gameserver.handler.targethandlers;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.skills.SkillTargetType;
import net.sf.l2j.gameserver.handler.ITargetHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.skills.L2Skill;

public class TargetEnemySummon implements ITargetHandler
{
	@Override
	public WorldObject[] getTargetList(L2Skill skill, Creature caster, Creature target, boolean onlyFirst)
	{
		if (caster instanceof Player && target instanceof Summon)
		{
			final Player summonOwner = target.getActingPlayer();
			
			if (caster.getSummon() != target && !target.isDead() && (summonOwner.getPvpFlag() != 0 || summonOwner.getKarma() > 0) || (summonOwner.isInsideZone(ZoneId.PVP) && caster.isInsideZone(ZoneId.PVP)) || (summonOwner.isInDuel() && ((Player) caster).isInDuel() && summonOwner.getDuelId() == ((Player) caster).getDuelId()))
				return new Creature[]
				{
					target
				};
		}
		return EMPTY_TARGET_ARRAY;
	}
	
	@Override
	public SkillTargetType getTargetType()
	{
		return SkillTargetType.ENEMY_SUMMON;
	}
}