/*
 * Copyright (C) 2004-2015 L2J DataPack
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
package instances.FinalEmperialTomb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import l2r.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.xml.impl.SkillData;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.enums.PcCondOverride;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.model.L2CommandChannel;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.L2Playable;
import l2r.gameserver.model.actor.instance.L2GrandBossInstance;
import l2r.gameserver.model.actor.instance.L2MonsterInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.holders.SkillHolder;
import l2r.gameserver.model.instancezone.InstanceWorld;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.network.NpcStringId;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.Earthquake;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.network.serverpackets.MagicSkillCanceld;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.network.serverpackets.SocialAction;
import l2r.gameserver.network.serverpackets.SpecialCamera;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;

import instances.AbstractInstance;

/**
 * Final Emperial Tomb instance zone.
 * @author vGodFather
 */
public final class FinalEmperialTomb extends AbstractInstance
{
	protected class FETWorld extends InstanceWorld
	{
		protected Lock lock = new ReentrantLock();
		protected List<L2Npc> npcList = new CopyOnWriteArrayList<>();
		protected int darkChoirPlayerCount = 0;
		protected FrintezzaSong OnSong = null;
		protected ScheduledFuture<?> songTask = null;
		protected ScheduledFuture<?> songEffectTask = null;
		protected boolean isVideo = false;
		protected L2Npc frintezzaDummy = null;
		protected L2Npc overheadDummy = null;
		protected L2Npc portraitDummy1 = null;
		protected L2Npc portraitDummy3 = null;
		protected L2Npc scarletDummy = null;
		protected L2GrandBossInstance frintezza = null;
		protected L2GrandBossInstance activeScarlet = null;
		protected List<L2MonsterInstance> demons = new CopyOnWriteArrayList<>();
		protected Map<L2MonsterInstance, Integer> portraits = new ConcurrentHashMap<>();
		protected int scarlet_x = 0;
		protected int scarlet_y = 0;
		protected int scarlet_z = 0;
		protected int scarlet_h = 0;
		protected int scarlet_a = 0;
		protected Future<?> _demonsSpawnTask;
		
		protected int lastSkillId = 0;
	}
	
	protected static class FETSpawn
	{
		public boolean isZone = false;
		public boolean isNeededNextFlag = false;
		public int npcId;
		public int x = 0;
		public int y = 0;
		public int z = 0;
		public int h = 0;
		public int zone = 0;
		public int count = 0;
	}
	
	private static class FrintezzaSong
	{
		public SkillHolder skill;
		public SkillHolder effectSkill;
		public NpcStringId songName;
		public int chance;
		
		public FrintezzaSong(SkillHolder sk, SkillHolder esk, NpcStringId sn, int ch)
		{
			skill = sk;
			effectSkill = esk;
			songName = sn;
			chance = ch;
		}
	}
	
	// NPCs
	private static final int GUIDE = 32011;
	private static final int CUBE = 29061;
	private static final int SCARLET1 = 29046;
	private static final int SCARLET2 = 29047;
	private static final int FRINTEZZA = 29045;
	private static final List<Integer> PORTRAITS = Arrays.asList(29048, 29049);
	private static final List<Integer> DEMONS = Arrays.asList(29050, 29051);
	private static final int HALL_ALARM = 18328;
	private static final int HALL_KEEPER_CAPTAIN = 18329;
	// Items
	private static final int HALL_KEEPER_SUICIDAL_SOLDIER = 18333;
	private static final int DARK_CHOIR_PLAYER = 18339;
	private static final int DEWDROP_OF_DESTRUCTION_ITEM_ID = 8556;
	private static final int FIRST_SCARLET_WEAPON = 8204;
	private static final int SECOND_SCARLET_WEAPON = 7903;
	// Skills
	private static final int DEWDROP_OF_DESTRUCTION_SKILL_ID = 2276;
	private static final int SOUL_BREAKING_ARROW_SKILL_ID = 2234;
	protected static final SkillHolder INTRO_SKILL = new SkillHolder(5004, 1);
	private static final SkillHolder FIRST_MORPH_SKILL = new SkillHolder(5017, 1);
	
	protected static final FrintezzaSong[] FRINTEZZASONGLIST =
	{
		new FrintezzaSong(new SkillHolder(5007, 1), new SkillHolder(5008, 1), NpcStringId.REQUIEM_OF_HATRED, 5),
		new FrintezzaSong(new SkillHolder(5007, 2), new SkillHolder(5008, 2), NpcStringId.RONDO_OF_SOLITUDE, 50),
		new FrintezzaSong(new SkillHolder(5007, 3), new SkillHolder(5008, 3), NpcStringId.FRENETIC_TOCCATA, 70),
		new FrintezzaSong(new SkillHolder(5007, 4), new SkillHolder(5008, 4), NpcStringId.FUGUE_OF_JUBILATION, 90),
		new FrintezzaSong(new SkillHolder(5007, 5), new SkillHolder(5008, 5), NpcStringId.HYPNOTIC_MAZURKA, 100),
	};
	// Locations
	private static final Location ENTER_TELEPORT = new Location(-88015, -141153, -9168);
	protected static final Location MOVE_TO_CENTER = new Location(-87904, -141296, -9168, 0);
	// Misc
	private static final int TEMPLATE_ID = 136; // this is the client number
	private static final int MIN_PLAYERS = Config.MIN_PLAYER_TO_FE;
	private static final int MAX_PLAYERS = Config.MAX_PLAYER_TO_FE;
	private static final int START_DELAY = 600; // In seconds
	private static final int TIME_BETWEEN_DEMON_SPAWNS = 20000;
	private static final int MAX_DEMONS = 24;
	private static final boolean debug = false;
	private final List<Integer> _mustKillMobsId = new ArrayList<>();
	protected static final List<Integer> FIRST_ROOM_DOORS = Arrays.asList(17130051, 17130052, 17130053, 17130054, 17130055, 17130056, 17130057, 17130058);
	protected static final List<Integer> SECOND_ROOM_DOORS = Arrays.asList(17130061, 17130062, 17130063, 17130064, 17130065, 17130066, 17130067, 17130068, 17130069, 17130070);
	protected static final List<Integer> FIRST_ROUTE_DOORS = Arrays.asList(17130042, 17130043);
	protected static final List<Integer> SECOND_ROUTE_DOORS = Arrays.asList(17130045, 17130046);
	// @formatter:off
	protected static final int[][] PORTRAIT_SPAWNS =
	{
		{29048, -89381, -153981, -9168, 3368, -89378, -153968, -9168, 3368},
		{29048, -86234, -152467, -9168, 37656, -86261, -152492, -9168, 37656},
		{29049, -89342, -152479, -9168, -5152, -89311, -152491, -9168, -5152},
		{29049, -86189, -153968, -9168, 29456, -86217, -153956, -9168, 29456},
	};
	
