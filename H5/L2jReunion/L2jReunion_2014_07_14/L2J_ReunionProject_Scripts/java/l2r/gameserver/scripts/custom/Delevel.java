package l2r.gameserver.scripts.custom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import l2r.L2DatabaseFactory;
import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.datatables.xml.ExperienceData;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.CreatureSay;

public class Delevel extends Quest
{
	private static final int NPC = 560;
	private static final int ADENA = 57; // item consumed for services
	private static final int COST1 = 5000; // cost per level, price=cost1*charlvl
	private static final int COST2 = 10000; // cost per vitality level, price=cost2*vitlvl
	private static final int COST3 = 20000; // cost for exp disable, price=cost3*charlvl
	private static final boolean DYNAMIC_PRICES = true; // if enabled, prices will be multiplied by player's level
	private static final int MINLVL = 20; // lowest level a player can delevel to and use other services (default: 20)
	private static final int KARMA = 0; // 0=don't allow karma, more=max value
	private static final int DELAY = 60; // delay in minutes between experience toggle
	private static final int ACCESSLEVEL = 20; // access level for experience toggle. Choose one that's not taken if the default one is
	
	private static final String LVL_TOO_LOW = "leveltoolow.htm";
	private static final String READY = "ready.htm";
	
	private int getDelevelPrice(final L2PcInstance player)
	{
		return DYNAMIC_PRICES ? COST1 * player.getLevel() : COST1;
	}
	
	private int getDevitalizePrice(final L2PcInstance player)
	{
		return DYNAMIC_PRICES ? COST2 * player.getVitalityLevel() : COST2;
	}
	
	private int getExpDisablePrice(final L2PcInstance player)
	{
		return DYNAMIC_PRICES ? COST3 * player.getVitalityLevel() : COST3;
	}
	
	public Delevel()
	{
		super(-1, "Delevel", "custom");
		
		addStartNpc(NPC);
		addFirstTalkId(NPC);
		addTalkId(NPC);
	}
	
	private void setupAccessLevels()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT accessLevel FROM `access_levels`");
			ResultSet rset = statement.executeQuery();
			boolean accessIsSet = false;
			
