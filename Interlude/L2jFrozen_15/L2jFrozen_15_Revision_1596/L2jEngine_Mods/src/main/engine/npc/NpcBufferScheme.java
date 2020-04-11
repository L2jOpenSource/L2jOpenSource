package main.engine.npc;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.actor.instance.L2CubicInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.model.actor.stat.PcStat;
import com.l2jfrozen.gameserver.model.actor.stat.SummonStat;
import com.l2jfrozen.gameserver.model.actor.status.PcStatus;
import com.l2jfrozen.gameserver.model.actor.status.SummonStatus;
import com.l2jfrozen.gameserver.network.serverpackets.SetSummonRemainTime;
import com.l2jfrozen.gameserver.network.serverpackets.SetupGauge;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

import main.data.xml.SchemeBufferPredefinedData;
import main.data.xml.SkillInfoData;
import main.engine.AbstractMod;
import main.enums.BuffType;
import main.holders.BuffHolder;
import main.holders.objects.CharacterHolder;
import main.holders.objects.NpcHolder;
import main.holders.objects.PlayerHolder;
import main.util.Util;
import main.util.UtilInventory;
import main.util.builders.html.Html;
import main.util.builders.html.HtmlBuilder;
import main.util.builders.html.HtmlBuilder.HtmlType;
import main.util.builders.html.L2UI;
import main.util.builders.html.L2UI_CH3;

/**
 * Adaptation of the buffer RIn4
 * @author fissban
 */
public class NpcBufferScheme extends AbstractMod
{
	private static final int NPC_ID = 60012;
	
	private static final String TITLE_NAME = "Buffer";
	
	private static final boolean FREE_BUFFS = true;
	
	// precio de usar cada buff, sea del tipo q sea.
	private static final int BUFF_PRICE = 1000;
	
	// precio de los buffs pre-establecidos
	private static final int BUFF_SET_PRICE = 1;
	// item a cobrar
	private static final int CONSUMABLE_ID = 57;
	
	private static final boolean TIME_OUT = true;
	// tiempo entre cada accion...crear scheme,bufearse,etc en segundos
	private static final int TIME_OUT_TIME = 1;
	// minimo nievel para usar el buffer
	private static final int MIN_LEVEL = 0;
	private static final int BUFF_REMOVE_PRICE = 1000;
	private static final int SCHEME_BUFF_PRICE = 1000;
	private static final int SCHEMES_PER_PLAYER = 3;
	
	// maximo de buffs que se pueden agregar de cada tipo por scheme
	private static final int MAX_SCHEME_BUFFS = 24;
	private static final int MAX_SCHEME_DANCES = 12;
	
	// MISC
	private static final String SCHEME_NAME = "schemeName";
	private static final String BLOCK_UNTIL_TIME = "blockUntilTime";
	
	public NpcBufferScheme()
	{
		registerMod(true);
	}
	
	@Override
	public void onModState()
	{
		switch (getState())
		{
			case START:
				loadValuesFromDb();
				break;
			case END:
				//
				break;
		}
	}
	
