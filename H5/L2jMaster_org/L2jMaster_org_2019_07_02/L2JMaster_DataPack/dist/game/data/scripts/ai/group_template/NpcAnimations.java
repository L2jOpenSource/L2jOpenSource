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
package ai.group_template;

import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.util.Util;

import ai.npc.AbstractNpcAI;

/**
 * @author MaGa
 */
public class NpcAnimations extends AbstractNpcAI
{
	private static final int[] SPAWN_ANIMATION =
	{
		// Pets
		1538, // Baby Rudolph
		1561, // Baby Rudolph
		1562, // Desheloph
		1563, // Hyum
		1564, // Lekang
		1565, // Lilias
		1566, // Lapham
		1567, // Mafum
		1568, // Desheloph
		1569, // Hyum
		1570, // Lekang
		1571, // Lilias
		1572, // Lapham
		1573, // Mafum
		1601, // Super Feline Queen Z
		1602, // Super Kat the Cat Z
		1603, // Super Mew the Cat Z
		12077, // Wolf
		12311, // Hatchling of the Wind
		12312, // Hatchling of the Star
		12313, // Hatchling of Twilight
		12526, // Wind Strider
		12527, // Star Strider
		12528, // Twilight Strider
		12564, // Sin Eater
		12780, // Baby Buffalo
		12781, // Baby Kookaburra
		12782, // Baby Cougar
		16025, // Great Wolf
		16030, // Great Wolf
		16037, // Great Snow Wolf
		16038, // Red Wind Strider
		16039, // Red Star Strider
		16040, // Red Twilight Strider
		16034, // Improved Baby Buffalo
		16035, // Improved Baby Kookaburra
		16036, // Improved Baby Cougar
		16041, // Fenrir
		16042, // Snow Fenrir
		16043, // Fox Shaman
		16044, // Wild Beast Fighter
		16045, // White Weasel
		16046, // Fairy Princess
		16051, // Spirit Shaman
		16052, // Toy Knight
		16053, // Turtle ascetic
		16050, // Owl Monk
		16067, // Deinonychus
		16068, // Guardian's Strider
		16071, // Maguen
		16072, // Elite Maguen
		
		// Pagan Temple
		22139, // Old Aristocrat's Soldier
		22140, // Zombie Worker
		22141, // Forgotten Victim
		22147, // Ritual Offering
		22149, // Ritual Offering
		22175, // Andreas' Captain of the Royal Guard
		
		// Seed of Annihilation
		22746, // Bgurent
		22747, // Brakian
		22748, // Groikan
		22749, // Treykan
		22750, // Elite Bgurent
		22751, // Elite Brakian
		22752, // Elite Groikan
		22753, // Elite Treykan
		22754, // Turtlelian
		22755, // Krajian
		22756, // Tardyon
		22757, // Elite Turtlelian
		22758, // Elite Krajian
		22759, // Elite Tardyon
		22760, // Kanibi
		22761, // Kiriona
		22762, // Kaiona
		22763, // Elite Kanibi
		22764, // Elite Kiriona
		22765, // Elite Kaiona
		
		// Antharas Lair
		22844, // Dragon Knight
		22845, // Dragon Knight
		22846, // Elite Dragon Knight
		22847, // Dragon Knight Warrior
		
		// Hunters Village
		27185, // Fairy Tree of Wind
		27186, // Fairy Tree of Star
		27187, // Fairy Tree of Twilight
		27188, // Fairy Tree of Abyss
		
		// Grand Boss
		29001, // Queen Ant
		29006, // Core
		29022, // Zaken
		
		// Seven Signs
		31095, // Gatekeeper Ziggurat
		31096, // Gatekeeper Ziggurat
		31097, // Gatekeeper Ziggurat
		31098, // Gatekeeper Ziggurat
		31099, // Gatekeeper Ziggurat
		31100, // Gatekeeper Ziggurat
		31101, // Gatekeeper Ziggurat
		31102, // Gatekeeper Ziggurat
		31103, // Gatekeeper Ziggurat
		31104, // Gatekeeper Ziggurat
		31105, // Gatekeeper Ziggurat
		31106, // Gatekeeper Ziggurat
		31107, // Gatekeeper Ziggurat
		31108, // Gatekeeper Ziggurat
		31109, // Gatekeeper Ziggurat
		31110, // Gatekeeper Ziggurat
		31111, // Gatekeeper Spirit
		31112, // Gatekeeper Spirit
		31114, // Gatekeeper Ziggurat
		31115, // Gatekeeper Ziggurat
		31116, // Gatekeeper Ziggurat
		31117, // Gatekeeper Ziggurat
		31118, // Gatekeeper Ziggurat
		31119, // Gatekeeper Ziggurat
		31120, // Gatekeeper Ziggurat
		31121, // Gatekeeper Ziggurat
		31122, // Gatekeeper Ziggurat
		31123, // Gatekeeper Ziggurat
		31124, // Gatekeeper Ziggurat
		31125, // Gatekeeper Ziggurat
	};
	
	public NpcAnimations()
	{
		super(NpcAnimations.class.getSimpleName(), "ai/group_template");
		addSpawnId(SPAWN_ANIMATION);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if (Util.contains(SPAWN_ANIMATION, npc.getId()))
		{
			npc.setShowSummonAnimation(true);
		}
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new NpcAnimations();
	}
}