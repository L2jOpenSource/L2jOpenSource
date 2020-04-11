package l2f.gameserver.handler.admincommands.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import l2f.gameserver.Config;
import l2f.loginserver.database.L2DatabaseFactory;
import l2f.gameserver.handler.admincommands.IAdminCommandHandler;
import l2f.gameserver.instancemanager.ReflectionManager;
import l2f.gameserver.instancemanager.ServerVariables;
import l2f.gameserver.model.GameObject;
import l2f.gameserver.model.GameObjectsStorage;
import l2f.gameserver.model.Party;
import l2f.gameserver.model.Playable;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.Skill;
import l2f.gameserver.model.World;
import l2f.gameserver.model.base.Experience;
import l2f.gameserver.model.base.RestartType;
import l2f.gameserver.model.entity.Hero;
import l2f.gameserver.model.items.Inventory;
import l2f.gameserver.model.items.ItemInstance;
import l2f.gameserver.model.pledge.Clan;
import l2f.gameserver.network.clientpackets.Say2C;
import l2f.gameserver.network.serverpackets.CreatureSay;
import l2f.gameserver.network.serverpackets.ExShowScreenMessage;
import l2f.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import l2f.gameserver.network.serverpackets.EtcStatusUpdate;
import l2f.gameserver.network.serverpackets.InventoryUpdate;
import l2f.gameserver.network.serverpackets.MagicSkillUse;
import l2f.gameserver.network.serverpackets.NpcHtmlMessage;
import l2f.gameserver.network.serverpackets.StatusUpdate;
import l2f.gameserver.tables.AugmentationData;
import l2f.gameserver.tables.GmListTable;
import l2f.gameserver.tables.SkillTable;
import l2f.gameserver.utils.Location;
import l2f.gameserver.utils.TeleportUtils;

public class AdminPanel implements IAdminCommandHandler
{
	private enum Commands
	{
		admin_panel,
		admin_controlpanelchar,
		admin_imitate,
		admin_sendexmsg,
		admin_sendcsmsg,
		admin_sit_down,
		admin_sit_down_party,
		admin_stand_up,
		admin_stand_up_party,
		
		admin_effects,
		admin_smallfirework,
		admin_mediumfirework,
		admin_bigfirework,
		admin_cppanel,
		admin_changevaluescppanel,
		admin_lockshout,
		admin_lockhero,
		admin_cleanup,
		admin_clanskills,
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		StringTokenizer st = new StringTokenizer(fullString);

		Commands command = Commands.valueOf(st.nextToken());

		if(comm == null)
			return false;
		
		NpcHtmlMessage html = null;
		String text = null;
		GameObject target = null;
		Player caster = null;
		Party p = null;
		final Iterable<Player> world;
		boolean no_token = false;
		String char_name;
		