	@Override
	public void onEvent(PlayerHolder ph, CharacterHolder npc, String command)
	{
		if (!Util.areObjectType(L2NpcInstance.class, npc))
		{
			return;
		}
		
		if (((NpcHolder) npc).getId() != NPC_ID)
		{
			return;
		}
		
		StringTokenizer st = new StringTokenizer(command, " ");
		String bypass = st.hasMoreTokens() ? st.nextToken() : "redirect_main";
		String eventParam1 = st.hasMoreTokens() ? st.nextToken() : "";
		String eventParam2 = st.hasMoreTokens() ? st.nextToken() : "";
		String eventParam3 = st.hasMoreTokens() ? st.nextToken() : "";
		String eventParam4 = st.hasMoreTokens() ? st.nextToken() : "";
		
		switch (bypass)
		{
			case "reloadscript":
			{
				if (eventParam1.equals("0"))
				{
					rebuildMainHtml(ph);
					return;
				}
			}
			case "redirect_main":
				rebuildMainHtml(ph);
				return;
			case "redirect_view_buff":
				buildHtml(ph, BuffType.BUFF, eventParam1.equals("") ? 1 : Integer.parseInt(eventParam1));
				return;
			case "redirect_view_resist":
				buildHtml(ph, BuffType.RESIST, eventParam1.equals("") ? 1 : Integer.parseInt(eventParam1));
				return;
			case "redirect_view_song":
				buildHtml(ph, BuffType.SONG, eventParam1.equals("") ? 1 : Integer.parseInt(eventParam1));
				return;
			case "redirect_view_dances":
				buildHtml(ph, BuffType.DANCE, eventParam1.equals("") ? 1 : Integer.parseInt(eventParam1));
				return;
			case "redirect_view_chants":
				buildHtml(ph, BuffType.CHANT, eventParam1.equals("") ? 1 : Integer.parseInt(eventParam1));
				return;
			case "redirect_view_other":
				buildHtml(ph, BuffType.OTHER, eventParam1.equals("") ? 1 : Integer.parseInt(eventParam1));
				return;
			case "redirect_view_special":
				buildHtml(ph, BuffType.SPECIAL, eventParam1.equals("") ? 1 : Integer.parseInt(eventParam1));
				return;
			case "redirect_view_cubic":
				buildHtml(ph, BuffType.CUBIC, eventParam1.equals("") ? 1 : Integer.parseInt(eventParam1));
				return;
			case "buffpet":
			{
				if (checkTimeOut(ph))
				{
					setValueDB(ph.getObjectId(), "Pet-On-Off", eventParam1);
					if (TIME_OUT)
					{
						addTimeout(ph, SetupGauge.GREEN, TIME_OUT_TIME / 2, 600);
					}
				}
				rebuildMainHtml(ph);
				return;
			}
			case "create":
			{
				// anti sql inject
				String name = eventParam1.replaceAll("[ !" + "\"" + "#$%&'()*+,/:;<=>?@" + "\\[" + "\\\\" + "\\]" + "\\^" + "`{|}~]", ""); // JOJO
				
				if ((name.length() == 0) || name.equals("no_name"))
				{
					// TODO missing INCORRECT_NAME_TRY_AGAIN
					ph.getInstance().sendPacket(new SystemMessage(204));
					showText(ph, "Info", "Please, enter the scheme name!", true, "Return", "main");
					return;
				}
				
				// get the list of schemes
				String allSchemes = getValueDB(ph.getObjectId(), SCHEME_NAME).getString();
				// the new name of the scheme is added
				if (allSchemes == null)
				{
					allSchemes = "";
				}
				else
				{
					// check if scheme name exist
					for (String s : allSchemes.split(","))
					{
						if ((s != null) && s.equals(name))
						{
							// TODO missing INCORRECT_NAME_TRY_AGAIN
							ph.getInstance().sendPacket(new SystemMessage(204));
							showText(ph, "Info", "The name you are trying to use is already in use!", true, "Return", "main");
							return;
						}
					}
				}
				
				allSchemes += name + ",";
				
				// the new listing is saved
				setValueDB(ph, SCHEME_NAME, allSchemes);
				
				rebuildMainHtml(ph);
				return;
			}
			case "delete":
			{
				// TODO missing code
				String schemeName = eventParam1;
				// the list of buffs is removed
				removeValueDB(ph.getObjectId(), schemeName);
				// the name of the scheme is removed from the list
				String schemes = getValueDB(ph.getObjectId(), SCHEME_NAME).getString();
				schemes = schemes.replace(schemeName + ",", "");
				
				// the new list of the names of the schemes is saved
				setValueDB(ph.getObjectId(), SCHEME_NAME, schemes);
				
				rebuildMainHtml(ph);
				return;
			}
			case "delete_c":
			{
				deleteSpecifiedScheme(ph, eventParam1);
				return;
			}
			case "create_1":
			{
				createScheme(ph);
				return;
			}
			case "edit_1":
			{
				editScheme(ph);
				return;
			}
			case "delete_1":
			{
				deleteScheme(ph);
				return;
			}
			case "manage_scheme_add":
			{
				viewAllSchemeBuffs(ph, eventParam1, eventParam2, "add");
				return;
			}
			case "manage_scheme_remove":
			{
				viewAllSchemeBuffs(ph, eventParam1, eventParam2, "remove");
				return;
			}
			case "manage_scheme_select":
			{
				getOptionList(ph, eventParam1);
				return;
			}
			case "remove_buff":
			{
				String[] split = eventParam1.split("_");
				String schemeNameRemove = split[0];
				String id = split[1];
				String level = split[2];
				// "DELETE FROM npcbuffer_scheme_contents WHERE scheme_id=? AND skill_id=? AND skill_level=? LIMIT 1"
				
				// obtenemos el scheme actual
				String listBuff = getValueDB(ph.getObjectId(), schemeNameRemove).getString();
				// agregamos el nuevo buff
				listBuff = listBuff.replaceFirst(id + "," + level + ";", "");
				// lo salvamos en la memoria y la db
				setValueDB(ph.getObjectId(), schemeNameRemove, listBuff);
				
				int temp = Integer.parseInt(eventParam3) - 1;
				
				if (temp <= 0)
				{
					getOptionList(ph, schemeNameRemove);
				}
				else
				{
					viewAllSchemeBuffs(ph, schemeNameRemove, eventParam2, "remove");
				}
				
				return;
			}
			case "add_buff":
			{
				String[] split = eventParam1.split("_");
				String schemeNameAdd = split[0];
				String id = split[1];
				String level = split[2];
				
				// "INSERT INTO npcbuffer_scheme_contents (scheme_id,skill_id,skill_level,buff_class) VALUES (?,?,?,?)"
				
				// obtenemos el scheme actual
				String listBuff = getValueDB(ph.getObjectId(), schemeNameAdd).getString();
				// agregamos el nuevo buff
				if (listBuff == null)
				{
					listBuff = id + "," + level + ";";
				}
				else
				{
					listBuff = listBuff.concat(id + "," + level + ";");
				}
				
				// lo salvamos en la memoria y la db
				setValueDB(ph.getObjectId(), schemeNameAdd, listBuff);
				
				int temp = Integer.parseInt(eventParam3) + 1;
				
				if (temp >= (MAX_SCHEME_BUFFS + MAX_SCHEME_DANCES))
				{
					getOptionList(ph, schemeNameAdd);
				}
				else
				{
					viewAllSchemeBuffs(ph, schemeNameAdd, eventParam2, "add");
				}
				return;
			}
			case "heal":
			{
				if (checkTimeOut(ph))
				{
					if (UtilInventory.getItemsCount(ph, CONSUMABLE_ID) < BUFF_PRICE)
					{
						showText(ph, "Sorry", "You don't have the enough items:<br>You need: <font color=\"LEVEL\">" + BUFF_PRICE + "</font> " + getItemNameHtml(CONSUMABLE_ID) + "!", false, "0", "0");
						return;
					}
					final boolean getPetbuff = isPetBuff(ph);
					if (getPetbuff)
					{
						if (ph.getInstance().getPet() != null)
						{
							heal(ph, getPetbuff);
						}
						else
						{
							showText(ph, "Info", "You can't use the Pet's options.<br>Summon your pet first!", false, "Return", "main");
							return;
						}
					}
					else
					{
						heal(ph, getPetbuff);
					}
					UtilInventory.takeItems(ph, CONSUMABLE_ID, BUFF_PRICE);
					if (TIME_OUT)
					{
						addTimeout(ph, SetupGauge.BLUE, TIME_OUT_TIME / 2, 600);
					}
				}
				rebuildMainHtml(ph);
				return;
			}
			case "removeBuffs":
			{
				if (checkTimeOut(ph))
				{
					if (UtilInventory.getItemsCount(ph, CONSUMABLE_ID) < BUFF_REMOVE_PRICE)
					{
						showText(ph, "Sorry", "You don't have the enough items:<br>You need: <font color=\"LEVEL\">" + BUFF_REMOVE_PRICE + "</font> " + getItemNameHtml(CONSUMABLE_ID) + "!", false, "0", "0");
						return;
					}
					final boolean getPetbuff = isPetBuff(ph);
					if (getPetbuff)
					{
						if (ph.getInstance().getPet() != null)
						{
							ph.getInstance().getPet().stopAllEffects();
						}
						else
						{
							showText(ph, "Info", "You can't use the Pet's options.<br>Summon your pet first!", false, "Return", "main");
							return;
						}
					}
					else
					{
						ph.getInstance().stopAllEffects();
						if (ph.getInstance().getCubics() != null)
						{
							for (L2CubicInstance cubic : ph.getInstance().getCubics().values())
							{
								cubic.stopAction();
								ph.getInstance().delCubic(cubic.getId());
							}
						}
					}
					UtilInventory.takeItems(ph, CONSUMABLE_ID, BUFF_REMOVE_PRICE);
					if (TIME_OUT)
					{
						addTimeout(ph, SetupGauge.RED, TIME_OUT_TIME / 2, 600);
					}
				}
				
				rebuildMainHtml(ph);
				return;
			}
			case "cast":
			{
				if (checkTimeOut(ph))
				{
					List<BuffHolder> buffs = new ArrayList<>();
					
					String shemeName = eventParam1;
					String buffList = getValueDB(ph.getObjectId(), shemeName).getString();
					
					if (buffList != null)
					{
						for (String buff : buffList.split(";"))
						{
							int id = Integer.parseInt(buff.split(",")[0]);
							int level = Integer.parseInt(buff.split(",")[1]);
							
							if (isEnabled(id, level))
							{
								buffs.add(new BuffHolder(id, level));
							}
						}
					}
					
					if (buffs.isEmpty())
					{
						viewAllSchemeBuffs(ph, eventParam1, "1", "add");
						return;
					}
					if (!FREE_BUFFS)
					{
						if (UtilInventory.getItemsCount(ph, CONSUMABLE_ID) < SCHEME_BUFF_PRICE)
						{
							showText(ph, "Sorry", "You don't have the enough items:<br>You need: <font color=\"LEVEL\">" + SCHEME_BUFF_PRICE + "</font> " + getItemNameHtml(CONSUMABLE_ID) + "!", false, "0", "0");
							return;
						}
					}
					
					boolean getPetbuff = isPetBuff(ph);
					
					for (BuffHolder bh : buffs)
					{
						if (!getPetbuff)
						{
							SkillTable.getInstance().getInfo(bh.getId(), bh.getLevel()).getEffects(ph.getInstance(), ph.getInstance());
						}
						else
						{
							if (ph.getInstance().getPet() != null)
							{
								SkillTable.getInstance().getInfo(bh.getId(), bh.getLevel()).getEffects(ph.getInstance().getPet(), ph.getInstance().getPet());
							}
							else
							{
								showText(ph, "Info", "You can't use the Pet's options.<br>Summon your pet first!", false, "Return", "main");
								return;
							}
						}
					}
					UtilInventory.takeItems(ph, CONSUMABLE_ID, SCHEME_BUFF_PRICE);
					if (TIME_OUT)
					{
						addTimeout(ph, SetupGauge.CYAN, TIME_OUT_TIME, 600);
					}
				}
				rebuildMainHtml(ph);
				return;
			}
			case "giveBuffs":
			{
				int cost = BUFF_PRICE;
				
				int id = Integer.parseInt(eventParam1);
				int level = Integer.parseInt(eventParam2);
				if (!isEnabled(id, level))
				{
					// posible bypass
					System.out.println("posible bypass en scheme buff -> " + ph.getName());
					return;
				}
				
				if (checkTimeOut(ph))
				{
					if (!FREE_BUFFS)
					{
						if (UtilInventory.getItemsCount(ph, CONSUMABLE_ID) < cost)
						{
							showText(ph, "Sorry", "You don't have the enough items:<br>You need: <font color=\"LEVEL\">" + cost + "</font> " + getItemNameHtml(CONSUMABLE_ID) + "!", false, "0", "0");
							return;
						}
					}
					
					boolean getPetbuff = isPetBuff(ph);
					if (!getPetbuff)
					{
						if (eventParam3.equals("CUBIC"))
						{
							if (!ph.getInstance().getCubics().isEmpty())
							{
								for (L2CubicInstance cubic : ph.getInstance().getCubics().values())
								{
									cubic.stopAction();
									ph.getInstance().delCubic(cubic.getId());
								}
							}
						}
					}
					else
					{
						if (eventParam3.equals("CUBIC"))
						{
							if (!ph.getInstance().getCubics().isEmpty())
							{
								for (L2CubicInstance cubic : ph.getInstance().getCubics().values())
								{
									cubic.stopAction();
									ph.getInstance().delCubic(cubic.getId());
								}
							}
						}
						else
						{
							if (ph.getInstance().getPet() == null)
							{
								showText(ph, "Info", "You can't use the Pet's options.<br>Summon your pet first!", false, "Return", "main");
								return;
							}
						}
					}
					
					L2Skill skill = SkillTable.getInstance().getInfo(id, level);
					
					if (getPetbuff)
					{
						if (ph.getInstance().getPet() == null)
						{
							showText(ph, "Info", "You can't use the Pet's options.<br>Summon your pet first!", false, "Return", "main");
							return;
						}
						skill.getEffects(ph.getInstance().getPet(), ph.getInstance().getPet());
					}
					else
					{
						skill.getEffects(ph.getInstance(), ph.getInstance());
					}
					
					UtilInventory.takeItems(ph, CONSUMABLE_ID, cost);
					if (TIME_OUT)
					{
						addTimeout(ph, SetupGauge.CYAN, TIME_OUT_TIME / 10, 600);
					}
				}
				buildHtml(ph, BuffType.valueOf(eventParam3), eventParam4.equals("") ? 1 : Integer.parseInt(eventParam4));
				return;
			}
			case "castBuffSet":
			{
				if (checkTimeOut(ph))
				{
					if (!FREE_BUFFS)
					{
						if (UtilInventory.getItemsCount(ph, CONSUMABLE_ID) < BUFF_SET_PRICE)
						{
							showText(ph, "Sorry", "You don't have the enough items:<br>You need: <font color=\"LEVEL\">" + BUFF_SET_PRICE + " " + getItemNameHtml(CONSUMABLE_ID) + "</font>!", false, "0", "0");
							return;
						}
					}
					
					boolean getPetbuff = isPetBuff(ph);
					if (!getPetbuff)
					{
						for (BuffHolder bh : ph.getInstance().isMageClass() ? SchemeBufferPredefinedData.getAllMageBuffs() : SchemeBufferPredefinedData.getAllWarriorBuffs())
						{
							SkillTable.getInstance().getInfo(bh.getId(), bh.getLevel()).getEffects(ph.getInstance(), ph.getInstance());
						}
					}
					else
					{
						if (ph.getInstance().getPet() != null)
						{
							// a los pets le daremos los mismos buff que a los guerreros
							for (BuffHolder bh : SchemeBufferPredefinedData.getAllWarriorBuffs())
							{
								SkillTable.getInstance().getInfo(bh.getId(), bh.getLevel()).getEffects(ph.getInstance().getPet(), ph.getInstance().getPet());
							}
						}
						else
						{
							showText(ph, "Info", "You can't use the Pet's options.<br>Summon your pet first!", false, "Return", "main");
							return;
						}
					}
					UtilInventory.takeItems(ph, CONSUMABLE_ID, BUFF_SET_PRICE);
					if (TIME_OUT)
					{
						addTimeout(ph, SetupGauge.CYAN, TIME_OUT_TIME, 600);
					}
				}
				rebuildMainHtml(ph);
				return;
			}
			
		}
		rebuildMainHtml(ph);
	}
	
