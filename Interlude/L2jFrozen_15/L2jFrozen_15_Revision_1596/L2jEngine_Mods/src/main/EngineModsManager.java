package main;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.zone.L2ZoneType;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.TutorialShowQuestionMark;
import com.l2jfrozen.gameserver.skills.Stats;

import main.concurrent.ThreadPool;
import main.data.memory.ObjectData;
import main.data.memory.WorldData;
import main.data.properties.ConfigData;
import main.data.xml.IconData;
import main.data.xml.SchemeBufferPredefinedData;
import main.data.xml.SkillInfoData;
import main.engine.AbstractMod;
import main.engine.admin.PanelAdmin;
import main.engine.admin.ReloadConfigs;
import main.engine.community.ClanCommunityBoard;
import main.engine.community.FavoriteCommunityBoard;
import main.engine.community.HomeComunityBoard;
import main.engine.community.MemoCommunityBoard;
import main.engine.community.RegionComunityBoard;
import main.engine.events.cooperative.EventCooperativeManager;
import main.engine.events.cooperative.npc.RegisterNpc;
import main.engine.events.daily.normal.types.BonusWeekend;
import main.engine.events.daily.normal.types.Champions;
import main.engine.events.daily.normal.types.FireCat;
import main.engine.events.daily.normal.types.HeavyMedals;
import main.engine.events.daily.normal.types.NpcClassMaster;
import main.engine.events.daily.normal.types.RandomBossSpawn;
import main.engine.events.daily.randoms.EventRandomManager;
import main.engine.mods.AnnounceKillBoss;
import main.engine.mods.AntiBot;
import main.engine.mods.ColorAccordingAmountPvPorPk;
import main.engine.mods.EnchantAbnormalEffectArmor;
import main.engine.mods.GosthDeath;
import main.engine.mods.NewCharacterCreated;
import main.engine.mods.OfflineShop;
import main.engine.mods.PvpReward;
import main.engine.mods.SellBuffs;
import main.engine.mods.SpreeKills;
import main.engine.mods.SubClassAcumulatives;
import main.engine.mods.SystemAio;
import main.engine.mods.SystemVip;
import main.engine.mods.VoteReward;
import main.engine.npc.NpcBufferScheme;
import main.engine.npc.NpcRanking;
import main.engine.npc.NpcVoteRewardHopzone;
import main.engine.npc.NpcVoteRewardNetwork;
import main.engine.npc.NpcVoteRewardTopzone;
import main.engine.stats.StatsPlayer;
import main.holders.objects.CharacterHolder;
import main.holders.objects.ItemHolder;
import main.holders.objects.NpcHolder;
import main.holders.objects.PlayerHolder;
import main.instances.NpcDropsInstance;
import main.instances.NpcExpInstance;

/**
 * @author fissban
 */
public class EngineModsManager
{
	private static final Logger LOG = Logger.getLogger(AbstractMod.class.getName());
	
	private static final Map<String, AbstractMod> ENGINES_MODS = new LinkedHashMap<>();
	
	public EngineModsManager()
	{
		//
	}
	
	/**
	 * Load the basic systems that contain necessary information for the engine
	 */
	public static void loadData()
	{
		ThreadPool.getInstance();
		
		ObjectData.loadPlayers();
		IconData.getInstance().load();
		SkillInfoData.getInstance().load();
		ConfigData.load();
		SchemeBufferPredefinedData.getInstance().load();
		WorldData.init();
	}
	