		switch(command)
		{
			case admin_panel:
				html = new NpcHtmlMessage(5);
				html.setFile("admin/panel/panel.htm");
				activeChar.sendPacket(html);
		        break;	        
			case admin_controlpanelchar:
				html = new NpcHtmlMessage(5);
				html.setFile("admin/panel/controlpanel.htm");
				activeChar.sendPacket(html);
		        break;

			case admin_effects:
				html = new NpcHtmlMessage(5);
				html.setFile("admin/panel/effects.htm");
				activeChar.sendPacket(html);
				break;

			case admin_imitate:
				target = activeChar.getTarget();
				
				if(target == null)
				{
					activeChar.sendMessage("Target incorrect");
					return false;
				}
				
				if(st.hasMoreTokens())
					text = fullString.substring(14);

				for (Player player : World.getAroundPlayers(activeChar))
					player.sendPacket(new CreatureSay(target.getObjectId(), 0, target.getName(), text));

		        activeChar.sendPacket(new CreatureSay(target.getObjectId(), 0, target.getName(), text));
		        html = new NpcHtmlMessage(5);
				html.setFile("admin/panel/controlpanel.htm");
				activeChar.sendPacket(html);
				break;

			case admin_sendexmsg:
				
				text = fullString.substring(15);
				
				if(!text.equals(""))
				{
					for (Player player : GameObjectsStorage.getAllPlayersForIterate())
						player.sendPacket(new ExShowScreenMessage(text, 5000, ScreenMessageAlign.TOP_CENTER, true));
				}

				html = new NpcHtmlMessage(5);
				html.setFile("admin/panel/controlpanel.htm");
				activeChar.sendPacket(html);
				
				break;
			case admin_sendcsmsg:

				text = fullString.substring(15);
				
				if(!text.equals(""))
				{
					text = text.substring(1);
					world = GameObjectsStorage.getAllPlayersForIterate();
					for (Player player : world)
						player.sendPacket(new CreatureSay(0, 15, activeChar.getName(), text));  
				}
				

				html = new NpcHtmlMessage(5);
				html.setFile("admin/panel/controlpanel.htm");
				activeChar.sendPacket(html);
				
				break;

			case admin_sit_down:

				activeChar.getTarget().getPlayer().sitDown(null);
				
				html = new NpcHtmlMessage(5);
				html.setFile("admin/panel/controlpanel.htm");
				activeChar.sendPacket(html);
				break;
				
			case admin_sit_down_party:
				target = activeChar.getTarget().getPlayer();

				p = target.getPlayer().getParty();
				
				if(p == null)
				{
					activeChar.sendMessage("This char has not party.");
					return false;
				}
				
				if(!p.isLeader(caster))
				{
					activeChar.sendMessage("You must use it on leader only. Leader of this party is "+p.getLeader().getName());
					return false;
				}
				
				for(Player ppl : p.getMembers())
					ppl.sitDown(null);

				html = new NpcHtmlMessage(5);
				html.setFile("admin/panel/controlpanel.htm");
				activeChar.sendPacket(html);
				break;
				
			case admin_stand_up:
				activeChar.getTarget().getPlayer().standUp();
				html = new NpcHtmlMessage(5);
				html.setFile("admin/panel/controlpanel.htm");
				activeChar.sendPacket(html);
				break;
				
			case admin_stand_up_party:
				p = activeChar.getTarget().getPlayer().getParty();
				
				if(p == null)
				{
					activeChar.sendMessage("This char has not party.");
					return false;
				}
				
				if(!p.isLeader(caster))
				{
					activeChar.sendMessage("You must use it on leader only. Leader of this party is "+p.getLeader().getName());
					return false;
				}
				
				for(Player ppl : p.getMembers())
					ppl.standUp();

				html = new NpcHtmlMessage(5);
				html.setFile("admin/panel/controlpanel.htm");
				activeChar.sendPacket(html);	
				break;
			case admin_smallfirework:
				world = GameObjectsStorage.getAllPlayersForIterate();
				for (Player player : world)
				{
	    			MagicSkillUse MSU = new MagicSkillUse(player, player, 2023, 1, 1, 0);
	    			player.broadcastPacket(MSU);
	    		}
	    		break;
			case admin_mediumfirework:
				world = GameObjectsStorage.getAllPlayersForIterate();
				for (Player player : world)
	    		{
	    			MagicSkillUse MSU = new MagicSkillUse(player, player, 2024, 1, 1, 0);
	    			player.broadcastPacket(MSU);
	    		} 
			break;
			case admin_bigfirework:
				world = GameObjectsStorage.getAllPlayersForIterate();
				for (Player player : world)
	    		{
	    			MagicSkillUse MSU = new MagicSkillUse(player, player, 2025, 1, 1, 0);
	    			player.broadcastPacket(MSU);
	    		}
	    		break;
			case admin_cppanel:
				CppanelMainPage(activeChar);
				break;
			case admin_changevaluescppanel:
				if(st.hasMoreTokens())
				{
					int value = 0;
					try
					{
						value = Integer.parseInt(st.nextToken());
					}
					catch (NumberFormatException e)
					{
						activeChar.sendMessage("Invalid Character in fill.");
						return false;
					}

					ServerVariables.set("fake_players", value);

					activeChar.sendMessage("You put "+value+" fakeplayers on database.");
					CppanelMainPage(activeChar);
				}
				break;
			case admin_clanskills:
				
				if(!st.hasMoreTokens())
				{
					clanSkillList(activeChar);
					return false;
				}
				
				if(activeChar.getTarget() == null)
				{
					clanSkillList(activeChar);
					activeChar.sendMessage("Invalid Target.");
					return false;
				}
				
				target = activeChar.getTarget().getPlayer();

				int skillId = Integer.parseInt(st.nextToken());
				int skillLevel = Integer.parseInt(st.nextToken());
				Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
				if(skill != null && target.getPlayer().getClan() != null)
				{
					Clan clan = target.getPlayer().getClan();
					clan.addSkill(skill, true);
					target.getPlayer().sendMessage("Admin add to your clan "+skill.getName()+" skill.");
					activeChar.sendMessage("You add "+skill.getName()+" skill to the clan "+clan.getName());
				}
				clanSkillList(activeChar);
				break;
			case admin_lockshout:
				Say2C.LOCK_SHOUT_VOICE = !Say2C.LOCK_SHOUT_VOICE;
				activeChar.sendMessage("Bool of Shout reversed to "+Say2C.LOCK_SHOUT_VOICE);
			break;
			case admin_lockhero:
				Say2C.LOCK_HERO_VOICE = !Say2C.LOCK_HERO_VOICE;
				activeChar.sendMessage("Bool of Hero reversed to "+Say2C.LOCK_HERO_VOICE);
			break;
			case admin_cleanup:
				System.runFinalization();
				System.gc();
				System.out.println("Java Memory Cleanup.");
				break;
		}
		return true;
	}
	