	@Override
	public boolean onInteract(PlayerHolder ph, CharacterHolder npc)
	{
		if (!Util.areObjectType(L2NpcInstance.class, npc))
		{
			return false;
		}
		
		if (((NpcHolder) npc).getId() != NPC_ID)
		{
			return false;
		}
		
		if (ph.getInstance().isGM())
		{
			rebuildMainHtml(ph);
			return true;
		}
		
		if (checkTimeOut(ph))
		{
			// Sacamos restriccion de vip
			if (ph.getInstance().getLevel() < MIN_LEVEL)
			{
				showText(ph, "Info", "Your level is too low!<br>You have to be at least level <font color=\"LEVEL\">" + MIN_LEVEL + "</font>,<br>to use my services!", false, "Return", "main");
				return true;
			}
			else if (ph.getInstance().isInCombat())
			{
				showText(ph, "Info", "You can't buff while you are attacking!<br>Stop your fight and try again!", false, "Return", "main");
				return true;
			}
			// return showText(st, "Sorry", "You have to wait a while!<br>if you wish to use my services!", false, "Return", "main");
		}
		rebuildMainHtml(ph);
		return true;
	}
	
	private static String getSkillIconHtml(int id, int level)
	{
		String iconNumber = SkillInfoData.getSkillIcon(id);
		return "<button action=\"bypass -h Engine NpcBufferScheme description " + id + " " + level + " x\" width=32 height=32 back=\"" + iconNumber + "\" fore=\"" + iconNumber + "\">";
	}
	
