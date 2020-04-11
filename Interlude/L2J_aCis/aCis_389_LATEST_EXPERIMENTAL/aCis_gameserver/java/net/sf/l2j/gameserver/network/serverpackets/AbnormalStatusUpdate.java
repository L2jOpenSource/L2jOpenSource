package net.sf.l2j.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.gameserver.model.holder.EffectHolder;
import net.sf.l2j.gameserver.skills.L2Skill;

public class AbnormalStatusUpdate extends L2GameServerPacket
{
	private final List<EffectHolder> _effects = new ArrayList<>();
	
	public AbnormalStatusUpdate()
	{
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x7f);
		
		writeH(_effects.size());
		for (EffectHolder holder : _effects)
		{
			writeD(holder.getId());
			writeH(holder.getValue());
			writeD((holder.getDuration() == -1) ? -1 : holder.getDuration() / 1000);
		}
	}
	
	public void addEffect(L2Skill skill, int duration)
	{
		_effects.add(new EffectHolder(skill, duration));
	}
}