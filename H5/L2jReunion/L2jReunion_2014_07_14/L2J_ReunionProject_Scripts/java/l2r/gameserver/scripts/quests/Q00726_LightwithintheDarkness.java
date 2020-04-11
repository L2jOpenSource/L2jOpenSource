package l2r.gameserver.scripts.quests;

import java.util.HashMap;
import java.util.Map;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.Fort;
import l2r.gameserver.model.entity.Instance;
import l2r.gameserver.model.instancezone.InstanceWorld;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.quest.State;
import l2r.gameserver.network.NpcStringId;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.network.serverpackets.SystemMessage;

public class Q00726_LightwithintheDarkness extends Quest
{
	protected class PAWORLD extends InstanceWorld
	{
	}
	
	public static class FortDungeon
	{
		private final int INSTANCEID;
		private long _reEnterTime = 0;
		
		public FortDungeon(int iId)
		{
			INSTANCEID = iId;
		}
		
		public int getInstanceId()
		{
			return INSTANCEID;
		}
		
		public long getReEnterTime()
		{
			return _reEnterTime;
		}
		
		public void setReEnterTime(long time)
		{
			_reEnterTime = time;
		}
	}
	
	private static final String qn = "_726_LightwithintheDarkness";
	
	private static int KNIGHTS_EPAULETTE = 9912;
	
	private static final int SEDUCED_KNIGHT = 36562;
	private static final int SEDUCED_RANGER = 36563;
	private static final int SEDUCED_MAGE = 36564;
	private static final int SEDUCED_WARRIOR = 36565;
	private static final int KANADIS_GUIDE1 = 25659;
	private static final int KANADIS_GUIDE2 = 25660;
	private static final int KANADIS_GUIDE3 = 25661;
	private static final int KANADIS_FOLLOWER1 = 25662;
	private static final int KANADIS_FOLLOWER2 = 25663;
	private static final int KANADIS_FOLLOWER3 = 25664;
	
	private final Map<Integer, FortDungeon> _fortDungeons = new HashMap<>(21);
	
	public Q00726_LightwithintheDarkness()
	{
		super(726, Q00726_LightwithintheDarkness.class.getSimpleName(), "");
		_fortDungeons.put(35666, new FortDungeon(80));
		_fortDungeons.put(35698, new FortDungeon(81));
		_fortDungeons.put(35735, new FortDungeon(82));
		_fortDungeons.put(35767, new FortDungeon(83));
		_fortDungeons.put(35804, new FortDungeon(84));
		_fortDungeons.put(35835, new FortDungeon(85));
		_fortDungeons.put(35867, new FortDungeon(86));
		_fortDungeons.put(35904, new FortDungeon(87));
		_fortDungeons.put(35936, new FortDungeon(88));
		_fortDungeons.put(35974, new FortDungeon(89));
		_fortDungeons.put(36011, new FortDungeon(90));
		_fortDungeons.put(36043, new FortDungeon(91));
		_fortDungeons.put(36081, new FortDungeon(92));
		_fortDungeons.put(36118, new FortDungeon(93));
		_fortDungeons.put(36149, new FortDungeon(94));
		_fortDungeons.put(36181, new FortDungeon(95));
		_fortDungeons.put(36219, new FortDungeon(96));
		_fortDungeons.put(36257, new FortDungeon(97));
		_fortDungeons.put(36294, new FortDungeon(98));
		_fortDungeons.put(36326, new FortDungeon(99));
		_fortDungeons.put(36364, new FortDungeon(100));
		
		for (int i : _fortDungeons.keySet())
		{
			addStartNpc(i);
			addTalkId(i);
		}
		
		addKillId(KANADIS_GUIDE3);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return getNoQuestMsg(player);
		}
		
		int cond = st.getInt("cond");
		String htmltext = event;
		
