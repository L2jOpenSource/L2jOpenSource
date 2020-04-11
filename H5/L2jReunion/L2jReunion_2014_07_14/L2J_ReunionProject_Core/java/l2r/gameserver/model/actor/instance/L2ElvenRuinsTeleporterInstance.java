package l2r.gameserver.model.actor.instance;

import java.util.List;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastSet;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.datatables.sql.NpcTable;
import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.L2Spawn;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;

/**
 * @author Matim
 * @version 1.0
 */
public class L2ElvenRuinsTeleporterInstance extends L2Npc
{
	private boolean _mayEnter = true;
	private long _enterTime;
	
	private final int[][] spawnList =
	{
		{
			47382,
			248864,
			-6361
		},
		{
			47648,
			248820,
			-6361
		},
		{
			46485,
			247892,
			-6361
		},
		{
			46087,
			247876,
			-6361
		},
		{
			47524,
			247185,
			-6617
		},
		{
			47476,
			246771,
			-6617
		},
		{
			47808,
			246603,
			-6617
		},
		{
			47957,
			247063,
			-6617
		},
		{
			45407,
			249046,
			-6361
		},
		{
			45130,
			249287,
			-6361
		},
		{
			45722,
			249420,
			-6361
		},
		{
			45142,
			249163,
			-6361
		},
		{
			45044,
			248507,
			-6411
		},
		{
			43664,
			248686,
			-6491
		},
		{
			42855,
			247334,
			-6462
		},
		{
			43018,
			245186,
			-6462
		},
		{
			42693,
			247676,
			-6462
		},
		{
			41634,
			246449,
			-6462
		},
		{
			43200,
			245624,
			-6462
		},
		{
			44058,
			246578,
			-6512
		},
		{
			45603,
			245721,
			-6612
		},
		{
			45533,
			246816,
			-6612
		},
		{
			47003,
			246591,
			-6662
		},
		{
			48000,
			246045,
			-6662
		},
		{
			47039,
			245266,
			-6662
		},
		{
			44657,
			245547,
			-6415
		},
		{
			44737,
			247436,
			-6436
		},
		{
			42648,
			245383,
			-6462
		},
		{
			41644,
			246296,
			-6462
		},
		{
			42780,
			247283,
			-6462
		},
		{
			44177,
			245545,
			-6462
		},
		{
			43717,
			244419,
			-6498
		},
		{
			45244,
			243127,
			-6463
		},
		{
			45754,
			244325,
			-6517
		},
		{
			47086,
			243145,
			-6563
		},
		{
			47999,
			244305,
			-6563
		},
	};
	
	private final int[] monsterId =
	{
		20099,
		20022,
		20020,
		20017,
		20015,
		20039,
		20001,
		20008,
		20012,
		20031,
		20034,
		20037,
	};
	
	private final List<L2PcInstance> _raiders = new FastList<>();
	private final FastSet<L2Npc> _monsters = new FastSet<>();
	
	public L2ElvenRuinsTeleporterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if ((player == null) || (player.getLastFolkNPC() == null) || (player.getLastFolkNPC().getObjectId() != this.getObjectId()))
		{
			return;
		}
		
