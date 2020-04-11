package com.l2jfrozen.gameserver.ai.special;

import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.quest.Quest;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.util.Util;

public class VarkaKetraAlly extends Quest implements Runnable
{
	public VarkaKetraAlly(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		final int[] mobs =
		{
			// ketra mobs
			21324,
			21325,
			21327,
			21328,
			21329,
			21331,
			21332,
			21334,
			21335,
			21336,
			21338,
			21339,
			21340,
			21342,
			21343,
			21344,
			21345,
			21346,
			21347,
			21348,
			21349,
			// varka mobs
			21350,
			21351,
			21353,
			21354,
			21355,
			21357,
			21358,
			21360,
			21361,
			21362,
			21364,
			21365,
			21366,
			21368,
			21369,
			21370,
			21371,
			21372,
			21373,
			21374,
			21375
		};
		
		for (final int mob : mobs)
		{
			addEventId(mob, Quest.QuestEventType.ON_KILL);
			addEventId(mob, Quest.QuestEventType.ON_ATTACK);
		}
	}
	
	protected static final int[] ketraAllyMark =
	{
		7211,
		7212,
		7213,
		7214,
		7215
	};
	
	protected static final int[] varkaAllyMark =
	{
		7225,
		7224,
		7223,
		7222,
		7221
	};
	
