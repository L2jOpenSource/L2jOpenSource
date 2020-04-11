package com.l2jfrozen.gameserver.model.scripts;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.controllers.GameTimeController;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.network.serverpackets.SetupGauge;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.gameserver.util.Broadcast;
import com.l2jfrozen.util.random.Rnd;

/**
 * @author xAddytzu moded by Bobi
 */
public class L2RBManager
{
	// Level 40-45
	public static void RaidbossLevel40(final L2PcInstance player)
	{
		if (!player.isInParty() || !player.getParty().isLeader(player))
		{
			player.sendMessage("You are not a party leader");
			return;
		}
		final int unstuckTimer = (Config.UNSTUCK_INTERVAL * 1000);
		player.setTarget(player);
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.disableAllSkills();
		final MagicSkillUser msk = new MagicSkillUser(player, 361, 1, unstuckTimer, 0);
		Broadcast.toSelfAndKnownPlayersInRadius(player, msk, 810000);
		final SetupGauge sg = new SetupGauge(0, unstuckTimer);
		player.sendPacket(sg);
		
		final RaidbossLevel40 ef = new RaidbossLevel40(player);
		player.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(ef, unstuckTimer));
		player.setSkillCastEndTime(10 + GameTimeController.getGameTicks() + unstuckTimer / GameTimeController.MILLIS_IN_TICK);
	}
	
	static class RaidbossLevel40 implements Runnable
	{
		private final L2PcInstance player;
		
		RaidbossLevel40(final L2PcInstance player)
		{
			this.player = player;
		}
		
		@Override
		public void run()
		{
			if (player.isDead())
			{
				return;
			}
			player.setIsIn7sDungeon(false);
			player.enableAllSkills();
			final int chance = Rnd.get(22);
			if (player.getParty().isLeader(player))
			{
				for (final L2PcInstance pm : player.getParty().getPartyMembers())
				{
					if (pm.getParty().isLeader(pm))
					{
						continue;
					}
					pm.showTeleportHtml();
				}
				if (chance == 0)
				{
					player.teleToLocation(87536, 75872, -3591, true); // Leto Chief Talkin (40)
				}
				else if (chance == 1)
				{
					player.teleToLocation(83056, 183232, -3616, true); // Water Spirit Lian (40)
				}
				else if (chance == 2)
				{
					player.teleToLocation(73520, 66912, -3728, true); // Shaman King Selu (40)
				}
				else if (chance == 3)
				{
					player.teleToLocation(86528, 216864, -3584, true); // Gwindorr (40)
				}
				else if (chance == 4)
				{
					player.teleToLocation(94000, 197500, -3300, true); // Icarus Sample 1 (40)
				}
				else if (chance == 5)
				{
					player.teleToLocation(112112, 209936, -3616, true); // Fafurion's Page Sika (40)
				}
				else if (chance == 6)
				{
					player.teleToLocation(128352, 138464, -3467, true); // Nakondas (40)
				}
				else if (chance == 7)
				{
					player.teleToLocation(72192, 125424, -3657, true); // Road Scavenger Leader (40)
				}
				else if (chance == 8)
				{
					player.teleToLocation(92528, 84752, -3703, true); // Wizard of Storm Teruk (40)
				}
				else if (chance == 9)
				{
					player.teleToLocation(73776, 201552, -3760, true); // Water Couatle Ateka (40)
				}
				else if (chance == 10)
				{
					player.teleToLocation(90848, 16368, -5296, true); // Crazy Mechanic Golem (43)
				}
				else if (chance == 11)
				{
					player.teleToLocation(125920, 190208, -3291, true); // Earth Protector Panathen (43)
				}
				else if (chance == 12)
				{
					player.teleToLocation(107000, 92000, -2272, true); // Thief Kelbar (44)
				}
				else if (chance == 13)
				{
					player.teleToLocation(66944, 67504, -3704, true); // Timak Orc Chief Ranger (44)
				}
				else if (chance == 14)
				{
					player.teleToLocation(64048, 16048, -3536, true); // Rotten Tree Repiro (44)
				}
				else if (chance == 15)
				{
					player.teleToLocation(62416, 8096, -3376, true); // Dread Avenger Kraven (44)
				}
				else if (chance == 16)
				{
					player.teleToLocation(107056, 168176, -3456, true); // Biconne of Blue Sky (45)
				}
				else if (chance == 17)
				{
					player.teleToLocation(111440, 82912, -2912, true); // Evil Spirit Cyrion (45)
				}
				else if (chance == 18)
				{
					player.teleToLocation(93120, 19440, -3607, true); // Iron Giant Totem (45)
				}
				else if (chance == 19)
				{
					player.teleToLocation(67296, 64128, -3723, true); // Timak Orc Gosmos (45)
				}
				else if (chance == 20)
				{
					player.teleToLocation(113840, 84256, -2480, true); // Shacram (45)
				}
				else if (chance == 21)
				{
					player.teleToLocation(126624, 174448, -3056, true); // Fafurion's Henchman Istary (45)
				}
			}
			else
			{
				return;
			}
		}
	}
	
	// Level 45-50
	public static void RaidbossLevel45(final L2PcInstance player)
	{
		if (!player.isInParty() || !player.getParty().isLeader(player))
		{
			player.sendMessage("You are not a party leader");
			return;
		}
		final int unstuckTimer = (Config.UNSTUCK_INTERVAL * 1000);
		player.setTarget(player);
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.disableAllSkills();
		final MagicSkillUser msk = new MagicSkillUser(player, 361, 1, unstuckTimer, 0);
		Broadcast.toSelfAndKnownPlayersInRadius(player, msk, 810000);
		final SetupGauge sg = new SetupGauge(0, unstuckTimer);
		player.sendPacket(sg);
		
		final RaidbossLevel45 ef = new RaidbossLevel45(player);
		player.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(ef, unstuckTimer));
		player.setSkillCastEndTime(10 + GameTimeController.getGameTicks() + unstuckTimer / GameTimeController.MILLIS_IN_TICK);
		
	}
	
	static class RaidbossLevel45 implements Runnable
	{
		private final L2PcInstance player;
		
		RaidbossLevel45(final L2PcInstance player)
		{
			this.player = player;
		}
		
		@Override
		public void run()
		{
			if (player.isDead())
			{
				return;
			}
			
			player.setIsIn7sDungeon(false);
			player.enableAllSkills();
			final int chance = Rnd.get(10);
			if (player.getParty().isLeader(player))
			{
				for (final L2PcInstance pm : player.getParty().getPartyMembers())
				{
					if (pm.getParty().isLeader(pm))
					{
						continue;
					}
					pm.showTeleportHtml();
				}
				if (chance == 0)
				{
					player.teleToLocation(81920, 113136, -3056, true); // Necrosentinel Royal Guard (47)
				}
				else if (chance == 1)
				{
					player.teleToLocation(107792, 27728, -3488, true); // Barion (47)
				}
				else if (chance == 2)
				{
					player.teleToLocation(42032, 24128, -4704, true); // Orfen's Handmaiden (48)
				}
				else if (chance == 3)
				{
					player.teleToLocation(77104, 5408, -3088, true); // King Tarlk (48)
				}
				else if (chance == 4)
				{
					player.teleToLocation(92976, 7920, -3914, true); // Katu Van Leader Atui (49)
				}
				else if (chance == 5)
				{
					player.teleToLocation(133632, 87072, -3623, true); // Mirror of Oblivion (49)
				}
				else if (chance == 6)
				{
					player.teleToLocation(116352, 27648, -3319, true); // Karte (49)
				}
				else if (chance == 7)
				{
					player.teleToLocation(169744, 11920, -2732, true); // Ghost of Peasant Leader (50)
				}
				else if (chance == 8)
				{
					player.teleToLocation(89904, 105712, -3292, true); // Cursed Clara (50)
				}
				else if (chance == 9)
				{
					player.teleToLocation(75488, -9360, -2720, true); // Carnage Lord Gato (50)
				}
			}
			else
			{
				return;
			}
		}
	}
	
	// Level 50-55
	public static void RaidbossLevel50(final L2PcInstance player)
	{
		if (!player.isInParty() || !player.getParty().isLeader(player))
		{
			player.sendMessage("You are not a party leader");
			return;
		}
		final int unstuckTimer = (Config.UNSTUCK_INTERVAL * 1000);
		player.setTarget(player);
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.disableAllSkills();
		final MagicSkillUser msk = new MagicSkillUser(player, 361, 1, unstuckTimer, 0);
		Broadcast.toSelfAndKnownPlayersInRadius(player, msk, 810000);
		final SetupGauge sg = new SetupGauge(0, unstuckTimer);
		player.sendPacket(sg);
		
		final RaidbossLevel50 ef = new RaidbossLevel50(player);
		player.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(ef, unstuckTimer));
		player.setSkillCastEndTime(10 + GameTimeController.getGameTicks() + unstuckTimer / GameTimeController.MILLIS_IN_TICK);
	}
	
	static class RaidbossLevel50 implements Runnable
	{
		private final L2PcInstance player;
		
		RaidbossLevel50(final L2PcInstance player)
		{
			this.player = player;
		}
		
		@Override
		public void run()
		{
			if (player.isDead())
			{
				return;
			}
			
			player.setIsIn7sDungeon(false);
			player.enableAllSkills();
			final int chance = Rnd.get(15);
			if (player.getParty().isLeader(player))
			{
				for (final L2PcInstance pm : player.getParty().getPartyMembers())
				{
					if (pm.getParty().isLeader(pm))
					{
						continue;
					}
					pm.showTeleportHtml();
				}
				if (chance == 0)
				{
					player.teleToLocation(125520, 27216, -3632, true); // Verfa (51)
				}
				else if (chance == 1)
				{
					player.teleToLocation(150304, 67776, -3688, true); // Deadman Ereve (51)
				}
				else if (chance == 2)
				{
					player.teleToLocation(94992, -23168, -2176, true); // Captain of Red Flag Shaka (52)
				}
				else if (chance == 3)
				{
					player.teleToLocation(175712, 29856, -3776, true); // Grave Robber Kim (52)
				}
				else if (chance == 4)
				{
					player.teleToLocation(124984, 43200, -3625, true); // Paniel the Unicorn (54)
				}
				else if (chance == 5)
				{
					player.teleToLocation(104096, -16896, -1803, true); // Bandit Leader Barda (55)
				}
				else if (chance == 6)
				{
					player.teleToLocation(83174, 254428, -10873, true); // Eva's Spirit Niniel (55)
				}
				else if (chance == 7)
				{
					player.teleToLocation(125280, 102576, -3305, true); // Beleth's Seer Sephia (55)
				}
				else if (chance == 8)
				{
					player.teleToLocation(85622, 88766, -5120, true); // Pagan Watcher Cerberon (55)
				}
				else if (chance == 9)
				{
					player.teleToLocation(73520, 66912, -3728, true); // Shaman King Selu (55)
				}
				else if (chance == 10)
				{
					player.teleToLocation(92544, 115232, -3200, true); // Black Lily (55)
				}
				else if (chance == 11)
				{
					player.teleToLocation(183568, 24560, -3184, true); // Ghost Knight Kabed (55)
				}
				else if (chance == 12)
				{
					player.teleToLocation(135872, 94592, -3735, true); // Sorcerer Isirr (55)
				}
				else if (chance == 13)
				{
					player.teleToLocation(113920, 52960, -3735, true); // Furious Thieles (55)
				}
				else if (chance == 14)
				{
					player.teleToLocation(125600, 50100, -3600, true); // Enchanted Forest Watcher Ruell (55)
				}
			}
			else
			{
				return;
			}
		}
	}
	
	// Level 55-60
	public static void RaidbossLevel55(final L2PcInstance player)
	{
		if (!player.isInParty() || !player.getParty().isLeader(player))
		{
			player.sendMessage("You are not a party leader");
			return;
		}
		final int unstuckTimer = (Config.UNSTUCK_INTERVAL * 1000);
		player.setTarget(player);
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.disableAllSkills();
		final MagicSkillUser msk = new MagicSkillUser(player, 361, 1, unstuckTimer, 0);
		Broadcast.toSelfAndKnownPlayersInRadius(player, msk, 810000);
		final SetupGauge sg = new SetupGauge(0, unstuckTimer);
		player.sendPacket(sg);
		
		final RaidbossLevel55 ef = new RaidbossLevel55(player);
		player.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(ef, unstuckTimer));
		player.setSkillCastEndTime(10 + GameTimeController.getGameTicks() + unstuckTimer / GameTimeController.MILLIS_IN_TICK);
	}
	
	static class RaidbossLevel55 implements Runnable
	{
		private final L2PcInstance player;
		
		RaidbossLevel55(final L2PcInstance player)
		{
			this.player = player;
		}
		
		@Override
		public void run()
		{
			if (player.isDead())
			{
				return;
			}
			
			player.setIsIn7sDungeon(false);
			player.enableAllSkills();
			
			final int chance = Rnd.get(13);
			if (player.getParty().isLeader(player))
			{
				for (final L2PcInstance pm : player.getParty().getPartyMembers())
				{
					if (pm.getParty().isLeader(pm))
					{
						continue;
					}
					pm.showTeleportHtml();
				}
				if (chance == 0)
				{
					player.teleToLocation(113600, 47120, -4640, true); // Fairy Queen Timiniel (56)
				}
				else if (chance == 1)
				{
					player.teleToLocation(166288, 68096, -3264, true);// Harit Guardian Garangky (56)
				}
				else if (chance == 2)
				{
					player.teleToLocation(86300, -8200, -3000, true); // Refugee Hopeful Leo (56)
				}
				else if (chance == 3)
				{
					player.teleToLocation(66672, 46704, -3920, true); // Timak Seer Ragoth (57)
				}
				else if (chance == 4)
				{
					player.teleToLocation(165424, 93776, -2992, true); // Soulless Wild Boar (59)
				}
				else if (chance == 5)
				{
					player.teleToLocation(155000, 85400, -3200, true); // Abyss Brukunt (59)
				}
				else if (chance == 6)
				{
					player.teleToLocation(194107, 53884, -4368, true); // Giant Marpanak (60)
				}
				else if (chance == 7)
				{
					player.teleToLocation(173880, -11412, -2880, true); // Ghost of the Well Lidia (60)
				}
				else if (chance == 8)
				{
					player.teleToLocation(181814, 52379, -4344, true); // Guardian of the Statue of Giant Karum (60)
				}
				else if (chance == 9)
				{
					player.teleToLocation(76787, 245775, -10376, true); // The 3rd Underwater Guardian (60)
				}
				else if (chance == 10)
				{
					player.teleToLocation(170320, 42640, -4832, true); // Taik High Prefect Arak (60)
				}
				else if (chance == 11)
				{
					player.teleToLocation(120080, 111248, -3047, true); // Ancient Weird Drake (60)
				}
				else if (chance == 12)
				{
					player.teleToLocation(115072, 112272, -3018, true); // Lord Ishka (60)
				}
			}
			else
			{
				return;
			}
		}
	}
	
	// Level 60-65
	public static void RaidbossLevel60(final L2PcInstance player)
	{
		if (!player.isInParty() || !player.getParty().isLeader(player))
		{
			player.sendMessage("You are not a party leader");
			return;
		}
		final int unstuckTimer = (Config.UNSTUCK_INTERVAL * 1000);
		player.setTarget(player);
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.disableAllSkills();
		final MagicSkillUser msk = new MagicSkillUser(player, 361, 1, unstuckTimer, 0);
		Broadcast.toSelfAndKnownPlayersInRadius(player, msk, 810000);
		final SetupGauge sg = new SetupGauge(0, unstuckTimer);
		player.sendPacket(sg);
		
		final RaidbossLevel60 ef = new RaidbossLevel60(player);
		player.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(ef, unstuckTimer));
		player.setSkillCastEndTime(10 + GameTimeController.getGameTicks() + unstuckTimer / GameTimeController.MILLIS_IN_TICK);
	}
	
	static class RaidbossLevel60 implements Runnable
	{
		private final L2PcInstance player;
		
		RaidbossLevel60(final L2PcInstance player)
		{
			this.player = player;
		}
		
		@Override
		public void run()
		{
			if (player.isDead())
			{
				return;
			}
			
			player.setIsIn7sDungeon(false);
			player.enableAllSkills();
			
			final int chance = Rnd.get(8);
			if (player.getParty().isLeader(player))
			{
				for (final L2PcInstance pm : player.getParty().getPartyMembers())
				{
					if (pm.getParty().isLeader(pm))
					{
						continue;
					}
					pm.showTeleportHtml();
				}
				if (chance == 0)
				{
					player.teleToLocation(104240, -3664, -3392, true); // Roaring Lord Kastor (62)
				}
				else if (chance == 1)
				{
					player.teleToLocation(186192, 61472, -4160, true); // Gorgolos (64)
				}
				else if (chance == 2)
				{
					player.teleToLocation(191975, 56959, -7616, true); // Hekaton Prime (65)
				}
				else if (chance == 3)
				{
					player.teleToLocation(170048, -24896, -3440, true); // Gargoyle Lord Tiphon (65)
				}
				else if (chance == 4)
				{
					player.teleToLocation(170656, 85184, -2000, true); // Fierce Tiger King Angel (65)
				}
				else if (chance == 5)
				{
					player.teleToLocation(113232, 17456, -4384, true); // Enmity Ghost Ramdal (65)
				}
				else if (chance == 6)
				{
					player.teleToLocation(117760, -9072, -3264, true); // Rahha (65)
				}
				else if (chance == 7)
				{
					player.teleToLocation(168288, 28368, -3632, true); // Shilen's Priest Hisilrome (65)
				}
			}
			else
			{
				return;
			}
		}
	}
	
	// Level 65-70
	public static void RaidbossLevel65(final L2PcInstance player)
	{
		if (!player.isInParty() || !player.getParty().isLeader(player))
		{
			player.sendMessage("You are not a party leader");
			return;
		}
		final int unstuckTimer = (Config.UNSTUCK_INTERVAL * 1000);
		player.setTarget(player);
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.disableAllSkills();
		final MagicSkillUser msk = new MagicSkillUser(player, 361, 1, unstuckTimer, 0);
		Broadcast.toSelfAndKnownPlayersInRadius(player, msk, 810000);
		final SetupGauge sg = new SetupGauge(0, unstuckTimer);
		player.sendPacket(sg);
		
		final RaidbossLevel65 ef = new RaidbossLevel65(player);
		player.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(ef, unstuckTimer));
		player.setSkillCastEndTime(10 + GameTimeController.getGameTicks() + unstuckTimer / GameTimeController.MILLIS_IN_TICK);
	}
	
	static class RaidbossLevel65 implements Runnable
	{
		private final L2PcInstance player;
		
		RaidbossLevel65(final L2PcInstance player)
		{
			this.player = player;
		}
		
		@Override
		public void run()
		{
			if (player.isDead())
			{
				return;
			}
			
			player.setIsIn7sDungeon(false);
			player.enableAllSkills();
			final int chance = Rnd.get(14);
			if (player.getParty().isLeader(player))
			{
				for (final L2PcInstance pm : player.getParty().getPartyMembers())
				{
					if (pm.getParty().isLeader(pm))
					{
						continue;
					}
					pm.showTeleportHtml();
				}
				if (chance == 0)
				{
					player.teleToLocation(93296, -75104, -1824, true); // Demon's Agent Falston (66)
				}
				else if (chance == 1)
				{
					player.teleToLocation(186896, 56276, -4576, true); // Last Titan utenus (66)
				}
				else if (chance == 2)
				{
					player.teleToLocation(144400, -28192, -1920, true); // Kernon's Faithful Servant Kelone (67)
				}
				else if (chance == 3)
				{
					player.teleToLocation(185800, -26500, -2000, true); // Spirit of Andras, the Betrayer (69)
				}
				else if (chance == 4)
				{
					player.teleToLocation(143265, 110044, -3944, true); // Bloody Priest Rudelto (69)
				}
				else if (chance == 5)
				{
					player.teleToLocation(180968, 12035, -2720, true); // Shilen's Messenger Cabrio (70)
				}
				else if (chance == 6)
				{
					player.teleToLocation(151053, 88124, -5424, true); // Anakim's Nemesis Zakaron (70)
				}
				else if (chance == 7)
				{
					player.teleToLocation(91008, -85904, -2736, true); // Flame of Splendor Barakiel (70)
				}
				else if (chance == 8)
				{
					player.teleToLocation(130500, 59098, 3584, true); // Roaring Skylancer (70)
				}
				else if (chance == 9)
				{
					player.teleToLocation(123504, -23696, -3481, true); // Beast Lord Behemoth (70)
				}
				else if (chance == 10)
				{
					player.teleToLocation(192376, 22087, -3608, true); // Palibati Queen Themis (70)
				}
				else if (chance == 11)
				{
					player.teleToLocation(102656, 157424, -3735, true); // Fafurion's Herald Lokness (70)
				}
				else if (chance == 12)
				{
					player.teleToLocation(156704, -6096, -4185, true); // Meanas Anor (70)
				}
				else if (chance == 13)
				{
					player.teleToLocation(116151, 16227, 1944, true); // Korim (70)
				}
			}
			else
			{
				return;
			}
		}
	}
	
	// 70-75
	public static void RaidbossLevel70(final L2PcInstance player)
	{
		if (!player.isInParty() || !player.getParty().isLeader(player))
		{
			player.sendMessage("You are not a party leader");
			return;
		}
		final int unstuckTimer = (Config.UNSTUCK_INTERVAL * 1000);
		player.setTarget(player);
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.disableAllSkills();
		final MagicSkillUser msk = new MagicSkillUser(player, 361, 1, unstuckTimer, 0);
		Broadcast.toSelfAndKnownPlayersInRadius(player, msk, 810000);
		final SetupGauge sg = new SetupGauge(0, unstuckTimer);
		player.sendPacket(sg);
		
		final RaidbossLevel70 ef = new RaidbossLevel70(player);
		player.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(ef, unstuckTimer));
		player.setSkillCastEndTime(10 + GameTimeController.getGameTicks() + unstuckTimer / GameTimeController.MILLIS_IN_TICK);
	}
	
	static class RaidbossLevel70 implements Runnable
	{
		private final L2PcInstance player;
		
		RaidbossLevel70(final L2PcInstance player)
		{
			this.player = player;
		}
		
		@Override
		public void run()
		{
			if (player.isDead())
			{
				return;
			}
			player.setIsIn7sDungeon(false);
			player.enableAllSkills();
			final int chance = Rnd.get(7);
			if (player.getParty().isLeader(player))
			{
				for (final L2PcInstance pm : player.getParty().getPartyMembers())
				{
					if (pm.getParty().isLeader(pm))
					{
						continue;
					}
					pm.showTeleportHtml();
				}
				if (chance == 0)
				{
					player.teleToLocation(113200, 17552, -1424, true); // Immortal Savior Mardil (71)
				}
				else if (chance == 1)
				{
					player.teleToLocation(116400, -62528, -3264, true); // Vanor Chief Kandra (72)
				}
				else if (chance == 2)
				{
					player.teleToLocation(108096, 157408, -3688, true); // Water Dragon Seer Sheshark (72)
				}
				else if (chance == 3)
				{
					player.teleToLocation(127903, -13399, -3720, true); // Doom Blade Tanatos (72)
				}
				else if (chance == 4)
				{
					player.teleToLocation(113551, 17083, -2120, true); // Death Lord Hallate (73)
				}
				else if (chance == 5)
				{
					player.teleToLocation(152660, 110387, -5520, true); // Antharas Priest Cloe (74)
				}
				else if (chance == 6)
				{
					player.teleToLocation(119760, 157392, -3744, true); // Krokian Padisha Sobekk (74)
				}
			}
			else
			{
				return;
			}
		}
	}
}