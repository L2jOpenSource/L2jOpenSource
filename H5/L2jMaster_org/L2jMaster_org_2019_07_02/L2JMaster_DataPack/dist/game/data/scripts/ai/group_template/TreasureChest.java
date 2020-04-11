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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.gameserver.datatables.SpawnTable;
import com.l2jserver.gameserver.enums.audio.Sound;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.L2Spawn;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.holders.ItemChanceHolder;
import com.l2jserver.gameserver.model.holders.SkillHolder;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.util.Util;

import ai.npc.AbstractNpcAI;

/**
 * Treasure Chest AI.
 * @version CT2.6P3
 * @author Sacrifice
 * @since 2.6.0.0
 */
public final class TreasureChest extends AbstractNpcAI
{
	private static final Logger LOG = LoggerFactory.getLogger(TreasureChest.class.getName());
	
	private static final int MAESTRO_KEY_ITEM = 21746;
	
	private static final int MAESTRO_KEY_SKILL = 22271;
	private static final int UNLOCK_SKILL = 27;
	
	private static final int TREASURE_CHEST_RESPAWN = 7200; // Retail 2 hours
	
	private static final int TREASURE_CHEST_LEVEL_21 = 18265;
	private static final int TREASURE_CHEST_LEVEL_24 = 18266;
	private static final int TREASURE_CHEST_LEVEL_27 = 18267;
	private static final int TREASURE_CHEST_LEVEL_30 = 18268;
	private static final int TREASURE_CHEST_LEVEL_33 = 18269;
	private static final int TREASURE_CHEST_LEVEL_36 = 18270;
	private static final int TREASURE_CHEST_LEVEL_39 = 18271;
	private static final int TREASURE_CHEST_LEVEL_42 = 18272;
	private static final int TREASURE_CHEST_LEVEL_45 = 18273;
	private static final int TREASURE_CHEST_LEVEL_48 = 18274;
	private static final int TREASURE_CHEST_LEVEL_51 = 18275;
	private static final int TREASURE_CHEST_LEVEL_54 = 18276;
	private static final int TREASURE_CHEST_LEVEL_57 = 18277;
	private static final int TREASURE_CHEST_LEVEL_60 = 18278;
	private static final int TREASURE_CHEST_LEVEL_63 = 18279;
	private static final int TREASURE_CHEST_LEVEL_66 = 18280;
	private static final int TREASURE_CHEST_LEVEL_69 = 18281;
	private static final int TREASURE_CHEST_LEVEL_72 = 18282;
	private static final int TREASURE_CHEST_LEVEL_75 = 18283;
	private static final int TREASURE_CHEST_LEVEL_78 = 18284;
	private static final int TREASURE_CHEST_LEVEL_81 = 18285;
	private static final int TREASURE_CHEST_LEVEL_84 = 18286;
	
	private static final int[] UNLOCK_SKILL_MAX_CHANCES =
	{
		98,
		84,
		99,
		84,
		88,
		90,
		89,
		88,
		86,
		90,
		87,
		89,
		89,
		89,
		89
	};
	
	private static final SkillHolder[] TREASURE_BOMBS = new SkillHolder[]
	{
		new SkillHolder(4143, 1),
		new SkillHolder(4143, 2),
		new SkillHolder(4143, 3),
		new SkillHolder(4143, 4),
		new SkillHolder(4143, 5),
		new SkillHolder(4143, 6),
		new SkillHolder(4143, 7),
		new SkillHolder(4143, 8),
		new SkillHolder(4143, 9),
		new SkillHolder(4143, 10)
	};
	