	private static final int[][] _mobLoc =
	{
		{18329, -88725, -142175, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -88239, -142613, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -88131, -142389, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -88131, -142629, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -88077, -142453, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -88641, -142469, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -88683, -142427, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -88515, -142595, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -89145, -141881, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -88515, -142343, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -89061, -142049, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -88599, -142343, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -88683, -142175, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -88977, -141881, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -89176, -141535, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -89240, -141379, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -89208, -141379, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -89192, -141431, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -89208, -141639, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -89064, -141483, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -88583, -140336, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -88667, -140292, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -88919, -140688, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -88541, -140072, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -88625, -140248, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -89129, -140732, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -88625, -140424, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -88793, -140556, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -89045, -140644, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -88793, -140644, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -88243, -140112, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -88081, -139936, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -88135, -139952, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -89000, -141067, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -89064, -141171, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -89192, -140911, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -89208, -140963, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -86793, -141539, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -86617, -141701, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -86553, -141701, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -86793, -141593, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -86825, -141539, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -86617, -141593, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -87034, -142282, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -86824, -142118, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -87034, -142159, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -87202, -142241, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -87244, -142159, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -87160, -142405, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -86992, -142118, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -86992, -142159, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -87286, -142323, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -86793, -141107, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -86745, -141107, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -86809, -141107, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -86825, -141053, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -86816, -140594, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -86690, -140637, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -86900, -140680, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -87194, -140422, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -87110, -140164, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -87320, -140293, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -87278, -140379, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -86984, -140594, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -86984, -140422, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -87068, -140465, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -87973, -139952, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -87865, -140160, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -87811, -140224, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -87757, -140224, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -87757, -140176, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -87919, -140080, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -87757, -140032, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -87861, -142677, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -87915, -142661, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -87483, -142549, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -87753, -142629, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -88023, -142597, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -87537, -142661, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18329, -87034, -142282, -9168, 0}, // misc stats: 333 278 160 40 21 40
		{18330, -86809, -141215, -9168, 0}, // misc stats: 333 278 175 70 12 40
		{18330, -86697, -141215, -9168, 0}, // misc stats: 333 278 175 70 12 40
		{18330, -86729, -140999, -9168, 0}, // misc stats: 333 278 175 70 12 40
		{18330, -86774, -140723, -9168, 0}, // misc stats: 333 278 175 70 12 40
		{18330, -87236, -140422, -9168, 0}, // misc stats: 333 278 175 70 12 40
		{18330, -88027, -140160, -9168, 0}, // misc stats: 333 278 175 70 12 40
		{18330, -88027, -140064, -9168, 0}, // misc stats: 333 278 175 70 12 40
		{18330, -87865, -140240, -9168, 0}, // misc stats: 333 278 175 70 12 40
		{18330, -87591, -142613, -9168, 0}, // misc stats: 333 278 175 70 12 40
		{18330, -87915, -142485, -9168, 0}, // misc stats: 333 278 175 70 12 40
		{18330, -87969, -142501, -9168, 0}, // misc stats: 333 278 175 70 12 40
		{18330, -87320, -140379, -9168, 0}, // misc stats: 333 278 175 70 12 40
		{18330, -87160, -142159, -9168, 0}, // misc stats: 333 278 175 70 12 40
		{18330, -87076, -142036, -9168, 0}, // misc stats: 333 278 175 70 12 40
		{18330, -87076, -142241, -9168, 0}, // misc stats: 333 278 175 70 12 40
		{18330, -89208, -141015, -9168, 0}, // misc stats: 333 278 175 70 12 40
		{18330, -89256, -141223, -9168, 0}, // misc stats: 333 278 175 70 12 40
		{18330, -88583, -140468, -9168, 0}, // misc stats: 333 278 175 70 12 40
		{18330, -88835, -140688, -9168, 0}, // misc stats: 333 278 175 70 12 40
		{18330, -89171, -140644, -9168, 0}, // misc stats: 333 278 175 70 12 40
		{18330, -89256, -141743, -9168, 0}, // misc stats: 333 278 175 70 12 40
		{18330, -88809, -142217, -9168, 0}, // misc stats: 333 278 175 70 12 40
		{18330, -88809, -142175, -9168, 0}, // misc stats: 333 278 175 70 12 40
		{18330, -88851, -141965, -9168, 0}, // misc stats: 333 278 175 70 12 40
		{18331, -89240, -141639, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -88347, -142677, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -88239, -142677, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -88185, -142485, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -88185, -142517, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -88131, -142629, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -88473, -142427, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -88473, -142385, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -88557, -142385, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -89061, -141881, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -88809, -142343, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -88641, -142511, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -88935, -141839, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -88683, -142427, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -88557, -142301, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -89145, -141965, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -89144, -141587, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -89096, -141535, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -89224, -141431, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -89112, -141379, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -89176, -141483, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -89128, -141483, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -88541, -140248, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -88625, -140424, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -88625, -140248, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -88583, -140336, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -88667, -140424, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -88499, -140028, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -89003, -140600, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -88835, -140732, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -88877, -140644, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -88751, -140336, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -88081, -140096, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -88135, -139984, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -88081, -140032, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -88189, -139968, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -88297, -140016, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -88189, -140048, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -88984, -141223, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -89256, -141223, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -89224, -141119, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -86649, -141593, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -86665, -141701, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -86777, -141539, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -86809, -141593, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -86681, -141485, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -87370, -142323, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -86992, -141913, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -87118, -142077, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -87370, -142323, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -86908, -141831, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -87286, -142610, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -86740, -141995, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -86782, -141954, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -86908, -141831, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -87244, -142282, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -88023, -142389, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -87969, -142565, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -87483, -142597, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -88023, -142469, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -87915, -142581, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -87649, -140192, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -87703, -139968, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -88027, -139984, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -87865, -140096, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -86984, -140594, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -87320, -140207, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -86774, -140508, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -87152, -140508, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -87194, -140336, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -86816, -140551, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -86648, -140723, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -86900, -140594, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -87152, -140508, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -86900, -140723, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -86649, -141269, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -86713, -140999, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -86633, -141107, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -86601, -141269, -9168, 0}, // misc stats: 333 278 110 27 12 31
		{18331, -86617, -140945, -9168, 0}, // misc stats: 333 278 110 27 12 31
	};
	
	private static final int[][] _mobLoc2 =
	{
		{18339, -87952, -147004, -9184, 17329}, // misc stats: 333 278 140 60 11 25
		{18339, -87794, -147193, -9184, 17466}, // misc stats: 333 278 140 60 11 25
		{18339, -88021, -147358, -9184, 0}, // misc stats: 333 278 140 60 11 25
		{18339, -87834, -147259, -9184, 55622} // misc stats: 333 278 140 60 11 25
	};
	
