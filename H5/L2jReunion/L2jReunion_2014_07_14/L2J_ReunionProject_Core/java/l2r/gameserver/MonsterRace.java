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
package l2r.gameserver;

import java.lang.reflect.Constructor;

import l2r.gameserver.datatables.sql.NpcTable;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.util.Rnd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonsterRace
{
	protected static final Logger _log = LoggerFactory.getLogger(MonsterRace.class);
	
	private final L2Npc[] _monsters;
	private Constructor<?> _constructor;
	private int[][] _speeds;
	private final int[] _first, _second;
	
	protected MonsterRace()
	{
		_monsters = new L2Npc[8];
		_speeds = new int[8][20];
		_first = new int[2];
		_second = new int[2];
	}
	
	public static MonsterRace getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public void newRace()
	{
		int random = 0;
		
		for (int i = 0; i < 8; i++)
		{
			int id = 31003;
			random = Rnd.get(24);
			while (true)
			{
				for (int j = i - 1; j >= 0; j--)
				{
					if (_monsters[j].getTemplate().getId() == (id + random))
					{
						random = Rnd.get(24);
						continue;
					}
				}
				break;
			}
			try
			{
				L2NpcTemplate template = NpcTable.getInstance().getTemplate(id + random);
				_constructor = Class.forName("l2r.gameserver.model.actor.instance." + template.getType() + "Instance").getConstructors()[0];
				int objectId = IdFactory.getInstance().getNextId();
				_monsters[i] = (L2Npc) _constructor.newInstance(objectId, template);
			}
			catch (Exception e)
			{
				_log.warn(String.valueOf(e));
			}
			// _log.info("Monster "+i+" is id: "+(id+random));
		}
		newSpeeds();
	}
	
	public void newSpeeds()
	{
		_speeds = new int[8][20];
		int total = 0;
		_first[1] = 0;
		_second[1] = 0;
		for (int i = 0; i < 8; i++)
		{
			total = 0;
			for (int j = 0; j < 20; j++)
			{
				if (j == 19)
				{
					_speeds[i][j] = 100;
				}
				else
				{
					_speeds[i][j] = Rnd.get(60) + 65;
				}
				total += _speeds[i][j];
			}
			if (total >= _first[1])
			{
				_second[0] = _first[0];
				_second[1] = _first[1];
				_first[0] = 8 - i;
				_first[1] = total;
			}
			else if (total >= _second[1])
			{
				_second[0] = 8 - i;
				_second[1] = total;
			}
		}
	}
	
	/**
	 * @return Returns the monsters.
	 */
	public L2Npc[] getMonsters()
	{
		return _monsters;
	}
	
	/**
	 * @return Returns the speeds.
	 */
	public int[][] getSpeeds()
	{
		return _speeds;
	}
	
	public int getFirstPlace()
	{
		return _first[0];
	}
	
	public int getSecondPlace()
	{
		return _second[0];
	}
	
	private static class SingletonHolder
	{
		protected static final MonsterRace _instance = new MonsterRace();
	}
}
