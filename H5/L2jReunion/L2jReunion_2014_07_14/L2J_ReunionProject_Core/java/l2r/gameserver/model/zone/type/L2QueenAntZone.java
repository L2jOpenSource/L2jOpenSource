/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.zone.type;

import l2r.Config;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2GrandBossInstance;
import l2r.gameserver.model.zone.L2ZoneType;

/**
 * @author -=DoctorNo=-
 */
public class L2QueenAntZone extends L2ZoneType
{
	public L2QueenAntZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (character.isPlayer())
		{
			if (!character.isGM() && (Config.QUEEN_ANT_CHAR_ENTER_LEVEL_RESTRICTION > 0) && (character.getLevel() > Config.QUEEN_ANT_CHAR_ENTER_LEVEL_RESTRICTION))
			{
				character.teleToLocation(-14417, 123749, -3117);
			}
		}
	}
	
	@Override
	protected void onExit(final L2Character character)
	{
		if (character instanceof L2GrandBossInstance)
		{
			if (((L2Npc) character).getId() == 29001)
			{
				character.teleToLocation(-21610, 181594, -5734);
			}
		}
	}
	
	@Override
	public void onDieInside(L2Character character)
	{
		
	}
	
	@Override
	public void onReviveInside(L2Character character)
	{
		
	}
}