	private boolean checkTimeOut(PlayerHolder ph)
	{
		String blockUntilTime = getValueDB(ph.getObjectId(), BLOCK_UNTIL_TIME).getString();
		if ((blockUntilTime == null) || ((int) (System.currentTimeMillis() / 1000) > Integer.parseInt(blockUntilTime)))
		{
			return true;
		}
		
		return false;
	}
	
	private void addTimeout(PlayerHolder ph, int gauge, int amount, int offset)
	{
		int endtime = (int) ((System.currentTimeMillis() + (amount * 1000)) / 1000);
		setValueDB(ph.getObjectId(), BLOCK_UNTIL_TIME, String.valueOf(endtime));
		ph.getInstance().sendPacket(new SetupGauge(gauge, (amount * 1000) + offset));
	}
	
	private static String getItemNameHtml(int itemId)
	{
		return "&#" + itemId + ";";
	}
	
	private static void heal(PlayerHolder ph, boolean isPet)
	{
		L2Summon target = ph.getInstance().getPet();
		if (!isPet)
		{
			PcStatus pcStatus = ph.getInstance().getStatus();
			PcStat pcStat = ph.getInstance().getStat();
			pcStatus.setCurrentHp(pcStat.getMaxHp());
			pcStatus.setCurrentMp(pcStat.getMaxMp());
			pcStatus.setCurrentCp(pcStat.getMaxCp());
		}
		else if (target != null)
		{
			SummonStatus petStatus = target.getStatus();
			SummonStat petStat = target.getStat();
			petStatus.setCurrentHp(petStat.getMaxHp());
			petStatus.setCurrentMp(petStat.getMaxMp());
			if (target instanceof L2PetInstance)
			{
				L2PetInstance pet = (L2PetInstance) target;
				pet.setCurrentFed(pet.getPetData().getPetMaxFeed());
				ph.getInstance().sendPacket(new SetSummonRemainTime(pet.getPetData().getPetMaxFeed(), pet.getCurrentFed()));
			}
		}
	}
	
	private void rebuildMainHtml(PlayerHolder player)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
		hb.append(Html.START);
		hb.append(Html.head("BUFFER"));
		hb.append("<br>");
		
		hb.append("<center>");
		
		hb.append(Html.image(L2UI.SquareWhite, 264, 1));
		hb.append("<table width=275 border=0 cellspacing=0 cellpadding=1 bgcolor=\"000000\">");
		hb.append("<tr>");
		hb.append("<td align=center><font color=\"FFFF00\">Buffs:</font></td>");
		hb.append("</tr>");
		hb.append("</table>");
		hb.append(Html.image(L2UI.SquareWhite, 264, 1));
		
		hb.append("<br>");
		