	private static final Map<Integer, List<ItemChanceHolder>> DROPS = new HashMap<>();
	static
	{
		DROPS.put(TREASURE_CHEST_LEVEL_21, Arrays.asList( // Treasure Chest
			new ItemChanceHolder(736, 2703, 7), // Scroll of Escape
			new ItemChanceHolder(1061, 2365, 4), // Major Healing Potion
			new ItemChanceHolder(737, 3784, 4), // Scroll of Resurrection
			new ItemChanceHolder(10260, 1136, 1), // Haste Potion
			new ItemChanceHolder(10261, 1136, 1), // Accuracy Juice
			new ItemChanceHolder(10262, 1136, 1), // Critical Damage Juice
			new ItemChanceHolder(10263, 1136, 1), // Critical Rate Juice
			new ItemChanceHolder(10264, 1136, 1), // Casting Spd. Juice
			new ItemChanceHolder(10265, 1136, 1), // Evasion Juice
			new ItemChanceHolder(10266, 1136, 1), // M. Atk. Juice
			new ItemChanceHolder(10267, 1136, 1), // P. Atk. Potion
			new ItemChanceHolder(10268, 1136, 1), // Wind Walk Juice
			new ItemChanceHolder(5593, 2365, 6), // SP Scroll (Low-grade)
			new ItemChanceHolder(5594, 1136, 1), // SP Scroll (Mid-grade)
			new ItemChanceHolder(10269, 1136, 1), // P. Def. Juice
			new ItemChanceHolder(10131, 4919, 1), // Transformation Scroll: Onyx Beast
			new ItemChanceHolder(10132, 4919, 1), // Transformation Scroll: Doom Wraith
			new ItemChanceHolder(10133, 4919, 1), // Transformation Scroll: Grail Apostle
			new ItemChanceHolder(1538, 3279, 1), // Blessed Scroll of Escape
			new ItemChanceHolder(3936, 1230, 1), // Blessed Scroll of Resurrection
			new ItemChanceHolder(68, 2617, 1), // Falchion
			new ItemChanceHolder(21747, 320, 1))); // Beginner Adventurer's Treasure Sack
		
		DROPS.put(TREASURE_CHEST_LEVEL_24, Arrays.asList( // Treasure Chest
			new ItemChanceHolder(736, 3159, 7), // Scroll of Escape
			new ItemChanceHolder(1061, 2764, 4), // Major Healing Potion
			new ItemChanceHolder(737, 4422, 4), // Scroll of Resurrection
			new ItemChanceHolder(10260, 1327, 1), // Haste Potion
			new ItemChanceHolder(10261, 1327, 1), // Accuracy Juice
			new ItemChanceHolder(10262, 1327, 1), // Critical Damage Juice
			new ItemChanceHolder(10263, 1327, 1), // Critical Rate Juice
			new ItemChanceHolder(10264, 1327, 1), // Casting Spd. Juice
			new ItemChanceHolder(10265, 1327, 1), // Evasion Juice
			new ItemChanceHolder(10266, 1327, 1), // M. Atk. Juice
			new ItemChanceHolder(10267, 1327, 1), // P. Atk. Potion
			new ItemChanceHolder(10268, 1327, 1), // Wind Walk Juice
			new ItemChanceHolder(5593, 2764, 6), // SP Scroll (Low-grade)
			new ItemChanceHolder(5594, 1327, 1), // SP Scroll (Mid-grade)
			new ItemChanceHolder(10269, 1327, 1), // P. Def. Juice
			new ItemChanceHolder(10131, 5749, 1), // Transformation Scroll: Onyx Beast
			new ItemChanceHolder(10132, 5749, 1), // Transformation Scroll: Doom Wraith
			new ItemChanceHolder(10133, 5749, 1), // Transformation Scroll: Grail Apostle
			new ItemChanceHolder(1538, 3833, 1), // Blessed Scroll of Escape
			new ItemChanceHolder(3936, 1438, 1), // Blessed Scroll of Resurrection
			new ItemChanceHolder(68, 3058, 1), // Falchion
			new ItemChanceHolder(21747, 374, 1))); // Beginner Adventurer's Treasure Sack
		
		DROPS.put(TREASURE_CHEST_LEVEL_27, Arrays.asList( // Treasure Chest
			new ItemChanceHolder(736, 3651, 7), // Scroll of Escape
			new ItemChanceHolder(1061, 3194, 4), // Major Healing Potion
			new ItemChanceHolder(737, 5111, 4), // Scroll of Resurrection
			new ItemChanceHolder(10260, 1534, 1), // Haste Potion
			new ItemChanceHolder(10261, 1534, 1), // Accuracy Juice
			new ItemChanceHolder(10262, 1534, 1), // Critical Damage Juice
			new ItemChanceHolder(10263, 1534, 1), // Critical Rate Juice
			new ItemChanceHolder(10264, 1534, 1), // Casting Spd. Juice
			new ItemChanceHolder(10265, 1534, 1), // Evasion Juice
			new ItemChanceHolder(10266, 1534, 1), // M. Atk. Juice
			new ItemChanceHolder(10267, 1534, 1), // P. Atk. Potion
			new ItemChanceHolder(10268, 1534, 1), // Wind Walk Juice
			new ItemChanceHolder(5593, 3194, 6), // SP Scroll (Low-grade)
			new ItemChanceHolder(5594, 1534, 1), // SP Scroll (Mid-grade)
			new ItemChanceHolder(10269, 1534, 1), // P. Def. Juice
			new ItemChanceHolder(10131, 6644, 1), // Transformation Scroll: Onyx Beast
			new ItemChanceHolder(10132, 6644, 1), // Transformation Scroll: Doom Wraith
			new ItemChanceHolder(10133, 6644, 1), // Transformation Scroll: Grail Apostle
			new ItemChanceHolder(1538, 4429, 1), // Blessed Scroll of Escape
			new ItemChanceHolder(3936, 1661, 1), // Blessed Scroll of Resurrection
			new ItemChanceHolder(68, 3534, 1), // Falchion
			new ItemChanceHolder(21747, 463, 1))); // Beginner Adventurer's Treasure Sack
		
		DROPS.put(TREASURE_CHEST_LEVEL_30, Arrays.asList( // Treasure Chest
			new ItemChanceHolder(736, 4200, 7), // Scroll of Escape
			new ItemChanceHolder(1061, 3675, 4), // Major Healing Potion
			new ItemChanceHolder(737, 5879, 4), // Scroll of Resurrection
			new ItemChanceHolder(10260, 1764, 1), // Haste Potion
			new ItemChanceHolder(10261, 1764, 1), // Accuracy Juice
			new ItemChanceHolder(10262, 1764, 1), // Critical Damage Juice
			new ItemChanceHolder(10263, 1764, 1), // Critical Rate Juice
			new ItemChanceHolder(10264, 1764, 1), // Casting Spd. Juice
			new ItemChanceHolder(10265, 1764, 1), // Evasion Juice
			new ItemChanceHolder(10266, 1764, 1), // M. Atk. Juice
			new ItemChanceHolder(10267, 1764, 1), // P. Atk. Potion
			new ItemChanceHolder(10268, 1764, 1), // Wind Walk Juice
			new ItemChanceHolder(5593, 3675, 6), // SP Scroll (Low-grade)
			new ItemChanceHolder(5594, 1764, 1), // SP Scroll (Mid-grade)
			new ItemChanceHolder(10269, 1764, 1), // P. Def. Juice
			new ItemChanceHolder(10134, 5095, 1), // Transformation Scroll: Unicorn
			new ItemChanceHolder(10135, 5095, 1), // Transformation Scroll: Lilim Knight
			new ItemChanceHolder(10136, 5095, 1), // Transformation Scroll: Golem Guardian
			new ItemChanceHolder(1538, 5095, 1), // Blessed Scroll of Escape
			new ItemChanceHolder(3936, 1911, 1), // Blessed Scroll of Resurrection
			new ItemChanceHolder(69, 1543, 1), // Bastard Sword
			new ItemChanceHolder(21747, 498, 1))); // Beginner Adventurer's Treasure Sack
		
		DROPS.put(TREASURE_CHEST_LEVEL_33, Arrays.asList( // Treasure Chest
			new ItemChanceHolder(736, 5010, 7), // Scroll of Escape
			new ItemChanceHolder(1061, 4383, 4), // Major Healing Potion
			new ItemChanceHolder(737, 7013, 4), // Scroll of Resurrection
			new ItemChanceHolder(10260, 2104, 1), // Haste Potion
			new ItemChanceHolder(10261, 2104, 1), // Accuracy Juice
			new ItemChanceHolder(10262, 2104, 1), // Critical Damage Juice
			new ItemChanceHolder(10263, 2104, 1), // Critical Rate Juice
			new ItemChanceHolder(10264, 2104, 1), // Casting Spd. Juice
			new ItemChanceHolder(10265, 2104, 1), // Evasion Juice
			new ItemChanceHolder(10266, 2104, 1), // M. Atk. Juice
			new ItemChanceHolder(10267, 2104, 1), // P. Atk. Potion
			new ItemChanceHolder(10268, 2104, 1), // Wind Walk Juice
			new ItemChanceHolder(5593, 4383, 6), // SP Scroll (Low-grade)
			new ItemChanceHolder(5594, 2104, 1), // SP Scroll (Mid-grade)
			new ItemChanceHolder(10269, 2104, 1), // P. Def. Juice
			new ItemChanceHolder(10134, 6078, 1), // Transformation Scroll: Unicorn
			new ItemChanceHolder(10135, 6078, 1), // Transformation Scroll: Lilim Knight
			new ItemChanceHolder(10136, 6078, 1), // Transformation Scroll: Golem Guardian
			new ItemChanceHolder(1538, 6078, 1), // Blessed Scroll of Escape
			new ItemChanceHolder(3936, 2280, 1), // Blessed Scroll of Resurrection
			new ItemChanceHolder(69, 1840, 1), // Bastard Sword
			new ItemChanceHolder(21747, 593, 1))); // Beginner Adventurer's Treasure Sack
		
		DROPS.put(TREASURE_CHEST_LEVEL_36, Arrays.asList( // Treasure Chest
			new ItemChanceHolder(736, 5894, 7), // Scroll of Escape
			new ItemChanceHolder(1061, 5157, 4), // Major Healing Potion
			new ItemChanceHolder(737, 8252, 4), // Scroll of Resurrection
			new ItemChanceHolder(10260, 2476, 1), // Haste Potion
			new ItemChanceHolder(10261, 2476, 1), // Accuracy Juice
			new ItemChanceHolder(10262, 2476, 1), // Critical Damage Juice
			new ItemChanceHolder(10263, 2476, 1), // Critical Rate Juice
			new ItemChanceHolder(10264, 2476, 1), // Casting Spd. Juice
			new ItemChanceHolder(10265, 2476, 1), // Evasion Juice
			new ItemChanceHolder(10266, 2476, 1), // M. Atk. Juice
			new ItemChanceHolder(10267, 2476, 1), // P. Atk. Potion
			new ItemChanceHolder(10268, 2476, 1), // Wind Walk Juice
			new ItemChanceHolder(5593, 5157, 6), // SP Scroll (Low-grade)
			new ItemChanceHolder(5594, 2476, 1), // SP Scroll (Mid-grade)
			new ItemChanceHolder(10269, 2476, 1), // P. Def. Juice
			new ItemChanceHolder(10134, 7152, 1), // Transformation Scroll: Unicorn
			new ItemChanceHolder(10135, 7152, 1), // Transformation Scroll: Lilim Knight
			new ItemChanceHolder(10136, 7152, 1), // Transformation Scroll: Golem Guardian
			new ItemChanceHolder(1538, 7152, 1), // Blessed Scroll of Escape
			new ItemChanceHolder(3936, 2682, 1), // Blessed Scroll of Resurrection
			new ItemChanceHolder(69, 2165, 1), // Bastard Sword
			new ItemChanceHolder(21747, 698, 1))); // Beginner Adventurer's Treasure Sack
		
		DROPS.put(TREASURE_CHEST_LEVEL_39, Arrays.asList( // Treasure Chest
			new ItemChanceHolder(736, 6879, 7), // Scroll of Escape
			new ItemChanceHolder(1061, 6019, 4), // Major Healing Potion
			new ItemChanceHolder(737, 9630, 4), // Scroll of Resurrection
			new ItemChanceHolder(10260, 2889, 1), // Haste Potion
			new ItemChanceHolder(10261, 2889, 1), // Accuracy Juice
			new ItemChanceHolder(10262, 2889, 1), // Critical Damage Juice
			new ItemChanceHolder(10263, 2889, 1), // Critical Rate Juice
			new ItemChanceHolder(10264, 2889, 1), // Casting Spd. Juice
			new ItemChanceHolder(10265, 2889, 1), // Evasion Juice
			new ItemChanceHolder(10266, 2889, 1), // M. Atk. Juice
			new ItemChanceHolder(10267, 2889, 1), // P. Atk. Potion
			new ItemChanceHolder(10268, 2889, 1), // Wind Walk Juice
			new ItemChanceHolder(5593, 6019, 6), // SP Scroll (Low-grade)
			new ItemChanceHolder(5594, 2889, 1), // SP Scroll (Mid-grade)
			new ItemChanceHolder(10269, 2889, 1), // P. Def. Juice
			new ItemChanceHolder(10134, 8346, 1), // Transformation Scroll: Unicorn
			new ItemChanceHolder(10135, 8346, 1), // Transformation Scroll: Lilim Knight
			new ItemChanceHolder(10136, 8346, 1), // Transformation Scroll: Golem Guardian
			new ItemChanceHolder(1538, 8346, 1), // Blessed Scroll of Escape
			new ItemChanceHolder(3936, 3130, 1), // Blessed Scroll of Resurrection
			new ItemChanceHolder(69, 2527, 1), // Bastard Sword
			new ItemChanceHolder(21747, 815, 1))); // Beginner Adventurer's Treasure Sack
		
		DROPS.put(TREASURE_CHEST_LEVEL_42, Arrays.asList( // Treasure Chest
			new ItemChanceHolder(736, 6668, 5), // Scroll of Escape
			new ItemChanceHolder(1061, 4168, 4), // Major Healing Potion
			new ItemChanceHolder(737, 2223, 3), // Scroll of Resurrection
			new ItemChanceHolder(1539, 6668, 5), // Major Healing Potion
			new ItemChanceHolder(8625, 3334, 2), // Elixir of Life (B-grade)
			new ItemChanceHolder(8631, 2874, 2), // Elixir of Mind (B-grade)
			new ItemChanceHolder(8637, 5557, 3), // Elixir of CP (B-grade)
			new ItemChanceHolder(8636, 5557, 4), // Elixir of CP (C-grade)
			new ItemChanceHolder(8630, 3832, 2), // Elixir of Mind (C-grade)
			new ItemChanceHolder(8624, 4631, 2), // Elixir of Life (C-grade)
			new ItemChanceHolder(10260, 5129, 1), // Haste Potion
			new ItemChanceHolder(10261, 5129, 1), // Accuracy Juice
			new ItemChanceHolder(10262, 5129, 1), // Critical Damage Juice
			new ItemChanceHolder(10263, 5129, 1), // Critical Rate Juice
			new ItemChanceHolder(10264, 5129, 1), // Casting Spd. Juice
			new ItemChanceHolder(10265, 5129, 1), // Evasion Juice
			new ItemChanceHolder(10266, 5129, 1), // M. Atk. Juice
			new ItemChanceHolder(10267, 5129, 1), // P. Atk. Potion
			new ItemChanceHolder(10268, 5129, 1), // Wind Walk Juice
			new ItemChanceHolder(5593, 7124, 9), // SP Scroll (Low-grade)
			new ItemChanceHolder(5594, 6411, 2), // SP Scroll (Mid-grade)
			new ItemChanceHolder(5595, 642, 1), // SP Scroll (High-grade)
			new ItemChanceHolder(10269, 5129, 1), // P. Def. Juice
			new ItemChanceHolder(10137, 5418, 1), // Transformation Scroll: Inferno Drake
			new ItemChanceHolder(10138, 5418, 1), // Transformation Scroll: Dragon Bomber
			new ItemChanceHolder(1538, 7223, 1), // Blessed Scroll of Escape
			new ItemChanceHolder(3936, 2709, 1), // Blessed Scroll of Resurrection
			new ItemChanceHolder(5577, 2167, 1), // Red Soul Crystal - Stage 11
			new ItemChanceHolder(5578, 2167, 1), // Green Soul Crystal - Stage 11
			new ItemChanceHolder(5579, 2167, 1), // Blue Soul Crystal - Stage 11
			new ItemChanceHolder(70, 1250, 1), // Claymore
			new ItemChanceHolder(21747, 940, 1))); // Beginner Adventurer's Treasure Sack
		
		DROPS.put(TREASURE_CHEST_LEVEL_45, Arrays.asList( // Treasure Chest
			new ItemChanceHolder(736, 7662, 5), // Scroll of Escape
			new ItemChanceHolder(1061, 4789, 4), // Major Healing Potion
			new ItemChanceHolder(737, 2554, 3), // Scroll of Resurrection
			new ItemChanceHolder(1539, 7662, 5), // Major Healing Potion
			new ItemChanceHolder(8625, 3831, 2), // Elixir of Life (B-grade)
			new ItemChanceHolder(8631, 3303, 2), // Elixir of Mind (B-grade)
			new ItemChanceHolder(8637, 6385, 3), // Elixir of CP (B-grade)
			new ItemChanceHolder(8636, 6385, 4), // Elixir of CP (C-grade)
			new ItemChanceHolder(8630, 4404, 2), // Elixir of Mind (C-grade)
			new ItemChanceHolder(8624, 5321, 2), // Elixir of Life (C-grade)
			new ItemChanceHolder(10260, 5894, 1), // Haste Potion
			new ItemChanceHolder(10261, 5894, 1), // Accuracy Juice
			new ItemChanceHolder(10262, 5894, 1), // Critical Damage Juice
			new ItemChanceHolder(10263, 5894, 1), // Critical Rate Juice
			new ItemChanceHolder(10264, 5894, 1), // Casting Spd. Juice
			new ItemChanceHolder(10265, 5894, 1), // Evasion Juice
			new ItemChanceHolder(10266, 5894, 1), // M. Atk. Juice
			new ItemChanceHolder(10267, 5894, 1), // P. Atk. Potion
			new ItemChanceHolder(10268, 5894, 1), // Wind Walk Juice
			new ItemChanceHolder(5593, 8186, 9), // SP Scroll (Low-grade)
			new ItemChanceHolder(5594, 7367, 2), // SP Scroll (Mid-grade)
			new ItemChanceHolder(5595, 737, 1), // SP Scroll (High-grade)
			new ItemChanceHolder(10269, 5894, 1), // P. Def. Juice
			new ItemChanceHolder(10137, 6226, 1), // Transformation Scroll: Inferno Drake
			new ItemChanceHolder(10138, 6226, 1), // Transformation Scroll: Dragon Bomber
			new ItemChanceHolder(1538, 8301, 1), // Blessed Scroll of Escape
			new ItemChanceHolder(3936, 3113, 1), // Blessed Scroll of Resurrection
			new ItemChanceHolder(5577, 2491, 1), // Red Soul Crystal - Stage 11
			new ItemChanceHolder(5578, 2491, 1), // Green Soul Crystal - Stage 11
			new ItemChanceHolder(5579, 2491, 1), // Blue Soul Crystal - Stage 11
			new ItemChanceHolder(70, 1437, 1), // Claymore
			new ItemChanceHolder(21747, 1080, 1))); // Beginner Adventurer's Treasure Sack
		
		DROPS.put(TREASURE_CHEST_LEVEL_48, Arrays.asList( // Treasure Chest
			new ItemChanceHolder(736, 8719, 5), // Scroll of Escape
			new ItemChanceHolder(1061, 5450, 4), // Major Healing Potion
			new ItemChanceHolder(737, 2907, 3), // Scroll of Resurrection
			new ItemChanceHolder(1539, 8719, 5), // Major Healing Potion
			new ItemChanceHolder(8625, 4360, 2), // Elixir of Life (B-grade)
			new ItemChanceHolder(8631, 3759, 2), // Elixir of Mind (B-grade)
			new ItemChanceHolder(8637, 7266, 3), // Elixir of CP (B-grade)
			new ItemChanceHolder(8636, 7266, 4), // Elixir of CP (C-grade)
			new ItemChanceHolder(8630, 5011, 2), // Elixir of Mind (C-grade)
			new ItemChanceHolder(8624, 6055, 2), // Elixir of Life (C-grade)
			new ItemChanceHolder(10260, 6707, 1), // Haste Potion
			new ItemChanceHolder(10261, 6707, 1), // Accuracy Juice
			new ItemChanceHolder(10262, 6707, 1), // Critical Damage Juice
			new ItemChanceHolder(10263, 6707, 1), // Critical Rate Juice
			new ItemChanceHolder(10264, 6707, 1), // Casting Spd. Juice
			new ItemChanceHolder(10265, 6707, 1), // Evasion Juice
			new ItemChanceHolder(10266, 6707, 1), // M. Atk. Juice
			new ItemChanceHolder(10267, 6707, 1), // P. Atk. Potion
			new ItemChanceHolder(10268, 6707, 1), // Wind Walk Juice
			new ItemChanceHolder(5593, 9315, 9), // SP Scroll (Low-grade)
			new ItemChanceHolder(5594, 8384, 2), // SP Scroll (Mid-grade)
			new ItemChanceHolder(5595, 839, 1), // SP Scroll (High-grade)
			new ItemChanceHolder(10269, 6707, 1), // P. Def. Juice
			new ItemChanceHolder(21180, 7084, 1), // Transformation Scroll: Heretic - Event
			new ItemChanceHolder(21181, 5668, 1), // Transformation Scroll: Veil Master - Event
			new ItemChanceHolder(1538, 9446, 1), // Blessed Scroll of Escape
			new ItemChanceHolder(3936, 3542, 1), // Blessed Scroll of Resurrection
			new ItemChanceHolder(5577, 2834, 1), // Red Soul Crystal - Stage 11
			new ItemChanceHolder(5578, 2834, 1), // Green Soul Crystal - Stage 11
			new ItemChanceHolder(5579, 2834, 1), // Blue Soul Crystal - Stage 11
			new ItemChanceHolder(135, 481, 1), // Samurai Long Sword
			new ItemChanceHolder(21747, 1229, 1))); // Beginner Adventurer's Treasure Sack
		
		DROPS.put(TREASURE_CHEST_LEVEL_51, Arrays.asList( // Treasure Chest
			new ItemChanceHolder(736, 9881, 5), // Scroll of Escape
			new ItemChanceHolder(1061, 6176, 4), // Major Healing Potion
			new ItemChanceHolder(737, 3294, 3), // Scroll of Resurrection
			new ItemChanceHolder(1539, 9881, 5), // Major Healing Potion
			new ItemChanceHolder(8625, 4941, 2), // Elixir of Life (B-grade)
			new ItemChanceHolder(8631, 4259, 2), // Elixir of Mind (B-grade)
			new ItemChanceHolder(8637, 8234, 3), // Elixir of CP (B-grade)
			new ItemChanceHolder(8636, 8234, 4), // Elixir of CP (C-grade)
			new ItemChanceHolder(8630, 5679, 2), // Elixir of Mind (C-grade)
			new ItemChanceHolder(8624, 6862, 2), // Elixir of Life (C-grade)
			new ItemChanceHolder(10260, 7601, 1), // Haste Potion
			new ItemChanceHolder(10261, 7601, 1), // Accuracy Juice
			new ItemChanceHolder(10262, 7601, 1), // Critical Damage Juice
			new ItemChanceHolder(10263, 7601, 1), // Critical Rate Juice
			new ItemChanceHolder(10264, 7601, 1), // Casting Spd. Juice
			new ItemChanceHolder(10265, 7601, 1), // Evasion Juice
			new ItemChanceHolder(10266, 7601, 1), // M. Atk. Juice
			new ItemChanceHolder(10267, 7601, 1), // P. Atk. Potion
			new ItemChanceHolder(10268, 7601, 1), // Wind Walk Juice
			new ItemChanceHolder(5593, 10557, 9), // SP Scroll (Low-grade)
			new ItemChanceHolder(5594, 9501, 2), // SP Scroll (Mid-grade)
			new ItemChanceHolder(5595, 951, 1), // SP Scroll (High-grade)
			new ItemChanceHolder(10269, 7601, 1), // P. Def. Juice
			new ItemChanceHolder(21180, 8028, 1), // Transformation Scroll: Heretic - Event
			new ItemChanceHolder(21181, 6423, 1), // Transformation Scroll: Veil Master - Event
			new ItemChanceHolder(1538, 10704, 1), // Blessed Scroll of Escape
			new ItemChanceHolder(3936, 4014, 1), // Blessed Scroll of Resurrection
			new ItemChanceHolder(5577, 3212, 1), // Red Soul Crystal - Stage 11
			new ItemChanceHolder(5578, 3212, 1), // Green Soul Crystal - Stage 11
			new ItemChanceHolder(5579, 3212, 1), // Blue Soul Crystal - Stage 11
			new ItemChanceHolder(135, 546, 1), // Samurai Long Sword
			new ItemChanceHolder(21747, 1393, 1))); // Beginner Adventurer's Treasure Sack
		
		DROPS.put(TREASURE_CHEST_LEVEL_54, Arrays.asList( // Treasure Chest
			new ItemChanceHolder(736, 7727, 8), // Scroll of Escape
			new ItemChanceHolder(1061, 7727, 4), // Major Healing Potion
			new ItemChanceHolder(737, 4121, 3), // Scroll of Resurrection
			new ItemChanceHolder(8625, 6182, 2), // Elixir of Life (B-grade)
			new ItemChanceHolder(8631, 5329, 2), // Elixir of Mind (B-grade)
			new ItemChanceHolder(8637, 7727, 4), // Elixir of CP (B-grade)
			new ItemChanceHolder(8638, 8242, 3), // Elixir of CP (A-grade)
			new ItemChanceHolder(8632, 4293, 2), // Elixir of Mind (A-grade)
			new ItemChanceHolder(8626, 4945, 2), // Elixir of Life (A-grade)
			new ItemChanceHolder(10260, 4451, 1), // Haste Potion
			new ItemChanceHolder(10261, 4451, 1), // Accuracy Juice
			new ItemChanceHolder(10262, 4451, 1), // Critical Damage Juice
			new ItemChanceHolder(10263, 4451, 1), // Critical Rate Juice
			new ItemChanceHolder(10264, 4451, 1), // Casting Spd. Juice
			new ItemChanceHolder(10265, 4451, 1), // Evasion Juice
			new ItemChanceHolder(10266, 4451, 1), // M. Atk. Juice
			new ItemChanceHolder(10267, 4451, 1), // P. Atk. Potion
			new ItemChanceHolder(10268, 4451, 1), // Wind Walk Juice
			new ItemChanceHolder(5594, 5563, 2), // SP Scroll (Mid-grade)
			new ItemChanceHolder(5595, 557, 1), // SP Scroll (High-grade)
			new ItemChanceHolder(10269, 4451, 1), // P. Def. Juice
			new ItemChanceHolder(8736, 6439, 1), // Mid-grade Life Stone - Lv. 55
			new ItemChanceHolder(8737, 5563, 1), // Mid-grade Life Stone - Lv. 58
			new ItemChanceHolder(8738, 4636, 1), // Mid-grade Life Stone - Lv. 61
			new ItemChanceHolder(21182, 5786, 1), // Transformation Scroll: Saber Tooth Tiger - Event
			new ItemChanceHolder(21183, 4822, 1), // Transformation Scroll: Oel Mahum - Event
			new ItemChanceHolder(1538, 4822, 2), // Blessed Scroll of Escape
			new ItemChanceHolder(3936, 3616, 1), // Blessed Scroll of Resurrection
			new ItemChanceHolder(9648, 670, 1), // Transformation Sealbook: Onyx Beast
			new ItemChanceHolder(9649, 804, 1), // Transformation Sealbook: Doom Wraith
			new ItemChanceHolder(5580, 145, 1), // Red Soul Crystal - Stage 12
			new ItemChanceHolder(5581, 145, 1), // Green Soul Crystal - Stage 12
			new ItemChanceHolder(5582, 145, 1), // Blue Soul Crystal - Stage 12
			new ItemChanceHolder(142, 217, 1), // Keshanberk
			new ItemChanceHolder(21748, 92, 1))); // Experienced Adventurer's Treasure Sack
		
		DROPS.put(TREASURE_CHEST_LEVEL_57, Arrays.asList( // Treasure Chest
			new ItemChanceHolder(736, 8657, 8), // Scroll of Escape
			new ItemChanceHolder(1061, 8657, 4), // Major Healing Potion
			new ItemChanceHolder(737, 4617, 3), // Scroll of Resurrection
			new ItemChanceHolder(8625, 6926, 2), // Elixir of Life (B-grade)
			new ItemChanceHolder(8631, 5971, 2), // Elixir of Mind (B-grade)
			new ItemChanceHolder(8637, 8657, 4), // Elixir of CP (B-grade)
			new ItemChanceHolder(8638, 9234, 3), // Elixir of CP (A-grade)
			new ItemChanceHolder(8632, 4810, 2), // Elixir of Mind (A-grade)
			new ItemChanceHolder(8626, 5541, 2), // Elixir of Life (A-grade)
			new ItemChanceHolder(10260, 4987, 1), // Haste Potion
			new ItemChanceHolder(10261, 4987, 1), // Accuracy Juice
			new ItemChanceHolder(10262, 4987, 1), // Critical Damage Juice
			new ItemChanceHolder(10263, 4987, 1), // Critical Rate Juice
			new ItemChanceHolder(10264, 4987, 1), // Casting Spd. Juice
			new ItemChanceHolder(10265, 4987, 1), // Evasion Juice
			new ItemChanceHolder(10266, 4987, 1), // M. Atk. Juice
			new ItemChanceHolder(10267, 4987, 1), // P. Atk. Potion
			new ItemChanceHolder(10268, 4987, 1), // Wind Walk Juice
			new ItemChanceHolder(5594, 6233, 2), // SP Scroll (Mid-grade)
			new ItemChanceHolder(5595, 624, 1), // SP Scroll (High-grade)
			new ItemChanceHolder(10269, 4987, 1), // P. Def. Juice
			new ItemChanceHolder(8736, 7214, 1), // Mid-grade Life Stone - Lv. 55
			new ItemChanceHolder(8737, 6233, 1), // Mid-grade Life Stone - Lv. 58
			new ItemChanceHolder(8738, 5195, 1), // Mid-grade Life Stone - Lv. 61
			new ItemChanceHolder(21183, 5402, 1), // Transformation Scroll: Oel Mahum - Event
			new ItemChanceHolder(21184, 5402, 1), // Transformation Scroll: Doll Blader - Event
			new ItemChanceHolder(1538, 5402, 2), // Blessed Scroll of Escape
			new ItemChanceHolder(3936, 4052, 1), // Blessed Scroll of Resurrection
			new ItemChanceHolder(9648, 751, 1), // Transformation Sealbook: Onyx Beast
			new ItemChanceHolder(9649, 901, 1), // Transformation Sealbook: Doom Wraith
			new ItemChanceHolder(5580, 163, 1), // Red Soul Crystal - Stage 12
			new ItemChanceHolder(5581, 163, 1), // Green Soul Crystal - Stage 12
			new ItemChanceHolder(5582, 163, 1), // Blue Soul Crystal - Stage 12
			new ItemChanceHolder(79, 161, 1), // Damascus Sword
			new ItemChanceHolder(21748, 103, 1))); // Experienced Adventurer's Treasure Sack
		
		DROPS.put(TREASURE_CHEST_LEVEL_60, Arrays.asList( // Treasure Chest
			new ItemChanceHolder(736, 9646, 8), // Scroll of Escape
			new ItemChanceHolder(1061, 9646, 4), // Major Healing Potion
			new ItemChanceHolder(737, 5145, 3), // Scroll of Resurrection
			new ItemChanceHolder(8625, 7717, 2), // Elixir of Life (B-grade)
			new ItemChanceHolder(8631, 6652, 2), // Elixir of Mind (B-grade)
			new ItemChanceHolder(8637, 9646, 4), // Elixir of CP (B-grade)
			new ItemChanceHolder(8638, 10289, 3), // Elixir of CP (A-grade)
			new ItemChanceHolder(8632, 5359, 2), // Elixir of Mind (A-grade)
			new ItemChanceHolder(8626, 6173, 2), // Elixir of Life (A-grade)
			new ItemChanceHolder(10260, 5556, 1), // Haste Potion
			new ItemChanceHolder(10261, 5556, 1), // Accuracy Juice
			new ItemChanceHolder(10262, 5556, 1), // Critical Damage Juice
			new ItemChanceHolder(10263, 5556, 1), // Critical Rate Juice
			new ItemChanceHolder(10264, 5556, 1), // Casting Spd. Juice
			new ItemChanceHolder(10265, 5556, 1), // Evasion Juice
			new ItemChanceHolder(10266, 5556, 1), // M. Atk. Juice
			new ItemChanceHolder(10267, 5556, 1), // P. Atk. Potion
			new ItemChanceHolder(10268, 5556, 1), // Wind Walk Juice
			new ItemChanceHolder(5594, 6945, 2), // SP Scroll (Mid-grade)
			new ItemChanceHolder(5595, 695, 1), // SP Scroll (High-grade)
			new ItemChanceHolder(10269, 5556, 1), // P. Def. Juice
			new ItemChanceHolder(8736, 8038, 1), // Mid-grade Life Stone - Lv. 55
			new ItemChanceHolder(8737, 6945, 1), // Mid-grade Life Stone - Lv. 58
			new ItemChanceHolder(8738, 5788, 1), // Mid-grade Life Stone - Lv. 61
			new ItemChanceHolder(21183, 6019, 1), // Transformation Scroll: Oel Mahum - Event
			new ItemChanceHolder(21184, 6019, 1), // Transformation Scroll: Doll Blader - Event
			new ItemChanceHolder(1538, 6019, 2), // Blessed Scroll of Escape
			new ItemChanceHolder(3936, 4514, 1), // Blessed Scroll of Resurrection
			new ItemChanceHolder(9648, 836, 1), // Transformation Sealbook: Onyx Beast
			new ItemChanceHolder(9649, 1004, 1), // Transformation Sealbook: Doom Wraith
			new ItemChanceHolder(5580, 181, 1), // Red Soul Crystal - Stage 12
			new ItemChanceHolder(5581, 181, 1), // Green Soul Crystal - Stage 12
			new ItemChanceHolder(5582, 181, 1), // Blue Soul Crystal - Stage 12
			new ItemChanceHolder(79, 179, 1), // Damascus Sword
			new ItemChanceHolder(21748, 115, 1))); // Experienced Adventurer's Treasure Sack
		
		DROPS.put(TREASURE_CHEST_LEVEL_63, Arrays.asList( // Treasure Chest
			new ItemChanceHolder(8627, 5714, 2), // Elixir of Life (S-grade)
			new ItemChanceHolder(8633, 5102, 2), // Elixir of Mind (S-grade)
			new ItemChanceHolder(8639, 5714, 5), // Elixir of CP (S-grade)
			new ItemChanceHolder(8638, 5714, 6), // Elixir of CP (A-grade)
			new ItemChanceHolder(8632, 5953, 2), // Elixir of Mind (A-grade)
			new ItemChanceHolder(8626, 4572, 3), // Elixir of Life (A-grade)
			new ItemChanceHolder(729, 96, 1), // Scroll: Enchant Weapon (A-grade)
			new ItemChanceHolder(730, 715, 1), // Scroll: Enchant Armor (A-grade)
			new ItemChanceHolder(1540, 4286, 4), // Quick Healing Potion
			new ItemChanceHolder(10260, 1929, 3), // Haste Potion
			new ItemChanceHolder(10261, 1929, 3), // Accuracy Juice
			new ItemChanceHolder(10262, 1929, 3), // Critical Damage Juice
			new ItemChanceHolder(10263, 1929, 3), // Critical Rate Juice
			new ItemChanceHolder(10264, 1929, 3), // Casting Spd. Juice
			new ItemChanceHolder(10265, 1929, 3), // Evasion Juice
			new ItemChanceHolder(10266, 1929, 3), // M. Atk. Juice
			new ItemChanceHolder(10267, 1929, 3), // P. Atk. Potion
			new ItemChanceHolder(10268, 1929, 3), // Wind Walk Juice
			new ItemChanceHolder(5595, 724, 1), // SP Scroll (High-grade)
			new ItemChanceHolder(9898, 724, 1), // SP Scroll (Top-grade)
			new ItemChanceHolder(10269, 1929, 3), // P. Def. Juice
			new ItemChanceHolder(8739, 4822, 1), // Mid-grade Life Stone - Lv. 64
			new ItemChanceHolder(8740, 4018, 1), // Mid-grade Life Stone - Lv. 67
			new ItemChanceHolder(8741, 3349, 1), // Mid-grade Life Stone - Lv. 70
			new ItemChanceHolder(8742, 3014, 1), // Mid-grade Life Stone - Lv. 76
			new ItemChanceHolder(21180, 9117, 1), // Transformation Scroll: Heretic - Event
			new ItemChanceHolder(21181, 7294, 1), // Transformation Scroll: Veil Master - Event
			new ItemChanceHolder(21182, 7294, 1), // Transformation Scroll: Saber Tooth Tiger - Event
			new ItemChanceHolder(1538, 6078, 2), // Blessed Scroll of Escape
			new ItemChanceHolder(3936, 4559, 1), // Blessed Scroll of Resurrection
			new ItemChanceHolder(9654, 845, 1), // Transformation Sealbook: Inferno Drake
			new ItemChanceHolder(9655, 845, 1), // Transformation Sealbook: Dragon Bomber
			new ItemChanceHolder(5580, 183, 1), // Red Soul Crystal - Stage 12
			new ItemChanceHolder(5581, 183, 1), // Green Soul Crystal - Stage 12
			new ItemChanceHolder(5582, 183, 1), // Blue Soul Crystal - Stage 12
			new ItemChanceHolder(80, 130, 1), // Tallum Blade
			new ItemChanceHolder(21748, 128, 1))); // Experienced Adventurer's Treasure Sack
		
		DROPS.put(TREASURE_CHEST_LEVEL_66, Arrays.asList( // Treasure Chest
			new ItemChanceHolder(8627, 6323, 2), // Elixir of Life (S-grade)
			new ItemChanceHolder(8633, 5646, 2), // Elixir of Mind (S-grade)
			new ItemChanceHolder(8639, 6323, 5), // Elixir of CP (S-grade)
			new ItemChanceHolder(8638, 6323, 6), // Elixir of CP (A-grade)
			new ItemChanceHolder(8632, 6587, 2), // Elixir of Mind (A-grade)
			new ItemChanceHolder(8626, 5059, 3), // Elixir of Life (A-grade)
			new ItemChanceHolder(729, 106, 1), // Scroll: Enchant Weapon (A-grade)
			new ItemChanceHolder(730, 791, 1), // Scroll: Enchant Armor (A-grade)
			new ItemChanceHolder(1540, 4742, 4), // Quick Healing Potion
			new ItemChanceHolder(10260, 2134, 3), // Haste Potion
			new ItemChanceHolder(10261, 2134, 3), // Accuracy Juice
			new ItemChanceHolder(10262, 2134, 3), // Critical Damage Juice
			new ItemChanceHolder(10263, 2134, 3), // Critical Rate Juice
			new ItemChanceHolder(10264, 2134, 3), // Casting Spd. Juice
			new ItemChanceHolder(10265, 2134, 3), // Evasion Juice
			new ItemChanceHolder(10266, 2134, 3), // M. Atk. Juice
			new ItemChanceHolder(10267, 2134, 3), // P. Atk. Potion
			new ItemChanceHolder(10268, 2134, 3), // Wind Walk Juice
			new ItemChanceHolder(5595, 801, 1), // SP Scroll (High-grade)
			new ItemChanceHolder(9898, 801, 1), // SP Scroll (Top-grade)
			new ItemChanceHolder(10269, 2134, 3), // P. Def. Juice
			new ItemChanceHolder(8739, 5335, 1), // Mid-grade Life Stone - Lv. 64
			new ItemChanceHolder(8740, 4446, 1), // Mid-grade Life Stone - Lv. 67
			new ItemChanceHolder(8741, 3705, 1), // Mid-grade Life Stone - Lv. 70
			new ItemChanceHolder(8742, 3335, 1), // Mid-grade Life Stone - Lv. 76
			new ItemChanceHolder(21180, 10088, 1), // Transformation Scroll: Heretic - Event
			new ItemChanceHolder(21181, 8070, 1), // Transformation Scroll: Veil Master - Event
			new ItemChanceHolder(21182, 8070, 1), // Transformation Scroll: Saber Tooth Tiger - Event
			new ItemChanceHolder(1538, 6725, 2), // Blessed Scroll of Escape
			new ItemChanceHolder(3936, 5044, 1), // Blessed Scroll of Resurrection
			new ItemChanceHolder(9654, 935, 1), // Transformation Sealbook: Inferno Drake
			new ItemChanceHolder(9655, 935, 1), // Transformation Sealbook: Dragon Bomber
			new ItemChanceHolder(5580, 202, 1), // Red Soul Crystal - Stage 12
			new ItemChanceHolder(5581, 202, 1), // Green Soul Crystal - Stage 12
			new ItemChanceHolder(5582, 202, 1), // Blue Soul Crystal - Stage 12
			new ItemChanceHolder(80, 144, 1), // Tallum Blade
			new ItemChanceHolder(21748, 141, 1))); // Experienced Adventurer's Treasure Sack
		
		DROPS.put(TREASURE_CHEST_LEVEL_69, Arrays.asList( // Treasure Chest
			new ItemChanceHolder(8627, 6967, 2), // Elixir of Life (S-grade)
			new ItemChanceHolder(8633, 6220, 2), // Elixir of Mind (S-grade)
			new ItemChanceHolder(8639, 6967, 5), // Elixir of CP (S-grade)
			new ItemChanceHolder(8638, 6967, 6), // Elixir of CP (A-grade)
			new ItemChanceHolder(8632, 7257, 2), // Elixir of Mind (A-grade)
			new ItemChanceHolder(8626, 5573, 3), // Elixir of Life (A-grade)
			new ItemChanceHolder(729, 117, 1), // Scroll: Enchant Weapon (A-grade)
			new ItemChanceHolder(730, 871, 1), // Scroll: Enchant Armor (A-grade)
			new ItemChanceHolder(1540, 5225, 4), // Quick Healing Potion
			new ItemChanceHolder(10260, 2352, 3), // Haste Potion
			new ItemChanceHolder(10261, 2352, 3), // Accuracy Juice
			new ItemChanceHolder(10262, 2352, 3), // Critical Damage Juice
			new ItemChanceHolder(10263, 2352, 3), // Critical Rate Juice
			new ItemChanceHolder(10264, 2352, 3), // Casting Spd. Juice
			new ItemChanceHolder(10265, 2352, 3), // Evasion Juice
			new ItemChanceHolder(10266, 2352, 3), // M. Atk. Juice
			new ItemChanceHolder(10267, 2352, 3), // P. Atk. Potion
			new ItemChanceHolder(10268, 2352, 3), // Wind Walk Juice
			new ItemChanceHolder(5595, 882, 1), // SP Scroll (High-grade)
			new ItemChanceHolder(9898, 882, 1), // SP Scroll (Top-grade)
			new ItemChanceHolder(10269, 2352, 3), // P. Def. Juice
			new ItemChanceHolder(8739, 5878, 1), // Mid-grade Life Stone - Lv. 64
			new ItemChanceHolder(8740, 4899, 1), // Mid-grade Life Stone - Lv. 67
			new ItemChanceHolder(8741, 4082, 1), // Mid-grade Life Stone - Lv. 70
			new ItemChanceHolder(8742, 3674, 1), // Mid-grade Life Stone - Lv. 76
			new ItemChanceHolder(21183, 7410, 1), // Transformation Scroll: Oel Mahum - Event
			new ItemChanceHolder(21184, 7410, 1), // Transformation Scroll: Doll Blader - Event
			new ItemChanceHolder(21185, 3705, 1), // Transformation Scroll: Zaken - Event
			new ItemChanceHolder(1538, 7410, 2), // Blessed Scroll of Escape
			new ItemChanceHolder(3936, 5558, 1), // Blessed Scroll of Resurrection
			new ItemChanceHolder(9654, 1030, 1), // Transformation Sealbook: Inferno Drake
			new ItemChanceHolder(9655, 1030, 1), // Transformation Sealbook: Dragon Bomber
			new ItemChanceHolder(5908, 112, 1), // Red Soul Crystal: Stage 13
			new ItemChanceHolder(5911, 112, 1), // Green Soul Crystal - Stage 13
			new ItemChanceHolder(5914, 112, 1), // Blue Soul Crystal: Stage 13
			new ItemChanceHolder(6364, 52, 1), // Forgotten Blade
			new ItemChanceHolder(21748, 156, 1))); // Experienced Adventurer's Treasure Sack
		
		DROPS.put(TREASURE_CHEST_LEVEL_72, Arrays.asList( // Treasure Chest
			new ItemChanceHolder(8627, 7649, 2), // Elixir of Life (S-grade)
			new ItemChanceHolder(8633, 6829, 2), // Elixir of Mind (S-grade)
			new ItemChanceHolder(8639, 7649, 5), // Elixir of CP (S-grade)
			new ItemChanceHolder(8638, 7649, 6), // Elixir of CP (A-grade)
			new ItemChanceHolder(8632, 7968, 2), // Elixir of Mind (A-grade)
			new ItemChanceHolder(8626, 6119, 3), // Elixir of Life (A-grade)
			new ItemChanceHolder(729, 128, 1), // Scroll: Enchant Weapon (A-grade)
			new ItemChanceHolder(730, 957, 1), // Scroll: Enchant Armor (A-grade)
			new ItemChanceHolder(1540, 5737, 4), // Quick Healing Potion
			new ItemChanceHolder(10260, 2582, 3), // Haste Potion
			new ItemChanceHolder(10261, 2582, 3), // Accuracy Juice
			new ItemChanceHolder(10262, 2582, 3), // Critical Damage Juice
			new ItemChanceHolder(10263, 2582, 3), // Critical Rate Juice
			new ItemChanceHolder(10264, 2582, 3), // Casting Spd. Juice
			new ItemChanceHolder(10265, 2582, 3), // Evasion Juice
			new ItemChanceHolder(10266, 2582, 3), // M. Atk. Juice
			new ItemChanceHolder(10267, 2582, 3), // P. Atk. Potion
			new ItemChanceHolder(10268, 2582, 3), // Wind Walk Juice
			new ItemChanceHolder(5595, 968, 1), // SP Scroll (High-grade)
			new ItemChanceHolder(9898, 968, 1), // SP Scroll (Top-grade)
			new ItemChanceHolder(10269, 2582, 3), // P. Def. Juice
			new ItemChanceHolder(8739, 6454, 1), // Mid-grade Life Stone - Lv. 64
			new ItemChanceHolder(8740, 5378, 1), // Mid-grade Life Stone - Lv. 67
			new ItemChanceHolder(8741, 4482, 1), // Mid-grade Life Stone - Lv. 70
			new ItemChanceHolder(8742, 4034, 1), // Mid-grade Life Stone - Lv. 76
			new ItemChanceHolder(21183, 8136, 1), // Transformation Scroll: Oel Mahum - Event
			new ItemChanceHolder(21184, 8136, 1), // Transformation Scroll: Doll Blader - Event
			new ItemChanceHolder(21185, 4068, 1), // Transformation Scroll: Zaken - Event
			new ItemChanceHolder(1538, 8136, 2), // Blessed Scroll of Escape
			new ItemChanceHolder(3936, 6102, 1), // Blessed Scroll of Resurrection
			new ItemChanceHolder(9654, 1130, 1), // Transformation Sealbook: Inferno Drake
			new ItemChanceHolder(9655, 1130, 1), // Transformation Sealbook: Dragon Bomber
			new ItemChanceHolder(5908, 123, 1), // Red Soul Crystal: Stage 13
			new ItemChanceHolder(5911, 123, 1), // Green Soul Crystal - Stage 13
			new ItemChanceHolder(5914, 123, 1), // Blue Soul Crystal: Stage 13
			new ItemChanceHolder(6364, 58, 1), // Forgotten Blade
			new ItemChanceHolder(21748, 171, 1))); // Experienced Adventurer's Treasure Sack
		
		DROPS.put(TREASURE_CHEST_LEVEL_75, Arrays.asList( // Treasure Chest
			new ItemChanceHolder(8627, 8366, 2), // Elixir of Life (S-grade)
			new ItemChanceHolder(8633, 7470, 2), // Elixir of Mind (S-grade)
			new ItemChanceHolder(8639, 8366, 5), // Elixir of CP (S-grade)
			new ItemChanceHolder(8638, 8366, 6), // Elixir of CP (A-grade)
			new ItemChanceHolder(8632, 8715, 2), // Elixir of Mind (A-grade)
			new ItemChanceHolder(8626, 6693, 3), // Elixir of Life (A-grade)
			new ItemChanceHolder(729, 140, 1), // Scroll: Enchant Weapon (A-grade)
			new ItemChanceHolder(730, 1046, 1), // Scroll: Enchant Armor (A-grade)
			new ItemChanceHolder(1540, 6275, 4), // Quick Healing Potion
			new ItemChanceHolder(10260, 2824, 3), // Haste Potion
			new ItemChanceHolder(10261, 2824, 3), // Accuracy Juice
			new ItemChanceHolder(10262, 2824, 3), // Critical Damage Juice
			new ItemChanceHolder(10263, 2824, 3), // Critical Rate Juice
			new ItemChanceHolder(10264, 2824, 3), // Casting Spd. Juice
			new ItemChanceHolder(10265, 2824, 3), // Evasion Juice
			new ItemChanceHolder(10266, 2824, 3), // M. Atk. Juice
			new ItemChanceHolder(10267, 2824, 3), // P. Atk. Potion
			new ItemChanceHolder(10268, 2824, 3), // Wind Walk Juice
			new ItemChanceHolder(5595, 1059, 1), // SP Scroll (High-grade)
			new ItemChanceHolder(9898, 1059, 1), // SP Scroll (Top-grade)
			new ItemChanceHolder(10269, 2824, 3), // P. Def. Juice
			new ItemChanceHolder(8739, 7059, 1), // Mid-grade Life Stone - Lv. 64
			new ItemChanceHolder(8740, 5883, 1), // Mid-grade Life Stone - Lv. 67
			new ItemChanceHolder(8741, 4902, 1), // Mid-grade Life Stone - Lv. 70
			new ItemChanceHolder(8742, 4412, 1), // Mid-grade Life Stone - Lv. 76
			new ItemChanceHolder(21183, 8898, 1), // Transformation Scroll: Oel Mahum - Event
			new ItemChanceHolder(21184, 8898, 1), // Transformation Scroll: Doll Blader - Event
			new ItemChanceHolder(21185, 4449, 1), // Transformation Scroll: Zaken - Event
			new ItemChanceHolder(1538, 8898, 2), // Blessed Scroll of Escape
			new ItemChanceHolder(3936, 6674, 1), // Blessed Scroll of Resurrection
			new ItemChanceHolder(9654, 1236, 1), // Transformation Sealbook: Inferno Drake
			new ItemChanceHolder(9655, 1236, 1), // Transformation Sealbook: Dragon Bomber
			new ItemChanceHolder(5908, 134, 1), // Red Soul Crystal: Stage 13
			new ItemChanceHolder(5911, 134, 1), // Green Soul Crystal - Stage 13
			new ItemChanceHolder(5914, 134, 1), // Blue Soul Crystal: Stage 13
			new ItemChanceHolder(6364, 63, 1), // Forgotten Blade
			new ItemChanceHolder(21748, 187, 1))); // Experienced Adventurer's Treasure Sack
		
		DROPS.put(TREASURE_CHEST_LEVEL_78, Arrays.asList( // Treasure Chest
			new ItemChanceHolder(8627, 6836, 2), // Elixir of Life (S-grade)
			new ItemChanceHolder(8633, 6103, 2), // Elixir of Mind (S-grade)
			new ItemChanceHolder(8639, 10000, 4), // Elixir of CP (S-grade)
			new ItemChanceHolder(9546, 821, 1), // Fire Stone
			new ItemChanceHolder(9547, 821, 1), // Water Stone
			new ItemChanceHolder(9548, 821, 1), // Earth Stone
			new ItemChanceHolder(9549, 821, 1), // Wind Stone
			new ItemChanceHolder(9550, 821, 1), // Dark Stone
			new ItemChanceHolder(9551, 821, 1), // Holy Stone
			new ItemChanceHolder(959, 42, 1), // Scroll: Enchant Weapon (S-grade)
			new ItemChanceHolder(960, 411, 1), // Scroll: Enchant Armor (S-grade)
			new ItemChanceHolder(14701, 2051, 2), // Superior Quick Healing Potion
			new ItemChanceHolder(10260, 3076, 3), // Haste Potion
			new ItemChanceHolder(10261, 3076, 3), // Accuracy Juice
			new ItemChanceHolder(10262, 3076, 3), // Critical Damage Juice
			new ItemChanceHolder(10263, 3076, 3), // Critical Rate Juice
			new ItemChanceHolder(10264, 3076, 3), // Casting Spd. Juice
			new ItemChanceHolder(10265, 3076, 3), // Evasion Juice
			new ItemChanceHolder(10266, 3076, 3), // M. Atk. Juice
			new ItemChanceHolder(10267, 3076, 3), // P. Atk. Potion
			new ItemChanceHolder(10268, 3076, 3), // Wind Walk Juice
			new ItemChanceHolder(5595, 577, 2), // SP Scroll (High-grade)
			new ItemChanceHolder(9898, 231, 1), // SP Scroll (Top-grade)
			new ItemChanceHolder(17185, 116, 1), // Scroll: 10,000 SP
			new ItemChanceHolder(10269, 3076, 3), // P. Def. Juice
			new ItemChanceHolder(9574, 4006, 1), // Mid-grade Life Stone - Lv. 80
			new ItemChanceHolder(10484, 3338, 1), // Mid-grade Life Stone - Lv. 82
			new ItemChanceHolder(14167, 2783, 1), // Mid-grade Life Stone - Lv. 84
			new ItemChanceHolder(21185, 2539, 1), // Transformation Scroll: Zaken - Event
			new ItemChanceHolder(21186, 1524, 1), // Transformation Scroll: Anakim - Event
			new ItemChanceHolder(21187, 2177, 1), // Transformation Scroll: Venom - Event
			new ItemChanceHolder(21188, 2177, 1), // Transformation Scroll: Gordon - Event
			new ItemChanceHolder(21189, 2177, 1), // Transformation Scroll: Ranku - Event
			new ItemChanceHolder(21190, 2177, 1), // Transformation Scroll: Kechi - Event
			new ItemChanceHolder(21191, 2177, 1), // Transformation Scroll: Demon Prince - Event
			new ItemChanceHolder(9552, 191, 1), // Fire Crystal
			new ItemChanceHolder(9553, 191, 1), // Water Crystal
			new ItemChanceHolder(9554, 191, 1), // Earth Crystal
			new ItemChanceHolder(9555, 191, 1), // Wind Crystal
			new ItemChanceHolder(9556, 191, 1), // Dark Crystal
			new ItemChanceHolder(9557, 191, 1), // Holy Crystal
			new ItemChanceHolder(6622, 3047, 1), // Lesser Giant's Codex
			new ItemChanceHolder(9627, 191, 1), // Lesser Giant's Codex - Mastery
			new ItemChanceHolder(1538, 5078, 2), // Blessed Scroll of Escape
			new ItemChanceHolder(3936, 3809, 1), // Blessed Scroll of Resurrection
			new ItemChanceHolder(9570, 39, 1), // Red Soul Crystal - Stage 14
			new ItemChanceHolder(9572, 39, 1), // Green Soul Crystal - Stage 14
			new ItemChanceHolder(9571, 39, 1), // Blue Soul Crystal - Stage 14
			new ItemChanceHolder(9442, 21, 1), // Dynasty Sword
			new ItemChanceHolder(21749, 25, 1))); // Great Adventurer's Treasure Sack
		
		DROPS.put(TREASURE_CHEST_LEVEL_81, Arrays.asList( // Treasure Chest
			new ItemChanceHolder(8627, 7420, 2), // Elixir of Life (S-grade)
			new ItemChanceHolder(8633, 6625, 2), // Elixir of Mind (S-grade)
			new ItemChanceHolder(8639, 10000, 4), // Elixir of CP (S-grade)
			new ItemChanceHolder(9546, 891, 1), // Fire Stone
			new ItemChanceHolder(9547, 891, 1), // Water Stone
			new ItemChanceHolder(9548, 891, 1), // Earth Stone
			new ItemChanceHolder(9549, 891, 1), // Wind Stone
			new ItemChanceHolder(9550, 891, 1), // Dark Stone
			new ItemChanceHolder(9551, 891, 1), // Holy Stone
			new ItemChanceHolder(959, 45, 1), // Scroll: Enchant Weapon (S-grade)
			new ItemChanceHolder(960, 446, 1), // Scroll: Enchant Armor (S-grade)
			new ItemChanceHolder(14701, 2226, 2), // Superior Quick Healing Potion
			new ItemChanceHolder(10260, 3339, 3), // Haste Potion
			new ItemChanceHolder(10261, 3339, 3), // Accuracy Juice
			new ItemChanceHolder(10262, 3339, 3), // Critical Damage Juice
			new ItemChanceHolder(10263, 3339, 3), // Critical Rate Juice
			new ItemChanceHolder(10264, 3339, 3), // Casting Spd. Juice
			new ItemChanceHolder(10265, 3339, 3), // Evasion Juice
			new ItemChanceHolder(10266, 3339, 3), // M. Atk. Juice
			new ItemChanceHolder(10267, 3339, 3), // P. Atk. Potion
			new ItemChanceHolder(10268, 3339, 3), // Wind Walk Juice
			new ItemChanceHolder(5595, 627, 2), // SP Scroll (High-grade)
			new ItemChanceHolder(9898, 251, 1), // SP Scroll (Top-grade)
			new ItemChanceHolder(17185, 126, 1), // Scroll: 10,000 SP
			new ItemChanceHolder(10269, 3339, 3), // P. Def. Juice
			new ItemChanceHolder(9574, 4348, 1), // Mid-grade Life Stone - Lv. 80
			new ItemChanceHolder(10484, 3623, 1), // Mid-grade Life Stone - Lv. 82
			new ItemChanceHolder(14167, 3021, 1), // Mid-grade Life Stone - Lv. 84
			new ItemChanceHolder(21185, 2756, 1), // Transformation Scroll: Zaken - Event
			new ItemChanceHolder(21186, 1654, 1), // Transformation Scroll: Anakim - Event
			new ItemChanceHolder(21187, 2363, 1), // Transformation Scroll: Venom - Event
			new ItemChanceHolder(21188, 2363, 1), // Transformation Scroll: Gordon - Event
			new ItemChanceHolder(21189, 2363, 1), // Transformation Scroll: Ranku - Event
			new ItemChanceHolder(21190, 2363, 1), // Transformation Scroll: Kechi - Event
			new ItemChanceHolder(21191, 2363, 1), // Transformation Scroll: Demon Prince - Event
			new ItemChanceHolder(9552, 207, 1), // Fire Crystal
			new ItemChanceHolder(9553, 207, 1), // Water Crystal
			new ItemChanceHolder(9554, 207, 1), // Earth Crystal
			new ItemChanceHolder(9555, 207, 1), // Wind Crystal
			new ItemChanceHolder(9556, 207, 1), // Dark Crystal
			new ItemChanceHolder(9557, 207, 1), // Holy Crystal
			new ItemChanceHolder(6622, 3308, 1), // Lesser Giant's Codex
			new ItemChanceHolder(9627, 207, 1), // Lesser Giant's Codex - Mastery
			new ItemChanceHolder(1538, 5512, 2), // Blessed Scroll of Escape
			new ItemChanceHolder(3936, 4134, 1), // Blessed Scroll of Resurrection
			new ItemChanceHolder(10480, 21, 1), // Red Soul Crystal - Stage 15
			new ItemChanceHolder(10482, 21, 1), // Green Soul Crystal - Stage 15
			new ItemChanceHolder(10481, 21, 1), // Blue Soul Crystal - Stage 15
			new ItemChanceHolder(10215, 16, 1), // Icarus Sawsword
			new ItemChanceHolder(21749, 27, 1))); // Great Adventurer's Treasure Sack
		
		DROPS.put(TREASURE_CHEST_LEVEL_84, Arrays.asList( // Treasure Chest
			new ItemChanceHolder(8627, 8005, 2), // Elixir of Life (S-grade)
			new ItemChanceHolder(8633, 7147, 2), // Elixir of Mind (S-grade)
			new ItemChanceHolder(8639, 10000, 4), // Elixir of CP (S-grade)
			new ItemChanceHolder(9546, 961, 1), // Fire Stone
			new ItemChanceHolder(9547, 961, 1), // Water Stone
			new ItemChanceHolder(9548, 961, 1), // Earth Stone
			new ItemChanceHolder(9549, 961, 1), // Wind Stone
			new ItemChanceHolder(9550, 961, 1), // Dark Stone
			new ItemChanceHolder(9551, 961, 1), // Holy Stone
			new ItemChanceHolder(959, 49, 1), // Scroll: Enchant Weapon (S-grade)
			new ItemChanceHolder(960, 481, 1), // Scroll: Enchant Armor (S-grade)
			new ItemChanceHolder(14701, 2402, 2), // Superior Quick Healing Potion
			new ItemChanceHolder(10260, 3602, 3), // Haste Potion
			new ItemChanceHolder(10261, 3602, 3), // Accuracy Juice
			new ItemChanceHolder(10262, 3602, 3), // Critical Damage Juice
			new ItemChanceHolder(10263, 3602, 3), // Critical Rate Juice
			new ItemChanceHolder(10264, 3602, 3), // Casting Spd. Juice
			new ItemChanceHolder(10265, 3602, 3), // Evasion Juice
			new ItemChanceHolder(10266, 3602, 3), // M. Atk. Juice
			new ItemChanceHolder(10267, 3602, 3), // P. Atk. Potion
			new ItemChanceHolder(10268, 3602, 3), // Wind Walk Juice
			new ItemChanceHolder(5595, 676, 2), // SP Scroll (High-grade)
			new ItemChanceHolder(9898, 271, 1), // SP Scroll (Top-grade)
			new ItemChanceHolder(17185, 136, 1), // Scroll: 10,000 SP
			new ItemChanceHolder(10269, 3602, 3), // P. Def. Juice
			new ItemChanceHolder(9574, 4690, 1), // Mid-grade Life Stone - Lv. 80
			new ItemChanceHolder(10484, 3909, 1), // Mid-grade Life Stone - Lv. 82
			new ItemChanceHolder(14167, 3259, 1), // Mid-grade Life Stone - Lv. 84
			new ItemChanceHolder(21185, 2973, 1), // Transformation Scroll: Zaken - Event
			new ItemChanceHolder(21186, 1784, 1), // Transformation Scroll: Anakim - Event
			new ItemChanceHolder(21187, 2549, 1), // Transformation Scroll: Venom - Event
			new ItemChanceHolder(21188, 2549, 1), // Transformation Scroll: Gordon - Event
			new ItemChanceHolder(21189, 2549, 1), // Transformation Scroll: Ranku - Event
			new ItemChanceHolder(21190, 2549, 1), // Transformation Scroll: Kechi - Event
			new ItemChanceHolder(21191, 2549, 1), // Transformation Scroll: Demon Prince - Event
			new ItemChanceHolder(9552, 223, 1), // Fire Crystal
			new ItemChanceHolder(9553, 223, 1), // Water Crystal
			new ItemChanceHolder(9554, 223, 1), // Earth Crystal
			new ItemChanceHolder(9555, 223, 1), // Wind Crystal
			new ItemChanceHolder(9556, 223, 1), // Dark Crystal
			new ItemChanceHolder(9557, 223, 1), // Holy Crystal
			new ItemChanceHolder(6622, 3568, 1), // Lesser Giant's Codex
			new ItemChanceHolder(9627, 223, 1), // Lesser Giant's Codex - Mastery
			new ItemChanceHolder(1538, 5946, 2), // Blessed Scroll of Escape
			new ItemChanceHolder(3936, 4460, 1), // Blessed Scroll of Resurrection
			new ItemChanceHolder(13071, 12, 1), // Red Soul Crystal - Stage 16
			new ItemChanceHolder(13073, 12, 1), // Green Soul Crystal - Stage 16
			new ItemChanceHolder(13072, 12, 1), // Blue Soul Crystal - Stage 16
			new ItemChanceHolder(13457, 13, 1), // Vesper Cutter
			new ItemChanceHolder(21749, 29, 1))); // Great Adventurer's Treasure Sack
	}
	private static final Map<Integer, Location[][][]> SPAWNS = new HashMap<>();
	static
	{
		// Treasure Chest Level 21
		SPAWNS.put(TREASURE_CHEST_LEVEL_21, new Location[][][]
		{
			{ // territory_begin
				{ // dion06_tb2122_20s
					new Location(40207, 145191, -3360),
					new Location(41090, 146457, -3360),
					new Location(40727, 147323, -3360),
					new Location(38391, 147509, -3360),
					new Location(38314, 146511, -3360),
					new Location(39653, 145086, -3360)
				}
			} // territory_end
		});
		
		// Treasure Chest Level 24
		SPAWNS.put(TREASURE_CHEST_LEVEL_24, new Location[][][]
		{
			{ // territory_begin
				{ // dion03_tb2121_05
					new Location(50270, 118823, -2324),
					new Location(54388, 121233, -2324),
					new Location(53606, 123429, -2324),
					new Location(51396, 124170, -2324),
					new Location(49641, 121266, -2324)
				}
			} // territory_end
		});
		
		// Treasure Chest Level 27
		SPAWNS.put(TREASURE_CHEST_LEVEL_27, new Location[][][]
		{
			{ // territory_begin
				{ // dion03_tb2121_06
					new Location(52120, 124066, -2921),
					new Location(51573, 126308, -2921),
					new Location(47433, 125979, -2921),
					new Location(48894, 124782, -2921)
				}
			}, // territory_end
			{ // territory_begin
				{ // dion02_tb2122_23
					new Location(42857, 141840, -2783),
					new Location(45622, 140341, -2783),
					new Location(49682, 140637, -2783),
					new Location(43661, 144493, -2783),
					new Location(42221, 144692, -2783),
					new Location(41750, 143115, -2783)
				}
			} // territory_end
		});
		
		// Treasure Chest Level 30
		SPAWNS.put(TREASURE_CHEST_LEVEL_30, new Location[][][]
		{
			{ // territory_begin
				{ // dion03_tb2121_13
					new Location(40023, 101330, -1000),
					new Location(40948, 101697, -1000),
					new Location(40974, 102893, -1000),
					new Location(39977, 102912, -1000),
					new Location(38967, 102328, -1000),
					new Location(39178, 101421, -1000)
				}
			}, // territory_end
			{ // territory_begin
				{ // dion02_tb2122_29
					new Location(45699, 151349, -2446),
					new Location(48307, 150431, -2446),
					new Location(50400, 154088, -2446),
					new Location(47523, 155433, -2446),
					new Location(46010, 154313, -2446),
					new Location(44995, 152326, -2446)
				}
			}, // territory_end
			{ // territory_begin
				{ // giran16_tb2221_22
					new Location(78196, 120284, -2120),
					new Location(79960, 120088, -2120),
					new Location(80496, 122136, -2120),
					new Location(79056, 122576, -2120),
					new Location(77320, 121136, -2120)
				}
			} // territory_end
		});
		
		// Treasure Chest Level 33
		SPAWNS.put(TREASURE_CHEST_LEVEL_33, new Location[][][]
		{
			{ // territory_begin
				{ // dion16_tb2023_01
					new Location(9544, 168893, -3098),
					new Location(7319, 164010, -3098),
					new Location(11818, 163965, -3098),
					new Location(11851, 166728, -3098)
				}
			}, // territory_end
			{ // territory_begin
				{ // dion02_tb2122_24
					new Location(44744, 145274, -3238),
					new Location(48911, 142988, -3238),
					new Location(50804, 145462, -3238),
					new Location(47162, 146946, -3238),
					new Location(45026, 146381, -3238)
				},
				{ // dion02_tb2122_26
					new Location(47199, 146990, -3238),
					new Location(50752, 145592, -3238),
					new Location(51469, 146809, -3238),
					new Location(50099, 148622, -3238),
					new Location(48427, 149892, -3238)
				}
			}, // territory_end
			{ // territory_begin
				{ // giran16_tb2221_31
					new Location(91956, 128668, -2680),
					new Location(96808, 128184, -2680),
					new Location(96792, 130724, -2680),
					new Location(92132, 131680, -2680)
				}
			}, // territory_end
			{ // territory_begin
				{ // giran17_tb2321_03
					new Location(116176, 126984, -2128),
					new Location(119716, 127592, -2128),
					new Location(121276, 129412, -2128),
					new Location(121348, 131652, -2128),
					new Location(114992, 130704, -2128)
				},
				{ // giran17_tb2321_04
					new Location(123704, 127516, -2600),
					new Location(124792, 127624, -2600),
					new Location(125436, 129880, -2600),
					new Location(124372, 130748, -2600),
					new Location(121468, 130724, -2600),
					new Location(121296, 128940, -2600)
				},
				{ // giran17_tb2322_17
					new Location(98552, 131280, -3084),
					new Location(103160, 131268, -3084),
					new Location(103056, 133876, -3084),
					new Location(100432, 136044, -3084),
					new Location(98604, 136436, -3084)
				},
				{ // giran17_tb2322_07
					new Location(118568, 131272, -3056),
					new Location(122612, 131248, -3056),
					new Location(122700, 134072, -3056),
					new Location(120168, 133340, -3056)
				}
			}, // territory_end
			{ // territory_begin
				{ // schuttgart11_tb2313_06
					new Location(109137, -157319, -1756),
					new Location(112040, -158196, -1756),
					new Location(111855, -157218, -1756),
					new Location(109034, -156490, -1756)
				},
				{ // schuttgart11_tb2313_07
					new Location(106551, -158264, -1748),
					new Location(109142, -157446, -1748),
					new Location(109019, -156444, -1748),
					new Location(105926, -156799, -1748)
				},
				{ // schuttgart11_tb2313_08
					new Location(106325, -160030, -1592),
					new Location(107721, -160274, -1592),
					new Location(108415, -157659, -1592),
					new Location(106569, -158263, -1592)
				},
				{ // schuttgart11_tb2313_09
					new Location(109922, -161771, -1412),
					new Location(109169, -159427, -1412),
					new Location(108094, -158895, -1412),
					new Location(107728, -160266, -1412)
				},
				{ // schuttgart11_tb2313_15
					new Location(107615, -156613, -1764),
					new Location(109245, -156406, -1764),
					new Location(109462, -154408, -1764),
					new Location(108522, -153828, -1764)
				},
				{ // schuttgart11_tb2313_25
					new Location(116124, -150947, -1880),
					new Location(117894, -150963, -1880),
					new Location(117902, -148827, -1880),
					new Location(116612, -148591, -1880)
				}
			} // territory_end
		});
		
		// Treasure Chest Level 36
		SPAWNS.put(TREASURE_CHEST_LEVEL_36, new Location[][][]
		{
			{ // territory_begin
				{ // giran06_tb2221_02
					new Location(65732, 104240, -3332),
					new Location(66852, 103532, -3332),
					new Location(67884, 105044, -3332),
					new Location(66912, 107704, -3332),
					new Location(65620, 107416, -3332)
				},
				{ // giran06_tb2221_03
					new Location(70156, 103296, -3396),
					new Location(71548, 102920, -3396),
					new Location(71324, 105680, -3396),
					new Location(70040, 104740, -3396)
				},
				{ // giran06_tb2221_06
					new Location(67964, 107148, -3340),
					new Location(68556, 107148, -3340),
					new Location(68280, 110844, -3340),
					new Location(67084, 109424, -3340),
					new Location(67168, 108680, -3340)
				},
				{ // giran06_tb2221_09
					new Location(66668, 113836, -3380),
					new Location(67792, 114592, -3380),
					new Location(69860, 117768, -3380),
					new Location(67104, 118324, -3380),
					new Location(65676, 117356, -3380),
					new Location(65652, 114184, -3380)
				}
			}, // territory_end
			{ // territory_begin
				{ // schuttgart11_tb2313_18
					new Location(107104, -146422, -3276),
					new Location(108656, -145478, -3276),
					new Location(107086, -143812, -3276),
					new Location(106365, -145027, -3276)
				},
				{ // schuttgart11_tb2313_22
					new Location(102181, -141112, -2812),
					new Location(104817, -141199, -2812),
					new Location(105270, -139329, -2812),
					new Location(101415, -139642, -2812)
				},
				{ // schuttgart11_tb2313_23
					new Location(100320, -148634, -2548),
					new Location(103204, -147891, -2548),
					new Location(102483, -145994, -2548),
					new Location(99865, -146715, -2548)
				},
				{ // schuttgart11_tb2313_24
					new Location(101241, -150512, -2516),
					new Location(103161, -150323, -2516),
					new Location(102628, -148049, -2516),
					new Location(100864, -148515, -2516)
				},
				{ // schuttgart11_tb2313_27
					new Location(121218, -149357, -2948),
					new Location(123654, -148398, -2948),
					new Location(123861, -145772, -2948),
					new Location(120575, -147657, -2948)
				},
				{ // schuttgart11_tb2313_29
					new Location(122990, -150619, -2640),
					new Location(125855, -151424, -2640),
					new Location(124588, -148038, -2640),
					new Location(122670, -148831, -2640)
				}
			} // territory_end
		});
		
		// Treasure Chest Level 39
		SPAWNS.put(TREASURE_CHEST_LEVEL_39, new Location[][][]
		{
			{ // territory_begin
				{ // giran06_tb2221_13
					new Location(70920, 122948, -3480),
					new Location(71744, 122960, -3480),
					new Location(71816, 124540, -3480),
					new Location(70884, 124856, -3480)
				},
				{ // giran06_tb2221_14
					new Location(70780, 125160, -3448),
					new Location(71852, 124640, -3448),
					new Location(72660, 125432, -3448),
					new Location(71504, 128292, -3448),
					new Location(70008, 126428, -3448)
				},
				{ // giran06_tb2221_19
					new Location(84084, 100264, -3288),
					new Location(85292, 101008, -3288),
					new Location(84380, 102780, -3288),
					new Location(80720, 102792, -3288),
					new Location(82360, 100564, -3288)
				},
				{ // giran06_tb2321_01
					new Location(98500, 98556, -2764),
					new Location(101700, 98472, -2764),
					new Location(105828, 101836, -2764),
					new Location(102240, 104628, -2764),
					new Location(98460, 103660, -2764)
				}
			} // territory_end
		});
		
		// Treasure Chest Level 42
		SPAWNS.put(TREASURE_CHEST_LEVEL_42, new Location[][][]
		{
			{ // territory_begin
				{ // dion18_tb2021_08
					new Location(13649, 109350, -11891),
					new Location(15077, 109339, -11891),
					new Location(15081, 109944, -11891),
					new Location(13646, 109939, -11891)
				},
				{ // dion18_tb2021_09
					new Location(11609, 109344, -11891),
					new Location(12871, 109345, -11891),
					new Location(12873, 109925, -11891),
					new Location(11608, 109926, -11891)
				},
				{ // dion18_tb2021_10
					new Location(12840, 110168, -11891),
					new Location(13360, 110168, -11891),
					new Location(13356, 111522, -11891),
					new Location(12824, 111524, -11891)
				},
				{ // dion18_tb2021_11
					new Location(12835, 107668, -11891),
					new Location(13346, 107668, -11891),
					new Location(13349, 109090, -11891),
					new Location(12817, 109084, -11891)
				}
			}, // territory_end
			{ // territory_begin
				{ // dion19_tb2021_01
					new Location(14797, 108785, -8882),
					new Location(15423, 108787, -8882),
					new Location(15420, 110128, -8882),
					new Location(14795, 110123, -8882)
				},
				{ // dion19_tb2021_02
					new Location(11953, 108548, -8918),
					new Location(13681, 108543, -8918),
					new Location(13682, 111412, -8918),
					new Location(11957, 111416, -8918)
				},
				{ // dion19_tb2021_06
					new Location(11758, 116701, -8918),
					new Location(13425, 116712, -8918),
					new Location(13420, 119731, -8918),
					new Location(11721, 119733, -8918)
				}
			}, // territory_end
			{ // territory_begin
				{ // innadril08_tb2323_01
					new Location(104628, 176783, -3237),
					new Location(107993, 178074, -3237),
					new Location(106605, 179551, -3237),
					new Location(104967, 180118, -3237),
					new Location(103399, 178771, -3237)
				},
				{ // innadril08_tb2323_02
					new Location(105994, 174722, -3122),
					new Location(109279, 177098, -3122),
					new Location(108259, 178061, -3122),
					new Location(104674, 176632, -3122),
					new Location(104234, 175221, -3122)
				}
			}, // territory_end
			{ // territory_begin
				{ // innadrill05_tb2225_11
					new Location(83469, 258300, -8966),
					new Location(83484, 258862, -8966),
					new Location(81423, 258849, -8966),
					new Location(81413, 258314, -8966)
				},
				{ // innadrill05_tb2225_16
					new Location(83344, 245135, -9066),
					new Location(83345, 245675, -9066),
					new Location(81710, 245688, -9066),
					new Location(81708, 245130, -9066)
				},
				{ // innadrill05_tb2225_17
					new Location(80898, 246443, -9128),
					new Location(80849, 246180, -9128),
					new Location(84441, 246134, -9128),
					new Location(84433, 246423, -9128)
				},
				{ // innadrill05_tb2225_19
					new Location(87531, 248553, -9178),
					new Location(87683, 248460, -9178),
					new Location(88805, 252029, -9178),
					new Location(88596, 252038, -9178)
				},
				{ // innadrill05_tb2225_21
					new Location(87407, 255537, -9178),
					new Location(87641, 255654, -9178),
					new Location(84634, 257815, -9178),
					new Location(84527, 257622, -9178)
				}
			}, // territory_end
			{ // territory_begin
				{ // oren28_tb2218_01
					new Location(69452, 36716, -3064),
					new Location(70752, 36332, -3064),
					new Location(73856, 39204, -3064),
					new Location(70760, 40964, -3064)
				}
			}, // territory_end
			{ // territory_begin
				{ // aden24_tb2320_05
					new Location(105272, 79440, -2148),
					new Location(106072, 79688, -2148),
					new Location(105180, 82120, -2148),
					new Location(103592, 82080, -2148),
					new Location(103664, 80724, -2148)
				},
				{ // aden24_tb2320_06
					new Location(103528, 82200, -2184),
					new Location(106404, 81860, -2184),
					new Location(109000, 82548, -2184),
					new Location(109324, 84768, -2184),
					new Location(104604, 85136, -2184)
				},
				{ // aden24_tb2320_07
					new Location(107936, 79612, -2552),
					new Location(108904, 79876, -2552),
					new Location(108948, 82532, -2552),
					new Location(107584, 82128, -2552)
				},
				{ // aden24_tb2320_09
					new Location(105772, 88692, -1884),
					new Location(108368, 88440, -1884),
					new Location(109656, 88836, -1884),
					new Location(108780, 92144, -1884),
					new Location(106332, 93504, -1884)
				},
				{ // aden24_tb2320_10
					new Location(111344, 86352, -1936),
					new Location(114532, 86832, -1936),
					new Location(115520, 90968, -1936),
					new Location(112312, 93284, -1936),
					new Location(108648, 92500, -1936)
				},
				{ // aden24_tb2320_11
					new Location(116544, 89184, -2912),
					new Location(119940, 92480, -2912),
					new Location(117868, 87400, -2912),
					new Location(119328, 87824, -2912),
					new Location(120768, 89796, -2912),
					new Location(118592, 92184, -2912)
				}
			}, // territory_end
			{ // territory_begin
				{ // aden10_tb2420_02
					new Location(131180, 65640, -3392),
					new Location(135812, 65632, -3392),
					new Location(136428, 67028, -3392),
					new Location(132616, 68964, -3392),
					new Location(131164, 68040, -3392)
				},
				{ // aden10_tb2420_03
					new Location(132524, 69020, -3332),
					new Location(136736, 66912, -3332),
					new Location(137608, 67744, -3332),
					new Location(137592, 69460, -3332),
					new Location(136916, 70500, -3332),
					new Location(134800, 71124, -3332)
				},
				{ // aden10_tb2420_04
					new Location(134684, 72980, -3028),
					new Location(135976, 71264, -3028),
					new Location(137284, 71652, -3028),
					new Location(138824, 73148, -3028),
					new Location(135704, 75096, -3028),
					new Location(134800, 74100, -3028)
				},
				{ // aden10_tb2420_05
					new Location(138876, 73236, -2832),
					new Location(141140, 73288, -2832),
					new Location(141388, 77764, -2832),
					new Location(135748, 75136, -2832)
				},
				{ // aden10_tb2420_06
					new Location(142284, 73384, -2776),
					new Location(145200, 72988, -2776),
					new Location(146580, 73440, -2776),
					new Location(146800, 75192, -2776),
					new Location(145444, 76620, -2776),
					new Location(142876, 75752, -2776)
				}
			}, // territory_end
		});
		
		// Treasure Chest Level 45
		SPAWNS.put(TREASURE_CHEST_LEVEL_45, new Location[][][] {});
		
		// Treasure Chest Level 48
		SPAWNS.put(TREASURE_CHEST_LEVEL_48, new Location[][][] {});
		
		// Treasure Chest Level 51
		SPAWNS.put(TREASURE_CHEST_LEVEL_51, new Location[][][] {});
		
		// Treasure Chest Level 54
		SPAWNS.put(TREASURE_CHEST_LEVEL_54, new Location[][][] {});
		
		// Treasure Chest Level 57
		SPAWNS.put(TREASURE_CHEST_LEVEL_57, new Location[][][] {});
		
		// Treasure Chest Level 60
		SPAWNS.put(TREASURE_CHEST_LEVEL_60, new Location[][][] {});
		
		// Treasure Chest Level 63
		SPAWNS.put(TREASURE_CHEST_LEVEL_63, new Location[][][] {});
		
		// Treasure Chest Level 66
		SPAWNS.put(TREASURE_CHEST_LEVEL_66, new Location[][][] {});
		
		// Treasure Chest Level 69
		SPAWNS.put(TREASURE_CHEST_LEVEL_69, new Location[][][] {});
		
		// Treasure Chest Level 72
		SPAWNS.put(TREASURE_CHEST_LEVEL_72, new Location[][][] {});
		
		// Treasure Chest Level 75
		SPAWNS.put(TREASURE_CHEST_LEVEL_75, new Location[][][] {});
		
		// Treasure Chest Level 78
		SPAWNS.put(TREASURE_CHEST_LEVEL_78, new Location[][][] {});
		
		// Treasure Chest Level 81
		SPAWNS.put(TREASURE_CHEST_LEVEL_81, new Location[][][] {});
		
		// Treasure Chest Level 84
		SPAWNS.put(TREASURE_CHEST_LEVEL_84, new Location[][][] {});
	}
	
