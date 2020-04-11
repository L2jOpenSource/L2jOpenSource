package net.sf.l2j.gameserver.handler.targethandlers;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.commons.util.ArraysUtil;

import net.sf.l2j.gameserver.enums.skills.SkillTargetType;
import net.sf.l2j.gameserver.handler.ITargetHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.ClanMember;
import net.sf.l2j.gameserver.skills.L2Skill;

public class TargetClan implements ITargetHandler
{
	@Override
	public WorldObject[] getTargetList(L2Skill skill, Creature caster, Creature target, boolean onlyFirst)
	{
		final List<Creature> list = new ArrayList<>();
		
		if (caster instanceof Playable)
		{
			final Player player = caster.getActingPlayer();
			if (player == null)
				return EMPTY_TARGET_ARRAY;
			
			if (onlyFirst || player.isInOlympiadMode())
				return new Creature[]
				{
					caster
				};
			
			list.add(player);
			
			if (L2Skill.addSummon(caster, player, skill.getSkillRadius(), false))
				list.add(player.getSummon());
			
			final Clan clan = player.getClan();
			if (clan != null)
			{
				for (ClanMember member : clan.getMembers())
				{
					final Player obj = member.getPlayerInstance();
					if (obj == null || obj == player)
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
					
					if (L2Skill.addSummon(caster, obj, skill.getSkillRadius(), false))
						list.add(obj.getSummon());
					
					if (!L2Skill.addCharacter(caster, obj, skill.getSkillRadius(), false))
						continue;
					
					list.add(obj);
				}
			}
		}
		else if (caster instanceof Npc)
		{
			list.add(caster);
			
			for (Npc npc : caster.getKnownTypeInRadius(Npc.class, skill.getCastRange()))
			{
				if (npc.isDead() || !ArraysUtil.contains(((Npc) caster).getTemplate().getClans(), npc.getTemplate().getClans()))
					continue;
				
				list.add(npc);
			}
		}
		return list.toArray(new Creature[list.size()]);
	}
	
	@Override
	public SkillTargetType getTargetType()
	{
		return SkillTargetType.CLAN;
	}
}