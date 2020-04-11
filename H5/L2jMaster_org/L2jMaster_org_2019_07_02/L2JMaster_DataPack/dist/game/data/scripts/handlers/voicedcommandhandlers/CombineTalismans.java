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
package handlers.voicedcommandhandlers;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

import com.l2jserver.Config;
import com.l2jserver.gameserver.handler.IVoicedCommandHandler;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.util.Util;

/**
 * This is a clean version of my original idea back in 2012.
 * @author Nik
 */
public class CombineTalismans implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"combinetalismans"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params)
	{
		if (!Config.COMBINE_TALISMAN_ENABLED)
		{
			activeChar.sendMessage("This command is Disabled!.");
			return false;
		}
		if (VOICED_COMMANDS[0].equalsIgnoreCase(command))
		{
			final Map<Integer, L2ItemInstance> talismans = new HashMap<>();
			activeChar.sendMessage("Combining talismans...");
			try
			{
				//@formatter:off
				Arrays.stream(activeChar.getInventory().getItems())
				.filter(Objects::nonNull)
				.filter(L2ItemInstance::isShadowItem)
				.filter(i -> Util.contains(TALISMAN_IDS, i.getId())) // Filter that only talismans go through.
				.sorted((i1, i2) -> Boolean.compare(i2.isEquipped(), i1.isEquipped())) // Equipped talismans first (so we can then pick equipped first for charging).
				.forEach(item ->
				{
					final L2ItemInstance talismanToCharge = talismans.putIfAbsent(item.getId(), item);
					if ((talismanToCharge != null) && activeChar.destroyItem(VOICED_COMMANDS[0], item, activeChar, false))
					{
						talismanToCharge.decreaseMana(false, -item.getMana()); // Minus in decrease = increase :P
					}
				});
				//@formatter:on
				
				activeChar.sendMessage(talismans.isEmpty() ? "...there were no combined talismans." : "...results:");
				talismans.values().forEach(i -> activeChar.sendMessage(i.getName() + " has been successfully combined."));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("There was a problem while combining your talismans, please consult with an admin, and tell him this date: " + Util.getDateString(new Date(System.currentTimeMillis())));
				_log.log(Level.WARNING, "Error while combining talismans: ", e);
			}
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
	
	//@formatter:off
	// All talismans inside high five client.
	public static final int[] TALISMAN_IDS =
	{
		9914, // Blue Talisman of Power		Increases P. Atk. when in use. Effect does not stack with additional Talismans of the same type. Shadow Item that cannot be traded or dropped.
		9915, // Blue Talisman of Wild Magic		Increases Critical Rate of magic attacks temporarily. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9916, // Blue Talisman of Defense		Temporarily decreases P. Def./M. Def./Evasion and increases P. Atk./M. Atk./Atk. Spd./Casting Spd./speed. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9917, // Red Talisman of Minimum Clarity		Decreases skill MP consumption when used. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9918, // Red Talisman of Maximum Clarity		Temporarily greatly decreases skill MP consumption when used. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9919, // Blue Talisman of Reflection		When used, grants a skill whereby any physical melee damage received will be reflected back onto your attacker. Effect does not stack with additional talismans of the same type. This item cannot be traded or dropped.
		9920, // Blue Talisman of Invisibility		Makes you invisible for a while, so you cannot be attacked by enemies. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9921, // Blue Talisman - Shield Protection		When used, increases the protection power of a shield. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9922, // Black Talisman - Mending		When used, it cures bleeding. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9923, // Black Talisman - Escape		When used, it cancels all Movement Inability States. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9924, // Blue Talisman of Healing		It increases HP Recovery Magic temporarily. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9925, // Red Talisman of Recovery		It recovers HP/CP. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9926, // Blue Talisman of Defense		When used, increases P. Def. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9927, // Blue Talisman of Magic Defense		When used, increases magic P. Def. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9928, // Red Talisman of Mental Regeneration		When used, it speeds up MP recovery. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9929, // Blue Talisman of Protection		When used, increases the protection power of a shield. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9930, // Blue Talisman of Evasion		When used, increases evasion and reduces Critical Damage possibilities. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9931, // Red Talisman of Meditation		When used, it greatly increases MP Recovery Rate. You cannot move during recovery. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9932, // Blue Talisman - Divine Protection		When used, P. Def. and M. Def. greatly increase momentarily. Only one effect is applied when you wear the same two talismans. Shadow Item with no exchange/drop available
		9933, // Yellow Talisman of Power		Increases P. Atk. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9934, // Yellow Talisman of Violent Haste		Increases Atk. Spd. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9935, // Yellow Talisman of Arcane Defense		Increases Magic Defense. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9936, // Yellow Talisman of Arcane Power		Increases M. Atk. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9937, // Yellow Talisman of Arcane Haste		Increases Casting Spd. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9938, // Yellow Talisman of Accuracy		Increases Accuracy. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9939, // Yellow Talisman of Defense		Increases Defense. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9940, // Yellow Talisman of Alacrity		Increases P. Atk., M. Atk., and Speed. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9941, // Yellow Talisman of Speed		Increases Speed. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9942, // Yellow Talisman of Critical Reduction		When used, it decreases damages from physical critical attacks. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9943, // Yellow Talisman of Critical Damage		Increases Critical P. Atk. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9944, // Yellow Talisman of Critical Dodging		When used, it decreases physical Critical Damage possibility. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9945, // Yellow Talisman of Evasion		Increases Evasion. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9946, // Yellow Talisman of Healing		Increases HP Recovery Magic temporarily. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9947, // Yellow Talisman of CP Regeneration		Increases CP recovery. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9948, // Yellow Talisman of Physical Regeneration		Increases HP recovery. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9949, // Yellow Talisman of Mental Regeneration		Increases MP recovery. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9950, // Grey Talisman of Weight Training		Increases weapon Weight Limit. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9951, // Grey Talisman of Mid-Grade Fishing		Obtains Mid-Grade Fishing Mastery Level (Lv 18). Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9952, // Orange Talisman - Hot Springs CP Potion		Create 1 Hot Springs CP Potion following the consumption of 16 Soul Ore. Effect does not stack with additional Talismans of the same type. Shadow Item that cannot be traded or dropped.
		9953, // Orange Talisman - Elixir of Life		Consumes 50 Soul Ore to create an A-Grade Elixir of Life. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9954, // Orange Talisman - Elixir of Mental Strength		Consumes 57 Soul Ore to create an A-Grade Elixir of Mental Strength. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9955, // Black Talisman - Vocalization		Breaks magic silence. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9956, // Black Talisman - Arcane Freedom		When used, it cancels Magical Movement Inability States. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9957, // Black Talisman - Physical Freedom		When used, it cancels Physical Movement Inability States. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9958, // Black Talisman - Rescue		When used, it cancels Physical Skill Inability States. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9959, // Black Talisman - Free Speech		Breaks silence. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9960, // White Talisman of Bravery		Increases Resistance to mental attacks. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9961, // White Talisman of Motion		When equipped, Resistance to Paralysis increases. Only one effect is applied when you wear the same two talismans. Shadow Item with no exchange/drop available
		9962, // White Talisman of Grounding		When equipped, Resistance to Shock attacks increases. Only one effect is applied when you wear the same two talismans. Shadow Item with no exchange/drop available
		9963, // White Talisman of Attention		When equipped, Resistance to sleep attacks increases. Only one effect is applied when you wear the same two talismans. Shadow Item with no exchange/drop available
		9964, // White Talisman of Bandages		When equipped, Resistance to bleed attacks increases. Only one effect is applied when you wear the same two talismans. Shadow Item with no exchange/drop available
		9965, // White Talisman of Protection		Increases Resistance to buff disarming magic. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		9966, // White Talisman of Freedom		When equipped, Resistance to Hold increases. Only one effect is applied when you wear the same two talismans. Shadow Item with no exchange/drop available
		10141, // Grey Talisman - Yeti Transform		Can be transformed into a Yeti for a certain amount of time. Transformation is cancelled if unequipped. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		10142, // Grey Talisman - Buffalo Transform		Can be transformed into a Buffalo for a certain amount of time. Transformation is cancelled if unequipped. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		10158, // Grey Talisman of Upper Grade Fishing		Obtains Upper-Grade Fishing Mastery Level (Lv 24). Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		10416, // Blue Talisman - Explosion		Increases P. Atk./Atk. Spd. Effect does not stack with additional Talismans of the same type. This item cannot be exchanged or dropped.
		10417, // Blue Talisman - Magic Explosion		Increases M. Atk. and M. Critical Rate . Effect does not stack with additional Talismans of the same type. This item cannot be exchanged or dropped.
		10418, // White Talisman - Storm		Increases Wind attribute. Effect does not stack with additional Talismans of the same type. This item cannot be exchanged or dropped.
		10419, // White Talisman - Darkness		Increases Dark attribute. Effect does not stack with additional Talismans of the same type. This item cannot be exchanged or dropped.
		10420, // White Talisman - Water		Increases Water attribute. Effect does not stack with additional Talismans of the same type. This item cannot be exchanged or dropped.
		10421, // White Talisman - Fire		Instantly increases fire elemental. Only one effect is applied when you wear the same two talismans. Projection Weapons with no exchange/drop available
		10422, // White Talisman - Light		Increases holy attribute. Effect does not stack with additional Talismans of the same type. This item cannot be exchanged or dropped.
		10423, // Blue Talisman - Self-Destruction		Damages a nearby enemy with a powerful explosion. Effect does not stack with additional Talismans of the same type. This item cannot be exchanged or dropped.
		10424, // Blue Talisman - Greater Healing		Increases heal power. Effect does not stack with additional Talismans of the same type. This item cannot be exchanged or dropped.
		10518, // Red Talisman - Life Force		Completely restores MP/HP when used. Effect does not stack with additional Talismans of the same type. This item cannot be exchanged or dropped.
		10519, // White Talisman -  Earth		Increases Earth attribute defense by 50 when used. Effect does not stack with additional Talismans of the same type. This item cannot be exchanged or dropped.
		10533, // Blue Talisman - P. Atk.		Increases P. Atk. when used. Effect does not stack with additional Talismans of the same type. A Shadow Item which cannot be exchanged or dropped.
		10534, // Blue Talisman - Shield Defense		Shield Def. increases when used. Effect does not stack with additional Talismans of the same type. A Shadow Item which cannot be exchanged or dropped.
		10535, // Yellow Talisman - P. Def.		P. Def. increases when equipped. Effect does not stack with additional Talismans of the same type. A Shadow Item which cannot be exchanged or dropped.
		10536, // Yellow Talisman - M. Atk.		Increases M. Atk. when equipped. Effect does not stack with additional Talismans of the same type. A Shadow Item which cannot be exchanged or dropped.
		10537, // Yellow Talisman - Evasion		Increases Evasion when equipped. Effect does not stack with additional Talismans of the same type. A Shadow Item which cannot be exchanged or dropped.
		10538, // Yellow Talisman - Healing Power		HP recovery M. Atk. increases when equipped. Effect does not stack with additional Talismans of the same type. A Shadow Item which cannot be exchanged or dropped.
		10539, // Yellow Talisman - CP Recovery Rate		CP recovery rate increases when equipped. Effect does not stack with additional Talismans of the same type. A Shadow Item which cannot be exchanged or dropped.
		10540, // Yellow Talisman - HP Recovery Rate		Increases HP Recovery Rate. Effect does not stack with additional Talismans of the same type. A Shadow Item which cannot be exchanged or dropped.
		10541, // Yellow Talisman - Low Grade MP Recovery Rate		Increases MP Recovery Rate. Effect does not stack with additional Talismans of the same type. This item cannot be exchanged or dropped.
		10542, // Red Talisman - HP/CP Recovery		HP/CP are recovered when equipped. Effect does not stack with additional Talismans of the same type. A Shadow Item which cannot be exchanged or dropped.
		10543, // Yellow Talisman - Speed		Speed increases when equipped. Effect does not stack with additional Talismans of the same type. A Shadow Item which cannot be exchanged or dropped.
		12815, // Red Talisman - Max CP		Increases Max CP when equipped. Effect does not stack with additional Talismans of the same type. Shadow Item that cannot be exchanged or dropped.
		12816, // Red Talisman - CP Regeneration		Increases CP regeneration rate when equipped. Effect does not stack with additional Talismans of the same type. Shadow Item that cannot be exchanged or dropped.
		12817, // Yellow Talisman - Increase Force		Increases your Force Level when attacked while equipped. Effect does not stack with additional Talismans of the same type. Shadow Item that cannot be exchanged or dropped.
		12818, // Yellow Talisman - Damage Transition		Transfers a portion of damage you take onto your servitor while equipped. Effect does not stack with additional Talismans of the same type. Shadow Item that cannot be exchanged or dropped.
		14604, // Red Talisman - Territory Guardian		When used, Max CP is greatly increased and a certain amount of CP is greatly recovered. Only one effect is applied when you wear the same two talismans. Shadow Item with no exchange/drop available
		14605, // Red Talisman - Territory Guard		When used, Max CP is increased and a certain amount of CP is recovered. Only one effect is applied when you wear the same two talismans. Shadow Item with no exchange/drop available
		14810, // Blue Talisman - Buff Cancel		Cancels the buffs of nearby enemies upon use. Only one effect is applied when you wear the same two talismans. Shadow Item with no exchange/drop available
		14811, // Blue Talisman - Buff Steal		Steals the target's abnormal status upon use. Only one effect is applied when you wear the same two talismans. Shadow Item with no exchange/drop available
		14812, // Red Talisman - Territory Guard		When used, Max CP is greatly increased and a certain amount of CP is greatly recovered. Only one effect is applied when you wear the same two talismans. Shadow Item with no exchange/drop available
		14813, // Blue Talisman - Lord's Divine Protection		Upon use, greatly decreases the damage received during PvP. Only one effect is applied when you wear the same two talismans. Shadow Item with no exchange/drop available
		14814, // White Talisman -  All Resistance		Increases Resistance to all elements. Effect does not stack with additional Talismans of the same type. This item cannot be traded or dropped.
		17051, // Talisman - STR		Event item. STR +2 when equipped. Cannot be traded, dropped, destroyed, or used in the Olympiad. \\nThis item will be deleted in bulk after the regular maintenance time on Jul. 7.
		17052, // Talisman - DEX		Event item. DEX +2 when equipped. Cannot be traded, dropped, destroyed, or used in the Olympiad. \\nThis item will be deleted in bulk after the regular maintenance time on Jul. 7.
		17053, // Talisman - CON		Event item. CON +2 when equipped. Cannot be traded, dropped, destroyed, or used in the Olympiad. \\nThis item will be deleted in bulk after the regular maintenance time on Jul. 7.
		17054, // Talisman - WIT		Event item. WIT +2 when equipped. Cannot be traded, dropped, destroyed, or used in the Olympiad. \\nThis item will be deleted in bulk after the regular maintenance time on Jul. 7.
		17055, // Talisman - INT		Event item. INT +2 when equipped. Cannot be traded, dropped, destroyed, or used in the Olympiad. \\nThis item will be deleted in bulk after the regular maintenance time on Jul. 7.
		17056, // Talisman - MEN		Event item. MEN +2 when equipped. Cannot be traded, dropped, destroyed, or used in the Olympiad. \\nThis item will be deleted in bulk after the regular maintenance time on Jul. 7.
		17057, // Talisman - Resistance to Stun		Event item. Resistance to Stun +15 when equipped. Cannot be traded, dropped, destroyed, or used in the Olympiad. \\nThis item will be deleted in bulk after the regular maintenance time on Jul. 7.
		17058, // Talisman - Resistance to Sleep		Event item. Resistance to Sleep +15 when equipped. Cannot be traded, dropped, destroyed, or used in the Olympiad. \\nThis item will be deleted in bulk after the regular maintenance time on Jul. 7.
		17059, // Talisman - Resistance to Hold		Event item. Resistance to Hold +15 when equipped. Cannot be traded, dropped, destroyed, or used in the Olympiad. \\nThis item will be deleted in bulk after the regular maintenance time on Jul. 7.
		17060, // Talisman - Paralyze Resistance		Event item. Paralysis resistance +15 when equipped. Cannot be traded, dropped, destroyed, or used in the Olympiad. \\nThis item will be deleted in bulk after the regular maintenance time on Jul. 7.
		17061, // Talisman - ALL STAT		Event item. All stats increase by 1 when equipped. Cannot be traded, dropped, destroyed, or used in the Olympiad. \\nThis item will be deleted after the regular maintenance on July 7th.
	22326, // Blue Talisman - Buff Cancel		Cancels the buffs of nearby enemies upon use. Only one effect is applies when you wear two of the same talismans. Cannot be exchanged or dropped. Can be destroyed. Can be stored in a private warehouse.
		22327 // Blue Talisman - Buff Steal		Stealsthe target's abnormal status upon use. Only one effect is applies when you wear two of the same talismans. Cannot be exchanged or dropped. Can be destroyed. Can be stored in a private warehouse.
	};
	//@formatter:on
}