	private static final int[][] _mobLoc3 =
	{
		//{29124, -87878, -141310, -9168, 0}, // misc stats: 333 278 1 1 0.1 0.1
		//{29124, -87868, -141350, -9168, 0}, // misc stats: 333 278 1 1 0.1 0.1
		{18336, -88502, -146049, -9152, 53280}, // misc stats: 333 278 180 100 25 52
		{18336, -88478, -146133, -9136, 53730}, // misc stats: 333 278 180 100 25 52
		{18336, -88486, -146022, -9168, 49304}, // misc stats: 333 278 180 100 25 52
		{18336, -88463, -145971, -9168, 1613}, // misc stats: 333 278 180 100 25 52
		{18336, -88467, -145827, -9168, 59045}, // misc stats: 333 278 180 100 25 52
		{18335, -88831, -146233, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18338, -89252, -147210, -9168, 0}, // misc stats: 333 278 190 30 23 40
		{18338, -89126, -146503, -9168, 0}, // misc stats: 333 278 190 30 23 40
		{18338, -89273, -147008, -9168, 0}, // misc stats: 333 278 190 30 23 40
		{18338, -89336, -147311, -9168, 0}, // misc stats: 333 278 190 30 23 40
		{18338, -89021, -147311, -9168, 0}, // misc stats: 333 278 190 30 23 40
		{18338, -89189, -146301, -9168, 0}, // misc stats: 333 278 190 30 23 40
		{18338, -89294, -146806, -9168, 0}, // misc stats: 333 278 190 30 23 40
		{18338, -89210, -146402, -9168, 0}, // misc stats: 333 278 190 30 23 40
		{18337, -89225, -147522, -9168, 46511}, // misc stats: 333 278 140 40 13 32.5
		{18337, -89100, -146892, -9168, 64239}, // misc stats: 333 278 140 40 13 32.5
		{18337, -89345, -147175, -9168, 54095}, // misc stats: 333 278 140 40 13 32.5
		{18337, -89142, -146423, -9168, 5920}, // misc stats: 333 278 140 40 13 32.5
		{18337, -89207, -147045, -9168, 40341}, // misc stats: 333 278 140 40 13 32.5
		{18337, -89311, -147221, -9168, 9413}, // misc stats: 333 278 140 40 13 32.5
		{18337, -89337, -147097, -9168, 27293}, // misc stats: 333 278 140 40 13 32.5
		{18337, -89126, -146907, -9168, 0}, // misc stats: 333 278 140 40 13 32.5
		{18335, -88851, -146518, -9160, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -88911, -146404, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -88831, -146233, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -88811, -146119, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -88891, -146461, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -88891, -146347, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -88971, -146404, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -88931, -146347, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -88871, -146518, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18336, -88486, -146114, -9136, 53844}, // misc stats: 333 278 180 100 25 52
		{18336, -88471, -145699, -9168, 55229}, // misc stats: 333 278 180 100 25 52
		{18336, -88474, -146041, -9160, 49453}, // misc stats: 333 278 180 100 25 52
		{18336, -88472, -146058, -9152, 48818}, // misc stats: 333 278 180 100 25 52
		{18334, -88755, -146963, -9136, 0}, // misc stats: 333 197 150 31 21 50
		{18334, -88755, -146723, -9136, 0}, // misc stats: 333 197 150 31 21 50
		{18334, -88755, -147187, -9136, 0}, // misc stats: 333 197 150 31 21 50
		{18334, -88755, -147395, -9136, 0}, // misc stats: 333 197 150 31 21 50
		{18335, -87158, -146011, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -86969, -146461, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -87158, -146311, -9160, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -87158, -146011, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -87011, -146411, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -87284, -145861, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -87116, -146261, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -87326, -146011, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -86969, -146211, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18338, -86912, -147192, -9168, 0}, // misc stats: 333 278 190 30 23 40
		{18335, -87074, -146111, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18338, -86786, -146471, -9168, 0}, // misc stats: 333 278 190 30 23 40
		{18338, -86828, -146780, -9168, 0}, // misc stats: 333 278 190 30 23 40
		{18338, -86828, -147398, -9168, 0}, // misc stats: 333 278 190 30 23 40
		{18338, -86870, -146471, -9168, 0}, // misc stats: 333 278 190 30 23 40
		{18338, -86744, -146471, -9168, 0}, // misc stats: 333 278 190 30 23 40
		{18337, -86846, -146945, -9168, 22779}, // misc stats: 333 278 140 40 13 32.5
		{18337, -86830, -146835, -9168, 24239}, // misc stats: 333 278 140 40 13 32.5
		{18337, -86814, -146833, -9168, 39002}, // misc stats: 333 278 140 40 13 32.5
		{18337, -86749, -146327, -9168, 2707}, // misc stats: 333 278 140 40 13 32.5
		{18337, -86921, -147059, -9168, 36804}, // misc stats: 333 278 140 40 13 32.5
		{18337, -86617, -146724, -9168, 6680}, // misc stats: 333 278 140 40 13 32.5
		{18337, -86832, -146307, -9168, 25692}, // misc stats: 333 278 140 40 13 32.5
		{18336, -87395, -145923, -9168, 33000}, // misc stats: 333 278 180 100 15 45
		{18336, -87395, -145779, -9168, 33000}, // misc stats: 333 278 180 100 15 45
		{18336, -87395, -145859, -9168, 33000}, // misc stats: 333 278 180 100 15 45
		{18336, -87427, -145955, -9168, 33000}, // misc stats: 333 278 180 100 15 45
		{18336, -87395, -145715, -9168, 33000}, // misc stats: 333 278 180 100 15 45
		{18336, -87427, -145747, -9168, 33000}, // misc stats: 333 278 180 100 15 45
		{18336, -87427, -145819, -9168, 33000}, // misc stats: 333 278 180 100 15 45
		{18336, -87427, -145883, -9168, 33000}, // misc stats: 333 278 180 100 15 45
		{18336, -87427, -145683, -9168, 33000}, // misc stats: 333 278 180 100 15 45
		{18334, -87171, -146739, -9136, 33000}, // misc stats: 333 197 150 31 21 50
		{18334, -87155, -147219, -9136, 33000}, // misc stats: 333 197 150 31 21 50
		{18334, -87155, -146963, -9136, 33000}, // misc stats: 333 197 150 31 21 50
		{18334, -87155, -147411, -9136, 33000}, // misc stats: 333 197 150 31 21 50
		{18335, -88685, -148239, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -88795, -147833, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -88861, -148181, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -88883, -147949, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -88773, -148181, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -88531, -148181, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -88773, -147717, -9152, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -88949, -148007, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -88575, -148181, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -88773, -148123, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18338, -88979, -148018, -9168, 0}, // misc stats: 333 278 190 30 23 40
		{18338, -89168, -147715, -9168, 0}, // misc stats: 333 278 190 30 23 40
		{18337, -89338, -147449, -9168, 53319}, // misc stats: 333 278 140 40 13 32.5
		{18337, -89076, -147524, -9168, 16383}, // misc stats: 333 278 140 40 13 32.5
		{18336, -88503, -148239, -9168, 0}, // misc stats: 333 278 180 100 15 45
		{18336, -88499, -148307, -9168, 0}, // misc stats: 333 278 180 100 15 45
		{18336, -88499, -148435, -9168, 0}, // misc stats: 333 278 180 100 15 45
		{18336, -88503, -148375, -9168, 0}, // misc stats: 333 278 180 100 15 45
		{18336, -88471, -148199, -9168, 0}, // misc stats: 333 278 180 100 15 45
		{18336, -88467, -148271, -9168, 0}, // misc stats: 333 278 180 100 15 45
		{18336, -88467, -148403, -9168, 0}, // misc stats: 333 278 180 100 15 45
		{18336, -88467, -148339, -9168, 0}, // misc stats: 333 278 180 100 15 45
		{18336, -88467, -148467, -9168, 0}, // misc stats: 333 278 180 100 15 45
		{18338, -86807, -147501, -9168, 0}, // misc stats: 333 278 190 30 23 40
		{18338, -86744, -147707, -9168, 0}, // misc stats: 333 278 190 30 23 40
		{18338, -86849, -147810, -9168, 0}, // misc stats: 333 278 190 30 23 40
		{18338, -86870, -147501, -9168, 0}, // misc stats: 333 278 190 30 23 40
		{18337, -86908, -147798, -9168, 3085}, // misc stats: 333 278 140 40 13 32.5
		{18337, -86891, -147604, -9168, 0}, // misc stats: 333 278 140 40 13 32.5
		{18337, -86591, -147633, -9168, 12766}, // misc stats: 333 278 140 40 13 32.5
		{18335, -87257, -148113, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -87086, -147663, -9152, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -87295, -148063, -9152, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -87200, -148213, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -87162, -148063, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -87333, -148263, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -87181, -147863, -9152, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -87314, -148163, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -87105, -147863, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18335, -87352, -148263, -9168, 0}, // misc stats: 333 278 200 30 23 40
		{18336, -87395, -148227, -9168, 33000}, // misc stats: 333 278 180 100 15 45
		{18336, -87395, -148371, -9168, 33000}, // misc stats: 333 278 180 100 15 45
		{18336, -87395, -148303, -9168, 33000}, // misc stats: 333 278 180 100 15 45
		{18336, -87395, -148435, -9168, 33000}, // misc stats: 333 278 180 100 15 45
		{18336, -87427, -148467, -9168, 33000}, // misc stats: 333 278 180 100 15 45
		{18336, -87427, -148331, -9168, 33000}, // misc stats: 333 278 180 100 15 45
		{18336, -87427, -148395, -9168, 33000}, // misc stats: 333 278 180 100 15 45
		{18336, -87427, -148263, -9168, 33000}, // misc stats: 333 278 180 100 15 45
		{18336, -87427, -148191, -9168, 33000} // misc stats: 333 278 180 100 15 45
	};
	// @formatter:on
	
	public FinalEmperialTomb()
	{
		super(FinalEmperialTomb.class.getSimpleName());
		
		addAttackId(SCARLET1, FRINTEZZA);
		addAttackId(PORTRAITS);
		addStartNpc(GUIDE, CUBE);
		addTalkId(GUIDE, CUBE);
		addKillId(HALL_ALARM, SCARLET2);
		addKillId(PORTRAITS);
		addKillId(DEMONS);
		addKillId(_mustKillMobsId);
		addSkillSeeId(PORTRAITS);
		addSpellFinishedId(HALL_KEEPER_SUICIDAL_SOLDIER);
		addSpellFinishedId(SCARLET1, SCARLET2);
		
		// Room Mobs
		addKillId(HALL_KEEPER_CAPTAIN, 18330, 18331, 18332, 18333, 18334, 18335, 18336, 18337, 18338, DARK_CHOIR_PLAYER);
		addSpawnId(HALL_KEEPER_CAPTAIN, 18330, 18331, 18332, 18333, 18334, 18335, 18336, 18337, 18338, DARK_CHOIR_PLAYER);
		
		// vGodFather addon just to avoid some camera failures
		addSpawnId(FRINTEZZA, SCARLET1, SCARLET2, 29048, 29049, 29050, 29051, 29059);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setWatchDistance(4500);
		if (npc.isAttackable())
		{
			((L2Attackable) npc).setSeeThroughSilentMove(true);
		}
		return super.onSpawn(npc);
	}
	
