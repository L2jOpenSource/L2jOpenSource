package com.l2jfrozen.gameserver.handler.skillhandlers;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlEvent;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.ai.L2AttackableAI;
import com.l2jfrozen.gameserver.datatables.xml.ExperienceData;
import com.l2jfrozen.gameserver.handler.ISkillHandler;
import com.l2jfrozen.gameserver.handler.SkillHandler;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Effect.EffectType;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SiegeSummonInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.skills.Formulas;
import com.l2jfrozen.gameserver.skills.Stats;
import com.l2jfrozen.util.random.Rnd;

/**
 * This Handles Disabler skills
 * @author drunk
 */
public class Disablers implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
		L2Skill.SkillType.STUN,
		L2Skill.SkillType.ROOT,
		L2Skill.SkillType.SLEEP,
		L2Skill.SkillType.CONFUSION,
		L2Skill.SkillType.AGGDAMAGE,
		L2Skill.SkillType.AGGREDUCE,
		L2Skill.SkillType.AGGREDUCE_CHAR,
		L2Skill.SkillType.AGGREMOVE,
		L2Skill.SkillType.UNBLEED,
		L2Skill.SkillType.UNPOISON,
		L2Skill.SkillType.MUTE,
		L2Skill.SkillType.FAKE_DEATH,
		L2Skill.SkillType.CONFUSE_MOB_ONLY,
		L2Skill.SkillType.NEGATE,
		L2Skill.SkillType.CANCEL,
		L2Skill.SkillType.PARALYZE,
		L2Skill.SkillType.ERASE,
		L2Skill.SkillType.MAGE_BANE,
		L2Skill.SkillType.WARRIOR_BANE,
		L2Skill.SkillType.BETRAY
	};
	
	protected static final Logger LOGGER = Logger.getLogger(L2Skill.class);
	private String[] negateSkillTypes = null;
	private String[] negateEffectTypes = null;
	private float negatePower = 0.f;
	private int negateId = 0;
	
	@Override
	public void useSkill(final L2Character activeChar, final L2Skill skill, final L2Object[] targets)
	{
		final SkillType type = skill.getSkillType();
		
		final boolean bss = activeChar.checkBss();
		final boolean sps = activeChar.checkSps();
		final boolean ss = activeChar.checkSs();
		
		for (final L2Object target2 : targets)
		{
			if (target2 == null)
			{
				continue;
			}
			
			// Get a target
			if (!(target2 instanceof L2Character))
			{
				continue;
			}
			
			L2Character target = (L2Character) target2;
			
			if (target.isDead())
			{
				continue;
			}
			
			switch (type)
			{
				case BETRAY:
				{
					if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						skill.getEffects(activeChar, target, ss, sps, bss);
					}
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
						sm.addString(target.getName());
						sm.addSkillName(skill.getId());
						activeChar.sendPacket(sm);
						sm = null;
					}
					break;
				}
				case FAKE_DEATH:
				{
					// stun/fakedeath is not mdef dependant, it depends on lvl difference, target CON and power of stun
					skill.getEffects(activeChar, target, ss, sps, bss);
					break;
				}
				case STUN:
					// Calculate skill evasion
					if (Formulas.calcPhysicalSkillEvasion(target, skill))
					{
						activeChar.sendPacket(new SystemMessage(SystemMessageId.ATTACK_FAILED));
						break;
					}
					// Calculate vengeance
					if (target.vengeanceSkill(skill))
					{
						target = activeChar;
					}
				case ROOT:
				{
					if (target.reflectSkill(skill))
					{
						target = activeChar;
					}
					
					if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						skill.getEffects(activeChar, target, ss, sps, bss);
					}
					else
					{
						if (activeChar instanceof L2PcInstance)
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
							sm.addString(target.getName());
							sm.addSkillName(skill.getDisplayId());
							activeChar.sendPacket(sm);
							sm = null;
						}
					}
					break;
				}
				case SLEEP:
				case PARALYZE: // use same as root for now
				{
					if (target.reflectSkill(skill))
					{
						target = activeChar;
					}
					
					if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						skill.getEffects(activeChar, target, ss, sps, bss);
					}
					else
					{
						if (activeChar instanceof L2PcInstance)
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
							sm.addString(target.getName());
							sm.addSkillName(skill.getDisplayId());
							activeChar.sendPacket(sm);
							sm = null;
						}
					}
					break;
				}
				case CONFUSION:
				case MUTE:
				{
					if (target.reflectSkill(skill))
					{
						target = activeChar;
					}
					
					if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						skill.getEffects(activeChar, target, ss, sps, bss);
					}
					else
					{
						if (activeChar instanceof L2PcInstance)
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
							sm.addString(target.getName());
							sm.addSkillName(skill.getDisplayId());
							activeChar.sendPacket(sm);
							sm = null;
						}
					}
					break;
				}
				case CONFUSE_MOB_ONLY:
				{
					// do nothing if not on mob
					if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						final L2Effect[] effects = target.getAllEffects();
						for (final L2Effect e : effects)
						{
							if (e.getSkill().getSkillType() == type)
							{
								e.exit(false);
							}
						}
						skill.getEffects(activeChar, target, ss, sps, bss);
					}
					else
					{
						if (activeChar instanceof L2PcInstance)
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
							activeChar.sendPacket(sm);
							sm = null;
						}
					}
				}
				case AGGDAMAGE:
				{
					if (target instanceof L2Attackable)
					{
						target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, (int) ((150 * skill.getPower()) / (target.getLevel() + 7)));
					}
					// TODO [Nemesiss] should this have 100% chance?
					skill.getEffects(activeChar, target, ss, sps, bss);
					break;
				}
				case AGGREDUCE:
				{
					// these skills needs to be rechecked
					if (target instanceof L2Attackable)
					{
						skill.getEffects(activeChar, target, ss, sps, bss);
						
						final double aggdiff = ((L2Attackable) target).getHating(activeChar) - target.calcStat(Stats.AGGRESSION, ((L2Attackable) target).getHating(activeChar), target, skill);
						
						if (skill.getPower() > 0)
						{
							((L2Attackable) target).reduceHate(null, (int) skill.getPower());
						}
						else if (aggdiff > 0)
						{
							((L2Attackable) target).reduceHate(null, (int) aggdiff);
						}
					}
					break;
				}
				case AGGREDUCE_CHAR:
				{
					// these skills needs to be rechecked
					if (skill.getName().equals("Bluff"))
					{
						if (target instanceof L2Attackable)
						{
							L2Attackable targetAttack = (L2Attackable) target;
							targetAttack.stopHating(activeChar);
							if (targetAttack.getMostHated() == null)
							{
								((L2AttackableAI) targetAttack.getAI()).setGlobalAggro(-25);
								targetAttack.clearAggroList();
								targetAttack.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
								targetAttack.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
								targetAttack.setWalking();
							}
							targetAttack = null;
						}
						skill.getEffects(activeChar, target, ss, sps, bss);
					}
					else if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						
						if (target instanceof L2Attackable)
						{
							L2Attackable targ = (L2Attackable) target;
							targ.stopHating(activeChar);
							if (targ.getMostHated() == null)
							{
								((L2AttackableAI) targ.getAI()).setGlobalAggro(-25);
								targ.clearAggroList();
								targ.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
								targ.setWalking();
							}
							targ = null;
						}
						skill.getEffects(activeChar, target, ss, sps, bss);
					}
					else
					{
						if (activeChar instanceof L2PcInstance)
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
							sm.addString(target.getName());
							sm.addSkillName(skill.getId());
							activeChar.sendPacket(sm);
							sm = null;
						}
					}
					break;
				}
				case AGGREMOVE:
				{
					// these skills needs to be rechecked
					if (target instanceof L2Attackable && !target.isRaid())
					{
						if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
						{
							if (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_UNDEAD)
							{
								if (target.isUndead())
								{
									((L2Attackable) target).reduceHate(null, ((L2Attackable) target).getHating(((L2Attackable) target).getMostHated()));
								}
							}
							else
							{
								((L2Attackable) target).reduceHate(null, ((L2Attackable) target).getHating(((L2Attackable) target).getMostHated()));
							}
						}
						else
						{
							if (activeChar instanceof L2PcInstance)
							{
								SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
								sm.addString(target.getName());
								sm.addSkillName(skill.getId());
								activeChar.sendPacket(sm);
								sm = null;
							}
						}
					}
					break;
				}
				case UNBLEED:
				{
					negateEffect(target, SkillType.BLEED, skill.getPower());
					break;
				}
				case UNPOISON:
				{
					negateEffect(target, SkillType.POISON, skill.getPower());
					break;
				}
				case ERASE:
				{
					if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss)
						// Doesn't affect siege golem, wild hog cannon and Pets
						&& !(target instanceof L2SiegeSummonInstance) && !(target instanceof L2PetInstance))
					{
						L2PcInstance summonOwner = null;
						L2Summon summonPet = null;
						summonOwner = ((L2Summon) target).getOwner();
						summonPet = summonOwner.getPet();
						summonPet.unSummon(summonOwner);
						summonPet = null;
						SystemMessage sm = new SystemMessage(SystemMessageId.LETHAL_STRIKE);
						summonOwner.sendPacket(sm);
						sm = null;
					}
					else
					{
						if (activeChar instanceof L2PcInstance)
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
							sm.addString(target.getName());
							sm.addSkillName(skill.getId());
							activeChar.sendPacket(sm);
							sm = null;
						}
					}
					break;
				}
				case MAGE_BANE:
				{
					
					for (final L2Object t : targets)
					{
						L2Character target1 = (L2Character) t;
						
						if (target1.reflectSkill(skill))
						{
							target1 = activeChar;
						}
						if (!Formulas.getInstance().calcSkillSuccess(activeChar, target1, skill, ss, sps, bss))
						{
							continue;
						}
						final L2Effect[] effects = target1.getAllEffects();
						for (final L2Effect e : effects)
						{
							
							if (e.getStackType().equals("mAtkSpeedUp") || e.getStackType().equals("mAtk") || e.getSkill().getId() == 1059 || e.getSkill().getId() == 1085 || e.getSkill().getId() == 4356 || e.getSkill().getId() == 4355)
							{
								e.exit();
							}
						}
					}
					break;
				}
				case WARRIOR_BANE:
				{
					for (final L2Object t : targets)
					{
						L2Character target1 = (L2Character) t;
						if (target1.reflectSkill(skill))
						{
							target1 = activeChar;
						}
						if (!Formulas.getInstance().calcSkillSuccess(activeChar, target1, skill, ss, sps, bss))
						{
							continue;
						}
						final L2Effect[] effects = target1.getAllEffects();
						for (final L2Effect e : effects)
						{
							if (e.getStackType().equals("SpeedUp") || e.getStackType().equals("pAtkSpeedUp") || e.getSkill().getId() == 1204 || e.getSkill().getId() == 1086 || e.getSkill().getId() == 4342 || e.getSkill().getId() == 4357)
							{
								e.exit();
							}
						}
					}
					break;
				}
				case CANCEL:
				{
					if (target.reflectSkill(skill))
					{
						target = activeChar;
					}
					
					if (skill.getId() == 1056)
					{
						// If target isInvul (for example Celestial shield) CANCEL doesn't work
						if (target.isInvul())
						{
							if (activeChar instanceof L2PcInstance)
							{
								SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
								sm.addString(target.getName());
								sm.addSkillName(skill.getDisplayId());
								activeChar.sendPacket(sm);
								sm = null;
							}
							break;
						}
						if (target.isRaid())
						{
							if (activeChar instanceof L2PcInstance)
							{
								SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
								sm.addString(target.getName());
								sm.addSkillName(skill.getDisplayId());
								activeChar.sendPacket(sm);
								sm = null;
							}
							break;
						}
						int lvlmodifier = 52 + skill.getLevel() * 2;
						if (skill.getLevel() == 12)
						{
							lvlmodifier = (ExperienceData.getInstance().getMaxLevel() - 1);
						}
						
						int landrate = (int) skill.getPower();
						if ((target.getLevel() - lvlmodifier) > 0)
						{
							landrate = 90 - 4 * (target.getLevel() - lvlmodifier);
						}
						
						landrate = (int) target.calcStat(Stats.CANCEL_VULN, landrate, target, null);
						
						if (Rnd.get(100) < landrate)
						{
							L2Effect[] effects = target.getAllEffects();
							int maxfive = 5;
							for (final L2Effect e : effects)
							{
								switch (e.getEffectType())
								{
									case SIGNET_GROUND:
									case SIGNET_EFFECT:
										continue;
								}
								
								if (e.getSkill().getId() != 4082 && e.getSkill().getId() != 4215 && e.getSkill().getId() != 5182 && e.getSkill().getId() != 4515 && e.getSkill().getId() != 110 && e.getSkill().getId() != 111 && e.getSkill().getId() != 1323 && e.getSkill().getId() != 1325)
								// Cannot cancel skills 4082, 4215, 4515, 110, 111, 1323, 1325
								{
									if (e.getSkill().getSkillType() != SkillType.BUFF)
									{
										e.exit(true);
									}
									else
									{
										int rate = 100;
										final int level = e.getLevel();
										if (level > 0)
										{
											rate = Integer.valueOf(150 / (1 + level));
										}
										
										if (rate > 95)
										{
											rate = 95;
										}
										else if (rate < 5)
										{
											rate = 5;
										}
										
										if (Rnd.get(100) < rate)
										{
											e.exit(true);
											maxfive--;
											if (maxfive == 0)
											{
												break;
											}
										}
									}
								}
							}
							effects = null;
						}
						else
						{
							if (activeChar instanceof L2PcInstance)
							{
								SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
								sm.addString(target.getName());
								sm.addSkillName(skill.getDisplayId());
								activeChar.sendPacket(sm);
								sm = null;
							}
						}
						break;
					}
					
					int landrate = (int) skill.getPower();
					landrate = (int) target.calcStat(Stats.CANCEL_VULN, landrate, target, null);
					if (Rnd.get(100) < landrate)
					{
						final L2Effect[] effects = target.getAllEffects();
						int maxdisp = (int) skill.getNegatePower();
						if (maxdisp == 0)
						{
							maxdisp = Config.BUFFS_MAX_AMOUNT + Config.DEBUFFS_MAX_AMOUNT + 6;
						}
						for (final L2Effect e : effects)
						{
							switch (e.getEffectType())
							{
								case SIGNET_GROUND:
								case SIGNET_EFFECT:
									continue;
							}
							
							if (e.getSkill().getId() != 4082 && e.getSkill().getId() != 4215 && e.getSkill().getId() != 5182 && e.getSkill().getId() != 4515 && e.getSkill().getId() != 110 && e.getSkill().getId() != 111 && e.getSkill().getId() != 1323 && e.getSkill().getId() != 1325)
							{
								if (e.getSkill().getSkillType() == SkillType.BUFF)
								{
									int rate = 100;
									final int level = e.getLevel();
									if (level > 0)
									{
										rate = Integer.valueOf(150 / (1 + level));
									}
									
									if (rate > 95)
									{
										rate = 95;
									}
									else if (rate < 5)
									{
										rate = 5;
									}
									
									if (Rnd.get(100) < rate)
									{
										e.exit(true);
										maxdisp--;
										if (maxdisp == 0)
										{
											break;
										}
									}
								}
							}
						}
						// effects = null;
					}
					else
					{
						if (activeChar instanceof L2PcInstance)
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
							sm.addString(target.getName());
							sm.addSkillName(skill.getDisplayId());
							activeChar.sendPacket(sm);
							sm = null;
						}
					}
					break;
				}
				case NEGATE:
				{
					if (skill.getId() == 2275) // fishing potion
					{
						negatePower = skill.getNegatePower();
						negateId = skill.getNegateId();
						negateEffect(target, SkillType.BUFF, negatePower, negateId);
					}
					else
					// all others negate type skills
					{
						negateSkillTypes = skill.getNegateSkillTypes();
						negateEffectTypes = skill.getNegateEffectTypes();
						negatePower = skill.getNegatePower();
						
						for (String stat : negateSkillTypes)
						{
							stat = stat.toLowerCase().intern();
							if (stat == "buff")
							{
								int lvlmodifier = 52 + skill.getMagicLevel() * 2;
								if (skill.getMagicLevel() == 12)
								{
									lvlmodifier = (ExperienceData.getInstance().getMaxLevel() - 1);
								}
								
								int landrate = 90;
								if ((target.getLevel() - lvlmodifier) > 0)
								{
									landrate = 90 - 4 * (target.getLevel() - lvlmodifier);
								}
								
								landrate = (int) target.calcStat(Stats.CANCEL_VULN, landrate, target, null);
								
								if (Rnd.get(100) < landrate)
								{
									negateEffect(target, SkillType.BUFF, -1);
								}
							}
							if (stat == "debuff")
							{
								negateEffect(target, SkillType.DEBUFF, -1);
							}
							
							if (stat == "weakness")
							{
								negateEffect(target, SkillType.WEAKNESS, -1);
							}
							
							if (stat == "stun")
							{
								negateEffect(target, SkillType.STUN, -1);
							}
							
							if (stat == "sleep")
							{
								negateEffect(target, SkillType.SLEEP, -1);
							}
							
							if (stat == "mdam")
							{
								negateEffect(target, SkillType.MDAM, -1);
							}
							
							if (stat == "confusion")
							{
								negateEffect(target, SkillType.CONFUSION, -1);
							}
							
							if (stat == "mute")
							{
								negateEffect(target, SkillType.MUTE, -1);
							}
							
							if (stat == "fear")
							{
								negateEffect(target, SkillType.FEAR, -1);
							}
							
							if (stat == "poison")
							{
								negateEffect(target, SkillType.POISON, negatePower);
							}
							
							if (stat == "bleed")
							{
								negateEffect(target, SkillType.BLEED, negatePower);
							}
							
							if (stat == "paralyze")
							{
								negateEffect(target, SkillType.PARALYZE, -1);
							}
							
							if (stat == "root")
							{
								negateEffect(target, SkillType.ROOT, -1);
							}
							
							if (stat == "heal")
							{
								ISkillHandler Healhandler = SkillHandler.getInstance().getSkillHandler(SkillType.HEAL);
								if (Healhandler == null)
								{
									LOGGER.warn("Couldn't find skill handler for HEAL.");
									continue;
								}
								
								final L2Object tgts[] = new L2Object[]
								{
									target
								};
								try
								{
									Healhandler.useSkill(activeChar, skill, tgts);
								}
								catch (final IOException e)
								{
									if (Config.ENABLE_ALL_EXCEPTIONS)
									{
										e.printStackTrace();
									}
									
									LOGGER.warn("", e);
								}
								Healhandler = null;
							}
						} // end for
						
						for (final String stat : negateEffectTypes)
						{
							EffectType effect_type = null;
							try
							{
								effect_type = EffectType.valueOf(stat.toUpperCase());
							}
							catch (final Exception e)
							{
								//
							}
							
							if (effect_type != null)
							{
								switch (effect_type)
								{
									case BUFF:
									{
										int lvlmodifier = 52 + skill.getMagicLevel() * 2;
										if (skill.getMagicLevel() == 12)
										{
											lvlmodifier = (ExperienceData.getInstance().getMaxLevel() - 1);
										}
										
										int landrate = 90;
										if ((target.getLevel() - lvlmodifier) > 0)
										{
											landrate = 90 - 4 * (target.getLevel() - lvlmodifier);
										}
										
										landrate = (int) target.calcStat(Stats.CANCEL_VULN, landrate, target, null);
										
										if (Rnd.get(100) < landrate)
										{
											target.stopEffects(effect_type);
										}
									}
										break;
									default:
									{
										target.stopEffects(effect_type);
									}
										break;
								}
							}
							
						} // end for
					} // end else
				} // end case
			}// end switch
			target = null;
		} // end for
		
		if (skill.isMagic())
		{
			if (bss)
			{
				activeChar.removeBss();
			}
			else if (sps)
			{
				activeChar.removeSps();
			}
			
		}
		else
		{
			
			activeChar.removeSs();
			
		}
		
		// self Effect :]
		L2Effect effect = activeChar.getFirstEffect(skill.getId());
		if (effect != null && effect.isSelfEffect())
		{
			// Replace old effect with new one.
			effect.exit(false);
		}
		effect = null;
		skill.getEffectsSelf(activeChar);
		
	} // end void
	
	private void negateEffect(final L2Character target, final SkillType type, final double power)
	{
		negateEffect(target, type, power, 0);
	}
	
	private void negateEffect(final L2Character target, final SkillType type, final double power, final int skillId)
	{
		L2Effect[] effects = target.getAllEffects();
		for (final L2Effect e : effects)
		{
			if (e.getSkill() != null && e.getSkill().getId() == 4215 || e.getSkill().getId() == 4515)
			{
				continue; // skills cannot be removed
			}
			else if (power == -1) // if power is -1 the effect is always removed without power/lvl check ^^
			{
				if (e.getSkill().getSkillType() == type || (e.getSkill().getEffectType() != null && e.getSkill().getEffectType() == type))
				{
					if (skillId != 0)
					{
						if (skillId == e.getSkill().getId())
						{
							e.exit(true);
						}
					}
					else
					{
						e.exit(true);
					}
				}
			}
			else if ((e.getSkill().getSkillType() == type && e.getSkill().getPower() <= power) || (e.getSkill().getEffectType() != null && e.getSkill().getEffectType() == type && e.getSkill().getEffectLvl() <= power))
			{
				if (skillId != 0)
				{
					if (skillId == e.getSkill().getId())
					{
						e.exit(true);
					}
				}
				else
				{
					e.exit(true);
				}
			}
		}
		effects = null;
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