	@Override
	public String onAttack(final L2NpcInstance npc, final L2PcInstance attacker, final int damage, final boolean isPet)
	{
		if (attacker.getAllianceWithVarkaKetra() != 0)
		{
			if (attacker.isAlliedWithKetra() && npc.getFactionId() == "ketra" || attacker.isAlliedWithVarka() && npc.getFactionId() == "varka")
			{
				final L2Skill skill = SkillTable.getInstance().getInfo(4578, 1);
				if (skill != null)
				{
					npc.setTarget(attacker);
					npc.doCast(skill);
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(final L2NpcInstance npc, final L2PcInstance killer, final boolean isPet)
	{
		if (killer.getParty() != null)
		{
			for (final L2PcInstance member : killer.getParty().getPartyMembers())
			{
				if (Util.checkIfInRange(6000, killer, member, true))
				{
					decreaseAlly(npc, member);
				}
			}
		}
		else
		{
			decreaseAlly(npc, killer);
		}
		
		return super.onKill(npc, killer, isPet);
	}
	
	private void decreaseAlly(final L2NpcInstance npc, final L2PcInstance player)
	{
		if (player.getAllianceWithVarkaKetra() != 0)
		{
			L2ItemInstance mark = null;
			
			if (player.isAlliedWithKetra() && npc.getFactionId() == "ketra")
			{
				final L2ItemInstance varkasBadgeSoldier = player.getInventory().getItemByItemId(7216);
				final L2ItemInstance varkasBadgeOfficer = player.getInventory().getItemByItemId(7217);
				final L2ItemInstance varkasBadgeCaptain = player.getInventory().getItemByItemId(7218);
				final L2ItemInstance valorTotem = player.getInventory().getItemByItemId(7219);
				final L2ItemInstance wisdomTotem = player.getInventory().getItemByItemId(7220);
				
				final int varkasBadgeSoldierCount = varkasBadgeSoldier == null ? 0 : varkasBadgeSoldier.getCount();
				final int varkasBadgeOfficerCount = varkasBadgeOfficer == null ? 0 : varkasBadgeOfficer.getCount();
				final int varkasBadgeCaptainCount = varkasBadgeCaptain == null ? 0 : varkasBadgeCaptain.getCount();
				final int valorTotemCount = valorTotem == null ? 0 : valorTotem.getCount();
				final int wisdomTotemCount = wisdomTotem == null ? 0 : wisdomTotem.getCount();
				
				if (varkasBadgeSoldierCount > 0)
				{
					player.getInventory().destroyItemByItemId("ketrasBadgeSoldier", 7216, varkasBadgeSoldierCount, player, player.getTarget());
				}
				if (varkasBadgeOfficerCount > 0)
				{
					player.getInventory().destroyItemByItemId("ketrasBadgeOfficer", 7217, varkasBadgeOfficerCount, player, player.getTarget());
				}
				if (varkasBadgeCaptainCount > 0)
				{
					player.getInventory().destroyItemByItemId("ketrasBadgeCaptain", 7218, varkasBadgeCaptainCount, player, player.getTarget());
				}
				if (valorTotemCount > 0)
				{
					player.getInventory().destroyItemByItemId("valorTotem", 7219, valorTotemCount, player, player.getTarget());
				}
				if (wisdomTotemCount > 0)
				{
					player.getInventory().destroyItemByItemId("wisdomTotem", 7220, wisdomTotemCount, player, player.getTarget());
				}
				
				player.getInventory().destroyItemByItemId("Mark", ketraAllyMark[player.getAllianceWithVarkaKetra() - 1], 1, player, player.getTarget());
				player.setAllianceWithVarkaKetra(player.getAllianceWithVarkaKetra() - 1);
				if (player.getAllianceWithVarkaKetra() != 0)
				{
					mark = player.getInventory().addItem("Mark", ketraAllyMark[player.getAllianceWithVarkaKetra() - 1], 1, player, player);
				}
			}
			
			if (player.isAlliedWithVarka() && npc.getFactionId() == "varka")
			{
				final L2ItemInstance ketrasBadgeSoldier = player.getInventory().getItemByItemId(7226);
				final L2ItemInstance ketrasBadgeOfficer = player.getInventory().getItemByItemId(7227);
				final L2ItemInstance ketrasBadgeCaptain = player.getInventory().getItemByItemId(7228);
				final L2ItemInstance featherValor = player.getInventory().getItemByItemId(7229);
				final L2ItemInstance featherWisdom = player.getInventory().getItemByItemId(7230);
				
				final int ketrasBadgeSoldierCount = ketrasBadgeSoldier == null ? 0 : ketrasBadgeSoldier.getCount();
				final int ketrasBadgeOfficerCount = ketrasBadgeOfficer == null ? 0 : ketrasBadgeOfficer.getCount();
				final int ketrasBadgeCaptainCount = ketrasBadgeCaptain == null ? 0 : ketrasBadgeCaptain.getCount();
				final int featherValorCount = featherValor == null ? 0 : featherValor.getCount();
				final int featherWisdomCount = featherWisdom == null ? 0 : featherWisdom.getCount();
				
				if (ketrasBadgeSoldierCount > 0)
				{
					player.getInventory().destroyItemByItemId("ketrasBadgeSoldier", 7226, ketrasBadgeSoldierCount, player, player.getTarget());
				}
				if (ketrasBadgeOfficerCount > 0)
				{
					player.getInventory().destroyItemByItemId("ketrasBadgeOfficer", 7227, ketrasBadgeOfficerCount, player, player.getTarget());
				}
				if (ketrasBadgeCaptainCount > 0)
				{
					player.getInventory().destroyItemByItemId("ketrasBadgeCaptain", 7228, ketrasBadgeCaptainCount, player, player.getTarget());
				}
				if (featherValorCount > 0)
				{
					player.getInventory().destroyItemByItemId("featherValor", 7229, featherValorCount, player, player.getTarget());
				}
				if (featherWisdomCount > 0)
				{
					player.getInventory().destroyItemByItemId("featherWisdom", 7230, featherWisdomCount, player, player.getTarget());
				}
				
				player.getInventory().destroyItemByItemId("Mark", varkaAllyMark[player.getAllianceWithVarkaKetra() + 5], 1, player, player.getTarget());
				player.setAllianceWithVarkaKetra(player.getAllianceWithVarkaKetra() + 1);
				if (player.getAllianceWithVarkaKetra() != 0)
				{
					mark = player.getInventory().addItem("Mark", varkaAllyMark[player.getAllianceWithVarkaKetra() + 5], 1, player, player);
				}
			}
			
			final InventoryUpdate u = new InventoryUpdate();
			u.addNewItem(mark);
			player.sendPacket(u);
		}
	}
	
	@Override
	public void run()
	{
	}
}