	private static void CppanelMainPage(Player activeChar)
	{
		int count = ServerVariables.getInt("fake_players");

	    NpcHtmlMessage nhm = new NpcHtmlMessage(5);
	    TextBuilder html = new TextBuilder("");
	    html.append("<html><head><title>CPPanel</title></head><body><center><br>");
	    html.append("<img src=\"L2UI_CH3.herotower_deco\" width=256 height=32>");
	    html.append("<br>");
	    html.append("Current FakePlayers: <font color=LEVEL>"+count+"</font><br>");
	    html.append("<br><br>");
	    html.append("Change FakePlayers Num: <edit var=\"value\" width=100 height=15><br>");
	    html.append("<button value=\"Do it!\" action=\"bypass -h admin_changevaluescppanel $value\" width=95 height=21 back=\"L2UI_ch3.bigbutton_down\" fore=\"L2UI_ch3.bigbutton\">");
     	html.append("<img src=\"L2UI_CH3.herotower_deco\" width=256 height=32></center></body></html>");
	    nhm.setHtml(html.toString());
	    activeChar.sendPacket(nhm);
	}
	
	public static void clanSkillList(Player player)
	{
		NpcHtmlMessage nhm = new NpcHtmlMessage(5);
	    TextBuilder html = new TextBuilder("");
	    html.append("<html><head><title>Clan Skill Panel</title></head><body>");
	    html.append("<center><table width=280 bgcolor=222120><tr><td><center>");
	    html.append("<font name=\"hs12\" color=\"FF0032\">L2Mythras</font></center></td></tr></table>");
	    html.append("<br><br><img src=\"L2UI.squaregray\" width=\"280\" height=\"2\">");
	    html.append("<table width=300 bgcolor=1F1818><tr><td width=220><center><font color=c1b33a>Option</font></center></td>");
	    html.append("<td width=140><center><font color=c1b33a>Status</font></center></td></tr></table>");
	    
	    html.append("<img src=\"L2UI.squaregray\" width=\"280\" height=\"2\"><table width=280 bgcolor=222120>");
	    
	    int lvl = 3;
		for (int i=370; i <= 391; i++)
		{
			if(i == 391)
				lvl = 1;
				
			Skill skill = SkillTable.getInstance().getInfo(i, lvl);
			
			html.append("<tr>");
			html.append("<td width=230 align=left>");
			html.append("<center><font color=878080>"+skill.getName()+"</font></center>");
			html.append("</td>");
			html.append("<td width=70>");
			html.append("<center>");
			html.append("<font color=c1b33a><a action=\"bypass -h admin_clanskills "+i+" "+lvl+"\">Add "+lvl+" Level</a></font>");
			html.append("</center>");
			html.append("</td>");
			html.append("</tr>");
		}
	    
	    html.append("</table></body></html>");
	    nhm.setHtml(html.toString());
	    player.sendPacket(nhm);
	}
	
