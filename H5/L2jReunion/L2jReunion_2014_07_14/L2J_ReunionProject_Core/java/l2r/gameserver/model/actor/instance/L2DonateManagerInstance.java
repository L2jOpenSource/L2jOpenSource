package l2r.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import l2r.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.datatables.sql.CharNameTable;
import l2r.gameserver.datatables.sql.ClanTable;
import l2r.gameserver.datatables.xml.MultiSell;
import l2r.gameserver.datatables.xml.SkillData;
import l2r.gameserver.datatables.xml.TransformData;
import l2r.gameserver.enums.InstanceType;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.model.actor.FakePc;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.PartySmallWindowAll;
import l2r.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import l2r.gameserver.util.Util;
import gr.reunion.aioItem.runnable.TransformFinalizer;
import gr.reunion.configsEngine.DonateManagerConfigs;
import gr.reunion.donateEngine.DonateHandler;
import gr.reunion.imageGeneratorEngine.GenerateLogos;
import gr.reunion.main.Conditions;
import gr.reunion.securityEngine.SecurityActions;
import gr.reunion.securityEngine.SecurityType;

public class L2DonateManagerInstance extends L2Npc
{
	/**
	 * @param objectId
	 * @param template
	 */
	public L2DonateManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2DonateManagerInstance);
		FakePc fpc = getFakePc();
		if (fpc != null)
		{
			setTitle(fpc.title);
		}
	}
	
	// Variable Section
	// Global Variable Item ID To Check or Destroy
	private static int itemIdToGet;
	private static int price;
	
	/**
	 * Method to send the html to char
	 * @param player
	 * @param html
	 */
	public void sendPacket(L2PcInstance player, String html)
	{
		NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
		msg.setFile(player.getHtmlPrefix(), "/data/html/DonateManager/" + html);
		msg.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(msg);
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getHtmlPrefix(), "data/html/DonateManager/main.htm");
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
	
	@Override
	public void onBypassFeedback(final L2PcInstance player, String command)
	{
		final String[] subCommand = command.split("_");
		
		// No null pointers
		if (player == null)
		{
			return;
		}
		
		// Restrictions Section
		if (!Conditions.checkPlayerBasicConditions(player))
		{
			return;
		}
		
		// Page navigation, html command how to starts
		if (command.startsWith("Chat"))
		{
			if (subCommand[1].isEmpty() || (subCommand[1] == null))
			{
				return;
			}
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(player.getHtmlPrefix(), "data/html/DonateManager/" + subCommand[1]);
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
		}
		
		// Add all clan skills
		else if (command.startsWith("givefullclan"))
		{
			itemIdToGet = DonateManagerConfigs.GET_FULL_CLAN_COIN;
			price = DonateManagerConfigs.GET_FULL_CLAN_PRICE;
			
			if ((player.getClan() == null) || (!player.isClanLeader()))
			{
				player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			
			if (!Conditions.checkPlayerItemCount(player, itemIdToGet, price))
			{
				return;
			}
			
			player.destroyItemByItemId("Clan donate", itemIdToGet, price, player, true);
			player.getClan().changeLevel(11);
			player.sendMessage("Clan level set to 11.");
			player.getClan().addReputationScore(DonateManagerConfigs.REPUTATION_POINTS_TO_ADD, true);
			player.getClan().addNewSkill(SkillData.getInstance().getInfo(391, 1));
			for (int mainSkill : DonateManagerConfigs.CLAN_MAIN_SKILLS)
			{
				player.getClan().addNewSkill(SkillData.getInstance().getInfo(mainSkill, 3));
			}
			for (int squadSkill : DonateManagerConfigs.CLAN_SQUAD_SKILLS)
			{
				player.getClan().addNewSkill(SkillData.getInstance().getInfo(squadSkill, 3), 0);
			}
			player.sendMessage("You have successfully perform this action.");
		}
		// Change player name
		else if (command.startsWith("changename"))
		{
			try
			{
				itemIdToGet = DonateManagerConfigs.CHANGE_NAME_COIN;
				price = DonateManagerConfigs.CHANGE_NAME_PRICE;
				String val = command.substring(11);
				// TODO: Need More checks?
				if (!Util.isAlphaNumeric(val))
				{
					player.sendMessage("Invalid character name.");
					return;
				}
				
				if (!Conditions.checkPlayerItemCount(player, itemIdToGet, price))
				{
					return;
				}
				
				if (CharNameTable.getInstance().getIdByName(val) > 0)
				{
					player.sendMessage("Warning, name " + val + " already exists.");
					return;
				}
				player.destroyItemByItemId("Name Change", itemIdToGet, price, player, true);
				player.setName(val);
				player.getAppearance().setVisibleName(val);
				player.store();
				
				player.sendMessage("Your name has been changed to " + val);
				player.broadcastUserInfo();
				
				if (player.isInParty())
				{
					// Delete party window for other party members
					player.getParty().broadcastToPartyMembers(player, new PartySmallWindowDeleteAll());
					for (L2PcInstance member : player.getParty().getMembers())
					{
						// And re-add
						if (member != player)
						{
							member.sendPacket(new PartySmallWindowAll(member, player.getParty()));
						}
					}
				}
				if (player.getClan() != null)
				{
					player.getClan().broadcastClanStatus();
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				// Case of empty character name
				player.sendMessage("Player name box cannot be empty.");
			}
		}
		// Change clan name
		else if (command.startsWith("changeclanname"))
		{
			try
			{
				itemIdToGet = DonateManagerConfigs.CHANGE_CNAME_COIN;
				price = DonateManagerConfigs.CHANGE_CNAME_PRICE;
				String val = command.substring(15);
				// TODO: Need More checks?
				if ((player.getClan() == null) || (!player.isClanLeader()))
				{
					player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
					return;
				}
				if (!Util.isAlphaNumeric(val))
				{
					player.sendPacket(SystemMessageId.CLAN_NAME_INCORRECT);
					return;
				}
				
				if (!Conditions.checkPlayerItemCount(player, itemIdToGet, price))
				{
					return;
				}
				
				if (ClanTable.getInstance().getClanByName(val) != null)
				{
					player.sendMessage("Warning, clan name " + val + " already exists.");
					return;
				}
				
				player.destroyItemByItemId("Clan Name Change", itemIdToGet, price, player, true);
				player.getClan().setName(val);
				player.getClan().updateClanNameInDB();
				player.sendMessage("Your clan name has been changed to " + val);
				player.broadcastUserInfo();
				
				if (player.isInParty())
				{
					// Delete party window for other party members
					player.getParty().broadcastToPartyMembers(player, new PartySmallWindowDeleteAll());
					for (L2PcInstance member : player.getParty().getMembers())
					{
						// And re-add
						if (member != player)
						{
							member.sendPacket(new PartySmallWindowAll(member, player.getParty()));
						}
					}
				}
				if (player.getClan() != null)
				{
					player.getClan().broadcastClanStatus();
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				// Case of empty character name
				player.sendMessage("Clan name box cannot be empty.");
			}
		}
		// Change gender options
		else if (command.startsWith("changeGender"))
		{
			if (command.startsWith("changeGenderDonate"))
			{
				itemIdToGet = DonateManagerConfigs.CHANGE_GENDER_DONATE_COIN;
				price = DonateManagerConfigs.CHANGE_GENDER_DONATE_PRICE;
			}
			
			if (!Conditions.checkPlayerItemCount(player, itemIdToGet, price))
			{
				return;
			}
			
			player.destroyItemByItemId("changeGender", itemIdToGet, price, player, true);
			player.getAppearance().setSex(player.getAppearance().getSex() ? false : true);
			player.sendMessage("Your gender has been changed.");
			player.broadcastUserInfo();
			// Transform-untransorm player quickly to force the client to reload the character textures
			TransformData.getInstance().transformPlayer(105, player);
			TransformFinalizer ef = new TransformFinalizer(player);
			player.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(ef, 200));
		}
		// GM Shop
		else if (command.startsWith("showMultiSellWindow"))
		{
			try
			{
				int multi = Integer.valueOf(subCommand[1]);
				
				if (DonateManagerConfigs.MULTISELL_LIST.contains(multi))
				{
					player.setIsUsingAioMultisell(true);
					MultiSell.getInstance().separateAndSend(multi, player, null, false);
				}
				else
				{
					SecurityActions.startSecurity(player, SecurityType.DONATE_MANAGER);
				}
			}
			catch (Exception e)
			{
				SecurityActions.startSecurity(player, SecurityType.DONATE_MANAGER);
			}
		}
		// Donate generate captcha code
		else if (command.startsWith("donateFormMain"))
		{
			NpcHtmlMessage playerReply = new NpcHtmlMessage(5);
			
			// Random image file name
			int imgId = IdFactory.getInstance().getNextId();
			// Conversion from .png to .dds, and crest packed send
			GenerateLogos.getInstance().captchaLogo(player, imgId);
			playerReply.setHtml("<html><body><title>Donate Manager</title><center><br><img src=\"l2ui.SquareGray\" width=270 height=1><br1><table width=\"262\" cellpadding=\"5\" bgcolor=\"151515\"><tr><td valign=\"top\"><center><font color=\"EBDF6C\">L2 Reunion</font> donate manager<br>Support our server by donating and receive special coins! Exchange here your donated coins for staff or services.</center></td></tr></table><br1><img src=\"l2ui.SquareGray\" width=270 height=1><br></center><br><center><img src=\"L2UI.SquareGray\" width=270 height=1><br><center><font color=\"EBDF6C\">Enter the 5-digits numbers to continue.</font></center><br><img src=\"Crest.crest_" + Config.SERVER_ID + "_" + imgId + "\" width=256 height=64>" + "<br><font color=\"888888\">(There are only numbers.)</font>" + "<br><edit var=\"captcha\" width=110><br><button value=\"Confirm\" action=\"bypass -h npc_%objectId%_confirmDonateCode $captcha\" width=80 height=26 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></center><br><img src=\"l2ui.SquareGray\" width=270 height=1></body></html>");
			playerReply.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(playerReply);
			player.setDonateCode(GenerateLogos.getInstance().getFinalString());
			GenerateLogos.getInstance().getFinalString().replace(0, 5, "");
			return;
		}
		// Donate captcha code
		else if (command.startsWith("confirmDonateCode"))
		{
			String value = command.substring(17);
			StringTokenizer st = new StringTokenizer(value, " ");
			try
			{
				String newpass = null, repeatnewpass = null;
				if (st.hasMoreTokens())
				{
					newpass = st.nextToken();
				}
				repeatnewpass = player.getDonateCode();
				
				if (!((newpass == null) || (repeatnewpass == null)))
				{
					if (newpass.equals(repeatnewpass)) // Right:)
					{
						sendPacket(player, "donateform.htm");
						return;
					}
				}
				if ((newpass == null) || !newpass.equals(repeatnewpass))
				{
					player.sendMessage("Incorrect captcha code try again.");
				}
			}
			catch (Exception e)
			{
				player.sendMessage("A problem occured while adding captcha!");
				_log.warn(String.valueOf(e));
			}
		}
		// Donate form
		else if (command.startsWith("sendDonateForm"))
		{
			DonateHandler.sendDonateForm(player, command);
		}
	}
}