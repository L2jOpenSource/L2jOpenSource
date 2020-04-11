/*
 * Copyright (C) 2004-2019 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package custom.CustomClanHalls;

import com.l2jserver.Config;
import com.l2jserver.gameserver.model.quest.Quest;

/**
 * @author Gnacik
 */
public class CustomClanHalls extends Quest
{
	public CustomClanHalls(int id, String name, String descr)
	{
		super(id, name, descr);
		
		if (Config.USE_CUSTOM_CLANHALLS)
		{
			_log.info("Custom ClanHalls: Enabled.");
			// Titanum Chamber
			addSpawn(35441, 80769, 56740, -1545, 17358, false, 0);
			
			// Knights Chamber
			addSpawn(35443, 82976, 56275, -1510, 16905, false, 0);
			
			// Phoenix Chamber
			addSpawn(35451, 81775, 53138, -1483, 49700, false, 0);
			
			// Waterfall Hall
			addSpawn(35441, 113954, 217226, -3628, 32964, false, 0);
			
			// Giants Hall
			addSpawn(35443, 114047, 222708, -3626, 16058, false, 0);
			
			// Earth Hall
			addSpawn(35441, 108620, 222411, -3599, 17251, false, 0);
			
			// Wenus Chamber
			addSpawn(35443, 108967, 218009, -3645, 16818, false, 0);
			
			// Saturn Chamber
			addSpawn(35443, 107716, 220589, -3584, 193, false, 0);
			
			// Hunters Hall
			addSpawn(35441, 120931, 77111, -2128, 51052, false, 0);
			
			// Forbidden Hall
			addSpawn(35443, 119833, 78450, -1802, 7152, false, 0);
			
			// Enchanted Hall
			addSpawn(35441, 118931, 79701, -1596, 22723, false, 0);
			
			// Lion Hall
			addSpawn(35443, 17274, 169753, -3483, 49151, false, 0);
			
			// Puma Hall
			addSpawn(35443, 17874, 170637, -3488, 16383, false, 0);
		}
	}
	
	public static void main(String[] args)
	{
		new CustomClanHalls(-1, "CustomClanHalls", "custom");
	}
}