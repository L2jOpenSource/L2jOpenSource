package main.engine.mods;

import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.olympiad.Olympiad;
import com.l2jfrozen.gameserver.network.clientpackets.Say2;

import main.concurrent.ThreadPool;
import main.data.memory.ObjectData;
import main.data.properties.ConfigData;
import main.data.xml.SkillInfoData;
import main.engine.AbstractMod;
import main.enums.TeamType;
import main.holders.objects.CharacterHolder;
import main.holders.objects.ObjectHolder;
import main.holders.objects.PlayerHolder;
import main.packets.SellBuffTitle;
import main.packets.SellBuffTitle.TitleType;
import main.util.UtilInventory;
import main.util.UtilMessage;
import main.util.builders.html.Html;
import main.util.builders.html.HtmlBuilder;
import main.util.builders.html.HtmlBuilder.HtmlType;

/**
 * @author fissban
 */
public class SellBuffs extends AbstractMod
{
	private static final int MAX_SKILL_PER_PAGE = 9;
	
	public SellBuffs()
	{
		registerMod(ConfigData.SELLBUFF_ENABLE);
	}
	
	@Override
	public void onModState()
	{
		//
	}
	
	@Override
	public boolean onVoicedCommand(PlayerHolder ph, String chat)
	{
		if (chat.startsWith("cancelsellbuff")) // cancel sellbuff state
		{
			if (!ph.isSellBuff())
			{
				return true;
			}
			
			ph.setSellBuff(false);
			
			ph.getInstance().standUp();
			ph.setTeam(TeamType.NONE);
			ph.getInstance().broadcastUserInfo();
			return true;
		}
		if (chat.startsWith("sellbuff")) // init sellbuff html
		{
			if (!ph.getInstance().isInsideZone(L2Character.ZONE_PEACE))
			{
				UtilMessage.sendCreatureMsg(ph, Say2.TELL, "[Engine]", "Only can sellbuff in peace zone!");
				return true;
			}
			
			HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
			
			hb.append("<html><body><center>");
			hb.append(Html.head("SELL BUFF"));
			hb.append("<font color=LEVEL>Welcome </font><font color=00C3FF>", ph.getName(), "</font> system selling buffs.<br1>");
			hb.append("<br1>");
			hb.append("is the type of support.<br1>");
			hb.append("You can sell your buffs at the price you want,");
			hb.append("those to which you do not define a price will not be sold.");
			hb.append("<br1>");
			hb.append("To cancel this state should use the command<br1>");
			hb.append("<font color=LEVEL>.CancelSellBuff</font>");
			hb.append("<br><br>");
			hb.append("<font color=00C3FF>Do you want to continue?</font>");
			hb.append("<br>");
			hb.append("<button value=START action=\"bypass -h Engine SellBuffs start\" width=80 height=25 back=L2UI_CH3.Btn1_normalOn fore=L2UI_CH3.btn1_normal>");
			hb.append("</center></body></html>");
			sendHtml(null, hb, ph);
			return true;
		}
		
		return false;
	}
	