		final String bottonA, bottonB, bottonC;
		if (isPetBuff(player))
		{
			bottonA = "Auto Buff Pet";
			bottonB = "Heal My Pet";
			bottonC = "Remove Buffs";
			hb.append("<button value=\"Pet Options\" action=\"bypass -h Engine NpcBufferScheme buffpet 0\" width=75 height=21  back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, ">");
		}
		else
		{
			bottonA = "Auto Buff";
			bottonB = "Heal";
			bottonC = "Remove Buffs";
			hb.append("<button value=\"Char Options\" action=\"bypass -h Engine NpcBufferScheme buffpet 1\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, ">");
		}
		
		hb.append("<table width=80% cellspacing=0 cellpadding=1>");
		hb.append("<tr>");
		hb.append("<td height=32 align=center><button value=Buffs action=\"bypass -h Engine NpcBufferScheme redirect_view_buff\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
		hb.append("<td height=32 align=center><button value=Resist action=\"bypass -h Engine NpcBufferScheme redirect_view_resist\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
		hb.append("</tr>");
		
		hb.append("<tr>");
		hb.append("<td height=32 align=center><button value=Songs action=\"bypass -h Engine NpcBufferScheme redirect_view_song\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
		hb.append("<td height=32 align=center><button value=Dances action=\"bypass -h Engine NpcBufferScheme redirect_view_dances\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
		hb.append("</tr>");
		
		hb.append("<tr>");
		hb.append("<td height=32 align=center><button value=Chants action=\"bypass -h Engine NpcBufferScheme redirect_view_chants\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
		hb.append("<td height=32 align=center><button value=Special action=\"bypass -h Engine NpcBufferScheme redirect_view_special\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
		hb.append("</tr>");
		
		hb.append("<tr>");
		hb.append("<td height=32 align=center><button value=Others action=\"bypass -h Engine NpcBufferScheme redirect_view_other\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
		hb.append("<td height=32 align=center><button value=Cubics action=\"bypass -h Engine NpcBufferScheme redirect_view_cubic\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
		hb.append("</tr>");
		hb.append("</table>");
		// ---------------------------------------------------------------------------------------------
		hb.append(Html.image(L2UI.SquareWhite, 264, 1));
		hb.append("<table width=275 border=0 cellspacing=0 cellpadding=1 bgcolor=\"000000\">");
		hb.append("<tr>");
		hb.append("<td align=center><font color=\"FFFF00\">Preset:</font></td>");
		hb.append("</tr>");
		hb.append("</table>");
		hb.append(Html.image(L2UI.SquareWhite, 264, 1));
		hb.append("<table width=100% height=37 border=0 cellspacing=0 cellpadding=5>");
		hb.append("<tr>");
		hb.append("<td><button value=\"", bottonA, "\" action=\"bypass -h Engine NpcBufferScheme castBuffSet 0 0 0\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
		hb.append("<td><button value=\"", bottonB, "\" action=\"bypass -h Engine NpcBufferScheme heal\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
		hb.append("<td><button value=\"", bottonC, "\" action=\"bypass -h Engine NpcBufferScheme removeBuffs 0 0 0\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
		hb.append("</tr>");
		hb.append("</table>");
		
		// generate html scheme
		hb.append(generateScheme(player));
		
		hb.append("<br>");
		hb.append("<font color=\"303030\">" + TITLE_NAME + "</font>");
		hb.append("</center>");
		hb.append(Html.END);
		
		sendHtml(null, hb, player);
	}
	
	private String generateScheme(PlayerHolder ph)
	{
		String schemeNames = getValueDB(ph.getObjectId(), SCHEME_NAME).getString();
		
		HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
		hb.append("<br1>");
		hb.append(Html.image(L2UI.SquareWhite, 264, 1));
		hb.append("<table width=271 bgcolor=\"000000\">");
		hb.append("<tr><td align=center><font color=\"FFFF00\">Scheme:</font></td></tr>");
		hb.append("</table>");
		hb.append(Html.image(L2UI.SquareWhite, 264, 1));
		
		hb.append("<br1>");
		
		// hb.append("<table cellspacing=0 cellpadding=5 height=28>");
		
		if ((schemeNames != null) && !schemeNames.isEmpty())
		{
			String[] TRS =
			{
				"<tr><td>",
				"</td>",
				"<td>",
				"</td></tr>"
			};
			
			hb.append("<table>");
			int td = 0;
			for (int i = 0; i < schemeNames.split(",").length; ++i)
			{
				if (td > 2)
				{
					td = 0;
				}
				hb.append(TRS[td] + "<button value=\"", schemeNames.split(",")[i], "\" action=\"bypass -h Engine NpcBufferScheme cast ", schemeNames.split(",")[i], "\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, ">", TRS[td + 1]);
				td += 2;
			}
			
			hb.append("</table>");
		}
		
		if ((schemeNames == null) || schemeNames.isEmpty() || (schemeNames.split(",").length < SCHEMES_PER_PLAYER))
		{
			hb.append("<br1><table><tr><td><button value=Create action=\"bypass -h Engine NpcBufferScheme create_1\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
		}
		else
		{
			hb.append("<br1><table width=100><tr>");
		}
		
		if ((schemeNames != null) && !schemeNames.isEmpty())
		{
			hb.append("<td><button value=Edit action=\"bypass -h Engine NpcBufferScheme edit_1\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td><td><button value=\"Delete\" action=\"bypass -h Engine NpcBufferScheme delete_1\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td></tr></table>");
		}
		else
		{
			hb.append("</tr></table>");
		}
		return hb.toString();
	}
	
	/**
	 * Chequeamos la cantidad de buffs q tiene un player en su scheme.
	 * @param  ph
	 * @param  schemeName
	 * @return
	 */
	private int getBuffCount(PlayerHolder ph, String schemeName)
	{
		String buffList = getValueDB(ph.getObjectId(), schemeName).getString();
		if (buffList != null)
		{
			return buffList.split(";").length;
		}
		
		return 0;
	}
	
	/**
	 * Chequeamos si el buff aun esta en nuestro listado de buff habilitados.
	 * @param  id
	 * @param  level
	 * @return
	 */
	private static boolean isEnabled(int id, int level)
	{
		for (BuffHolder bh : SchemeBufferPredefinedData.getAllGeneralBuffs())
		{
			if ((bh.getId() == id) && (bh.getLevel() == level))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Chequeamos si el player tiene un determinado buff en su scheme o no.
	 * @param  ph
	 * @param  scheme
	 * @param  id
	 * @param  level
	 * @return
	 */
	private boolean isUsed(PlayerHolder ph, String scheme, int id, int level)
	{
		String buffList = getValueDB(ph.getObjectId(), scheme).getString();
		
		if (buffList == null)
		{
			return false;
		}
		
		for (String buff : buffList.split(";"))
		{
			if ((Integer.parseInt(buff.split(",")[0]) == id) && (Integer.parseInt(buff.split(",")[1]) == level))
			{
				return true;
			}
		}
		
		return false;
	}
	
	private static void showText(PlayerHolder ph, String type, String text, boolean buttonEnabled, String buttonName, String location)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
		hb.append(Html.START);
		hb.append(Html.head("BUFFER"));
		hb.append("<center>");
		hb.append("<br>");
		hb.append("<font color=\"LEVEL\">", type, "</font>");
		hb.append("<br>", text, "<br>");
		if (buttonEnabled)
		{
			hb.append("<button value=\"" + buttonName + "\" action=\"bypass -h Engine NpcBufferScheme redirect_", location, " 0 0\" width=75 height=21  back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, ">");
		}
		hb.append("<font color=\"303030\">", TITLE_NAME, "</font></center>");
		hb.append(Html.END);
		sendHtml(null, hb, ph);
	}
	
	/**
	 * Chequeamos si el player selecciono la opcion de pet o char para bufear
	 * @param  ph
	 * @return
	 */
	private boolean isPetBuff(PlayerHolder ph)
	{
		String pettBuff = getValueDB(ph.getObjectId(), "Pet-On-Off").getString();
		return (pettBuff != null) && pettBuff.equals("1");
	}
	
	private static void deleteSpecifiedScheme(PlayerHolder ph, String eventParam1)
	{
		HtmlBuilder hb = new HtmlBuilder();
		hb.append(Html.START);
		hb.append(Html.head("BUFFER"));
		hb.append("<center>");
		hb.append("<br>Do you really want to delete '" + eventParam1 + "' scheme?<br><br>");
		hb.append("<button value=\"Yes\" action=\"bypass -h Engine NpcBufferScheme delete " + eventParam1 + "\" width=75 height=21 back=" + L2UI_CH3.Btn1_normalOn + " fore=" + L2UI_CH3.Btn1_normal + ">");
		hb.append("<button value=\"No\" action=\"bypass -h Engine NpcBufferScheme delete_1\" width=75 height=21 back=" + L2UI_CH3.Btn1_normalOn + " fore=" + L2UI_CH3.Btn1_normal + "><br>");
		hb.append("<font color=\"303030\">" + TITLE_NAME + "</font></center>");
		hb.append(Html.END);
		
		sendHtml(null, hb, ph);
	}
	
	/**
	 * Mini menu para los scheme
	 * @return
	 */
	private static void createScheme(PlayerHolder ph)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
		hb.append(Html.START);
		hb.append(Html.head("BUFFER"));
		hb.append("<center>");
		hb.append("<br><br>");
		hb.append("You MUST seprerate new words with a dot (.)");
		hb.append("<br><br>");
		hb.append("Scheme name: <edit var=\"name\" width=100>");
		hb.append("<br><br>");
		hb.append("<button value=\"Create Scheme\" action=\"bypass -h Engine NpcBufferScheme create $name no_name\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, ">");
		hb.append("<br>");
		hb.append("<button value=\"Back\" action=\"bypass -h Engine NpcBufferScheme redirect_main\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, ">");
		hb.append("<br>");
		hb.append("<font color=\"303030\">", TITLE_NAME, "</font>");
		hb.append("</center>");
		hb.append(Html.END);
		sendHtml(null, hb, ph);
	}
	
	private void deleteScheme(PlayerHolder ph)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
		hb.append(Html.START);
		hb.append(Html.head("BUFFER"));
		hb.append("<center>");
		hb.append("<br>Available schemes:<br><br>");
		
		// XXX "SELECT * FROM npcbuffer_scheme_list WHERE player_id=?"
		String schemeNames = getValueDB(ph.getObjectId(), SCHEME_NAME).getString();
		for (String scheme : schemeNames.split(","))
		{
			hb.append("<button value=\"", scheme, "\" action=\"bypass -h Engine NpcBufferScheme delete_c ", scheme, " x\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, ">");
		}
		
		hb.append("<br>");
		hb.append("<button value=\"Back\" action=\"bypass -h Engine NpcBufferScheme redirect_main\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, ">");
		hb.append("<br>");
		hb.append("<font color=\"303030\">" + TITLE_NAME + "</font>");
		hb.append("</center>");
		hb.append(Html.END);
		
		sendHtml(null, hb, ph);
	}
	
	private void editScheme(PlayerHolder ph)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
		hb.append(Html.START);
		hb.append(Html.head("BUFFER"));
		hb.append("<center>");
		hb.append("<br>Select a scheme that you would like to manage:<br><br>");
		
		// XXX"SELECT * FROM npcbuffer_scheme_list WHERE player_id=?"
		
		String schemeNames = getValueDB(ph.getObjectId(), SCHEME_NAME).getString();
		for (String scheme : schemeNames.split(","))
		{
			hb.append("<button value=\"" + scheme + "\" action=\"bypass -h Engine NpcBufferScheme manage_scheme_select " + scheme + "\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, ">");
		}
		
		hb.append("<br>");
		hb.append("<button value=\"Back\" action=\"bypass -h Engine NpcBufferScheme redirect_main\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, ">");
		hb.append("<br>");
		hb.append("<font color=\"303030\">" + TITLE_NAME + "</font>");
		hb.append("</center>");
		hb.append(Html.END);
		
		sendHtml(null, hb, ph);
	}
	
	private void getOptionList(PlayerHolder ph, String scheme)
	{
		int bcount = getBuffCount(ph, scheme);
		
		HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
		hb.append(Html.START);
		hb.append(Html.head("BUFFER"));
		hb.append("<center>");
		hb.append("<br>There are ", Html.fontColor("LEVEL", bcount), " buffs in current scheme!<br><br>");
		
		if (bcount < (MAX_SCHEME_BUFFS + MAX_SCHEME_DANCES))
		{
			hb.append("<button value=\"Add buffs\" action=\"bypass -h Engine NpcBufferScheme manage_scheme_add ", scheme, " 1\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, ">");
		}
		if (bcount > 0)
		{
			hb.append("<button value=\"Remove buffs\" action=\"bypass -h Engine NpcBufferScheme manage_scheme_remove ", scheme, " 1\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, ">");
		}
		hb.append("<br>");
		hb.append("<button value=\"Back\" action=\"bypass -h Engine NpcBufferScheme edit_1\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, ">");
		hb.append("<button value=\"Home\" action=\"bypass -h Engine NpcBufferScheme redirect_main\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, ">");
		hb.append("<br>");
		hb.append(Html.fontColor("303030", TITLE_NAME));
		hb.append("</center>");
		hb.append(Html.END);
		
		sendHtml(null, hb, ph);
	}
	
	private static void buildHtml(PlayerHolder ph, BuffType buffType, int page)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
		hb.append(Html.START);
		hb.append("<center><br>");
		
		List<BuffHolder> buffs = new ArrayList<>();
		
		for (BuffHolder bh : SchemeBufferPredefinedData.getAllGeneralBuffs())
		{
			if (bh.getSkill() == null)
			{
				System.out.println("No existe el buffId: " + bh.getId() + ", remove from xml!");
			}
			else if (bh.getType() == buffType)
			{
				buffs.add(bh);
			}
		}
		
		if (buffs.size() == 0)
		{
			hb.append("No buffs are available at this moment!");
		}
		else
		{
			if (FREE_BUFFS)
			{
				hb.append("All buffs are for <font color=\"LEVEL\">free</font>!");
			}
			else
			{
				int price = BUFF_PRICE;
				
				hb.append("All special buffs cost <font color=\"LEVEL\">" + Html.formatAdena(price) + "</font> adena!");
			}
			hb.append("<br1>");
			
			int MAX_PER_PAGE = 10;
			int searchPage = MAX_PER_PAGE * (page - 1);
			int count = 0;
			
			hb.append(Html.image(L2UI.SquareWhite, 264, 1));
			for (BuffHolder bh : buffs)
			{
				// min
				if (count < searchPage)
				{
					count++;
					continue;
				}
				// max
				if (count >= (searchPage + MAX_PER_PAGE))
				{
					continue;
				}
				
				hb.append("<table width=264", (count % 2) == 0 ? " bgcolor=\"000000\">" : ">");
				String name = bh.getSkill().getName().replace("+", " ");
				hb.append("<tr>");
				hb.append("<td height=32 fixwidth=32>", getSkillIconHtml(bh.getId(), bh.getLevel()), "</td>");
				hb.append("<td height=32 fixwidth=232 align=center><a action=\"bypass -h Engine NpcBufferScheme giveBuffs ", bh.getId(), " ", bh.getLevel(), " ", buffType.name(), " ", page, "\">", name, "</a></td>");
				hb.append("<td height=32 fixwidth=32>", getSkillIconHtml(bh.getId(), bh.getLevel()), "</td>");
				hb.append("</tr>");
				
				hb.append("</table>");
				hb.append(Html.image(L2UI.SquareWhite, 264, 1));
				count++;
			}
			
			hb.append("<center>");
			hb.append(Html.image(L2UI.SquareWhite, 264, 1));
			hb.append("<table bgcolor=\"000000\">");
			hb.append("<tr>");
			
			int currentPage = 1;
			for (int i = 0; i < buffs.size(); i++)
			{
				if ((i % MAX_PER_PAGE) == 0)
				{
					hb.append("<td width=20 align=center><a action=\"bypass -h Engine NpcBufferScheme redirect_view_", buffType.name().toLowerCase(), " ", currentPage, "\">", currentPage, "</a></td>");
					currentPage++;
				}
			}
			
			hb.append("</tr>");
			hb.append("</table>");
			hb.append(Html.image(L2UI.SquareWhite, 264, 1));
			hb.append("</center>");
		}
		hb.append("<br>");
		hb.append("<button value=\"Back\" action=\"bypass -h Engine NpcBufferScheme redirect_main\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, ">");
		hb.append("<br>");
		hb.append("<font color=\"303030\">", TITLE_NAME, "</font>");
		hb.append("</center>");
		hb.append(Html.END);
		
		sendHtml(null, hb, ph);
	}
	
	/**
	 * Get the amount of buffs dances and songs obtained in specific scheme.
	 * @param  ph
	 * @param  scheme
	 * @return
	 */
	private String viewAllSchemeBuffsGetBuffCount(PlayerHolder ph, String scheme)
	{
		int count = 0;
		int D_S_Count = 0;
		int B_Count = 0;
		
		// get the skill list of a player's scheme
		String buffList = getValueDB(ph.getObjectId(), scheme).getString();
		
		if ((buffList != null) && !buffList.isEmpty())
		{
			// every buff is parse
			for (String buff : buffList.split(";"))
			{
				// get the id and level of each buff
				int id = Integer.parseInt(buff.split(",")[0]);
				int level = Integer.parseInt(buff.split(",")[1]);
				
				count++;
				
				for (BuffHolder bh : SchemeBufferPredefinedData.getAllGeneralBuffs())
				{
					if ((bh.getId() == id) && (bh.getLevel() == level))
					{
						if ((bh.getType() == BuffType.SONG) || (bh.getType() == BuffType.DANCE))
						{
							D_S_Count++;
						}
						else
						{
							B_Count++;
						}
						break;
					}
				}
			}
		}
		
		return count + " " + B_Count + " " + D_S_Count;
	}
	
	/**
	 * @param ph
	 * @param schemeName
	 * @param page
	 * @param action
	 */
	private void viewAllSchemeBuffs(PlayerHolder ph, String schemeName, String page, String action)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
		hb.append(Html.START);
		hb.append(Html.head("BUFFER"));
		hb.append("<center>");
		
		hb.append("<br>");
		
		String[] eventSplit = viewAllSchemeBuffsGetBuffCount(ph, schemeName).split(" ");
		
		int TOTAL_BUFF = Integer.parseInt(eventSplit[0]);
		int BUFF_COUNT = Integer.parseInt(eventSplit[1]);
		int DANCE_SONG = Integer.parseInt(eventSplit[2]);
		
		List<BuffHolder> buffs = new ArrayList<>();
		
		if (action.equals("add"))
		{
			hb.append("You can add <font color=\"LEVEL\">", MAX_SCHEME_BUFFS - BUFF_COUNT, "</font> Buffs and <font color=\"LEVEL\">", MAX_SCHEME_DANCES - DANCE_SONG, "</font> Dances more!");
			
			for (BuffHolder bh : SchemeBufferPredefinedData.getAllGeneralBuffs())
			{
				if (DANCE_SONG > MAX_SCHEME_DANCES)
				{
					if ((bh.getType() == BuffType.DANCE) || (bh.getType() == BuffType.SONG))
					{
						continue;
					}
				}
				
				if (BUFF_COUNT > MAX_SCHEME_BUFFS)
				{
					if ((bh.getType() != BuffType.DANCE) && (bh.getType() != BuffType.SONG))
					{
						continue;
					}
				}
				
				buffs.add(bh);
			}
		}
		else if (action.equals("remove"))
		{
			hb.append("You have <font color=\"LEVEL\">", BUFF_COUNT, "</font> Buffs and <font color=\"LEVEL\">", DANCE_SONG, "</font> Dances");
			
			String buffList = getValueDB(ph.getObjectId(), schemeName).getString();
			if (buffList == null)
			{
				System.out.println("error en remove buff");
			}
			else
			{
				for (String buff : buffList.split(";"))
				{
					int id = Integer.parseInt(buff.split(",")[0]);
					int level = Integer.parseInt(buff.split(",")[1]);
					
					buffs.add(new BuffHolder(id, level));
				}
			}
		}
		else
		{
			throw new RuntimeException();
		}
		
		hb.append("<br1>", Html.image(L2UI.SquareWhite, 264, 1), "<table border=0 bgcolor=\"000000\"><tr>");
		int buffsPerPage = 10;
		String width = "";
		int pc = ((buffs.size() - 1) / buffsPerPage) + 1;
		
		// definimos el largo de las celdas con las pagina
		if (pc > 5)
		{
			width = "25";
		}
		else
		{
			width = "50";
		}
		
		for (int ii = 1; ii <= pc; ++ii)
		{
			// creamos la botonera con las paginas
			if (ii == Integer.parseInt(page))
			{
				hb.append("<td width=", width, "><font color=\"LEVEL\">", ii, "</font></td>");
			}
			else if (action.equals("add"))
			{
				hb.append("<td width=", width, ">", "<a action=\"bypass -h Engine NpcBufferScheme manage_scheme_add ", schemeName, " ", ii, " x\">", ii, "</a></td>");
			}
			else if (action.equals("remove"))
			{
				hb.append("<td width=", width, ">", "<a action=\"bypass -h Engine NpcBufferScheme manage_scheme_remove ", schemeName, " ", ii, " x\">", ii, "</a></td>");
			}
			else
			{
				throw new RuntimeException();
			}
		}
		hb.append("</tr></table>", Html.image(L2UI.SquareWhite, 264, 1));
		
		int limit = buffsPerPage * Integer.parseInt(page);
		int start = limit - buffsPerPage;
		int end = Math.min(limit, buffs.size());
		int k = 0;
		for (int i = start; i < end; ++i)
		{
			BuffHolder bh = buffs.get(i);
			
			String name = bh.getSkill().getName();
			int id = bh.getId();
			int level = bh.getLevel();
			
			if (action.equals("add"))
			{
				if (!isUsed(ph, schemeName, id, level))
				{
					if ((k % 2) != 0)
					{
						
						hb.append("<br1>", Html.image(L2UI.SquareGray, 264, 1), "<table border=0>");
					}
					else
					{
						hb.append("<br1>", Html.image(L2UI.SquareGray, 264, 1), "<table border=0 bgcolor=\"000000\">");
					}
					
					hb.append("<tr>");
					hb.append("<td width=35>", getSkillIconHtml(id, level), "</td>");
					hb.append("<td fixwidth=170>", name, "</td>");
					hb.append("<td><button value=\"Add\" action=\"bypass -h Engine NpcBufferScheme add_buff ", schemeName, "_", id, "_", level, " ", page, " ", TOTAL_BUFF, "\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
					hb.append("</tr>");
					hb.append("</table>");
					k += 1;
				}
			}
			else if (action.equals("remove"))
			{
				if ((k % 2) != 0)
				{
					hb.append("<br1>", Html.image(L2UI.SquareGray, 264, 1), "<table border=0>");
				}
				else
				{
					hb.append("<br1>", Html.image(L2UI.SquareGray, 264, 1), "<table border=0 bgcolor=\"000000\">");
				}
				hb.append("<tr>");
				hb.append("<td width=35>", getSkillIconHtml(id, level), "</td>");
				hb.append("<td fixwidth=170>", name, "</td>");
				hb.append("<td><button value=Remove action=\"bypass -h Engine NpcBufferScheme remove_buff ", schemeName, "_", id, "_", level, " ", page, " ", TOTAL_BUFF, "\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
				hb.append("</tr>");
				hb.append("</table>");
				k += 1;
			}
		}
		hb.append("<br><br>");
		hb.append("<button value=Back action=\"bypass -h Engine NpcBufferScheme manage_scheme_select ", schemeName, "\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, ">");
		hb.append("<button value=Home action=\"bypass -h Engine NpcBufferScheme redirect_main\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, ">");
		hb.append("<br>");
		hb.append("<font color=\"303030\">", TITLE_NAME, "</font>");
		hb.append("</center>");
		hb.append(Html.END);
		
		sendHtml(null, hb, ph);
	}
}
