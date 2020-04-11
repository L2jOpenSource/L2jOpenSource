package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 * @author  godson
 */
public class ExOlympiadSpelledInfo extends L2GameServerPacket
{
	// chdd(dhd)
	private final L2PcInstance player;
	private final List<Effect> effects;
	
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
	
	public ExOlympiadSpelledInfo(final L2PcInstance player)
	{
		effects = new ArrayList<>();
		this.player = player;
	}
	
	public void addEffect(final int skillId, final int dat, final int duration)
	{
		effects.add(new Effect(skillId, dat, duration));
	}
	
	@Override
	protected final void writeImpl()
	{
		if (player == null)
		{
			return;
		}
		writeC(0xfe);
		writeH(0x2a);
		writeD(player.getObjectId());
		writeD(effects.size());
		for (final Effect temp : effects)
		{
			writeD(temp.skillId);
			writeH(temp.level);
			writeD(temp.duration / 1000);
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:2A ExOlympiadSpelledInfo";
	}
}
