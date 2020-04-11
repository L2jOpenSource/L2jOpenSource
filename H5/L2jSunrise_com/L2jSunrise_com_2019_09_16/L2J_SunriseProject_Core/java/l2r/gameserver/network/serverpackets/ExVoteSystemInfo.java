/*
 * Copyright (C) 2004-2015 L2J Server
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

import l2r.gameserver.model.actor.instance.L2PcInstance;

/**
 * ExVoteSystemInfo packet implementation.
 * @author vGodFather
 */
public class ExVoteSystemInfo extends L2GameServerPacket
{
	private final int _receivedRec, _givingRec, _bonusTimeLeft, _bonusPercent;
	private final boolean _showTimer;
	
	public ExVoteSystemInfo(L2PcInstance player)
	{
		_receivedRec = player.getRecomLeft();
		_givingRec = player.getRecomHave();
		_bonusTimeLeft = player.getRecomBonusTime();
		_bonusPercent = player.getRecomBonus();
		_showTimer = !player.isRecomTimerActive() || player.isHourglassEffected();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xC9);
		writeD(_receivedRec);
		writeD(_givingRec);
		writeD(_bonusTimeLeft);
		writeD(_bonusPercent);
		writeD(_showTimer ? 0x01 : 0x00); // 0-show timer, 1-paused (if _bonusTime > 0) otherwise Quit
	}
}
