package com.l2jfrozen.gameserver.skills.l2skills;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.skills.Formulas;
import com.l2jfrozen.gameserver.templates.StatsSet;

public class L2SkillElemental extends L2Skill
{
	
	private final int[] seeds;
	private final boolean seedAny;
	
	public L2SkillElemental(final StatsSet set)
	{
		super(set);
		
		seeds = new int[3];
		seeds[0] = set.getInteger("seed1", 0);
		seeds[1] = set.getInteger("seed2", 0);
		seeds[2] = set.getInteger("seed3", 0);
		
		if (set.getInteger("seed_any", 0) == 1)
		{
			seedAny = true;
		}
		else
		{
			seedAny = false;
		}
	}
	
	@Override
	public void useSkill(final L2Character activeChar, final L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
		{
			return;
		}
		
		final boolean sps = activeChar.checkSps();
		final boolean bss = activeChar.checkBss();
		
		for (final L2Object target2 : targets)
		{
			final L2Character target = (L2Character) target2;
			if (target.isAlikeDead())
			{
				continue;
			}
			
			boolean charged = true;
			if (!seedAny)
			{
				for (final int seed : seeds)
				{
					if (seed != 0)
					{
						final L2Effect e = target.getFirstEffect(seed);
						if (e == null || !e.getInUse())
						{
							charged = false;
							break;
						}
					}
				}
			}
			else
			{
				charged = false;
				for (final int seed : seeds)
				{
					if (seed != 0)
					{
						final L2Effect e = target.getFirstEffect(seed);
						if (e != null && e.getInUse())
						{
							charged = true;
							break;
						}
					}
				}
			}
			if (!charged)
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString("Target is not charged by elements.");
				activeChar.sendPacket(sm);
				continue;
			}
			
			final boolean mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, this));
			
			final int damage = (int) Formulas.calcMagicDam(activeChar, target, this, sps, bss, mcrit);
			
			if (damage > 0)
			{
				target.reduceCurrentHp(damage, activeChar);
				
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				
				activeChar.sendDamageMessage(target, damage, false, false, false);
				
			}
			
			// activate attacked effects, if any
			target.stopSkillEffects(getId());
			getEffects(activeChar, target, false, sps, bss);
		}
		
		if (bss)
		{
			activeChar.removeBss();
		}
		else if (sps)
		{
			activeChar.removeSps();
		}
		
	}
}