	@Override
	protected boolean checkConditions(L2PcInstance player)
	{
		if (debug || player.canOverrideCond(PcCondOverride.INSTANCE_CONDITIONS))
		{
			return true;
		}
		
		final L2Party party = player.getParty();
		if (party == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
			return false;
		}
		
		final L2CommandChannel channel = player.getParty().getCommandChannel();
		if (channel == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_COMMAND_CHANNEL_CANT_ENTER);
			return false;
		}
		else if (channel.getLeader() != player)
		{
			player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
			return false;
		}
		else if (player.getInventory().getItemByItemId(8073) == null)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
			sm.addPcName(player);
			player.sendPacket(sm);
			return false;
		}
		else if ((channel.getMemberCount() < MIN_PLAYERS) || (channel.getMemberCount() > MAX_PLAYERS))
		{
			player.sendPacket(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
			return false;
		}
		for (L2PcInstance channelMember : channel.getMembers())
		{
			if (channelMember.getLevel() < Config.MIN_LEVEL_TO_FE)
			{
				party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_S_LEVEL_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addPcName(channelMember));
				return false;
			}
			if (!Util.checkIfInRange(1000, player, channelMember, true))
			{
				party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_LOCATION_WHICH_CANNOT_BE_ENTERED_THEREFORE_IT_CANNOT_BE_PROCESSED).addPcName(channelMember));
				return false;
			}
			final Long reentertime = InstanceManager.getInstance().getInstanceTime(channelMember.getObjectId(), TEMPLATE_ID);
			if (System.currentTimeMillis() < reentertime)
			{
				party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_RE_ENTER_YET).addPcName(channelMember));
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void onEnterInstance(L2PcInstance player, InstanceWorld world, boolean firstEntrance)
	{
		if (firstEntrance)
		{
			controlStatus((FETWorld) world);
			
			if ((player.getParty() == null) || (player.getParty().getCommandChannel() == null))
			{
				player.destroyItemByItemId(getName(), DEWDROP_OF_DESTRUCTION_ITEM_ID, player.getInventory().getInventoryItemCount(DEWDROP_OF_DESTRUCTION_ITEM_ID, -1), null, true);
				world.addAllowed(player.getObjectId());
				teleportPlayer(player, ENTER_TELEPORT, world.getInstanceId(), false);
			}
			else
			{
				for (L2PcInstance channelMember : player.getParty().getCommandChannel().getMembers())
				{
					channelMember.destroyItemByItemId(getName(), DEWDROP_OF_DESTRUCTION_ITEM_ID, channelMember.getInventory().getInventoryItemCount(DEWDROP_OF_DESTRUCTION_ITEM_ID, -1), null, true);
					world.addAllowed(channelMember.getObjectId());
					teleportPlayer(channelMember, ENTER_TELEPORT, world.getInstanceId(), false);
				}
			}
		}
		else
		{
			teleportPlayer(player, ENTER_TELEPORT, world.getInstanceId(), false);
		}
	}
	
	protected boolean checkKillProgress(L2Npc mob, FETWorld world)
	{
		world.npcList.remove(mob);
		return world.npcList.isEmpty();
	}
	
	private void spawnFlaggedNPCs(FETWorld world, int flag)
	{
		if (world.lock.tryLock())
		{
			try
			{
				if (flag == 0)
				{
					L2Npc npc = addSpawn(HALL_ALARM, -87904, -141296, -9168, 0, false, 0, true, world.getInstanceId());
					npc.disableCoreAI(true);
					
					for (int[] spawn : _mobLoc) // First room mobs
					{
						L2Npc mob = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, true, world.getInstanceId());
						world.npcList.add(mob);
					}
				}
				else
				{
					for (int[] spawn : _mobLoc2) // Second room doors (dark coir)
					{
						addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, true, world.getInstanceId());
						world.darkChoirPlayerCount++;
					}
					
					for (int[] spawn : _mobLoc3) // Second room doors (global)
					{
						L2Npc mob = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, true, world.getInstanceId());
						world.npcList.add(mob);
					}
				}
			}
			finally
			{
				world.lock.unlock();
			}
		}
	}
	
	protected boolean controlStatus(FETWorld world)
	{
		if (world.lock.tryLock())
		{
			try
			{
				if (debug)
				{
					_log.info(FinalEmperialTomb.class.getSimpleName() + ": Starting " + world.getStatus() + ". status.");
				}
				world.npcList.clear();
				switch (world.getStatus())
				{
					case 0:
						spawnFlaggedNPCs(world, 0);
						break;
					case 1:
						FIRST_ROUTE_DOORS.forEach(doorId -> openDoor(doorId, world.getInstanceId()));
						spawnFlaggedNPCs(world, world.getStatus());
						break;
					case 2:
						SECOND_ROUTE_DOORS.forEach(doorId -> openDoor(doorId, world.getInstanceId()));
						ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 0), START_DELAY * 1000);
						break;
					case 3: // first morph
						if (world.songEffectTask != null)
						{
							world.songEffectTask.cancel(false);
							world.songEffectTask = null;
						}
						
						world.activeScarlet.setIsInvul(true);
						if (world.activeScarlet.isCastingNow())
						{
							world.activeScarlet.abortCast();
						}
						handleReenterTime(world);
						world.activeScarlet.doCast(FIRST_MORPH_SKILL.getSkill());
						ThreadPoolManager.getInstance().scheduleGeneral(new SongTask(world, 2), 1500);
						break;
					case 4: // second morph
						world.isVideo = true;
						broadCastPacket(world, new MagicSkillCanceld(world.frintezza.getObjectId()));
						if (world.songEffectTask != null)
						{
							world.songEffectTask.cancel(false);
							world.songEffectTask = null;
						}
						
						ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 23), 2000);
						ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 24), 2100);
						break;
					case 5: // raid success
						world.isVideo = true;
						broadCastPacket(world, new MagicSkillCanceld(world.frintezza.getObjectId()));
						if (world.songTask != null)
						{
							world.songTask.cancel(true);
							world.songTask = null;
						}
						if (world.songEffectTask != null)
						{
							world.songEffectTask.cancel(false);
							world.songEffectTask = null;
						}
						
						ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 33), 500);
						break;
					case 6: // open doors
						InstanceManager.getInstance().getInstance(world.getInstanceId()).setDuration(300000);
						// vGodFather just in case there are some demons or portraits left
						cleanDemons(world);
						
						FIRST_ROOM_DOORS.forEach(doorId -> openDoor(doorId, world.getInstanceId()));
						FIRST_ROUTE_DOORS.forEach(doorId -> openDoor(doorId, world.getInstanceId()));
						SECOND_ROUTE_DOORS.forEach(doorId -> openDoor(doorId, world.getInstanceId()));
						SECOND_ROOM_DOORS.forEach(doorId -> closeDoor(doorId, world.getInstanceId()));
						break;
				}
				world.incStatus();
				return true;
			}
			finally
			{
				world.lock.unlock();
			}
		}
		return false;
	}
	
	private class DemonSpawnTask implements Runnable
	{
		private final FETWorld _world;
		
		DemonSpawnTask(FETWorld world)
		{
			_world = world;
		}
		
		@Override
		public void run()
		{
			if ((InstanceManager.getInstance().getWorld(_world.getInstanceId()) != _world) || _world.portraits.isEmpty())
			{
				if (debug)
				{
					_log.info(FinalEmperialTomb.class.getSimpleName() + ": Instance is deleted or all Portraits is killed.");
				}
				return;
			}
			for (int i : _world.portraits.values())
			{
				if (_world.demons.size() > MAX_DEMONS)
				{
					break;
				}
				L2MonsterInstance demon = (L2MonsterInstance) addSpawn(PORTRAIT_SPAWNS[i][0] + 2, PORTRAIT_SPAWNS[i][5], PORTRAIT_SPAWNS[i][6], PORTRAIT_SPAWNS[i][7], PORTRAIT_SPAWNS[i][8], false, 0, false, _world.getInstanceId());
				_world.demons.add(demon);
			}
			ThreadPoolManager.getInstance().scheduleGeneral(new DemonSpawnTask(_world), TIME_BETWEEN_DEMON_SPAWNS);
		}
	}
	
	private class SoulBreakingArrow implements Runnable
	{
		private final L2Npc _npc;
		
		protected SoulBreakingArrow(L2Npc npc)
		{
			_npc = npc;
		}
		
		@Override
		public void run()
		{
			_npc.setScriptValue(0);
		}
	}
	
	private class SongTask implements Runnable
	{
		private final FETWorld _world;
		private final int _status;
		
		SongTask(FETWorld world, int status)
		{
			_world = world;
			_status = status;
		}
		
		@Override
		public void run()
		{
			if (InstanceManager.getInstance().getWorld(_world.getInstanceId()) != _world)
			{
				return;
			}
			switch (_status)
			{
				case 0: // new song play
					if (_world.isVideo)
					{
						_world.songTask = ThreadPoolManager.getInstance().scheduleGeneral(new SongTask(_world, 0), 1000);
					}
					else if ((_world.frintezza != null) && !_world.frintezza.isDead())
					{
						if (_world.frintezza.getScriptValue() != 1)
						{
							int rnd = getRandom(100);
							for (FrintezzaSong element : FRINTEZZASONGLIST)
							{
								if (rnd < element.chance)
								{
									_world.OnSong = element;
									broadCastPacket(_world, new ExShowScreenMessage(2, -1, 2, 0, 0, 0, 0, true, 4000, false, null, element.songName, null));
									broadCastPacket(_world, new MagicSkillUse(_world.frintezza, _world.frintezza, element.skill.getSkillId(), element.skill.getSkillLvl(), element.skill.getSkill().getHitTime(), 0));
									_world.songEffectTask = ThreadPoolManager.getInstance().scheduleGeneral(new SongTask(_world, 1), element.skill.getSkill().getHitTime() - 10000);
									_world.songTask = ThreadPoolManager.getInstance().scheduleGeneral(new SongTask(_world, 0), element.skill.getSkill().getHitTime());
									break;
								}
							}
						}
						else
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new SoulBreakingArrow(_world.frintezza), 35000);
						}
					}
					break;
				case 1: // Frintezza song effect
					_world.songEffectTask = null;
					L2Skill skill = _world.OnSong.effectSkill.getSkill();
					if (skill == null)
					{
						return;
					}
					
					if ((_world.frintezza != null) && !_world.frintezza.isDead() && (_world.activeScarlet != null) && !_world.activeScarlet.isDead())
					{
						final List<L2Character> targetList = new ArrayList<>();
						if (skill.hasEffectType(L2EffectType.STUN) || skill.isDebuff())
						{
							for (int objId : _world.getAllowed())
							{
								L2PcInstance player = L2World.getInstance().getPlayer(objId);
								if ((player != null) && player.isOnline() && (player.getInstanceId() == _world.getInstanceId()))
								{
									if (!player.isDead())
									{
										targetList.add(player);
									}
									if (player.hasSummon() && !player.getSummon().isDead())
									{
										targetList.add(player.getSummon());
									}
								}
							}
						}
						else
						{
							targetList.add(_world.activeScarlet);
						}
						if (!targetList.isEmpty())
						{
							_world.frintezza.doCast(skill, targetList.get(0), targetList.toArray(new L2Character[targetList.size()]));
						}
					}
					break;
				case 2: // finish morph
					_world.activeScarlet.setRHandId(SECOND_SCARLET_WEAPON);
					_world.activeScarlet.setIsInvul(false);
					
					// start halisha skills
					startQuestTimer("callSkillAI", 1000, _world.activeScarlet, null, true);
					break;
			}
		}
	}
	
	private class IntroTask implements Runnable
	{
		private final FETWorld _world;
		private final int _status;
		
		IntroTask(FETWorld world, int status)
		{
			_world = world;
			_status = status;
		}
		
		@Override
		public void run()
		{
			switch (_status)
			{
				case 0:
					broadCastPacket(_world, new Earthquake(-87784, -155083, -9087, 45, 5));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 1), 5000);
					break;
				case 1:
					FIRST_ROOM_DOORS.forEach(doorId -> closeDoor(doorId, _world.getInstanceId()));
					FIRST_ROUTE_DOORS.forEach(doorId -> closeDoor(doorId, _world.getInstanceId()));
					SECOND_ROUTE_DOORS.forEach(doorId -> closeDoor(doorId, _world.getInstanceId()));
					SECOND_ROOM_DOORS.forEach(doorId -> closeDoor(doorId, _world.getInstanceId()));
					
					addSpawn(CUBE, -87904, -141296, -9168, 0, false, 0, false, _world.getInstanceId());
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 2), 1500);
					break;
				case 2:
					_world.frintezzaDummy = addSpawn(29052, -87784, -155096, -9080, 16048, false, 0, false, _world.getInstanceId());
					_world.frintezzaDummy.setIsInvul(true);
					_world.frintezzaDummy.setIsImmobilized(true);
					
					_world.overheadDummy = addSpawn(29052, -87793, -153301, -9188, 16384, false, 0, false, _world.getInstanceId());
					_world.overheadDummy.setIsInvul(true);
					_world.overheadDummy.setIsImmobilized(true);
					_world.overheadDummy.setCollisionHeight(600);
					_world.overheadDummy.broadcastInfo();
					
					_world.portraitDummy1 = addSpawn(29052, -89566, -153168, -9165, 16048, false, 0, false, _world.getInstanceId());
					_world.portraitDummy1.setIsImmobilized(true);
					_world.portraitDummy1.setIsInvul(true);
					
					_world.portraitDummy3 = addSpawn(29052, -86004, -153168, -9165, 16048, false, 0, false, _world.getInstanceId());
					_world.portraitDummy3.setIsImmobilized(true);
					_world.portraitDummy3.setIsInvul(true);
					
					_world.scarletDummy = addSpawn(29053, -87793, -153301, -9188, 16384, false, 0, false, _world.getInstanceId());
					_world.scarletDummy.setIsInvul(true);
					_world.scarletDummy.setIsImmobilized(true);
					
					stopPc();
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 3), 1000);
					break;
				case 3:
					showSocialActionMovie(_world, _world.overheadDummy, 0, 75, -89, 0, 100, 0);
					showSocialActionMovie(_world, _world.overheadDummy, 300, 90, -10, 3500, 7000, 0);
					
					_world.frintezza = (L2GrandBossInstance) addSpawn(FRINTEZZA, -87780, -155086, -9080, 16384, false, 0, false, _world.getInstanceId());
					_world.frintezza.setIsImmobilized(true);
					_world.frintezza.setIsInvul(true);
					_world.frintezza.disableAllSkills();
					
					for (int[] element : PORTRAIT_SPAWNS)
					{
						L2MonsterInstance demon = (L2MonsterInstance) addSpawn(element[0] + 2, element[5], element[6], element[7], element[8], false, 0, false, _world.getInstanceId());
						demon.setIsImmobilized(true);
						demon.disableAllSkills();
						_world.demons.add(demon);
					}
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 4), 3500);
					break;
				case 4:
					showSocialActionMovie(_world, _world.frintezzaDummy, 1800, 90, 8, 3500, 7000, 0);
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 5), 900);
					break;
				case 5:
					showSocialActionMovie(_world, _world.frintezzaDummy, 140, 90, 10, 2500, 4500, 0);
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 6), 4000);
					break;
				case 6:
					showSocialActionMovie(_world, _world.frintezza, 40, 75, -10, 0, 1000, 0);
					showSocialActionMovie(_world, _world.frintezza, 40, 75, -10, 0, 12000, 0);
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 7), 1350);
					break;
				case 7:
					broadCastPacket(_world, new SocialAction(_world.frintezza.getObjectId(), 2));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 8), 7000);
					break;
				case 8:
					_world.frintezzaDummy.deleteMe();
					_world.frintezzaDummy = null;
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 9), 1000);
					break;
				case 9:
					broadCastPacket(_world, new SocialAction(_world.demons.get(1).getObjectId(), 1));
					broadCastPacket(_world, new SocialAction(_world.demons.get(2).getObjectId(), 1));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 10), 400);
					break;
				case 10:
					broadCastPacket(_world, new SocialAction(_world.demons.get(0).getObjectId(), 1));
					broadCastPacket(_world, new SocialAction(_world.demons.get(3).getObjectId(), 1));
					sendPacketX(new SpecialCamera(_world.portraitDummy1, 1000, 118, 0, 0, 1000, 0, 0, 1, 0, 0), new SpecialCamera(_world.portraitDummy3, 1000, 62, 0, 0, 1000, 0, 0, 1, 0, 0), -87784);
					sendPacketX(new SpecialCamera(_world.portraitDummy1, 1000, 118, 0, 0, 10000, 0, 0, 1, 0, 0), new SpecialCamera(_world.portraitDummy3, 1000, 62, 0, 0, 10000, 0, 0, 1, 0, 0), -87784);
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 11), 2000);
					break;
				case 11:
					showSocialActionMovie(_world, _world.frintezza, 240, 90, 0, 0, 1000, 0);
					showSocialActionMovie(_world, _world.frintezza, 240, 90, 25, 5500, 10000, 3);
					_world.portraitDummy1.deleteMe();
					_world.portraitDummy3.deleteMe();
					_world.portraitDummy1 = null;
					_world.portraitDummy3 = null;
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 12), 4500);
					break;
				case 12:
					showSocialActionMovie(_world, _world.frintezza, 100, 195, 35, 0, 10000, 0);
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 13), 700);
					break;
				case 13:
					showSocialActionMovie(_world, _world.frintezza, 100, 195, 35, 0, 10000, 0);
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 14), 1300);
					break;
				case 14:
					broadCastPacket(_world, new ExShowScreenMessage(NpcStringId.MOURNFUL_CHORALE_PRELUDE, 2, 5000));
					showSocialActionMovie(_world, _world.frintezza, 120, 180, 45, 1500, 10000, 0);
					
					broadCastPacket(_world, new MagicSkillUse(_world.frintezza, _world.frintezza, 5006, 1, 34000, 0));
					
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 15), 1500);
					break;
				case 15:
					showSocialActionMovie(_world, _world.frintezza, 520, 135, 45, 8000, 10000, 0);
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 16), 7500);
					break;
				case 16:
					showSocialActionMovie(_world, _world.frintezza, 1500, 110, 25, 8000, 13000, 0);
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 17), 7500);
					break;
				case 17:
					showSocialActionMovie(_world, _world.overheadDummy, 930, 160, -20, 0, 10000, 0);
					showSocialActionMovie(_world, _world.overheadDummy, 600, 180, -20, 0, 10000, 0);
					
					broadCastPacket(_world, new MagicSkillUse(_world.scarletDummy, _world.overheadDummy, 5004, 1, 5800, 0));
					
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 177), 3300);
					break;
				case 177:
					_world.activeScarlet = (L2GrandBossInstance) addSpawn(SCARLET1, -87789, -153295, -9176, 16384, false, 0, true, _world.getInstanceId());
					_world.activeScarlet.setRHandId(FIRST_SCARLET_WEAPON);
					_world.activeScarlet.setIsInvul(true);
					_world.activeScarlet.setIsImmobilized(true);
					
					broadCastPacket(_world, new SocialAction(_world.activeScarlet.getObjectId(), 3));
					
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 18), 1800);
					break;
				case 18:
					showSocialActionMovie(_world, _world.activeScarlet, 800, 180, 10, 1000, 10000, 0);
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 19), 2100);
					break;
				case 19:
					showSocialActionMovie(_world, _world.activeScarlet, 300, 60, 8, 0, 10000, 0);
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 20), 2000);
					break;
				case 20:
					showSocialActionMovie(_world, _world.activeScarlet, 500, 90, 10, 3000, 5000, 0);
					_world.songTask = ThreadPoolManager.getInstance().scheduleGeneral(new SongTask(_world, 0), 100);
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 21), 3000);
					break;
				case 21:
					for (int i = 0; i < PORTRAIT_SPAWNS.length; i++)
					{
						L2MonsterInstance portrait = (L2MonsterInstance) addSpawn(PORTRAIT_SPAWNS[i][0], PORTRAIT_SPAWNS[i][1], PORTRAIT_SPAWNS[i][2], PORTRAIT_SPAWNS[i][3], PORTRAIT_SPAWNS[i][4], false, 0, false, _world.getInstanceId());
						_world.portraits.put(portrait, i);
					}
					
					_world.overheadDummy.deleteMe();
					_world.scarletDummy.deleteMe();
					_world.overheadDummy = null;
					_world.scarletDummy = null;
					
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 22), 2000);
					break;
				case 22:
					for (L2MonsterInstance demon : _world.demons)
					{
						demon.setIsImmobilized(false);
						demon.enableAllSkills();
					}
					_world.activeScarlet.setIsInvul(false);
					_world.activeScarlet.setIsImmobilized(false);
					_world.activeScarlet.enableAllSkills();
					_world.activeScarlet.setRunning();
					_world.activeScarlet.doCast(INTRO_SKILL.getSkill());
					_world.frintezza.enableAllSkills();
					_world.frintezza.disableCoreAI(true);
					_world.frintezza.setIsMortal(false);
					startPc();
					startDemons();
					
					// start halisha skills
					startQuestTimer("callSkillAI", 1000, _world.activeScarlet, null, true);
					
					_world._demonsSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new DemonSpawnTask(_world), TIME_BETWEEN_DEMON_SPAWNS);
					break;
				case 23:
					broadCastPacket(_world, new SocialAction(_world.frintezza.getObjectId(), 4));
					break;
				case 24:
					stopDemons();
					stopPc();
					showSocialActionMovie(_world, _world.frintezza, 250, 120, 15, 0, 1000, 0);
					showSocialActionMovie(_world, _world.frintezza, 250, 120, 15, 0, 10000, 0);
					_world.activeScarlet.abortAttack();
					_world.activeScarlet.abortCast();
					_world.activeScarlet.setIsInvul(true);
					_world.activeScarlet.setIsImmobilized(true);
					_world.activeScarlet.disableAllSkills();
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 25), 7000);
					break;
				case 25:
					broadCastPacket(_world, new MagicSkillUse(_world.frintezza, _world.frintezza, 5006, 1, 34000, 0));
					showSocialActionMovie(_world, _world.frintezza, 500, 70, 15, 3000, 10000, 0);
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 26), 3000);
					break;
				case 26:
					showSocialActionMovie(_world, _world.frintezza, 2500, 90, 12, 6000, 10000, 0);
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 27), 3000);
					break;
				case 27:
					_world.scarlet_x = _world.activeScarlet.getX();
					_world.scarlet_y = _world.activeScarlet.getY();
					_world.scarlet_z = _world.activeScarlet.getZ();
					_world.scarlet_h = _world.activeScarlet.getHeading();
					_world.scarlet_a = _world.scarlet_h < 32768 ? Math.abs(180 - (int) (_world.scarlet_h / 182.044444444)) : Math.abs(540 - (int) (_world.scarlet_h / 182.044444444));
					
					showSocialActionMovie(_world, _world.activeScarlet, 250, _world.scarlet_a, 12, 0, 1000, 0);
					showSocialActionMovie(_world, _world.activeScarlet, 250, _world.scarlet_a, 12, 0, 10000, 0);
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 28), 500);
					break;
				case 28:
					_world.activeScarlet.doDie(_world.activeScarlet);
					showSocialActionMovie(_world, _world.activeScarlet, 450, _world.scarlet_a, 14, 8000, 8000, 0);
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 29), 6250);
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 30), 7200);
					break;
				case 29:
					_world.activeScarlet.deleteMe();
					_world.activeScarlet = null;
					break;
				case 30:
					_world.activeScarlet = (L2GrandBossInstance) addSpawn(SCARLET2, _world.scarlet_x, _world.scarlet_y, _world.scarlet_z, _world.scarlet_h, false, 0, false, _world.getInstanceId());
					_world.activeScarlet.setIsInvul(true);
					_world.activeScarlet.setIsImmobilized(true);
					_world.activeScarlet.disableAllSkills();
					
					showSocialActionMovie(_world, _world.activeScarlet, 450, _world.scarlet_a, 12, 500, 14000, 2);
					
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 31), 8100);
					break;
				case 31:
					broadCastPacket(_world, new SocialAction(_world.activeScarlet.getObjectId(), 2));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 32), 9000);
					break;
				case 32:
					startPc();
					startDemons();
					_world.activeScarlet.setIsInvul(false);
					_world.activeScarlet.setIsImmobilized(false);
					_world.activeScarlet.enableAllSkills();
					_world.isVideo = false;
					
					// start halisha skills
					startQuestTimer("callSkillAI", 1000, _world.activeScarlet, null, true);
					break;
				case 33:
					stopDemons();
					stopPc();
					showSocialActionMovie(_world, _world.activeScarlet, 300, _world.scarlet_a - 180, 5, 0, 7000, 0);
					showSocialActionMovie(_world, _world.activeScarlet, 200, _world.scarlet_a, 85, 4000, 10000, 0);
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 34), 7400);
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 35), 7500);
					break;
				case 34:
					_world.frintezza.doDie(_world.frintezza);
					break;
				case 35:
					showSocialActionMovie(_world, _world.frintezza, 100, 120, 5, 0, 7000, 0);
					showSocialActionMovie(_world, _world.frintezza, 100, 90, 5, 5000, 15000, 0);
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 36), 7000);
					break;
				case 36:
					showSocialActionMovie(_world, _world.frintezza, 900, 90, 25, 7000, 10000, 0);
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 37), 9000);
					break;
				case 37:
					controlStatus(_world);
					_world.isVideo = false;
					startPc();
					startDemons();
					break;
			}
		}
		
		private void stopPc()
		{
			for (int objId : _world.getAllowed())
			{
				L2PcInstance player = L2World.getInstance().getPlayer(objId);
				if ((player != null) && player.isOnline() && (player.getInstanceId() == _world.getInstanceId()))
				{
					player.abortAttack();
					player.abortCast();
					player.disableAllSkills();
					player.setTarget(null);
					player.stopMove(null);
					player.setIsImmobilized(true);
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				}
			}
		}
		
		private void startPc()
		{
			for (int objId : _world.getAllowed())
			{
				L2PcInstance player = L2World.getInstance().getPlayer(objId);
				if ((player != null) && player.isOnline() && (player.getInstanceId() == _world.getInstanceId()))
				{
					player.enableAllSkills();
					player.setIsImmobilized(false);
				}
			}
		}
		
		private void stopDemons()
		{
			for (L2MonsterInstance demon : _world.demons)
			{
				if (demon != null)
				{
					demon.abortAttack();
					demon.abortCast();
					demon.setIsInvul(true);
					demon.setIsImmobilized(true);
					demon.disableAllSkills();
				}
			}
		}
		
		private void startDemons()
		{
			for (L2MonsterInstance demon : _world.demons)
			{
				if (demon != null)
				{
					demon.setIsInvul(false);
					demon.setIsImmobilized(false);
					demon.enableAllSkills();
				}
			}
		}
		
		private void sendPacketX(L2GameServerPacket packet1, L2GameServerPacket packet2, int x)
		{
			for (int objId : _world.getAllowed())
			{
				L2PcInstance player = L2World.getInstance().getPlayer(objId);
				if ((player != null) && player.isOnline() && (player.getInstanceId() == _world.getInstanceId()))
				{
					if (player.getX() < x)
					{
						player.sendPacket(packet1);
					}
					else
					{
						player.sendPacket(packet2);
					}
				}
			}
		}
	}
	
	private class StatusTask implements Runnable
	{
		private final FETWorld _world;
		private final int _status;
		
		StatusTask(FETWorld world, int status)
		{
			_world = world;
			_status = status;
		}
		
		@Override
		public void run()
		{
			if (InstanceManager.getInstance().getWorld(_world.getInstanceId()) != _world)
			{
				return;
			}
			switch (_status)
			{
				case 0:
					ThreadPoolManager.getInstance().scheduleGeneral(new StatusTask(_world, 1), 2000);
					FIRST_ROOM_DOORS.forEach(doorId -> openDoor(doorId, _world.getInstanceId()));
					break;
				case 1:
					addAggroToMobs();
					break;
				case 2:
					ThreadPoolManager.getInstance().scheduleGeneral(new StatusTask(_world, 3), 100);
					SECOND_ROOM_DOORS.forEach(doorId -> openDoor(doorId, _world.getInstanceId()));
					break;
				case 3:
					addAggroToMobs();
					break;
				case 4:
					controlStatus(_world);
					break;
			}
		}
		
		private void addAggroToMobs()
		{
			L2PcInstance target = L2World.getInstance().getPlayer(_world.getAllowed().get(getRandom(_world.getAllowed().size())));
			if ((target == null) || (target.getInstanceId() != _world.getInstanceId()) || target.isDead() || target.isFakeDeath())
			{
				for (int objId : _world.getAllowed())
				{
					target = L2World.getInstance().getPlayer(objId);
					if ((target != null) && (target.getInstanceId() == _world.getInstanceId()) && !target.isDead() && !target.isFakeDeath())
					{
						break;
					}
					target = null;
				}
			}
			for (L2Npc mob : _world.npcList)
			{
				mob.setRunning();
				if (target != null)
				{
					((L2MonsterInstance) mob).addDamageHate(target, 0, 500);
					mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				}
				else
				{
					mob.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_CENTER);
				}
			}
		}
	}
	
	protected void broadCastPacket(FETWorld world, L2GameServerPacket packet)
	{
		for (int objId : world.getAllowed())
		{
			L2PcInstance player = L2World.getInstance().getPlayer(objId);
			if ((player != null) && player.isOnline() && (player.getInstanceId() == world.getInstanceId()))
			{
				player.sendPacket(packet);
			}
		}
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon, L2Skill skill)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof FETWorld)
		{
			final FETWorld world = (FETWorld) tmpworld;
			if ((npc.getId() == SCARLET1) && (world.getStatus() == 3) && (npc.getCurrentHp() < (npc.getMaxHp() * 0.80)))
			{
				cancelQuestTimer("callSkillAI", npc, null);
				controlStatus(world);
			}
			else if ((npc.getId() == SCARLET1) && (world.getStatus() == 4) && (npc.getCurrentHp() < (npc.getMaxHp() * 0.20)))
			{
				cancelQuestTimer("callSkillAI", npc, null);
				controlStatus(world);
			}
		}
		return null;
	}
	
	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isSummon)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof FETWorld)
		{
			if (skill != null)
			{
				// When Dewdrop of Destruction is used on Portraits they suicide.
				if (PORTRAITS.contains(npc.getId()) && (skill.getId() == DEWDROP_OF_DESTRUCTION_SKILL_ID))
				{
					npc.doDie(caster);
				}
				else if ((npc.getId() == FRINTEZZA) && (skill.getId() == SOUL_BREAKING_ARROW_SKILL_ID))
				{
					npc.setScriptValue(1);
					npc.setTarget(null);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				}
			}
		}
		return null;
	}
	
	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		switch (npc.getId())
		{
			case HALL_KEEPER_SUICIDAL_SOLDIER:
				if (skill.isSuicideAttack())
				{
					return onKill(npc, null, false);
				}
				break;
			case SCARLET1:
			case SCARLET2:
				if (!npc.isCastingNow() && (npc.getInstanceId() > 0) && (InstanceManager.getInstance().getWorld(npc.getInstanceId()) instanceof FETWorld))
				{
					callSkillAI(npc, (FETWorld) InstanceManager.getInstance().getWorld(npc.getInstanceId()));
				}
				break;
		}
		
		return super.onSpellFinished(npc, player, skill);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof FETWorld)
		{
			FETWorld world = (FETWorld) tmpworld;
			if (npc.getId() == HALL_ALARM)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new StatusTask(world, 0), 2000);
				if (debug)
				{
					_log.info(FinalEmperialTomb.class.getSimpleName() + ": Hall alarm is disabled, doors will open!");
				}
			}
			else if (npc.getId() == DARK_CHOIR_PLAYER)
			{
				world.darkChoirPlayerCount--;
				if (world.darkChoirPlayerCount < 1)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new StatusTask(world, 2), 2000);
					if (debug)
					{
						_log.info(FinalEmperialTomb.class.getSimpleName() + ": All Dark Choir Players are killed, doors will open!");
					}
				}
			}
			else if (npc.getId() == SCARLET2)
			{
				controlStatus(world);
			}
			else if (world.getStatus() <= 2)
			{
				if (npc.getId() == HALL_KEEPER_CAPTAIN)
				{
					if (getRandom(100) < 5)
					{
						npc.dropItem(player, DEWDROP_OF_DESTRUCTION_ITEM_ID, 1);
					}
				}
				
				if (checkKillProgress(npc, world))
				{
					controlStatus(world);
				}
			}
			else
			{
				world.demons.remove(npc);
				world.portraits.remove(npc);
			}
		}
		return "";
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		int npcId = npc.getId();
		getQuestState(player, true);
		if (npcId == GUIDE)
		{
			enterInstance(player, new FETWorld(), "FinalEmperialTomb.xml", TEMPLATE_ID);
		}
		else if (npc.getId() == CUBE)
		{
			int x = -87534 + getRandom(500);
			int y = -153048 + getRandom(500);
			player.teleToLocation(x, y, -9165);
			return null;
		}
		return "";
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("callSkillAI"))
		{
			if (!npc.isCastingNow() && (npc.getInstanceId() > 0) && (InstanceManager.getInstance().getWorld(npc.getInstanceId()) instanceof FETWorld))
			{
				callSkillAI(npc, (FETWorld) InstanceManager.getInstance().getWorld(npc.getInstanceId()));
			}
		}
		
		return null;
	}
	
	private L2Skill getRandomSkill(L2Npc npc, FETWorld world)
	{
		final int rnd = Rnd.get(1000);
		if (world.getStatus() == 3) // First Transform
		{
			if (world.lastSkillId == 5014)
			{
				if (rnd < (15 * 10))
				{
					return SkillData.getInstance().getInfo(5016, 1); // Paralysis
				}
				return SkillData.getInstance().getInfo(5014, 1); // The usual blow
			}
			return SkillData.getInstance().getInfo(5014, 1); // The usual blow
		}
		else if (world.getStatus() == 4) // Second transform
		{
			if (world.lastSkillId == 5014)
			{
				if (rnd < (20 * 10))
				{
					return SkillData.getInstance().getInfo(5016, 1); // Paralysis
				}
				else if (rnd < (30 * 10))
				{
					return SkillData.getInstance().getInfo(5015, 2); // Rush
				}
				else
				{
					return SkillData.getInstance().getInfo(5014, 2); // The usual blow
				}
			}
			else if (world.lastSkillId == 5016)
			{
				return SkillData.getInstance().getInfo(5014, 2); // The usual blow
			}
			else if (world.lastSkillId == 5015)
			{
				if (rnd < (30 * 10))
				{
					return SkillData.getInstance().getInfo(5018, 1); // Demon Field hit
				}
				return SkillData.getInstance().getInfo(5014, 2); // The usual blow
			}
			else
			{
				return SkillData.getInstance().getInfo(5014, 2); // The usual blow
			}
		}
		else if (world.getStatus() == 5) // Third Transform
		{
			if (npc.getCurrentHp() < (npc.getMaxHp() * 0.75))
			{
				if (world.lastSkillId == 5014)
				{
					if (rnd < (40 * 10))
					{
						return SkillData.getInstance().getInfo(5018, 2); // Demon Field
					}
					else if (rnd < (60 * 10))
					{
						return SkillData.getInstance().getInfo(5016, 1); // Paralysis
					}
					else
					{
						return SkillData.getInstance().getInfo(5014, 3); // The usual blow
					}
				}
				else if (world.lastSkillId == 5016)
				{
					if (rnd < (15 * 10))
					{
						return SkillData.getInstance().getInfo(5015, 3); // Rush
					}
					return SkillData.getInstance().getInfo(5014, 3); // The usual blow
				}
				else if (world.lastSkillId == 5015)
				{
					if (rnd < (30 * 10))
					{
						return SkillData.getInstance().getInfo(5018, 2); // Demon Field
					}
					return SkillData.getInstance().getInfo(5014, 3); // The usual blow
				}
				else
				{
					return SkillData.getInstance().getInfo(5014, 3); // The usual blow
				}
			}
			if (world.lastSkillId == 5014)
			{
				if (rnd < (20 * 10))
				{
					return SkillData.getInstance().getInfo(5016, 1); // Paralysis
				}
				return SkillData.getInstance().getInfo(5014, 3); // The usual blow
			}
			else if (world.lastSkillId == 5016)
			{
				if (rnd < (15 * 10))
				{
					return SkillData.getInstance().getInfo(5015, 3); // Rush
				}
				return SkillData.getInstance().getInfo(5014, 3); // The usual blow
			}
			else if (world.lastSkillId == 5015)
			{
				if (rnd < (30 * 10))
				{
					return SkillData.getInstance().getInfo(5019, 3); // AOE hit
				}
				return SkillData.getInstance().getInfo(5014, 3); // The usual blow
			}
			else
			{
				return SkillData.getInstance().getInfo(5014, 3); // The usual blow
			}
		}
		return null;
	}
	
	private void cleanDemons(FETWorld world)
	{
		if (world._demonsSpawnTask != null)
		{
			world._demonsSpawnTask.cancel(true);
			world._demonsSpawnTask = null;
		}
		
		world.demons.forEach(npc -> npc.doDie(null));
		world.demons.clear();
		world.portraits.keySet().forEach(npc -> npc.doDie(null));
		world.portraits.clear();
	}
	
	private synchronized void callSkillAI(final L2Npc npc, final FETWorld world)
	{
		if ((npc == null) || npc.isDead() || npc.isInvul() || npc.isCastingNow())
		{
			return;
		}
		
		L2Character _target = getRandomTarget(npc);
		if ((_target == null) || npc.isCastingNow())
		{
			return;
		}
		
		L2Skill _skill = getRandomSkill(npc, world);
		if (Util.checkIfInRange(_skill.getCastRange(), npc, _target, true))
		{
			npc.getAI().stopFollow();
			npc.setTarget(_target);
			world.lastSkillId = _skill.getId();
			npc.doCast(_skill);
			if (debug)
			{
				_log.info(FinalEmperialTomb.class.getSimpleName() + ": Casting skill: " + _skill.getId() + ":" + _skill.getName());
			}
		}
		else
		{
			world.lastSkillId = 0;
			if (!npc.isRunning())
			{
				npc.setIsRunning(true);
			}
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, _target);
		}
	}
	
	private L2Playable getRandomTarget(L2Npc npc)
	{
		for (L2Character creature : npc.getKnownList().getKnownCharacters())
		{
			if ((creature != null) && creature.isPlayable() && !creature.isDead())
			{
				return (L2Playable) creature;
			}
		}
		return null;
	}
	
	/**
	 * Shows a movie to the players in the lair.
	 * @param _world
	 * @param npc - L2NpcInstance target is the center of this movie
	 * @param dist - int distance from target
	 * @param yaw - angle of movie (north = 90, south = 270, east = 0 , west = 180)
	 * @param pitch - pitch > 0 looks up / pitch < 0 looks down
	 * @param time - fast ++ or slow -- depends on the value
	 * @param duration - How long to watch the movie
	 * @param socialAction - 1,2,3,4 social actions / other values do nothing
	 */
	protected void showSocialActionMovie(FETWorld _world, L2Npc npc, int dist, int yaw, int pitch, int time, int duration, int socialAction)
	{
		if (npc == null)
		{
			return;
		}
		
		broadCastPacket(_world, new SpecialCamera(npc, dist, yaw, pitch, time, duration));
		
		if ((socialAction > 0) && (socialAction < 5))
		{
			npc.broadcastPacket(new SocialAction(npc.getObjectId(), socialAction));
		}
	}
}
