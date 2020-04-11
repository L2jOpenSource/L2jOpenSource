package net.sf.l2j.gameserver.scripting.scripts.ai.individual;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.data.xml.MapRegionData.TeleportType;
import net.sf.l2j.gameserver.enums.SayType;
import net.sf.l2j.gameserver.enums.ScriptEventType;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.network.serverpackets.NpcSay;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;
import net.sf.l2j.gameserver.skills.L2Skill;

/**
 * Benom is a specific Raid Boss, appearing in Rune Castle. He is aggressive towards anyone.<br>
 * <br>
 * The castle owning clan can defeat Benom. It can teleport to Benom's den using a specific gatekeeper, 24 hours before siege start. If the clan doesn't kill Benom before the siege start, Benom will appear during the siege if at least 2 life controls crystals have been broken.
 */
public class Benom extends L2AttackableAIScript
{
	private static final int BENOM = 29054;
	private static final int TELEPORT_CUBE = 29055;
	private static final int DUNGEON_KEEPER = 35506;
	
	// Important : the heading is used as offset.
	private static final SpawnLocation[] TARGET_TELEPORTS =
	{
		new SpawnLocation(12860, -49158, -976, 650),
		new SpawnLocation(14878, -51339, 1024, 100),
		new SpawnLocation(15674, -49970, 864, 100),
		new SpawnLocation(15696, -48326, 864, 100),
		new SpawnLocation(14873, -46956, 1024, 100),
		new SpawnLocation(12157, -49135, -1088, 650),
		new SpawnLocation(12875, -46392, -288, 200),
		new SpawnLocation(14087, -46706, -288, 200),
		new SpawnLocation(14086, -51593, -288, 200),
		new SpawnLocation(12864, -51898, -288, 200),
		new SpawnLocation(15538, -49153, -1056, 200),
		new SpawnLocation(17001, -49149, -1064, 650)
	};
	
	private static final SpawnLocation THRONE_LOC = new SpawnLocation(11025, -49152, -537, 0);
	private static final SpawnLocation PRISON_LOC = new SpawnLocation(11882, -49216, -3008, 0);
	
	private static final int RUNE_CASTLE = 8;
	
	private Npc _benom;
	
	private boolean _isPrisonOpened;
	
	private List<Player> _targets = new ArrayList<>();
	
	public Benom()
	{
		super("ai/individual");
		
		addSiegeNotify(RUNE_CASTLE);
		
		addStartNpc(DUNGEON_KEEPER, TELEPORT_CUBE);
		addTalkId(DUNGEON_KEEPER, TELEPORT_CUBE);
	}
	
	@Override
	protected void registerNpcs()
	{
		addEventIds(BENOM, ScriptEventType.ON_AGGRO, ScriptEventType.ON_SPELL_FINISHED, ScriptEventType.ON_ATTACK, ScriptEventType.ON_KILL);
	}
	
	@Override
	public String onTalk(Npc npc, Player talker)
	{
		switch (npc.getNpcId())
		{
			case TELEPORT_CUBE:
				talker.teleportTo(TeleportType.TOWN);
				break;
			
			case DUNGEON_KEEPER:
				if (_isPrisonOpened)
					talker.teleportTo(12589, -49044, -3008, 0);
				else
					return HtmCache.getInstance().getHtm("data/html/doormen/35506-2.htm");
				break;
		}
		return super.onTalk(npc, talker);
	}
	
	@Override
	public String onTimer(String name, Npc npc, Player player)
	{
		if (name.equalsIgnoreCase("benom_spawn"))
		{
			_isPrisonOpened = true;
			
			_benom = addSpawn(BENOM, PRISON_LOC, false, 0, false);
			_benom.broadcastNpcSay("Who dares to covet the throne of our castle! Leave immediately or you will pay the price of your audacity with your very own blood!");
		}
		else if (name.equalsIgnoreCase("tower_check"))
		{
			final Siege siege = CastleManager.getInstance().getCastleById(RUNE_CASTLE).getSiege();
			if (siege.getControlTowerCount() < 2)
			{
				npc.teleportTo(THRONE_LOC, 0);
				// TODO get Dungeon Keeper instance and use regular NpcSay constructor with instance
				siege.getCastle().getSiegeZone().broadcastPacket(new NpcSay(DUNGEON_KEEPER, SayType.ALL, "Oh no! The defenses have failed. It is too dangerous to remain inside the castle. Flee! Every man for himself!"));
				
				cancelQuestTimers("tower_check");
				startQuestTimerAtFixedRate("raid_check", npc, null, 10000);
			}
		}
		else if (name.equalsIgnoreCase("raid_check"))
		{
			if (!npc.isInsideZone(ZoneId.SIEGE) && !npc.isTeleporting())
				npc.teleportTo(THRONE_LOC, 0);
		}
		return super.onTimer(name, npc, player);
	}
	