	private TreasureChest()
	{
		super(TreasureChest.class.getSimpleName(), "ai/group_template");
		addSpawnId(SPAWNS.keySet());
		addAttackId(SPAWNS.keySet());
		addSkillSeeId(SPAWNS.keySet());
		addSpellFinishedId(SPAWNS.keySet());
		spawnTreasureChests();
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		switch (event)
		{
			case "spawnTreasureChest":
				spawnTreasureChests();
				break;
			case "despawnTreasureChest":
				onDespawnTreasureChest(npc);
				break;
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		addSkillCastDesire(npc, attacker, TREASURE_BOMBS[npc.getLevel() / 10], 1000000);
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, Skill skill, L2Object[] targets, boolean isSummon)
	{
		if (!Util.contains(targets, npc))
		{
			return null;
		}
		
		int openChance = 0;
		
		switch (skill.getId())
		{
			case UNLOCK_SKILL:
				int maxChance = 0;
				
				try
				{
					maxChance = UNLOCK_SKILL_MAX_CHANCES[skill.getLevel() - 1];
				}
				catch (RuntimeException e)
				{
					e.printStackTrace();
				}
				openChance = Math.min(maxChance, maxChance - ((npc.getLevel() - (skill.getLevel() * 4) - 16) * 6));
				break;
			case MAESTRO_KEY_SKILL:
				if (((caster.getLevel() <= 77) && (Math.abs(npc.getLevel() - caster.getLevel()) <= 6)) || ((caster.getLevel() >= 78) && (Math.abs(npc.getLevel() - caster.getLevel()) <= 5)))
				{
					openChance = 100;
				}
				break;
			default:
				addSkillCastDesire(npc, caster, TREASURE_BOMBS[npc.getLevel() / 10], 1000000);
				return null;
		}
		
		if (getRandom(100) <= openChance)
		{
			final List<ItemChanceHolder> items = DROPS.get(npc.getId());
			
			if (items == null)
			{
				LOG.warn("{} with Id {}, doesn't have a drop list!", TreasureChest.class.getSimpleName(), npc.getId());
			}
			else
			{
				for (ItemChanceHolder item : items)
				{
					if (getRandom(10000) <= item.getChance())
					{
						npc.dropItem(caster, item.getId(), item.getCount());
					}
				}
				npc.setIsInvul(false);
				npc.reduceCurrentHp(npc.getMaxHp(), caster, skill);
			}
		}
		else
		{
			if (!(caster.getInventory().getItemsByItemId(MAESTRO_KEY_ITEM) != null))
			{
				caster.sendPacket(SystemMessageId.IF_YOU_HAVE_A_MAESTROS_KEY_YOU_CAN_USE_IT_TO_OPEN_THE_TREASURE_CHEST);
			}
			else
			{
				playSound(caster, Sound.ITEMSOUND_BROKEN_KEY);
			}
			npc.deleteMe();
		}
		return super.onSkillSee(npc, caster, skill, targets, isSummon);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		switch (npc.getId())
		{
			case TREASURE_CHEST_LEVEL_21:
			case TREASURE_CHEST_LEVEL_24:
			case TREASURE_CHEST_LEVEL_27:
			case TREASURE_CHEST_LEVEL_30:
			case TREASURE_CHEST_LEVEL_33:
			case TREASURE_CHEST_LEVEL_36:
			case TREASURE_CHEST_LEVEL_39:
			case TREASURE_CHEST_LEVEL_42:
			case TREASURE_CHEST_LEVEL_45:
			case TREASURE_CHEST_LEVEL_48:
			case TREASURE_CHEST_LEVEL_51:
			case TREASURE_CHEST_LEVEL_54:
			case TREASURE_CHEST_LEVEL_57:
			case TREASURE_CHEST_LEVEL_60:
			case TREASURE_CHEST_LEVEL_63:
			case TREASURE_CHEST_LEVEL_66:
			case TREASURE_CHEST_LEVEL_69:
			case TREASURE_CHEST_LEVEL_72:
			case TREASURE_CHEST_LEVEL_75:
			case TREASURE_CHEST_LEVEL_78:
			case TREASURE_CHEST_LEVEL_81:
			case TREASURE_CHEST_LEVEL_84:
				npc.disableCoreAI(true);
				npc.setIsInvul(true);
				break;
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, Skill skill)
	{
		npc.deleteMe();
		return super.onSpellFinished(npc, player, skill);
	}
	
	private void onDespawnTreasureChest(L2Npc npc)
	{
		switch (npc.getId())
		{
			case TREASURE_CHEST_LEVEL_21:
			case TREASURE_CHEST_LEVEL_24:
			case TREASURE_CHEST_LEVEL_27:
			case TREASURE_CHEST_LEVEL_30:
			case TREASURE_CHEST_LEVEL_33:
			case TREASURE_CHEST_LEVEL_36:
			case TREASURE_CHEST_LEVEL_39:
			case TREASURE_CHEST_LEVEL_42:
			case TREASURE_CHEST_LEVEL_45:
			case TREASURE_CHEST_LEVEL_48:
			case TREASURE_CHEST_LEVEL_51:
			case TREASURE_CHEST_LEVEL_54:
			case TREASURE_CHEST_LEVEL_57:
			case TREASURE_CHEST_LEVEL_60:
			case TREASURE_CHEST_LEVEL_63:
			case TREASURE_CHEST_LEVEL_66:
			case TREASURE_CHEST_LEVEL_69:
			case TREASURE_CHEST_LEVEL_72:
			case TREASURE_CHEST_LEVEL_75:
			case TREASURE_CHEST_LEVEL_78:
			case TREASURE_CHEST_LEVEL_81:
			case TREASURE_CHEST_LEVEL_84:
				npc.deleteMe();
				break;
		}
		cancelQuestTimer("despawnTreasureChest", npc, null);
	}
	
	private void spawnNpc(int npcId, Location loc, int respawnDelay)
	{
		try
		{
			final L2Spawn npcSpawn = new L2Spawn(npcId);
			npcSpawn.setLocation(loc);
			npcSpawn.setAmount(1);
			npcSpawn.setRespawnDelay(respawnDelay);
			npcSpawn.startRespawn();
			SpawnTable.getInstance().addNewSpawn(npcSpawn, false);
			npcSpawn.doSpawn();
		}
		catch (Exception e)
		{
			LOG.warn("Error trying to spawn chest Id {} in location {}, {}, {}", npcId, loc.getX(), loc.getY(), loc.getZ());
			e.printStackTrace();
		}
	}
	
	private void spawnTreasureChests()
	{
		for (Entry<Integer, Location[][][]> entry : SPAWNS.entrySet())
		{
			for (Location[][] territory : entry.getValue())
			{
				switch (entry.getKey())
				{
					case TREASURE_CHEST_LEVEL_21:
						for (Location[] territoryGrid : territory)
						{
							if (Util.contains(territory, territory[0]))
							{
								for (int i = 0; i < 2; i++)
								{
									spawnNpc(entry.getKey(), territoryGrid[i], TREASURE_CHEST_RESPAWN);
								}
							}
							else
							{
								for (int i = 0; i < 1; i++)
								{
									spawnNpc(entry.getKey(), territoryGrid[i], TREASURE_CHEST_RESPAWN);
								}
							}
						}
						break;
					case TREASURE_CHEST_LEVEL_24:
					case TREASURE_CHEST_LEVEL_27:
					case TREASURE_CHEST_LEVEL_30:
					case TREASURE_CHEST_LEVEL_33:
					case TREASURE_CHEST_LEVEL_36:
					case TREASURE_CHEST_LEVEL_39:
					case TREASURE_CHEST_LEVEL_42:
					case TREASURE_CHEST_LEVEL_45:
						for (Location[] territoryGrid : territory)
						{
							for (int i = 0; i < 1; i++)
							{
								spawnNpc(entry.getKey(), territoryGrid[i], TREASURE_CHEST_RESPAWN);
							}
						}
						break;
					case TREASURE_CHEST_LEVEL_48:
						for (Location[] territoryGrid : territory)
						{
							if (Util.contains(territory, territory[0]))
							{
								for (int i = 0; i < 3; i++)
								{
									spawnNpc(entry.getKey(), territoryGrid[i], TREASURE_CHEST_RESPAWN);
								}
							}
							else if (Util.contains(territory, territory[1]))
							{
								for (int i = 0; i < 2; i++)
								{
									spawnNpc(entry.getKey(), territoryGrid[i], TREASURE_CHEST_RESPAWN);
								}
							}
							else
							{
								for (int i = 0; i < 1; i++)
								{
									spawnNpc(entry.getKey(), territoryGrid[i], TREASURE_CHEST_RESPAWN);
								}
							}
						}
						break;
					case TREASURE_CHEST_LEVEL_51:
						for (Location[] territoryGrid : territory)
						{
							if (Util.contains(territory, territory[1]))
							{
								for (int i = 0; i < 2; i++)
								{
									spawnNpc(entry.getKey(), territoryGrid[i], TREASURE_CHEST_RESPAWN);
								}
							}
							else
							{
								for (int i = 0; i < 1; i++)
								{
									spawnNpc(entry.getKey(), territoryGrid[i], TREASURE_CHEST_RESPAWN);
								}
							}
						}
						break;
					case TREASURE_CHEST_LEVEL_54:
					case TREASURE_CHEST_LEVEL_57:
					case TREASURE_CHEST_LEVEL_60:
					case TREASURE_CHEST_LEVEL_63:
					case TREASURE_CHEST_LEVEL_66:
					case TREASURE_CHEST_LEVEL_69:
					case TREASURE_CHEST_LEVEL_72:
					case TREASURE_CHEST_LEVEL_75:
					case TREASURE_CHEST_LEVEL_78:
					case TREASURE_CHEST_LEVEL_81:
					case TREASURE_CHEST_LEVEL_84:
						for (Location[] territoryGrid : territory)
						{
							for (int i = 0; i < 1; i++)
							{
								spawnNpc(entry.getKey(), territoryGrid[i], TREASURE_CHEST_RESPAWN);
							}
						}
						break;
				}
			}
		}
	}
	
	public static void main(String[] args)
	{
		new TreasureChest();
	}
}