package l2r.gameserver.scripts.ai.zone.LairOfAntharas;

import java.io.File;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastMap;
import l2r.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.MoveToLocation;
import l2r.gameserver.scripts.ai.npc.AbstractNpcAI;
import l2r.gameserver.util.Util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class DrakeWarriors extends AbstractNpcAI
{
	private static final int DRAKE_WARRIOR = 22849;
	
	protected static final FastMap<Integer, DrakeWarriorGroup> _warriorsGroups = new FastMap<>();
	
	protected class DrakeWarriorGroup
	{
		protected final int _id;
		protected L2Npc _warrior;
		protected int _currentRoute = 0;
		protected boolean _attackDirection = false;
		protected TreeMap<Integer, Location> _pathRoutes;
		
		protected DrakeWarriorGroup(int id)
		{
			_id = id;
		}
	}
	
	public DrakeWarriors(String name, String descr)
	{
		super(name, descr);
		
		addAggroRangeEnterId(DRAKE_WARRIOR);
		addAttackId(DRAKE_WARRIOR);
		load();
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (npc.getId() == DRAKE_WARRIOR)
		{
			DrakeWarriorGroup group = getGroup(npc);
			
			if ((!group._warrior.isCastingNow()) && (!group._warrior.isAttackingNow()) && (!group._warrior.isInCombat()) && (!player.isDead()))
			{
				group._attackDirection = true;
				((L2Attackable) group._warrior).addDamageHate(player, 0, 999);
				group._warrior.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
			}
		}
		return null;
	}
	
	@Override
	public final String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if (npc.getId() == DRAKE_WARRIOR)
		{
			DrakeWarriorGroup group = getGroup(npc);
			group._attackDirection = true;
		}
		return null;
	}
	
	protected class RunTask implements Runnable
	{
		@Override
		public void run()
		{
			for (int groupId : _warriorsGroups.keySet())
			{
				DrakeWarriorGroup group = _warriorsGroups.get(groupId);
				if (group._warrior.isInCombat() || group._warrior.isCastingNow() || group._warrior.isAttackingNow() || group._warrior.isDead() || (group._warrior.getAI().getIntention() == CtrlIntention.AI_INTENTION_MOVE_TO))
				{
					continue;
				}
				
				group._currentRoute = getNextRoute(group, group._currentRoute);
				Location loc = group._pathRoutes.get(group._currentRoute);
				int nextPathRoute;
				if (group._attackDirection)
				{
					nextPathRoute = getNextRoute(group, group._currentRoute - 1);
				}
				else
				{
					nextPathRoute = getNextRoute(group, group._currentRoute);
				}
				loc.setHeading(calculateHeading(loc, group._pathRoutes.get(nextPathRoute)));
				if (!group._warrior.isRunning())
				{
					group._warrior.setIsRunning(true);
				}
				MoveToLocation mov = new MoveToLocation(group._warrior);
				group._warrior.broadcastPacket(mov);
				group._warrior.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(loc.getX(), loc.getY(), loc.getZ(), loc.getHeading()));
			}
		}
	}
	
	protected int getNextRoute(DrakeWarriorGroup group, int currentRoute)
	{
		if (group._pathRoutes.lastKey().intValue() == currentRoute)
		{
			group._currentRoute = 0;
			return group._pathRoutes.firstKey();
		}
		return group._pathRoutes.higherKey(currentRoute);
	}
	
	protected DrakeWarriorGroup getGroup(L2Npc npc)
	{
		if ((npc == null) || (npc.getId() != DRAKE_WARRIOR))
		{
			return null;
		}
		
		for (DrakeWarriorGroup group : _warriorsGroups.values())
		{
			if ((npc.getId() == DRAKE_WARRIOR) && npc.equals(group._warrior))
			{
				return group;
			}
		}
		return null;
	}
	
	protected int calculateHeading(Location fromLoc, Location toLoc)
	{
		return Util.calculateHeadingFrom(fromLoc.getX(), fromLoc.getY(), toLoc.getX(), toLoc.getY());
	}
	
	protected void loadSpawns()
	{
		for (Integer integer : _warriorsGroups.keySet())
		{
			final int groupId = integer;
			DrakeWarriorGroup group = _warriorsGroups.get(groupId);
			Location spawn = group._pathRoutes.firstEntry().getValue();
			group._warrior = addSpawn(DRAKE_WARRIOR, spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getHeading(), false, 0);
			group._warrior.getSpawn().setAmount(1);
			group._warrior.getSpawn().startRespawn();
			group._warrior.getSpawn().setRespawnDelay(300);
			group._warrior.setIsRunner(true);
			group._warrior.getKnownList().startTrackingTask();
			group._warrior.setRunning();
		}
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new RunTask(), 30000, 1000);
	}
	
	protected void load()
	{
		File f = new File(Config.DATAPACK_ROOT, "data/xml/spawnZones/drake_warriors.xml");
		if (!f.exists())
		{
			_log.error("[Drake Warriors AI]: Error! drake_warriors.xml file is missing!");
			return;
		}
		
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringComments(true);
			factory.setValidating(true);
			Document doc = factory.newDocumentBuilder().parse(f);
			
			for (Node n = doc.getDocumentElement().getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("drakeWarrior".equalsIgnoreCase(n.getNodeName()))
				{
					final int id = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
					DrakeWarriorGroup group = new DrakeWarriorGroup(id);
					group._pathRoutes = new TreeMap<>();
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("pathRoute".equalsIgnoreCase(d.getNodeName()))
						{
							final int order = Integer.parseInt(d.getAttributes().getNamedItem("position").getNodeValue());
							final int x = Integer.parseInt(d.getAttributes().getNamedItem("locX").getNodeValue());
							final int y = Integer.parseInt(d.getAttributes().getNamedItem("locY").getNodeValue());
							final int z = Integer.parseInt(d.getAttributes().getNamedItem("locZ").getNodeValue());
							Location loc = new Location(x, y, z, 0);
							group._pathRoutes.put(order, loc);
						}
					}
					_warriorsGroups.put(id, group);
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("[Drake Warriors AI]: Error while loading drake_warriors.xml file: " + e.getMessage(), e);
		}
		loadSpawns();
	}
	
	public static void main(String[] args)
	{
		new DrakeWarriors(DrakeWarriors.class.getSimpleName(), "ai");
	}
}