		if (command.startsWith("enterInside"))
		{
			teleportInside(player);
		}
		else if (command.startsWith("giranTele"))
		{
			player.teleToLocation(83551, 147945, -3400);
		}
		else if (command.startsWith("mainWindow"))
		{
			showChatWindow(player);
		}
		else if (command.startsWith("tutorial"))
		{
			showHtml(player, "guide.htm");
		}
	}
	
	/**
	 * Check if party may enter.
	 * @param player
	 * @return
	 */
	private boolean checkConditions(L2PcInstance player)
	{
		L2Party party = player.getParty();
		
		if (party == null)
		{
			player.sendPacket(new ExShowScreenMessage("You are not in a party!", 5000));
			return false;
		}
		if (party.getLeader() != player)
		{
			player.sendPacket(new ExShowScreenMessage("You are not party leader!", 5000));
			return false;
		}
		if (party.getMemberCount() < 5)
		{
			player.sendPacket(new ExShowScreenMessage("You need party with at least 5 members to enter!", 5000));
			return false;
		}
		for (L2PcInstance partyMember : party.getMembers())
		{
			if (partyMember.getLevel() < 82)
			{
				ExShowScreenMessage msg = new ExShowScreenMessage("Each party member should be level 82 at least!", 5000);
				party.broadcastPacket(msg);
				return false;
			}
			if (!Util.checkIfInRange(500, player, partyMember, true))
			{
				ExShowScreenMessage msg = new ExShowScreenMessage("Each party member should be close to the leader!", 5000);
				party.broadcastPacket(msg);
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Elven Ruins Manager:</title><body><center><br>");
		tb.append("Glad to see you <font color=\"LEVEL\">" + player.getName() + "</font>!<br>");
		
		if (mayEnter())
		{
			tb.append("This Location is: <font color=\"LEVEL\">Empty</font>!<br>");
			tb.append("Feel free to enter with your party!<br>");
			tb.append("<button value=\"Enter with Party\" action=\"bypass -h npc_%objectId%_enterInside\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		}
		else
		{
			tb.append("This Location is: <font color=\"LEVEL\">Not Empty</font>!<br>");
			tb.append("You will have to wait!<br>");
			tb.append("About: <font color=\"LEVEL\">" + getTimeLeft() + " minutes left!<br>");
		}
		tb.append("<button value=\"How does it work\" action=\"bypass -h npc_%objectId%_tutorial\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		tb.append("<button value=\"Giran Teleport\" action=\"bypass -h npc_%objectId%_giranTele\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		tb.append("<br><br><br>By Matim");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(this.getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(this.getObjectId()));
		
		player.sendPacket(msg);
	}
	
	/**
	 * @param player
	 * @param htm
	 */
	public void showHtml(L2PcInstance player, String htm)
	{
		String html = null;
		html = HtmCache.getInstance().getHtm(null, "data/html/mods/RuinsMod/" + htm);
		
		if (html == null)
		{
			player.sendMessage("Something is wrong report this bug to Gms.");
			return;
		}
		NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
		msg.setHtml(html);
		msg.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(msg);
	}
	
	/**
	 * Player need party with at least 5 party members. If they meets the requirements, they may enter. For next 10 min, there won't be any possibility to enter there again for example by another parties.
	 * @param player
	 */
	private void teleportInside(L2PcInstance player)
	{
		if (checkConditions(player))
		{
			L2Party party = player.getParty();
			
			for (L2PcInstance member : party.getMembers())
			{
				member.teleToLocation(49315, 248452, -5960);
				_raiders.add(member);
				setEnterTime(System.currentTimeMillis());
				spawnMonsters();
				setMayEnter(false);
				ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEnterTask(), 600000);
			}
		}
	}
	
	/**
	 * Clear Elven ruins, kick each player inside Ruins and send them info about that its time to leave :)
	 */
	public void clearRuins()
	{
		if (!_raiders.isEmpty())
		{
			for (L2PcInstance raider : _raiders)
			{
				raider.teleToLocation(-112367, 234703, -3688); // TODO is this correct?
				raider.sendMessage("Time is over, you have to leave now!");
			}
			
			_raiders.clear();
			_monsters.clear();
		}
	}
	
	private void spawnMonsters()
	{
		L2Npc monster = null;
		
		for (int[] spawn : spawnList)
		{
			monster = addSpawn(monsterId[Rnd.get(monsterId.length)], spawn[0], spawn[1], spawn[2]);
			_monsters.add(monster);
		}
	}
	
	private static L2Npc addSpawn(int npcId, int x, int y, int z)
	{
		L2Npc result = null;
		try
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
			if (template != null)
			{
				L2Spawn spawn = new L2Spawn(template);
				spawn.setInstanceId(0);
				spawn.setHeading(1);
				spawn.setX(x);
				spawn.setY(y);
				spawn.setZ(z);
				spawn.stopRespawn();
				result = spawn.spawnOne(true);
				
				return result;
			}
		}
		catch (Exception e1)
		{
			
		}
		return null;
	}
	
	private class ScheduleEnterTask implements Runnable
	{
		public ScheduleEnterTask()
		{
			// Nothing
		}
		
		@Override
		public void run()
		{
			clearRuins();
			setMayEnter(true);
		}
	}
	
	public int getTimeLeft()
	{
		long ms = System.currentTimeMillis() - _enterTime;
		long seconds = ms / 1000;
		int minutes = (int) (seconds / 60);
		
		return minutes;
	}
	
	public boolean mayEnter()
	{
		return _mayEnter;
	}
	
	public void setMayEnter(boolean b)
	{
		_mayEnter = b;
	}
	
	public void setEnterTime(long l)
	{
		_enterTime = l;
	}
}