	@Override
	public String onAggro(Npc npc, Player player, boolean isPet)
	{
		if (isPet)
			return super.onAggro(npc, player, isPet);
		
		if (_targets.size() < 10 && Rnd.get(3) < 1)
			_targets.add(player);
		
		return super.onAggro(npc, player, isPet);
	}
	
	@Override
	public void onSiegeEvent(Siege siege)
	{
		// Don't go further if the castle isn't owned.
		if (siege.getCastle().getOwnerId() <= 0)
			return;
		
		switch (siege.getStatus())
		{
			case IN_PROGRESS:
				_isPrisonOpened = false;
				if (_benom != null && !_benom.isDead())
					startQuestTimerAtFixedRate("tower_check", _benom, null, 30000);
				break;
			
			case REGISTRATION_OPENED:
				_isPrisonOpened = false;
				
				if (_benom != null)
				{
					cancelQuestTimers("tower_check");
					cancelQuestTimers("raid_check");
					
					_benom.deleteMe();
				}
				
				startQuestTimer("benom_spawn", null, null, siege.getSiegeDate().getTimeInMillis() - 8640000 - System.currentTimeMillis());
				break;
			
			case REGISTRATION_OVER:
				startQuestTimer("benom_spawn", null, null, 0);
				break;
		}
	}
	
	@Override
	public String onSpellFinished(Npc npc, Player player, L2Skill skill)
	{
		switch (skill.getId())
		{
			case 4995:
				teleportTarget(player);
				((Attackable) npc).stopHating(player);
				break;
			
			case 4996:
				teleportTarget(player);
				((Attackable) npc).stopHating(player);
				if (!_targets.isEmpty())
				{
					for (Player target : _targets)
					{
						final long x = player.getX() - target.getX();
						final long y = player.getY() - target.getY();
						final long z = player.getZ() - target.getZ();
						final long range = 250;
						if (((x * x) + (y * y) + (z * z)) <= (range * range))
						{
							teleportTarget(target);
							((Attackable) npc).stopHating(target);
						}
					}
					_targets.clear();
				}
				break;
		}
		
		return null;
	}
	
	@Override
	public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		if (attacker instanceof Playable)
		{
			if (Rnd.get(100) <= 25)
			{
				npc.setTarget(attacker);
				npc.getCast().doCast(4995, 1);
			}
			else if (!npc.getCast().isCastingNow())
			{
				if ((npc.getCurrentHp() < (npc.getMaxHp() / 3)) && Rnd.get(500) < 1)
				{
					npc.setTarget(attacker);
					npc.getCast().doCast(4996, 1);
				}
				else if (!npc.isIn3DRadius(attacker, 300) && Rnd.get(100) < 1)
				{
					npc.setTarget(attacker);
					npc.getCast().doCast(4993, 1);
				}
				else if (Rnd.get(100) < 1)
				{
					npc.setTarget(attacker);
					npc.getCast().doCast(4994, 1);
				}
			}
		}
		return super.onAttack(npc, attacker, damage, skill);
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		npc.broadcastNpcSay("It's not over yet... It won't be... over... like this... Never...");
		cancelQuestTimers("raid_check");
		
		addSpawn(TELEPORT_CUBE, 12589, -49044, -3008, 0, false, 120000, false);
		
		return null;
	}
	
	/**
	 * Move a player by Skill. Venom has two skill related.
	 * @param player the player targeted
	 */
	private static void teleportTarget(Player player)
	{
		if (player != null)
		{
			final SpawnLocation loc = Rnd.get(TARGET_TELEPORTS);
			player.teleportTo(loc, loc.getHeading());
		}
	}
}