		if (event.equalsIgnoreCase("FortWarden-04.htm"))
		{
			st.set("cond", "1");
			st.set("kanadis", "0");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("reward") && (cond == 2) && (st.getInt("done") == 1))
		{
			st.set("done", "0");
			st.giveItems(KNIGHTS_EPAULETTE, 152);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
			return null;
		}
		else if (event.equalsIgnoreCase("enter"))
		{
			int[] tele = new int[3];
			tele[0] = 48200;
			tele[1] = -12232;
			tele[2] = -9128;
			return enterInstance(player, "RimPailakaCastle.xml", tele, _fortDungeons.get(npc.getId()), checkFortCondition(player, npc, true));
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return getNoQuestMsg(player);
		}
		
		QuestState qs = player.getQuestState("_727_HopeWithinTheDarkness");
		String ret = checkFortCondition(player, npc, false);
		
		if (ret != null)
		{
			return ret;
		}
		
		if (qs != null)
		{
			st.exitQuest(true);
			return "FortWarden-01b.htm";
		}
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 70)
				{
					htmltext = "FortWarden-01.htm";
				}
				else
				{
					htmltext = "FortWarden-00.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				if (st.getInt("done") == 1)
				{
					htmltext = "FortWarden-06.htm";
				}
				else
				{
					htmltext = "FortWarden-05.htm";
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		final L2Party party = player.getParty();
		if (party == null)
		{
			return null;
		}
		
		int npcId = npc.getId();
		
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof PAWORLD)
		{
			PAWORLD world = (PAWORLD) tmpworld;
			
			for (L2PcInstance partymember : party.getMembers())
			{
				QuestState st = partymember.getQuestState(qn);
				if (st != null)
				{
					int cond = st.getInt("cond");
					if ((cond == 1) && (npcId == KANADIS_GUIDE3))
					{
						if (!partymember.isDead() && partymember.isInsideRadius(npc, 1000, true, false))
						{
							if (st.getInt("kanadis") == 0)
							{
								st.set("kanadis", "1");
							}
							else
							{
								st.set("done", "1");
								st.set("cond", "2");
								st.playSound("ItemSound.quest_middle");
								Instance instanceObj = InstanceManager.getInstance().getInstance(world.getInstanceId());
								instanceObj.setDuration(360000);
								instanceObj.removeNpcs();
							}
						}
					}
				}
				
			}
			
		}
		return null;
	}
	
	protected String enterInstance(L2PcInstance player, String template, int[] coords, FortDungeon dungeon, String ret)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null)
		{
			if (!(world instanceof PAWORLD))
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return "";
			}
			teleportPlayer(player, coords, world.getInstanceId());
			return "";
		}
		
		if (ret != null)
		{
			return ret;
		}
		ret = checkConditions(player);
		if (ret != null)
		{
			return ret;
		}
		L2Party party = player.getParty();
		int instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		Instance ins = InstanceManager.getInstance().getInstance(instanceId);
		ins.setSpawnLoc(new Location(player));
		world = new PAWORLD();
		world.setInstanceId(instanceId);
		world.setTemplateId(dungeon.getInstanceId());
		world.setStatus(0);
		dungeon.setReEnterTime(System.currentTimeMillis() + 14400000);
		InstanceManager.getInstance().addWorld(world);
		ThreadPoolManager.getInstance().scheduleGeneral(new spawnNpcs((PAWORLD) world), 10000);
		
		for (L2PcInstance partyMember : party.getMembers())
		{
			teleportPlayer(partyMember, coords, instanceId);
			world.addAllowed(partyMember.getObjectId());
			if (partyMember.getQuestState(qn) == null)
			{
				newQuestState(partyMember);
			}
		}
		return "";
	}
	
	private void teleportPlayer(L2PcInstance player, int[] coords, int instanceId)
	{
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2]);
	}
	
	private String checkConditions(L2PcInstance player)
	{
		final L2Party party = player.getParty();
		if (party == null)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_IN_PARTY_CANT_ENTER));
			return "FortWarden-10.htm";
		}
		else if (party.getLeader() != player)
		{
			player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
			return null;
		}
		for (L2PcInstance partymember : party.getMembers())
		{
			if (!partymember.isInsideRadius(player, 1000, true, true))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
				sm.addPcName(partymember);
				player.sendPacket(sm);
				return null;
			}
			if (partymember.getLevel() < 70)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(partymember);
				player.sendPacket(sm);
				return null;
			}
		}
		return null;
	}
	
	private String checkFortCondition(L2PcInstance player, L2Npc npc, boolean isEnter)
	{
		Fort fort = npc.getFort();
		FortDungeon dungeon = _fortDungeons.get(npc.getId());
		if ((player == null) || (fort == null) || (dungeon == null))
		{
			return "FortWarden-01a.htm";
		}
		if ((player.getClan() == null) || (player.getClan().getFortId() != fort.getResidenceId()))
		{
			return "FortressWarden-01.htm";
		}
		else if (fort.getFortState() == 0)
		{
			return "FortWarden-07.htm";
		}
		else if (fort.getFortState() == 2)
		{
			return "FortWarden-08.htm";
		}
		else if (isEnter && (dungeon.getReEnterTime() > System.currentTimeMillis()))
		{
			return "FortWarden-09.htm";
		}
		
		L2Party party = player.getParty();
		if (party == null)
		{
			return "FortWarden-10.htm";
		}
		for (L2PcInstance partyMember : party.getMembers())
		{
			if ((partyMember.getClan() == null) || (partyMember.getClan().getFortId() == 0) || (partyMember.getClan().getFortId() != fort.getResidenceId()))
			{
				return showHtmlFile(player, "FortWarden-11.htm").replace("%player%", partyMember.getName());
			}
		}
		return null;
	}
	
	protected class spawnNpcs implements Runnable
	{
		private final PAWORLD _world;
		
		public spawnNpcs(PAWORLD world)
		{
			_world = world;
		}
		
		@Override
		public void run()
		{
			FirstWave(_world);
		}
	}
	
	protected void FirstWave(final PAWORLD world)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				addSpawn(SEDUCED_KNIGHT, 49384, -12232, -9384, 0, false, 0, false, world.getInstanceId());
				addSpawn(SEDUCED_RANGER, 49192, -12232, -9384, 0, false, 0, false, world.getInstanceId());
				addSpawn(SEDUCED_MAGE, 49192, -12456, -9392, 0, false, 0, false, world.getInstanceId());
				addSpawn(SEDUCED_WARRIOR, 49192, -11992, -9392, 0, false, 0, false, world.getInstanceId());
				addSpawn(KANADIS_GUIDE1, 50536, -12232, -9384, 32768, false, 0, false, world.getInstanceId());
				broadCastPacket(world, new ExShowScreenMessage(NpcStringId.BEGIN_STAGE_1, 2, 3000));
				for (int i = 0; i < 10; i++)
				{
					addSpawn(KANADIS_FOLLOWER1, 50536, -12232, -9384, 32768, false, 0, false, world.getInstanceId());
				}
				ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
				{
					@Override
					public void run()
					{
						SecondWave(world);
					}
				}, 8 * 60 * 1000);
			}
		}, 10000);
	}
	
	protected void SecondWave(final PAWORLD world)
	{
		broadCastPacket(world, new ExShowScreenMessage(NpcStringId.BEGIN_STAGE_2, 2, 3000));
		addSpawn(KANADIS_GUIDE2, 50536, -12232, -9384, 32768, false, 0, false, world.getInstanceId());
		for (int i = 0; i < 10; i++)
		{
			addSpawn(KANADIS_FOLLOWER2, 50536, -12232, -9384, 32768, false, 0, false, world.getInstanceId());
		}
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				ThirdWave(world);
			}
		}, 8 * 60 * 1000);
	}
	
	protected void ThirdWave(final PAWORLD world)
	{
		broadCastPacket(world, new ExShowScreenMessage(NpcStringId.BEGIN_STAGE_3, 2, 3000));
		addSpawn(KANADIS_GUIDE3, 50536, -12232, -9384, 32768, false, 0, false, world.getInstanceId());
		addSpawn(KANADIS_GUIDE3, 50536, -12232, -9384, 32768, false, 0, false, world.getInstanceId());
		for (int i = 0; i < 10; i++)
		{
			addSpawn(KANADIS_FOLLOWER3, 50536, -12232, -9384, 32768, false, 0, false, world.getInstanceId());
		}
	}
	
	protected void broadCastPacket(PAWORLD world, L2GameServerPacket packet)
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
}