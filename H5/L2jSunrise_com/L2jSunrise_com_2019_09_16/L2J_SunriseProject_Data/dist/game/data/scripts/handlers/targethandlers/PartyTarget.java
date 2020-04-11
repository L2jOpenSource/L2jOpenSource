package handlers.targethandlers;

import java.util.ArrayList;
import java.util.List;

import l2r.gameserver.handler.ITargetTypeHandler;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.skills.targets.L2TargetType;

/**
 * Implementation target's party<br>
 * if affect scope is target and skill type is party
 * @author vGodFather
 */
public class PartyTarget implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		if (target == null)
		{
			return _emptyTargetList;
		}
		
		List<L2Character> targetList = new ArrayList<>();
		if (onlyFirst)
		{
			return new L2Character[]
			{
				target
			};
		}
		
		targetList.add(target);
		
		final int radius = skill.getAffectRange();
		L2PcInstance player = target.getActingPlayer();
		if (target.isSummon())
		{
			if (L2Skill.addCharacter(target, player, radius, false))
			{
				targetList.add(player);
			}
		}
		else if (target.isPlayer())
		{
			if (L2Skill.addSummon(target, player, radius, false))
			{
				targetList.add(player.getSummon());
			}
		}
		
		if (target.isInParty())
		{
			// Get a list of Party Members
			for (L2PcInstance partyMember : target.getParty().getMembers())
			{
				if ((partyMember == null) || (partyMember == player))
				{
					continue;
				}
				
				if (L2Skill.addCharacter(target, partyMember, radius, false))
				{
					targetList.add(partyMember);
				}
				
				if (L2Skill.addSummon(target, partyMember, radius, false))
				{
					targetList.add(partyMember.getSummon());
				}
			}
		}
		return targetList.toArray(new L2Character[targetList.size()]);
	}
	
	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.PARTY_TARGET;
	}
}
