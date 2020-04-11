/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.network.serverpackets;

import java.util.List;

import javolution.util.FastList;
import l2r.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author godson
 */
public class ExOlympiadSpelledInfo extends L2GameServerPacket
{
	private final int _playerID;
	private final List<Effect> _effects;
	
	private static class Effect
	{
		protected int _skillId;
		protected int _level;
		protected int _duration;
		
		public Effect(int pSkillId, int pLevel, int pDuration)
		{
			_skillId = pSkillId;
			_level = pLevel;
			_duration = pDuration;
		}
	}
	
	public ExOlympiadSpelledInfo(L2PcInstance player)
	{
		_effects = new FastList<>();
		_playerID = player.getObjectId();
	}
	
	public void addEffect(int skillId, int level, int duration)
	{
		_effects.add(new Effect(skillId, level, duration));
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0x7B);
		writeD(_playerID);
		writeD(_effects.size());
		for (Effect temp : _effects)
		{
			writeD(temp._skillId);
			writeH(temp._level);
			writeD(temp._duration / 1000);
		}
	}
}
