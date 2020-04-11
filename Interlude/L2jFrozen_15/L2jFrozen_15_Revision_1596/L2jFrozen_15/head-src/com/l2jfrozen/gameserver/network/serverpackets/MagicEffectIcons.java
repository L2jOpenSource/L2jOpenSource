package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

/**
 * MagicEffectIcons format h (dhd)
 * @version $Revision: 1.3.2.1.2.6 $ $Date: 2005/04/05 19:41:08 $
 */
public class MagicEffectIcons extends L2GameServerPacket
{
	private final List<Effect> effects;
	private final List<Effect> debuffs;
	
	private class Effect
	{
		protected int skillId;
		protected int level;
		protected int duration;
		
		public Effect(final int pSkillId, final int pLevel, final int pDuration)
		{
			skillId = pSkillId;
			level = pLevel;
			duration = pDuration;
		}
	}
	
	public MagicEffectIcons()
	{
		effects = new ArrayList<>();
		debuffs = new ArrayList<>();
	}
	
	public void addEffect(final int skillId, final int level, final int duration, final boolean debuff)
	{
		if (skillId == 2031 || skillId == 2032 || skillId == 2037)
		{
			return;
		}
		
		if (debuff)
		{
			debuffs.add(new Effect(skillId, level, duration));
		}
		else
		{
			effects.add(new Effect(skillId, level, duration));
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x7f);
		
		writeH(effects.size() + debuffs.size());
		
		for (final Effect temp : effects)
		{
			writeD(temp.skillId);
			writeH(temp.level);
			
			if (temp.duration == -1)
			{
				writeD(-1);
			}
			else
			{
				writeD(temp.duration / 1000);
			}
		}
		
		for (final Effect temp : debuffs)
		{
			writeD(temp.skillId);
			writeH(temp.level);
			
			if (temp.duration == -1)
			{
				writeD(-1);
			}
			else
			{
				writeD(temp.duration / 1000);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] 7f MagicEffectIcons";
	}
}