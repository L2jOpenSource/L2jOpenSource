package com.l2jfrozen.gameserver.handler.skillhandlers;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.handler.ISkillHandler;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.skills.BaseStats;
import com.l2jfrozen.gameserver.skills.Formulas;
import com.l2jfrozen.gameserver.skills.effects.EffectCharge;
import com.l2jfrozen.gameserver.templates.L2WeaponType;
import com.l2jfrozen.logs.Log;
import com.l2jfrozen.util.random.Rnd;

/**
 * This class ...
 * @version $Revision: 1.1.2.7.2.16 $ $Date: 2005/04/06 16:13:49 $
 */

public class Pdam implements ISkillHandler
{
	// all the items ids that this handler knowns
	private static Logger LOGGER = Logger.getLogger(Pdam.class);
	
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.PDAM,
		SkillType.FATALCOUNTER
		/* , SkillType.CHARGEDAM */
	};
	
	@Override
	public void useSkill(final L2Character activeChar, final L2Skill skill, final L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
		{
			return;
		}
		
		int damage = 0;
		
		if (Config.DEBUG)
		{
			LOGGER.debug("Begin Skill processing in Pdam.java " + skill.getSkillType());
		}
		
		// Calculate targets based on vegeance
		final List<L2Object> target_s = new ArrayList<>();
		
		for (final L2Object targetObject : targets)
		{
			
			target_s.add(targetObject);
			
			final L2Character target = (L2Character) targetObject;
			
			if (target.vengeanceSkill(skill))
			{
				target_s.add(activeChar);
			}
		}
		
		final boolean bss = activeChar.checkBss();
		final boolean sps = activeChar.checkSps();
		final boolean ss = activeChar.checkSs();
		
		for (final L2Object target2 : target_s)
		{
			if (target2 == null)
			{
				continue;
			}
			
			L2Character target = (L2Character) target2;
			Formulas f = Formulas.getInstance();
			L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
			
			if (activeChar instanceof L2PcInstance && target instanceof L2PcInstance && target.isAlikeDead() && target.isFakeDeath())
			{
				target.stopFakeDeath(null);
			}
			else if (target.isAlikeDead())
			{
				continue;
			}
			
			/*
			 * if(target.isInvul()){ continue; }
			 */
			
			// Calculate skill evasion
			// Formulas.getInstance();
			if (Formulas.calcPhysicalSkillEvasion(target, skill))
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.ATTACK_FAILED));
				continue;
			}
			
			final boolean dual = activeChar.isUsingDualWeapon();
			final boolean shld = Formulas.calcShldUse(activeChar, target);
			// PDAM critical chance not affected by buffs, only by STR. Only some skills are meant to crit.
			boolean crit = false;
			if (skill.getBaseCritRate() > 0)
			{
				crit = Formulas.calcCrit(skill.getBaseCritRate() * 10 * BaseStats.STR.calcBonus(activeChar));
			}
			
			boolean soul = false;
			if (weapon != null)
			{
				soul = (ss && weapon.getItemType() != L2WeaponType.DAGGER);
			}
			
			if (!crit && (skill.getCondition() & L2Skill.COND_CRIT) != 0)
			{
				damage = 0;
			}
			else
			{
				damage = (int) Formulas.calcPhysDam(activeChar, target, skill, shld, false, dual, soul);
			}
			
			if (crit)
			{
				damage *= 2; // PDAM Critical damage always 2x and not affected by buffs
			}
			
			if (damage > 50000 && Config.LOG_HIGH_DAMAGES && activeChar instanceof L2PcInstance)
			{
				String name = "";
				if (target instanceof L2RaidBossInstance)
				{
					name = "RaidBoss ";
				}
				if (target instanceof L2NpcInstance)
				{
					name += target.getName() + "(" + ((L2NpcInstance) target).getTemplate().npcId + ")";
				}
				if (target instanceof L2PcInstance)
				{
					name = target.getName() + "(" + target.getObjectId() + ") ";
				}
				name += target.getLevel() + " lvl";
				Log.add(activeChar.getName() + "(" + activeChar.getObjectId() + ") " + activeChar.getLevel() + " lvl did damage " + damage + " with skill " + skill.getName() + "(" + skill.getId() + ") to " + name, "damage_pdam");
			}
			
			if (damage > 0)
			{
				if (target != activeChar)
				{
					activeChar.sendDamageMessage(target, damage, false, crit, false);
				}
				else
				{
					final SystemMessage smsg = new SystemMessage(SystemMessageId.S1_HIT_YOU_S2_DMG);
					smsg.addString(target.getName());
					smsg.addNumber(damage);
					activeChar.sendPacket(smsg);
				}
				
				if (!target.isInvul())
				{
					
					if (skill.hasEffects())
					{
						if (target.reflectSkill(skill))
						{
							activeChar.stopSkillEffects(skill.getId());
							
							skill.getEffects(null, activeChar, ss, sps, bss);
							SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
							sm.addSkillName(skill.getId());
							activeChar.sendPacket(sm);
							sm = null;
						}
						else
						{
							// activate attacked effects, if any
							if (f.calcSkillSuccess(activeChar, target, skill, soul, false, false))
							{
								// Like L2OFF must remove the first effect if the second effect lands
								// target.stopSkillEffects(skill.getId());
								skill.getEffects(activeChar, target, ss, sps, bss);
								SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
								sm.addSkillName(skill.getId());
								target.sendPacket(sm);
								sm = null;
							}
							else
							{
								SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
								sm.addString(target.getName());
								sm.addSkillName(skill.getDisplayId());
								activeChar.sendPacket(sm);
								sm = null;
							}
						}
					}
				}
				
				// Success of lethal effect
				final int chance = Rnd.get(1000);
				if (target != activeChar && !target.isRaid() && chance < skill.getLethalChance1() && !(target instanceof L2DoorInstance) && !(target instanceof L2NpcInstance && ((L2NpcInstance) target).getNpcId() == 35062))
				{
					// 1st lethal effect activate (cp to 1 or if target is npc then hp to 50%)
					if (skill.getLethalChance2() > 0 && chance >= skill.getLethalChance2())
					{
						if (target instanceof L2PcInstance)
						{
							L2PcInstance player = (L2PcInstance) target;
							if (!player.isInvul())
							{
								player.setCurrentCp(1); // Set CP to 1
								player.reduceCurrentHp(damage, activeChar);
							}
							player = null;
						}
						else if (target instanceof L2MonsterInstance) // If is a monster remove first damage and after 50% of current hp
						{
							target.reduceCurrentHp(damage, activeChar);
							target.reduceCurrentHp(target.getCurrentHp() / 2, activeChar);
						}
						// Half Kill!
						activeChar.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
					}
					else
					// 2nd lethal effect activate (cp,hp to 1 or if target is npc then hp to 1)
					{
						// If is a monster damage is (CurrentHp - 1) so HP = 1
						if (target instanceof L2NpcInstance)
						{
							target.reduceCurrentHp(target.getCurrentHp() - 1, activeChar);
						}
						else if (target instanceof L2PcInstance) // If is a active player set his HP and CP to 1
						{
							L2PcInstance player = (L2PcInstance) target;
							if (!player.isInvul())
							{
								player.setCurrentHp(1);
								player.setCurrentCp(1);
							}
							player = null;
						}
						// Lethal Strike was succefful!
						activeChar.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
						activeChar.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE_SUCCESSFUL));
					}
				}
				else
				{
					// Make damage directly to HP
					if (skill.getDmgDirectlyToHP() || !(activeChar instanceof L2PlayableInstance))
					{
						if (target instanceof L2PcInstance)
						{
							L2PcInstance player = (L2PcInstance) target;
							if (!player.isInvul())
							{
								if (damage >= player.getCurrentHp())
								{
									if (player.isInDuel())
									{
										player.setCurrentHp(1);
									}
									else
									{
										player.setCurrentHp(0);
										if (player.isInOlympiadMode())
										{
											player.abortAttack();
											player.abortCast();
											player.getStatus().stopHpMpRegeneration();
										}
										else
										{
											player.doDie(activeChar);
										}
									}
								}
								else
								{
									player.setCurrentHp(player.getCurrentHp() - damage);
								}
							}
							SystemMessage smsg = new SystemMessage(SystemMessageId.S1_HIT_YOU_S2_DMG);
							smsg.addString(activeChar.getName());
							smsg.addNumber(damage);
							player.sendPacket(smsg);
							
							player = null;
							smsg = null;
						}
						else
						{
							target.reduceCurrentHp(damage, activeChar);
						}
					}
					else
					{
						// only players can reduce CPs each other
						if (activeChar instanceof L2PcInstance && target instanceof L2PcInstance && !target.isInvul())
						{
							final L2PcInstance player = (L2PcInstance) target;
							
							double hp_damage = 0;
							
							if (damage >= player.getCurrentCp())
							{
								final double cur_cp = player.getCurrentCp();
								hp_damage = damage - cur_cp;
								player.setCurrentCp(1);
							}
							else
							{
								final double cur_cp = player.getCurrentCp();
								player.setCurrentCp(cur_cp - damage);
								
							}
							
							if (hp_damage > 0)
							{
								player.reduceCurrentHp(damage, activeChar);
							}
						}
						else
						{
							target.reduceCurrentHp(damage, activeChar);
						}
					}
				}
			}
			else
			// No - damage
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.ATTACK_FAILED));
			}
			
			target = null;
			f = null;
			weapon = null;
			
			if (skill.getId() == 345 || skill.getId() == 346) // Sonic Rage or Raging Force
			{
				EffectCharge effect = (EffectCharge) activeChar.getFirstEffect(L2Effect.EffectType.CHARGE);
				if (effect != null)
				{
					int effectcharge = effect.getLevel();
					if (effectcharge < 7)
					{
						effectcharge++;
						effect.addNumCharges(1);
						
						activeChar.sendPacket(new EtcStatusUpdate((L2PcInstance) activeChar));
						final SystemMessage sm = new SystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1);
						sm.addNumber(effectcharge);
						activeChar.sendPacket(sm);
					}
					else
					{
						final SystemMessage sm = new SystemMessage(SystemMessageId.FORCE_MAXLEVEL_REACHED);
						activeChar.sendPacket(sm);
					}
				}
				else
				{
					if (skill.getId() == 345) // Sonic Rage
					{
						L2Skill dummy = SkillTable.getInstance().getInfo(8, 7); // Lv7 Sonic Focus
						dummy.getEffects(activeChar, activeChar, ss, sps, bss);
						dummy = null;
					}
					else if (skill.getId() == 346) // Raging Force
					{
						L2Skill dummy = SkillTable.getInstance().getInfo(50, 7); // Lv7 Focused Force
						dummy.getEffects(activeChar, activeChar, ss, sps, bss);
						dummy = null;
					}
				}
				effect = null;
			}
			// self Effect :]
			L2Effect effect = activeChar.getFirstEffect(skill.getId());
			if (effect != null && effect.isSelfEffect())
			{
				// Replace old effect with new one.
				effect.exit(false);
			}
			skill.getEffectsSelf(activeChar);
			effect = null;
		}
		
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
		
		if (skill.isSuicideAttack() && !activeChar.isInvul())
		{
			activeChar.doDie(null);
			activeChar.setCurrentHp(0);
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}