	public String showItems(Playable target) 
	{
		String items = "";

		String lHand = "---";
		if (target.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) != null)
			lHand = target.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND).getName() + " <button value=\"Augment it\" action=\"bypass -h admin_setaugmentonweapon 1 $id $lvl\" width=180 height=32 back=\"L2UI_CH3.refinegrade3_21\" fore=\"L2UI_CH3.refinegrade3_21\">";

		String rHand = "---";
		if (target.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND) != null)
			rHand = target.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND).getName() + " <button value=\"Augment it\" action=\"bypass -h admin_setaugmentonweapon 2 $id $lvl\" width=180 height=32 back=\"L2UI_CH3.refinegrade3_21\" fore=\"L2UI_CH3.refinegrade3_21\">";
		
		items += "</font><br><font color=\"FF00FF\"> LHand: </font><font color=\"LEVEL\">" +lHand+
			"</font><br><font color=\"FF00FF\"> RHand: </font><font color=\"LEVEL\">" +rHand+

			"</font><br>";
		return items;
		
	}
	
	public void doHero(Player activeChar, Player _player, String _playername, String _time)
	{
		int days = Integer.parseInt(_time);

		if (_player == null)
		{
			activeChar.sendMessage("not found char" + _playername);
			return;
		}

		if(days > 0)
		{
			long expire = ((long)60 * 1000 * 60 * 24 * days);
			_player.setHero(true);
			Hero.addSkills(_player);
			_player.setVar("DonateHero", "true", System.currentTimeMillis() + expire);
			GmListTable.broadcastMessageToGMs("GM "+ activeChar.getName()+ " set hero stat for player "+ _playername + " for " + _time + " day(s)");
			_player.sendMessage(activeChar.getName()+", added you hero for "+days+" days.");
			activeChar.sendMessage("The hero status added to "+_player.getName()+" for "+days+" days.");

			_player.broadcastCharInfo();
		}
		else
		{
			activeChar.sendMessage("You must put up to 1 day for hero.");
		}
	}

	public void removeHero(Player activeChar, Player _player, String _playername)
	{
		_player.setHero(false);
		Hero.removeSkills(_player);
		_player.unsetVar("DonateHero");

		GmListTable.broadcastMessageToGMs("GM "+activeChar.getName()+" remove hero stat of player "+ _playername);
		_player.sendMessage("Your hero status removed by admin.");
		activeChar.sendMessage("The hero status removed from: "+_player.getName());
		
		_player.broadcastCharInfo();
	}
	
	/*public void doAio(Player activeChar, Player _player, String _playername, String _time)
	{
		int days = Integer.parseInt(_time);

		if (_player == null)
		{
			activeChar.sendMessage("not found char" + _playername);
			return;
		}

		if(days > 0)
		{
			long expire = ((long)60 * 1000 * 60 * 24 * days);
			_player.setVar("DonateAio", "true", System.currentTimeMillis() + expire);
			Long exp_add = Experience.LEVEL[85] - _player.getExp();
			_player.addExpAndSp(exp_add, 0);

			if(Config.ALLOW_AIO_NCOLOR){
				_player.setNameColor(Config.AIO_NCOLOR);
			}

			if(Config.ALLOW_AIO_TCOLOR){
				_player.setTitleColor(Config.AIO_TCOLOR);
			}
				
			//_player.rewardAioSkills();

			//_player.getInventory().addItem(13539, 1);
			ItemInstance item = _player.getInventory().getItemByItemId(13539);
			_player.getInventory().equipItem(item);
			
			 Location loc = TeleportUtils.getRestartLocation(activeChar, RestartType.TO_VILLAGE);
			_player.teleToLocation(loc, ReflectionManager.DEFAULT);

			_player.broadcastUserInfo(false);
			_player.sendPacket(new EtcStatusUpdate(_player));

			GmListTable.broadcastMessageToGMs("GM "+ activeChar.getName()+ " set aio stat for player "+ _playername + " for " + _time + " day(s)");
			_player.sendMessage(activeChar.getName()+", added you aio for "+days+" days.");
			activeChar.sendMessage("The aio status added to "+_player.getName()+" for "+days+" days.");
			
			_player.broadcastCharInfo();
		}
		else
		{
			activeChar.sendMessage("You must put up to 1 day for aio.");
		}
	}
	*/

	/*public void removeAio(Player activeChar, Player _player, String _playername)
	{
		_player.unsetVar("DonateAio");
		//_player.lostAioSkills();
		//_player.getInventory().destroyItemByItemId(8689, 1);
		_player.setNameColor(0xFFFFFF);
		_player.setTitleColor(0xFFFFFF);
		_player.broadcastUserInfo(false);
		_player.sendPacket(new EtcStatusUpdate(_player));

		GmListTable.broadcastMessageToGMs("GM "+activeChar.getName()+" remove aio stat of player "+ _playername);
		_player.sendMessage("Your aio status removed by admin.");
		activeChar.sendMessage("The aio status removed from: "+_player.getName());
		
		_player.broadcastCharInfo();
	}
*/
	public void doPremiumAccount(Player activeChar, Player _player, String _playername, String _time)
	{
		int days = Integer.parseInt(_time);
		if(days == 0)
		{
			activeChar.sendMessage("You can't give 0 days premiumAccount.");
			return;
		}

		Connection con = null;
		try
		{
			long expire = ((long)60 * 1000 * 60 * 24 * days);
			
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE accounts SET premium=?, premium_enddate=? WHERE login=?");
			statement.setInt(1, 1);
			statement.setLong(2, System.currentTimeMillis() + expire);
			statement.setString(3, _player.getAccountName());
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (con != null)
				{
					con.close();
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		GmListTable.broadcastMessageToGMs("GM "+ activeChar.getName()+ " set Premium Account for player "+ _playername + " for " + days + " day(s)");
		_player.sendMessage(activeChar.getName()+", added you Premium Account for "+days+" days.");
		activeChar.sendMessage("The Premium Account added to "+_player.getName()+" for "+days+" days.");
	}

	public void removePremiumAccount(Player activeChar, Player _player, String _playername)
	{
		//_player.setPremiumService(0);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE accounts SET premium = 0,premium_enddate = 0 WHERE login=?");
			statement.setString(1, _player.getAccountName());
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		GmListTable.broadcastMessageToGMs("GM "+activeChar.getName()+" remove Premium Account of player "+ _playername);
		_player.sendMessage("Your Premium Account removed by admin.");
		activeChar.sendMessage("The Premium Account removed from: "+_player.getName());
	}
	
	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}
