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

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.olympiad.Participant;

/**
 * @author godson
 */
public class ExOlympiadUserInfo extends L2GameServerPacket
{
	private final L2PcInstance _player;
	private Participant _par = null;
	private int _curHp;
	private int _maxHp;
	private int _curCp;
	private int _maxCp;
	
	public ExOlympiadUserInfo(L2PcInstance player)
	{
		_player = player;
		if (_player != null)
		{
			_curHp = (int) _player.getCurrentHp();
			_maxHp = _player.getMaxHp();
			_curCp = (int) _player.getCurrentCp();
			_maxCp = _player.getMaxCp();
		}
		else
		{
			_curHp = 0;
			_maxHp = 100;
			_curCp = 0;
			_maxCp = 100;
		}
	}
	
	public ExOlympiadUserInfo(Participant par)
	{
		_par = par;
		_player = par.getPlayer();
		if (_player != null)
		{
			_curHp = (int) _player.getCurrentHp();
			_maxHp = _player.getMaxHp();
			_curCp = (int) _player.getCurrentCp();
			_maxCp = _player.getMaxCp();
		}
		else
		{
			_curHp = 0;
			_maxHp = 100;
			_curCp = 0;
			_maxCp = 100;
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0x7A);
		if (_player != null)
		{
			writeC(_player.getOlympiadSide());
			writeD(_player.getObjectId());
			writeS(_player.getName());
			writeD(_player.getClassId().getId());
		}
		else
		{
			writeC(_par.getSide());
			writeD(_par.getObjectId());
			writeS(_par.getName());
			writeD(_par.getBaseClass());
		}
		
		writeD(_curHp);
		writeD(_maxHp);
		writeD(_curCp);
		writeD(_maxCp);
	}
}