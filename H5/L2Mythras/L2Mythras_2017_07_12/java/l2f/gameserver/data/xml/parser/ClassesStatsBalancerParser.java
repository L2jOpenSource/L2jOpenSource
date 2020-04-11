package l2f.gameserver.data.xml.parser;

import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastMap;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2f.gameserver.data.xml.newreader.IXmlReader;
import l2f.gameserver.data.xml.holder.ClassesStatsBalancerHolder;
import l2f.gameserver.model.GameObjectsStorage;
import l2f.gameserver.model.Player;
import l2f.gameserver.network.serverpackets.UserInfo;
import l2f.gameserver.stats.Stats;
import l2f.gameserver.templates.StatsSet;
/**
 * @author Grivesky
 */
public class ClassesStatsBalancerParser implements IXmlReader
{
	private static final Logger _log = Logger.getLogger(ClassesStatsBalancerParser.class.getName());
	private final Map<Integer, Map<Stats, ClassesStatsBalancerHolder>> _balance = new FastMap<>();

	protected ClassesStatsBalancerParser()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_balance.clear();
		parseDatapackFile("config/balancer/ClassesStatsBalancer.xml");
	}
	
	public void reload()
	{
		load();
		synchronizePlayers();
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		int classes = 0;
		NamedNodeMap attrs;
		Node attr;
		StatsSet set;
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("class".equalsIgnoreCase(d.getNodeName()))
					{
						int id = parseInteger(d.getAttributes(), "id");

						if (!_balance.containsKey(id)){
							_balance.put(id, new FastMap<Stats, ClassesStatsBalancerHolder>());
						}

						for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
						{
							if ("stat".equalsIgnoreCase(cd.getNodeName()))
							{
								attrs = cd.getAttributes();
								set = new StatsSet();
								for (int i = 0; i < attrs.getLength(); i++)
								{
									attr = attrs.item(i);
									set.set(attr.getNodeName(), attr.getNodeValue());
								}
								ClassesStatsBalancerHolder data = new ClassesStatsBalancerHolder(set);
								_balance.get(id).put(data.getStat(), data);
								classes++;
							}
						}
					}
				}
			}
		}
		_log.info(getClass().getSimpleName() + ": Loaded: " + classes + " balances for " + _balance.size() + " classes.");
	}
	
	public void synchronizePlayers()
	{
		try
		{
			for (Player onlinePlayer : GameObjectsStorage.getAllPlayersForIterate()) 
	        {
				onlinePlayer.updateStats();
	        	onlinePlayer.broadcastUserInfo(true);
	        	onlinePlayer.broadcastCharInfo();
	        	onlinePlayer.broadcastStatusUpdate();
				UserInfo info2 = new UserInfo(onlinePlayer);
				onlinePlayer.sendPacket(info2);
	        }	
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		_log.info(getClass().getSimpleName() + ": Synchronize Players in game done.");
	}

	public ClassesStatsBalancerHolder getBalanceForClass(int classId, Stats stat)
	{
		return (_balance.containsKey(classId) ? _balance.get(classId).get(stat) : null);
	}

	public static ClassesStatsBalancerParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ClassesStatsBalancerParser _instance = new ClassesStatsBalancerParser();
	}
}