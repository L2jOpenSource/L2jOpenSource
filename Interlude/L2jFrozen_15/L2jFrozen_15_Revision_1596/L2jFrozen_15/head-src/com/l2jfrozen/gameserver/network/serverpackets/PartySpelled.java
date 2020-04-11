package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SummonInstance;

/**
 * This class ...
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class PartySpelled extends L2GameServerPacket
{
	private final List<Effect> effects;
	private final L2Character activeChar;
	
	private class Effect
	{
		protected int skillId;
		protected int dat;
		protected int duration;
		
		public Effect(final int pSkillId, final int pDat, final int pDuration)
		{
			skillId = pSkillId;
			dat = pDat;
			duration = pDuration;
		}
	}
	
	public PartySpelled(final L2Character cha)
	{
		effects = new ArrayList<>();
		activeChar = cha;
	}
	
	@Override
	protected final void writeImpl()
	{
		if (activeChar == null)
		{
			return;
		}
		writeC(0xee);
		writeD(activeChar instanceof L2SummonInstance ? 2 : activeChar instanceof L2PetInstance ? 1 : 0);
		writeD(activeChar.getObjectId());
		writeD(effects.size());
		for (final Effect temp : effects)
		{
			writeD(temp.skillId);
			writeH(temp.dat);
			writeD(temp.duration / 1000);
		}
		
	}
	
	public void addPartySpelledEffect(final int skillId, final int dat, final int duration)
	{
		effects.add(new Effect(skillId, dat, duration));
	}
	
	@Override
	public String getType()
	{
		return "[S] EE PartySpelled";
	}
}
