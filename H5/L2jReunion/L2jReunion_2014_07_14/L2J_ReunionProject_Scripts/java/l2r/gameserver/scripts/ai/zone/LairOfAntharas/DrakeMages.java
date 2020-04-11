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

public class DrakeMages extends AbstractNpcAI
{
	private static final int DRAKE_MAGE = 22851;
	
	protected static final FastMap<Integer, DrakeMageGroup> _magesGroups = new FastMap<>();
	
	protected class DrakeMageGroup
	{
		protected final int _id;
		protected L2Npc _mage;
		protected int _currentRoute = 0;
		protected boolean _attackDirection = false;
		protected TreeMap<Integer, Location> _pathRoutes;
		
		protected DrakeMageGroup(int id)
		{
			_id = id;
		}
	}
	
	public DrakeMages(String name, String descr)
	{
		super(name, descr);
		
		addAggroRangeEnterId(DRAKE_MAGE);
		addAttackId(DRAKE_MAGE);
		load();
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (npc.getId() == DRAKE_MAGE)
		{
			DrakeMageGroup group = getGroup(npc);
			
			if ((!group._mage.isCastingNow()) && (!group._mage.isAttackingNow()) && (!group._mage.isInCombat()) && (!player.isDead()))
			{
				group._attackDirection = true;
				((L2Attackable) group._mage).addDamageHate(player, 0, 999);
				group._mage.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
			}
		}
		return null;
	}
	
	@Override
	public final String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if (npc.getId() == DRAKE_MAGE)
		{
			DrakeMageGroup group = getGroup(npc);
			group._attackDirection = true;
		}
		return null;
	}
	
	protected class RunTask implements Runnable
	{
		@Override
		public void run()
		{
			for (int groupId : _magesGroups.keySet())
			{
				DrakeMageGroup group = _magesGroups.get(groupId);
				if (group._mage.isInCombat() || group._mage.isCastingNow() || group._mage.isAttackingNow() || group._mage.isDead() || (group._mage.getAI().getIntention() == CtrlIntention.AI_INTENTION_MOVE_TO))
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
				if (!group._mage.isRunning())
				{
					group._mage.setIsRunning(true);
				}
				MoveToLocation mov = new MoveToLocation(group._mage);
				group._mage.broadcastPacket(mov);
				group._mage.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(loc.getX(), loc.getY(), loc.getZ(), loc.getHeading()));
			}
		}
	}
	
	protected int getNextRoute(DrakeMageGroup group, int currentRoute)
	{
		if (group._pathRoutes.lastKey().intValue() == currentRoute)
		{
			group._currentRoute = 0;
			return group._pathRoutes.firstKey();
		}
		return group._pathRoutes.higherKey(currentRoute);
	}
	
	protected DrakeMageGroup getGroup(L2Npc npc)
	{
		if ((npc == null) || (npc.getId() != DRAKE_MAGE))
		{
			return null;
		}
		
		for (DrakeMageGroup group : _magesGroups.values())
		{
			if ((npc.getId() == DRAKE_MAGE) && npc.equals(group._mage))
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
		for (Integer integer : _magesGroups.keySet())
		{
			final int groupId = integer;
			DrakeMageGroup group = _magesGroups.get(groupId);
			Location spawn = group._pathRoutes.firstEntry().getValue();
			group._mage = addSpawn(DRAKE_MAGE, spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getHeading(), false, 0);
			group._mage.getSpawn().setAmount(1);
			group._mage.getSpawn().startRespawn();
			group._mage.getSpawn().setRespawnDelay(300);
			group._mage.setIsRunner(true);
			group._mage.getKnownList().startTrackingTask();
			group._mage.setRunning();
		}
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new RunTask(), 30000, 1000);
	}
	
	protected void load()
	{
		File f = new File(Config.DATAPACK_ROOT, "data/xml/spawnZones/drake_mages.xml");
		if (!f.exists())
		{
			_log.error("[Drake Mages AI]: Error! drake_mages.xml file is missing!");
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
				if ("drakeMage".equalsIgnoreCase(n.getNodeName()))
				{
					final int id = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
					DrakeMageGroup group = new DrakeMageGroup(id);
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
					_magesGroups.put(id, group);
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("[Drake Mages AI]: Error while loading drake_mages.xml file: " + e.getMessage(), e);
		}
		loadSpawns();
	}
	
	public static void main(String[] args)
	{
		new DrakeMages(DrakeMages.class.getSimpleName(), "ai");
	}
}