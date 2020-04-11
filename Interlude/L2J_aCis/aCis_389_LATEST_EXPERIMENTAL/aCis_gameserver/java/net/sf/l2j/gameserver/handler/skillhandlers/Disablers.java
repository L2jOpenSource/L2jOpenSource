package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.enums.AiEventType;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.items.ShotType;
import net.sf.l2j.gameserver.enums.skills.EffectType;
import net.sf.l2j.gameserver.enums.skills.SkillTargetType;
import net.sf.l2j.gameserver.enums.skills.SkillType;
import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.ai.type.AttackableAI;
import net.sf.l2j.gameserver.model.actor.instance.SiegeSummon;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.AbstractEffect;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.L2Skill;

public class Disablers implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.STUN,
		SkillType.ROOT,
		SkillType.SLEEP,
		SkillType.CONFUSION,
		SkillType.AGGDAMAGE,
		SkillType.AGGREDUCE,
		SkillType.AGGREDUCE_CHAR,
		SkillType.AGGREMOVE,
		SkillType.MUTE,
		SkillType.FAKE_DEATH,
		SkillType.NEGATE,
		SkillType.CANCEL_DEBUFF,
		SkillType.PARALYZE,
		SkillType.ERASE,
		SkillType.BETRAY
	};
	
	@Override
	public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets)
	{
		final SkillType type = skill.getSkillType();
		
		final boolean bsps = activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT);
		
		for (WorldObject obj : targets)
		{
			if (!(obj instanceof Creature))
				continue;
			
			Creature target = (Creature) obj;
			if (target.isDead() || (target.isInvul() && !target.isParalyzed())) // bypass if target is dead or invul (excluding invul from Petrification)
				continue;
			
			if (skill.isOffensive() && target.getFirstEffect(EffectType.BLOCK_DEBUFF) != null)
				continue;
			
			final byte shld = Formulas.calcShldUse(activeChar, target, skill);
			
			switch (type)
			{
				case BETRAY:
					if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps))
						skill.getEffects(activeChar, target, shld, bsps);
					else
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
					break;
				
				case FAKE_DEATH:
					// stun/fakedeath is not mdef dependant, it depends on lvl difference, target CON and power of stun
					skill.getEffects(activeChar, target, shld, bsps);
					break;
				
				case ROOT:
				case STUN:
					if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
						target = activeChar;
					
					if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps))
						skill.getEffects(activeChar, target, shld, bsps);
					else
					{
						if (activeChar instanceof Player)
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill.getId()));
					}
					break;
				
				case SLEEP:
				case PARALYZE: // use same as root for now
					if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
						target = activeChar;
					
					if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps))
						skill.getEffects(activeChar, target, shld, bsps);
					else
					{
						if (activeChar instanceof Player)
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill.getId()));
					}
					break;
				
				case MUTE:
					if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
						target = activeChar;
					
					if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps))
					{
						// stop same type effect if available
						for (AbstractEffect effect : target.getAllEffects())
						{
							if (effect.getTemplate().getStackOrder() == 99)
								continue;
							
							if (effect.getSkill().getSkillType() == type)
								effect.exit();
						}
						skill.getEffects(activeChar, target, shld, bsps);
					}
					else
					{
						if (activeChar instanceof Player)
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill.getId()));
					}
					break;
				
				case CONFUSION:
					// do nothing if not on mob
					if (target instanceof Attackable)
					{
						if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps))
						{
							for (AbstractEffect effect : target.getAllEffects())
							{
								if (effect.getTemplate().getStackOrder() == 99)
									continue;
								
								if (effect.getSkill().getSkillType() == type)
									effect.exit();
							}
							skill.getEffects(activeChar, target, shld, bsps);
						}
						else
						{
							if (activeChar instanceof Player)
								activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
						}
					}
					else
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					break;
				
				case AGGDAMAGE:
					if (target instanceof Attackable)
						target.getAI().notifyEvent(AiEventType.AGGRESSION, activeChar, (int) ((150 * skill.getPower()) / (target.getLevel() + 7)));
					
					skill.getEffects(activeChar, target, shld, bsps);
					break;
				
				case AGGREDUCE:
					// these skills needs to be rechecked
					if (target instanceof Attackable)
					{
						skill.getEffects(activeChar, target, shld, bsps);
						
						final double aggdiff = ((Attackable) target).getHating(activeChar) - target.calcStat(Stats.AGGRESSION, ((Attackable) target).getHating(activeChar), target, skill);
						
						if (skill.getPower() > 0)
							((Attackable) target).reduceHate(null, (int) skill.getPower());
						else if (aggdiff > 0)
							((Attackable) target).reduceHate(null, (int) aggdiff);
					}
					break;
				
				case AGGREDUCE_CHAR:
					// these skills needs to be rechecked
					if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps))
					{
						if (target instanceof Attackable)
						{
							final Attackable targ = (Attackable) target;
							targ.stopHating(activeChar);
							if (targ.getMostHated() == null && targ.hasAI() && targ.getAI() instanceof AttackableAI)
							{
								((AttackableAI) targ.getAI()).setGlobalAggro(-25);
								targ.getAggroList().clear();
								targ.getAI().tryTo(IntentionType.ACTIVE, null, null);
								targ.forceWalkStance();
							}
						}
						skill.getEffects(activeChar, target, shld, bsps);
					}
					else
					{
						if (activeChar instanceof Player)
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
					}
					break;
				
				case AGGREMOVE:
					// these skills needs to be rechecked
					if (target instanceof Attackable && !target.isRaidRelated())
					{
						if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps))
						{
							if (skill.getTargetType() == SkillTargetType.UNDEAD)
							{
								if (target.isUndead())
									((Attackable) target).reduceHate(null, ((Attackable) target).getHating(((Attackable) target).getMostHated()));
							}
							else
								((Attackable) target).reduceHate(null, ((Attackable) target).getHating(((Attackable) target).getMostHated()));
						}
						else
						{
							if (activeChar instanceof Player)
								activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
						}
					}
					break;
				
				case ERASE:
					// doesn't affect siege summons
					if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps) && !(target instanceof SiegeSummon))
					{
						final Player summonOwner = ((Summon) target).getOwner();
						final Summon summonPet = summonOwner.getSummon();
						if (summonPet != null)
						{
							summonPet.unSummon(summonOwner);
							summonOwner.sendPacket(SystemMessageId.YOUR_SERVITOR_HAS_VANISHED);
						}
					}
					else
					{
						if (activeChar instanceof Player)
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
					}
					break;
				
				case CANCEL_DEBUFF:
					final AbstractEffect[] effects = target.getAllEffects();
					if (effects == null || effects.length == 0)
						break;
					
					int count = (skill.getMaxNegatedEffects() > 0) ? 0 : -2;
					for (AbstractEffect effect : effects)
					{
						if (!effect.getSkill().isDebuff() || !effect.getSkill().canBeDispeled() || effect.getTemplate().getStackOrder() == 99)
							continue;
						
						effect.exit();
						
						if (count > -1)
						{
							count++;
							if (count >= skill.getMaxNegatedEffects())
								break;
						}
					}
					break;
				
				case NEGATE:
					if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
						target = activeChar;
					
					// Skills with negateId (skillId)
					if (skill.getNegateId().length != 0)
					{
						for (int id : skill.getNegateId())
						{
							if (id != 0)
								target.stopSkillEffects(id);
						}
					}
					// All others negate type skills
					else
					{
						for (AbstractEffect effect : target.getAllEffects())
						{
							if (effect.getTemplate().getStackOrder() == 99)
								continue;
							
							final L2Skill effectSkill = effect.getSkill();
							for (SkillType skillType : skill.getNegateStats())
							{
								// If power is -1 the effect is always removed without lvl check
								if (skill.getNegateLvl() == -1)
								{
									if (effectSkill.getSkillType() == skillType || (effectSkill.getEffectType() != null && effectSkill.getEffectType() == skillType))
										effect.exit();
								}
								// Remove the effect according to its power.
								else
								{
									if (effectSkill.getEffectType() != null && effectSkill.getEffectAbnormalLvl() >= 0)
									{
										if (effectSkill.getEffectType() == skillType && effectSkill.getEffectAbnormalLvl() <= skill.getNegateLvl())
											effect.exit();
									}
									else if (effectSkill.getSkillType() == skillType && effectSkill.getAbnormalLvl() <= skill.getNegateLvl())
										effect.exit();
								}
							}
						}
					}
					skill.getEffects(activeChar, target, shld, bsps);
					break;
			}
		}
		
		if (skill.hasSelfEffects())
		{
			final AbstractEffect effect = activeChar.getFirstEffect(skill.getId());
			if (effect != null && effect.isSelfEffect())
				effect.exit();
			
			skill.getEffectsSelf(activeChar);
		}
		activeChar.setChargedShot(bsps ? ShotType.BLESSED_SPIRITSHOT : ShotType.SPIRITSHOT, skill.isStaticReuse());
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}