			while (rset.next())
			{
				if (rset.getInt("accessLevel") == ACCESSLEVEL)
				{
					accessIsSet = true;
					break;
				}
			}
			rset = null;
			if (!accessIsSet)
			{
				statement = con.prepareStatement("INSERT INTO access_levels VALUES (?,'Delevel Manager access',-1,-1,'',0,0,0,1,0,1,1,0)");
				statement.setInt(1, ACCESSLEVEL);
				statement.execute();
			}
			statement = null;
		}
		catch (final SQLException e)
		{
			_log.warn("Could not store Delevel Manager access level:" + e);
		}
	}
	
	@Override
	public String onSpawn(final L2Npc npc)
	{
		setupAccessLevels();
		return null;
	}
	
	@Override
	public String onFirstTalk(final L2Npc npc, final L2PcInstance player)
	{
		if (player.getKarma() > KARMA)
		{
			player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "Delevel Manager", "I don't offer my services to Karma players!"));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return null;
		}
		
		String htmltext = "welcome.htm";
		
		if (player.getLevel() < MINLVL)
		{
			final QuestState qs = player.getQuestState(getName());
			final String filename = ((qs != null) && (qs.get("experience") == "off") ? "emergency.htm" : LVL_TOO_LOW);
			htmltext = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/scripts/custom/Delevel/" + filename);
			htmltext = htmltext.replace("%MINLVL%", String.valueOf(MINLVL));
		}
		
		return htmltext;
	}
	
	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		String htmltext = null;
		
		final int VITALITY = player.getVitalityPoints();
		
		if (event.equalsIgnoreCase("talk"))
		{
			if (player.getKarma() > KARMA)
			{
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "Delevel Manager", "I don't offer my services to Karma players!"));
			}
			else if (player.getLevel() < MINLVL)
			{
				htmltext = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/scripts/custom/Delevel/" + LVL_TOO_LOW);
				htmltext = htmltext.replace("%MINLVL%", String.valueOf(MINLVL));
			}
			else
			{
				htmltext = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/scripts/custom/Delevel/" + READY);
				htmltext = htmltext.replace("%MINLVL%", String.valueOf(MINLVL));
				htmltext = htmltext.replace("%KARMA%", String.valueOf(KARMA));
				htmltext = htmltext.replace("%LEVEL%", String.valueOf(player.getLevel()));
				htmltext = htmltext.replace("%PRICE1%", String.valueOf(getDelevelPrice(player)));
				htmltext = htmltext.replace("%VITLVL%", String.valueOf(player.getVitalityLevel()));
				htmltext = htmltext.replace("%PRICE2%", String.valueOf(getDevitalizePrice(player)));
				htmltext = htmltext.replace("%PRICE3%", String.valueOf(getExpDisablePrice(player)));
				htmltext = (st.get("experience") == null) || (st.get("experience") == "on") ? htmltext.replace("%TOGGLE%", "Disable") : htmltext.replace("%TOGGLE%", "Enable");
			}
		}
		else if (event.equalsIgnoreCase("experience"))
		{
			if (player.isGM() || ((player.getAccessLevel().getLevel() > 0) && (player.getAccessLevel().getLevel() < ACCESSLEVEL)))
			{
				htmltext = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/scripts/custom/Delevel/notforgm.htm");
			}
			else if (player.getKarma() > KARMA)
			{
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "Delevel Manager", "I don't offer my services to Karma players!"));
			}
			else if (player.getLevel() < MINLVL)
			{
				// The player has exp disabled and has managed to delevel himself by dying.
				// Since he is unable to gain exp, we enable it,
				// otherwise he will bug the admins/GMs, and we don't want that, do we?
				if (player.getAccessLevel().getLevel() == ACCESSLEVEL)
				{
					player.setAccessLevel(0);
					st.set("experience", "on");
					player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "Delevel Manager", "Be more careful next time, " + player.getName() + "!"));
				}
				else
				{
					htmltext = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/scripts/custom/Delevel/" + LVL_TOO_LOW);
					htmltext = htmltext.replace("%MINLVL%", String.valueOf(MINLVL));
				}
			}
			else if (st.getQuestItemsCount(ADENA) < getExpDisablePrice(player))
			{
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "Delevel Manager", "Sorry, " + player.getName() + ", You don't have enough money!"));
			}
			else if (st.get("experience") == null)
			{
				st.takeItems(ADENA, getExpDisablePrice(player));
				player.setAccessLevel(ACCESSLEVEL);
				st.set("experience", "off");
				st.set("timeleft", String.valueOf(System.currentTimeMillis() + (DELAY * 60000)));
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "Delevel Manager", "Congratulations, " + player.getName() + ", Your Experience gain has been disabled!"));
			}
			else if (Long.parseLong(st.get("timeleft")) > System.currentTimeMillis())
			{
				htmltext = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/scripts/custom/Delevel/timeleft.htm");
				
				htmltext = htmltext.replace("%DELAY%", String.valueOf(DELAY / 60) + " hour(s) and " + String.valueOf(DELAY % 60) + " minute(s)");
				
				final long TIMELEFT = Long.parseLong(st.get("timeleft"));
				
				if ((TIMELEFT - System.currentTimeMillis()) > (DELAY * 60000 * 2)) // if more than 2 hours left
				{
					final int hours = (int) ((TIMELEFT - System.currentTimeMillis()) / 3600000);
					htmltext = htmltext.replace("%TIMELEFT%", String.valueOf(hours) + " hour(s) and " + String.valueOf(hours % 60) + " minute(s)");
				}
				else
				{
					htmltext = htmltext.replace("%TIMELEFT%", String.valueOf((TIMELEFT - System.currentTimeMillis()) / 60000) + " minute(s)");
				}
			}
			else
			{
				st.takeItems(ADENA, getExpDisablePrice(player));
				player.setAccessLevel(0);
				st.set("experience", "on");
				st.set("timeleft", String.valueOf(System.currentTimeMillis() + (DELAY * 60000)));
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "Delevel Manager", "Congratulations, " + player.getName() + ", Your Experience gain has been enabled!"));
			}
		}
		else if (event.equalsIgnoreCase("level"))
		{
			if (player.getKarma() > KARMA)
			{
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "Delevel Manager", "I don't offer my services to Karma players!"));
			}
			else if (player.getLevel() < MINLVL)
			{
				htmltext = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/scripts/custom/Delevel/" + LVL_TOO_LOW);
				htmltext = htmltext.replace("%MINLVL%", String.valueOf(MINLVL));
			}
			else if (player.getLevel() == MINLVL)
			{
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "Delevel Manager", "Sorry, " + player.getName() + ", You can't delevel below level " + MINLVL + "!"));
			}
			else if (st.getQuestItemsCount(ADENA) < getDelevelPrice(player))
			{
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "Delevel Manager", "Sorry, " + player.getName() + ", You don't have enough money!"));
			}
			else
			{
				st.takeItems(ADENA, getDelevelPrice(player));
				player.setExp(player.getStat().getExpForLevel(player.getLevel()));
				// sets exp to 0%, if you don't like people abusing this by
				// deleveling at 99% exp, comment the previous line
				player.removeExpAndSp(player.getExp() - ExperienceData.getInstance().getExpForLevel(player.getLevel() - 1), 0);
				// Lets see if this will fix skill learn
				player.rewardSkills();
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "Delevel Manager", "Congratulations, " + player.getName() + ", Your level has been decreased!"));
				
				if (player.getLevel() >= MINLVL)
				{
					htmltext = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/scripts/custom/Delevel/" + READY);
					htmltext = htmltext.replace("%MINLVL%", String.valueOf(MINLVL));
					htmltext = htmltext.replace("%KARMA%", String.valueOf(KARMA));
					htmltext = htmltext.replace("%LEVEL%", String.valueOf(player.getLevel()));
					htmltext = htmltext.replace("%PRICE1%", String.valueOf(getDelevelPrice(player)));
					htmltext = htmltext.replace("%VITLVL%", String.valueOf(player.getVitalityLevel()));
					htmltext = htmltext.replace("%PRICE2%", String.valueOf(getDevitalizePrice(player)));
					htmltext = htmltext.replace("%PRICE3%", String.valueOf(getExpDisablePrice(player)));
					htmltext = (st.get("experience") == null) || (st.get("experience") == "on") ? htmltext.replace("%TOGGLE%", "Disable") : htmltext.replace("%TOGGLE%", "Enable");
				}
			}
		}
		else if (event.equalsIgnoreCase("vitality"))
		{
			if (player.getKarma() > KARMA)
			{
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "Delevel Manager", "I don't offer my services to Karma players!"));
			}
			else if (player.getLevel() <= MINLVL)
			{
				htmltext = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/scripts/custom/Delevel/" + LVL_TOO_LOW);
				htmltext = htmltext.replace("%MINLVL%", String.valueOf(MINLVL + 1));
			}
			else if (st.getQuestItemsCount(ADENA) < getDevitalizePrice(player))
			{
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "Delevel Manager", "Sorry, " + player.getName() + ", You don't have enough money!"));
			}
			else
			{
				if (VITALITY < 240)
				{
					player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "Delevel Manager", "Sorry, " + player.getName() + ", Your Vitality can't be decreased anymore!"));
				}
				else if (VITALITY < 2000)
				{
					st.takeItems(ADENA, getDevitalizePrice(player));
					player.setVitalityPoints(0, true);
					player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "Delevel Manager", "Congratulations, " + player.getName() + ", Your Vitality has been decreased!"));
				}
				else if (VITALITY < 13000)
				{
					st.takeItems(ADENA, getDevitalizePrice(player));
					player.setVitalityPoints(241, true);
					player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "Delevel Manager", "Congratulations, " + player.getName() + ", Your Vitality has been decreased!"));
				}
				else if (VITALITY < 17000)
				{
					st.takeItems(ADENA, getDevitalizePrice(player));
					player.setVitalityPoints(2001, true);
					player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "Delevel Manager", "Congratulations, " + player.getName() + ", Your Vitality has been decreased!"));
				}
				else if (VITALITY > 17000)
				{
					st.takeItems(ADENA, getDevitalizePrice(player));
					player.setVitalityPoints(13001, true);
					player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "Delevel Manager", "Congratulations, " + player.getName() + ", Your Vitality has been decreased!"));
				}
				
				htmltext = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/scripts/custom/Delevel/" + READY);
				htmltext = htmltext.replace("%MINLVL%", String.valueOf(MINLVL));
				htmltext = htmltext.replace("%KARMA%", String.valueOf(KARMA));
				htmltext = htmltext.replace("%LEVEL%", String.valueOf(player.getLevel()));
				htmltext = htmltext.replace("%PRICE1%", String.valueOf(getDelevelPrice(player)));
				htmltext = htmltext.replace("%VITLVL%", String.valueOf(player.getVitalityLevel()));
				htmltext = htmltext.replace("%PRICE2%", String.valueOf(getDevitalizePrice(player)));
				htmltext = htmltext.replace("%PRICE3%", String.valueOf(getExpDisablePrice(player)));
				htmltext = (st.get("experience") == null) || (st.get("experience") == "on") ? htmltext.replace("%TOGGLE%", "Disable") : htmltext.replace("%TOGGLE%", "Enable");
			}
		}
		return htmltext;
	}
	
	public static void main(final String[] args)
	{
		new Delevel();
	}
}