	/**
	 * Load all mods and events
	 */
	public static void loadScripts()
	{
		try
		{
			// stats
			new StatsPlayer();
			// admin commands
			new PanelAdmin();
			new ReloadConfigs();
			// mods
			new ColorAccordingAmountPvPorPk();
			new EnchantAbnormalEffectArmor();
			new SpreeKills();
			new SubClassAcumulatives();
			new PvpReward();
			new AnnounceKillBoss();
			new SellBuffs();
			new VoteReward();
			new AntiBot();
			new NewCharacterCreated();
			new SystemAio();
			new SystemVip();
			new OfflineShop();
			new VoteReward();
			new GosthDeath();
			// events normals
			new BonusWeekend();
			new Champions();
			new FireCat();
			new HeavyMedals();
			new NpcClassMaster();
			new RandomBossSpawn();
			// events cooperative
			new EventCooperativeManager();
			new RegisterNpc();
			// events random
			new EventRandomManager();
			// npc
			new NpcRanking();
			new NpcBufferScheme();
			new NpcVoteRewardHopzone();
			new NpcVoteRewardNetwork();
			new NpcVoteRewardTopzone();
			// community
			new ClanCommunityBoard();
			new FavoriteCommunityBoard();
			new HomeComunityBoard();
			new MemoCommunityBoard();
			new RegionComunityBoard();
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void registerMod(AbstractMod mod)
	{
		ENGINES_MODS.put(mod.getClass().getSimpleName(), mod);
	}
	
	public static Collection<AbstractMod> getAllMods()
	{
		return ENGINES_MODS.values();
	}
	
	public static AbstractMod getMod(String name)
	{
		return ENGINES_MODS.get(name);
	}
	
	/** MISC ---------------------------------------------------------------------------------------------- */
	public static Class<?> createCustomEffect(String name)
	{
		Class<?> func = null;
		
		try
		{
			func = Class.forName("main.engine.effects.Effect" + name);
		}
		catch (ClassNotFoundException e)
		{
			//
		}
		
		return func;
	}
	
	/** XXX LISTENERS ----------------------------------------------------------------------------------------- */
	
	public static void onIncreaseLvl(L2PcInstance player)
	{
		PlayerHolder ph = ObjectData.get(PlayerHolder.class, player);
		for (AbstractMod mod : ENGINES_MODS.values())
		{
			try
			{
				mod.onIncreaseLvl(ph);
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				// LOG.severe(msg);
				e.printStackTrace();
			}
		}
	}
	
	public static boolean onUseSkill(L2PcInstance player, L2Skill skill)
	{
		PlayerHolder ph = ObjectData.get(PlayerHolder.class, player);
		
		for (AbstractMod mod : ENGINES_MODS.values())
		{
			try
			{
				if (!mod.isStarting())
				{
					continue;
				}
				
				if (!mod.onUseSkill(ph, skill))
				{
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		}
		
		return true;
	}
	
	public static boolean onUseItem(L2PcInstance player, L2ItemInstance item)
	{
		PlayerHolder ph = ObjectData.get(PlayerHolder.class, player);
		ItemHolder ih = ObjectData.get(ItemHolder.class, item);
		
		for (AbstractMod mod : ENGINES_MODS.values())
		{
			try
			{
				if (!mod.isStarting())
				{
					continue;
				}
				
				if (!mod.onUseItem(ph, ih))
				{
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		}
		
		return true;
	}
	
	public static void onSellItems(L2PcInstance player, L2ItemInstance item)
	{
		PlayerHolder ph = ObjectData.get(PlayerHolder.class, player);
		ItemHolder ih = ObjectData.get(ItemHolder.class, item);
		
		ENGINES_MODS.values().stream().filter(mod -> mod.isStarting()).forEach(mod ->
		{
			try
			{
				mod.onSellItems(ph, ih);
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		});
	}
	
	public static boolean onCommunityBoard(L2PcInstance player, String command)
	{
		PlayerHolder ph = ObjectData.get(PlayerHolder.class, player);
		
		for (AbstractMod mod : ENGINES_MODS.values())
		{
			try
			{
				if (!mod.isStarting())
				{
					continue;
				}
				if (mod.onCommunityBoard(ph, command))
				{
					return true;
				}
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	public static void onShutDown()
	{
		ENGINES_MODS.values().stream().filter(mod -> mod.isStarting()).forEach(mod ->
		{
			try
			{
				mod.onShutDown();
				mod.endMod();
				mod.cancelScheduledState();
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		});
	}
	
	public static boolean onExitWorld(L2PcInstance player)
	{
		PlayerHolder ph = ObjectData.get(PlayerHolder.class, player);
		
		if (ph.getWorldId() != 0)
		{
			WorldData.get(ph.getWorldId()).remove(ph);
		}
		
		boolean exitPlayer = false;
		for (AbstractMod mod : ENGINES_MODS.values())
		{
			try
			{
				if (!mod.isStarting())
				{
					continue;
				}
				if (mod.onExitWorld(ph))
				{
					exitPlayer = true;
				}
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		}
		
		if (!exitPlayer)
		{
			ObjectData.removeObject(player);
		}
		
		return exitPlayer;
	}
	
	public static boolean onNpcExpSp(L2Attackable npc, L2Character creature)
	{
		PlayerHolder phKiller = ObjectData.get(PlayerHolder.class, creature);
		NpcHolder nh = ObjectData.get(NpcHolder.class, npc);
		
		if ((phKiller == null) || (phKiller.getInstance() == null))
		{
			return false;
		}
		
		NpcExpInstance instance = new NpcExpInstance();
		
		ENGINES_MODS.values().stream().filter(mod -> mod.isStarting()).forEach(mod ->
		{
			try
			{
				mod.onNpcExpSp(phKiller, nh, instance);
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		});
		
		if (instance.hasSettings())
		{
			instance.init(npc, creature);
			return true;
		}
		
		return false;
	}
	
	public static boolean onNpcDrop(L2Attackable npc, L2Character creature)
	{
		PlayerHolder phKiller = ObjectData.get(PlayerHolder.class, creature);
		NpcHolder nh = ObjectData.get(NpcHolder.class, npc);
		
		NpcDropsInstance instance = new NpcDropsInstance();
		
		for (AbstractMod mod : ENGINES_MODS.values())
		{
			try
			{
				if (mod.isStarting())
				{
					mod.onNpcDrop(phKiller, nh, instance);
				}
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		}
		
		if (instance.hasSettings())
		{
			instance.init(npc, creature);
			return true;
		}
		
		return false;
	}
	
	public static void onEnterZone(L2Character creature, L2ZoneType zone)
	{
		CharacterHolder ch = ObjectData.get(CharacterHolder.class, creature);
		
		ENGINES_MODS.values().stream().filter(mod -> mod.isStarting()).forEach(mod ->
		{
			try
			{
				mod.onEnterZone(ch, zone);
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		});
	}
	
	public static void onExitZone(L2Character creature, L2ZoneType zone)
	{
		CharacterHolder ch = ObjectData.get(CharacterHolder.class, creature);
		
		ENGINES_MODS.values().stream().filter(mod -> mod.isStarting()).forEach(mod ->
		{
			try
			{
				mod.onExitZone(ch, zone);
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		});
	}
	
	public static void onCreateCharacter(L2PcInstance player)
	{
		PlayerHolder ph = ObjectData.get(PlayerHolder.class, player);
		
		// Quest system
		ph.getInstance().sendPacket(new TutorialShowQuestionMark(-1));
		
		ENGINES_MODS.values().stream().filter(mod -> mod.isStarting()).forEach(mod ->
		{
			try
			{
				mod.onCreateCharacter(ph);
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		});
	}
	
	public static boolean onVoiced(L2PcInstance player, String chat)
	{
		PlayerHolder ph = ObjectData.get(PlayerHolder.class, player);
		
		for (AbstractMod mod : ENGINES_MODS.values())
		{
			if (!mod.isStarting())
			{
				continue;
			}
			
			try
			{
				if (chat.startsWith("admin_"))
				{
					if (player.getAccessLevel().getLevel() < 1)
					{
						return false;
					}
					
					if (mod.onAdminCommand(ph, chat.replace("admin_", "")))
					{
						return true;
					}
				}
				else if (chat.startsWith("."))
				{
					if (mod.onVoicedCommand(ph, chat.replace(".", "")))
					{
						return true;
					}
				}
				else
				{
					if (mod.onChat(ph, chat))
					{
						return true;
					}
				}
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	public static boolean onInteract(L2PcInstance player, L2Character creature)
	{
		PlayerHolder ph = ObjectData.get(PlayerHolder.class, player);
		CharacterHolder ch = ObjectData.get(CharacterHolder.class, creature);
		
		if (ch == null)
		{
			return false;
		}
		
		for (AbstractMod mod : ENGINES_MODS.values())
		{
			if (!mod.isStarting())
			{
				continue;
			}
			
			try
			{
				if (!mod.onInteract(ph, ch))
				{
					continue;
				}
				else
				{
					return true;
				}
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		}
		
		// Send a Server->Client packet ActionFailed to the L2PcInstance
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		return false;
	}
	
	/**
	 * Todos los bypass tienen que tener el formato "bypass -h Engine modName bypassName", pero al mod solo llegara "bypassName".
	 * @param player
	 * @param creature
	 * @param command
	 */
	public static void onEvent(L2PcInstance player, String command)
	{
		PlayerHolder ph = ObjectData.get(PlayerHolder.class, player);
		CharacterHolder ch = ph.getTarget();
		
		ENGINES_MODS.values().stream().filter(mod -> command.startsWith(mod.getClass().getSimpleName()) && mod.isStarting()).forEach(mod ->
		{
			// if ((ch != null) && !player.isInsideRadius(ch.getInstance(), L2NpcInstance.INTERACTION_DISTANCE, false, false))
			// {
			// return;
			// }
			
			try
			{
				mod.onEvent(ph, ch, command.replace(mod.getClass().getSimpleName() + " ", ""));
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		});
		
		// Send a Server->Client packet ActionFailed to the L2PcInstance
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public static void onSpawn(L2NpcInstance npc)
	{
		// TODO es realmente necesario?
		if (ObjectData.get(NpcHolder.class, npc) == null)
		{
			ObjectData.addObject(npc);
		}
		
		NpcHolder nh = ObjectData.get(NpcHolder.class, npc);
		
		ENGINES_MODS.values().stream().filter(mod -> mod.isStarting()).forEach(mod ->
		{
			try
			{
				mod.onSpawn(nh);
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		});
	}
	
	public static void onEnterWorld(L2PcInstance player)
	{
		PlayerHolder ph = ObjectData.get(PlayerHolder.class, player);
		
		ENGINES_MODS.values().stream().filter(mod -> mod.isStarting()).forEach(mod ->
		{
			try
			{
				mod.onEnterWorld(ph);
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		});
		
		// Quest system
		ph.getInstance().sendPacket(new TutorialShowQuestionMark(-1));
	}
	
	public static boolean onAttack(L2Character killer, L2Character victim)
	{
		CharacterHolder chKiller = ObjectData.get(CharacterHolder.class, killer);
		CharacterHolder chVictim = ObjectData.get(CharacterHolder.class, victim);
		
		if (chVictim == null)
		{
			return false;
		}
		
		for (AbstractMod mod : ENGINES_MODS.values())
		{
			if (!mod.isStarting())
			{
				continue;
			}
			
			try
			{
				if (!mod.onAttack(chKiller, chVictim))
				{
					return true;
				}
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	public static boolean canAttack(L2Character killer, L2Character victim)
	{
		CharacterHolder chKiller = ObjectData.get(CharacterHolder.class, killer);
		CharacterHolder chVictim = ObjectData.get(CharacterHolder.class, victim);
		
		if (chVictim == null)
		{
			return false;
		}
		
		for (AbstractMod mod : ENGINES_MODS.values())
		{
			if (!mod.isStarting())
			{
				continue;
			}
			
			try
			{
				if (!mod.canAttack(chKiller, chVictim))
				{
					return false;
				}
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		}
		
		return true;
	}
	
	public static void onKill(L2Character killer, L2Character victim, boolean isPet)
	{
		CharacterHolder chKiller = ObjectData.get(CharacterHolder.class, killer);
		CharacterHolder chVictim = ObjectData.get(CharacterHolder.class, victim);
		
		if (chVictim == null)
		{
			return;
		}
		
		ENGINES_MODS.values().stream().filter(mod -> mod.isStarting()).forEach(mod ->
		{
			try
			{
				mod.onKill(chKiller, chVictim, isPet);
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		});
	}
	
	public static void onDeath(L2Character character)
	{
		CharacterHolder ch = ObjectData.get(CharacterHolder.class, character);
		
		try
		{
			ENGINES_MODS.values().stream().filter(mod -> mod.isStarting()).forEach(mod -> mod.onDeath(ch));
		}
		catch (Exception e)
		{
			LOG.log(Level.SEVERE, e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void onEnchant(L2PcInstance player)
	{
		PlayerHolder ph = ObjectData.get(PlayerHolder.class, player);
		
		ENGINES_MODS.values().stream().filter(mod -> mod.isStarting()).forEach(mod ->
		{
			try
			{
				mod.onEnchant(ph);
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		});
	}
	
	public static void onEquip(L2Character creature)
	{
		CharacterHolder ch = ObjectData.get(CharacterHolder.class, creature);
		
		ENGINES_MODS.values().stream().filter(mod -> mod.isStarting()).forEach(mod ->
		{
			try
			{
				mod.onEquip(ch);
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		});
	}
	
	public static void onUnequip(L2Character creature)
	{
		CharacterHolder ch = ObjectData.get(CharacterHolder.class, creature);
		
		ENGINES_MODS.values().stream().filter(mod -> mod.isStarting()).forEach(mod ->
		{
			try
			{
				mod.onUnequip(ch);
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		});
	}
	
	public static boolean onRestoreSkills(L2PcInstance player)
	{
		PlayerHolder ph = ObjectData.get(PlayerHolder.class, player);
		
		ENGINES_MODS.values().stream().filter(mod -> mod.isStarting()).forEach(mod ->
		{
			try
			{
				mod.onRestoreSkills(ph);
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		});
		
		return false;
	}
	
	public static void onCreatedItem(L2ItemInstance item)
	{
		ItemHolder ih = ObjectData.get(ItemHolder.class, item);
		
		for (AbstractMod mod : ENGINES_MODS.values())
		{
			if (!mod.isStarting())
			{
				continue;
			}
			
			try
			{
				mod.onCreatedItem(ih);
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	public static double onStats(Stats stat, L2Character creature, double value)
	{
		CharacterHolder ch = ObjectData.get(CharacterHolder.class, creature);
		
		if ((ch == null) || (ch.getInstance() == null))
		{
			return value;
		}
		
		for (AbstractMod mod : ENGINES_MODS.values())
		{
			if (!mod.isStarting())
			{
				continue;
			}
			
			try
			{
				value += mod.onStats(stat, ch, value) - value;
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		}
		
		return value;
	}
	
	public static void onSendData(ByteBuffer data)
	{
		if (data == null)
		{
			return;
		}
		
		for (AbstractMod mod : ENGINES_MODS.values())
		{
			if (!mod.isStarting())
			{
				continue;
			}
			
			try
			{
				mod.onSendData(data);
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	public static void onReceiveData(ByteBuffer data)
	{
		if (data == null)
		{
			return;
		}
		
		for (AbstractMod mod : ENGINES_MODS.values())
		{
			if (!mod.isStarting())
			{
				continue;
			}
			
			try
			{
				mod.onReceiveData(data);
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