	@Override
	public void onEvent(PlayerHolder ph, CharacterHolder ch, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		
		switch (st.nextToken())
		{
			case "sendPacketSellBuff":
			{
				if (!ph.isSellBuff())
				{
					break;
				}
				
				// Send custom packet client<->server
				if (ch.getInstance() != null)
				{
					ch.getInstance().sendPacket(new SellBuffTitle(ph, TitleType.SELL, "SellBuffs"));
				}
				break;
			}
			case "view":// see list of buffs for sale
			{
				int page = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
				
				getBuffList(ph, ph.getTarget(), page);
				break;
			}
			case "finish": // sit to sell buffs
			{
				ph.setSellBuff(true);
				
				ph.getInstance().sitDown();
				ph.setTeam(TeamType.BLUE);
				ph.getInstance().broadcastPacket(new SellBuffTitle(ph, TitleType.SELL, "SellBuffs"));
				ph.getInstance().broadcastUserInfo();
				break;
			}
			case "buy":// buy buff
			{
				try
				{
					int id = Integer.parseInt(st.nextToken());
					int lvl = Integer.parseInt(st.nextToken());
					String sellerName = st.nextToken();
					
					L2PcInstance sellerBuff = L2World.getInstance().getPlayer(sellerName);
					
					// Check if target inside radius
					if ((sellerBuff == null) || !ph.getInstance().isInsideRadius(sellerBuff, 500, false, false))
					{
						UtilMessage.sendCreatureMsg(ph, Say2.TELL, "[Engine]", "Missing seller buff!");
						return;
					}
					
					PlayerHolder phSeller = ObjectData.get(PlayerHolder.class, sellerBuff);
					
					if ((phSeller == null) || !phSeller.isSellBuff())
					{
						return;
					}
					
					// prevent bypass exploit
					if ((sellerBuff.getSkillLevel(id) == -1) && (sellerBuff.getSkillLevel(id) != lvl))
					{
						return;
					}
					
					int price = phSeller.getSellBuffPrice(id);
					
					// prevent bypass exploit
					if (price == -1)
					{
						return;
					}
					
					// Reduce adena for buyer
					if (!UtilInventory.takeItems(phSeller, 57, price))
					{
						return;
					}
					// Add adena for seller
					UtilInventory.giveItems(ph, 57, price);
					
					// Standup and launch skill.
					sellerBuff.standUp();
					sellerBuff.setTarget(ph.getInstance());
					L2Skill skill = SkillTable.getInstance().getInfo(id, lvl);
					sellerBuff.doCast(skill);
					skill.getEffects(ph.getInstance(), ph.getInstance());
					sellerBuff.setCurrentMp(sellerBuff.getStat().getMaxMp());
					// Sit down at the end of throwing the skill.
					ThreadPool.schedule(() -> sellerBuff.sitDown(), skill.getHitTime() + 100);
					
					int page = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
					
					getBuffList(ph, phSeller, page);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				
				break;
			}
			case "start":
			{
				int page = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
				
				startHtml(ph, page);
				break;
			}
			case "set":
			{
				try
				{
					int skillId = Integer.parseInt(st.nextToken());
					int price = Integer.parseInt(st.nextToken());
					
					ph.setSellBuffPrice(skillId, price);
				}
				catch (Exception e)
				{
					UtilMessage.sendCreatureMsg(ph, Say2.TELL, "sellbuff", "Wrong price!!!!!");
				}
				startHtml(ph, 1);
				break;
			}
		}
	}
	
	@Override
	public boolean onInteract(PlayerHolder ph, CharacterHolder sellerBuff)
	{
		if (!(sellerBuff instanceof PlayerHolder))
		{
			return false;
		}
		
		PlayerHolder seller = (PlayerHolder) sellerBuff;
		
		if ((seller == null) || !seller.isSellBuff())
		{
			return false;
		}
		
		// Check if target inside radius
		if (!ph.getInstance().isInsideRadius(sellerBuff.getInstance(), 500, false, false))
		{
			return false;
		}
		
		getBuffList(ph, seller, 1);
		return true;
	}
	
	@Override
	public boolean onExitWorld(PlayerHolder ph)
	{
		if (ph.getInstance() == null)
		{
			return false;
		}
		
		if (ph.isSellBuff() && ConfigData.OFFLINE_SELLBUFF_ENABLE)
		{
			L2PcInstance player = ph.getInstance();
			
			if (!player.isInsideZone(L2Character.ZONE_PEACE))
			{
				player.sendMessage("You're out of the peace zone!");
				return true;
			}
			
			if (player.isInOlympiadMode() || player.isFestivalParticipant() || player.isInJail())
			{
				return true;
			}
			
			// If a party is in progress, leave it
			if (player.isInParty())
			{
				player.getParty().removePartyMember(player, false);
			}
			
			// If the player has Pet, unsummon it
			if (player.getPet() != null)
			{
				player.getPet().unSummon(player);
			}
			
			// Removal from olympiad game
			if (Olympiad.getInstance().isRegistered(player) || (player.getOlympiadGameId() != -1))
			{
				Olympiad.getInstance().removeDisconnectedCompetitor(player);
			}
			
			if (ConfigData.OFFLINE_SET_NAME_COLOR)
			{
				ThreadPool.schedule(() -> player.getAppearance().setNameColor(ConfigData.OFFLINE_NAME_COLOR), 5000);
			}
			
			ph.setOffline(true);
		}
		
		return false;
	}
	
	// HTML's ----------------------------------------------------------------------------------------------------------- //
	
	/**
	 * Generate and send start html view sellbuffs
	 * @param ph
	 * @param page
	 */
	private static void startHtml(PlayerHolder ph, int page)
	{
		HtmlBuilder hb = new HtmlBuilder();
		hb.append(Html.START);
		hb.append(Html.head("SELL BUFF"));
		hb.append("<br>");
		hb.append("<center>");
		
		List<L2Skill> skills = ((L2Character) ph.getInstance()).getSkills().values().stream().filter(sk -> checkSkil(sk)).collect(Collectors.toList());
		
		if (skills.isEmpty())
		{
			hb.append("<br>Empty buffs!");
			hb.append("</center>");
			hb.append(Html.END);
			sendHtml(null, hb, ph);
			return;
		}
		
		hb.append("<table border=0 cellspacing=0 cellpadding=0>");
		
		int searchPage = MAX_SKILL_PER_PAGE * (page - 1);
		int count = 0;
		
		for (L2Skill sk : skills)
		{
			count++;
			// min
			if (count < searchPage)
			{
				continue;
			}
			// max
			if (count >= (searchPage + MAX_SKILL_PER_PAGE))
			{
				continue;
			}
			
			int price = ph.getSellBuffPrice(sk.getId());
			
			hb.append("<tr>");
			hb.append("<td width=32 height=25><img src=", SkillInfoData.getSkillIcon(sk.getId()), " width=32 height=25></td>");
			hb.append("<td width=50><edit var=price" + sk.getId() + " width=50></td>");
			hb.append("<td width=80><button value=Set action=\"bypass -h Engine SellBuffs set " + sk.getId() + " $price" + sk.getId() + "\" width=80 height=25 back=L2UI_CH3.Btn1_normalOn fore=L2UI_CH3.btn1_normal></td>");
			hb.append("<td width=64 align=center><font color=LEVEL>Price: ", Html.formatAdena(price), "</font></td>");
			hb.append("</tr>");
		}
		hb.append("</table>");
		hb.append("<br>");
		hb.append("<button value=Finish action=\"bypass -h Engine SellBuffs finish\" width=80 height=25 back=L2UI_CH3.Btn1_normalOn fore=L2UI_CH3.btn1_normal>");
		
		hb.append("<img src=L2UI.SquareGray width=264 height=1>");
		hb.append("<table bgcolor=000000>");
		hb.append("<tr>");
		
		int currentPage = 1;
		
		// Generate page system
		for (int i = 0; i < count; i++)
		{
			if ((i % MAX_SKILL_PER_PAGE) == 0)
			{
				hb.append("<td width=18 align=center><a action=\"bypass -h Engine SellBuffs start " + currentPage + "\">" + currentPage + "</a></td>");
				currentPage++;
			}
		}
		
		hb.append("</tr>");
		hb.append("</table>");
		hb.append("<img src=L2UI.SquareGray width=264 height=1>");
		hb.append("</center>");
		hb.append(Html.END);
		sendHtml(null, hb, ph);
	}
	
	private static void getBuffList(PlayerHolder ph, ObjectHolder sellerBuff, int page)
	{
		if ((sellerBuff == null) || !(sellerBuff instanceof PlayerHolder) || !((PlayerHolder) sellerBuff).isSellBuff())
		{
			return;
		}
		
		PlayerHolder seller = (PlayerHolder) sellerBuff;
		
		HtmlBuilder hb = new HtmlBuilder();
		
		hb.append(Html.START);
		hb.append("<br><br>");
		hb.append("<center>");
		hb.append("<font color=LEVEL>Hello </font><font color=00C3FF>", ph.getName(), "</font><font color=LEVEL> want my Buff!</font>");
		hb.append("<br>");
		List<L2Skill> skills = ((L2Character) seller.getInstance()).getSkills().values().stream().filter(sk -> checkSkil(sk) && (seller.getSellBuffPrice(sk.getId()) >= 0)).collect(Collectors.toList());
		
		if (skills.isEmpty())
		{
			hb.append("<br>Empty buffs!");
			hb.append("</center>");
			hb.append(Html.END);
			sendHtml(null, hb, ph);
			return;
		}
		
		hb.append("<table border=0 cellspacing=0 cellpadding=0>");
		
		int searchPage = MAX_SKILL_PER_PAGE * (page - 1);
		int count = 0;
		
		for (L2Skill sk : skills)
		{
			count++;
			
			// min
			if (count < searchPage)
			{
				continue;
			}
			// max
			if (count >= (searchPage + MAX_SKILL_PER_PAGE))
			{
				continue;
			}
			
			int price = ((PlayerHolder) sellerBuff).getSellBuffPrice(sk.getId());
			
			hb.append("<tr>");
			hb.append("<td width=32 height=16><img src=\"", SkillInfoData.getSkillIcon(sk.getId()), "\" width=32 height=25></td>");
			hb.append("<td width=180 align=center height=16><a action=\"bypass -h Engine SellBuffs buy ", sk.getId(), " ", sk.getLevel(), " ", ((PlayerHolder) sellerBuff).getName(), " ", page, "\">", sk.getName(), " (", price, ")</td>");
			hb.append("<td width=32 height=16><img src=\"", SkillInfoData.getSkillIcon(sk.getId()), "\" width=32 height=16></td>");
			hb.append("</tr>");
		}
		
		hb.append("</table>");
		hb.append("<table bgcolor=000000>");
		hb.append("<tr>");
		
		// Generate page system
		int currentPage = 1;
		
		for (int i = 0; i < count; i++)
		{
			if ((i % MAX_SKILL_PER_PAGE) == 0)
			{
				hb.append("<td width=18 align=center><a action=\"bypass -h Engine SellBuffs view " + currentPage + "\">" + currentPage + "</a></td>");
				currentPage++;
			}
		}
		
		hb.append("</tr>");
		hb.append("</table>");
		hb.append("<img src=L2UI.SquareGray width=264 height=1>");
		hb.append("</center>");
		
		hb.append(Html.END);
		
		sendHtml(null, hb, ph);
	}
	
	/**
	 * Check if skill can use in sell buff system.
	 * @param  sk
	 * @return
	 */
	private static boolean checkSkil(L2Skill sk)
	{
		if (sk.isPassive() || sk.isOffensive() || (sk.getSkillType() != SkillType.BUFF))
		{
			return false;
		}
		
		switch (sk.getTargetType())
		{
			case TARGET_SELF:
			case TARGET_PET:
			case TARGET_CORPSE:
			case TARGET_CORPSE_PLAYER:
			case TARGET_CORPSE_PET:
				return false;
		}
		
		return true;
	}
}
