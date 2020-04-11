package l2r.gameserver.ai.model;

import static l2r.gameserver.enums.CtrlIntention.AI_INTENTION_ACTIVE;

import java.util.Collection;

import l2r.gameserver.ai.L2FortSiegeGuardAI;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2DefenderInstance;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.skills.L2SkillType;

/**
 * This vGodFather
 */
public class SiegeHealerAI extends L2FortSiegeGuardAI
{
	public SiegeHealerAI(L2DefenderInstance creature)
	{
		super(creature);
	}
	
	@Override
	protected void attackPrepare()
	{
		final L2Character mostHate = ((L2Attackable) _actor).getMostHated();
		if (mostHate == null)
		{
			setIntention(AI_INTENTION_ACTIVE);
			return;
		}
		
		setAttackTarget(mostHate);
		
		final L2Character npc = getActor();
		
		Collection<L2Skill> skills = _actor.getAllSkills();
		if (!_actor.isMuted() && !skills.isEmpty())
		{
			for (L2Character obj : npc.getKnownList().getKnownCharactersInRadius(300))
			{
				if (!(obj instanceof L2DefenderInstance))
				{
					continue;
				}
				
				for (L2Skill sk : skills)
				{
					if (sk.isPassive())
					{
						continue;
					}
					
					if (!checkSkillCastConditions(npc.getAttackable(), sk))
					{
						continue;
					}
					
					boolean docast = false;
					if ((sk.hasEffectType(L2EffectType.HEAL, L2EffectType.HEAL_OVER_TIME, L2EffectType.HEAL_PERCENT)) && (obj.getCurrentHp() < (obj.getMaxHp() * 0.9)))
					{
						docast = true;
					}
					
					if ((sk.getSkillType() == L2SkillType.BUFF) && !_actor.isAffectedBySkill(sk.getId()))
					{
						docast = true;
					}
					
					if (docast)
					{
						if (maybeMoveToPawn(obj, _actor.getMagicalAttackRange(sk)))
						{
							return;
						}
						
						clientStopMoving(null);
						npc.setTarget(obj);
						npc.doCast(sk);
						
						if (npc.isDebug())
						{
							_log.info("{} used skill {} on target: " + npc.getTarget(), this, sk);
						}
						return;
					}
				}
			}
		}
		
		if (_actor.isMuted())
		{
			super.attackPrepare();
		}
	}
}