package net.sf.l2j.gameserver.handler.targethandlers;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.gameserver.enums.skills.SkillTargetType;
import net.sf.l2j.gameserver.handler.ITargetHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.skills.L2Skill;

public class TargetAlly implements ITargetHandler
{
	@Override
	public WorldObject[] getTargetList(L2Skill skill, Creature caster, Creature target, boolean onlyFirst)
	{
		final Player player = caster.getActingPlayer();
		if (player == null)
			return EMPTY_TARGET_ARRAY;
		
		if (onlyFirst || player.isInOlympiadMode())
			return new Creature[]
			{
				caster
			};
		
		final List<Creature> list = new ArrayList<>();
		list.add(player);
		
		if (L2Skill.addSummon(caster, player, skill.getSkillRadius(), false))
			list.add(player.getSummon());
		
		if (player.getClan() != null)
		{
			for (Player obj : caster.getKnownTypeInRadius(Player.class, skill.getSkillRadius()))
			{
				if ((obj.getAllyId() == 0 || obj.getAllyId() != player.getAllyId()) && (obj.getClan() == null || obj.getClanId() != player.getClanId()))
					continue;
				
				if (player.isInDuel())
				{
					if (player.getDuelId() != obj.getDuelId())
						continue;
					
					if (player.isInParty() && obj.isInParty() && player.getParty().getLeaderObjectId() != obj.getParty().getLeaderObjectId())
						continue;
				}
				
				if (!player.checkPvpSkill(obj, skill))
					continue;
				
				final Summon summon = obj.getSummon();
				if (summon != null && !summon.isDead())
					list.add(summon);
				
				if (!obj.isDead())
					list.add(obj);
			}
		}
		return list.toArray(new Creature[list.size()]);
	}
	
	@Override
	public SkillTargetType getTargetType()
	{
		return SkillTargetType.ALLY;
	}
}