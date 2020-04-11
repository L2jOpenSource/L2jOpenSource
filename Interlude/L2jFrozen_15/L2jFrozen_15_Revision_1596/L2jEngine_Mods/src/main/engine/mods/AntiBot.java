package main.engine.mods;

import java.util.Arrays;
import java.util.List;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance.PunishLevel;
import com.l2jfrozen.util.random.Rnd;

import main.data.properties.ConfigData;
import main.data.xml.IconData;
import main.engine.AbstractMod;
import main.enums.ItemIconType;
import main.enums.MathType;
import main.holders.objects.CharacterHolder;
import main.holders.objects.NpcHolder;
import main.holders.objects.PlayerHolder;
import main.util.Util;
import main.util.builders.html.Html;
import main.util.builders.html.HtmlBuilder;
import main.util.builders.html.HtmlBuilder.HtmlType;

/**
 * AntiBot style system Google
 * @author fissban
 */
public class AntiBot extends AbstractMod
{
	public AntiBot()
	{
		registerMod(ConfigData.ENABLE_AntiBot);
	}
	
	@Override
	public void onModState()
	{
		switch (getState())
		{
			case START:
			{
				break;
			}
			case END:
			{
				cancelTimers("sendJail");
				break;
			}
		}
	}
	
	@Override
	public void onKill(CharacterHolder killer, CharacterHolder victim, boolean isPet)
	{
		if (!Util.areObjectType(L2MonsterInstance.class, victim) || (killer.getActingPlayer() == null))
		{
			return;
		}
		
		PlayerHolder ph = killer.getActingPlayer();
		
		// increasing the amount of dead mobs
		ph.modifyAntiBotKills(MathType.INCREASE_BY_ONE);
		
		int count = ConfigData.KILLER_MONSTERS_ANTIBOT_INCREASE_LEVEL ? ConfigData.KILLER_MONSTERS_ANTIBOT + (ph.getInstance().getLevel() * 3) : ConfigData.KILLER_MONSTERS_ANTIBOT;
		
		if (ph.getAntiBotKillsCount() >= count)
		{
			ph.modifyAntiBotKills(MathType.INIT);
			ph.modifyAntiBotAttempts(MathType.INIT);
			ph.setAntiBotAnswerRight("");
			
			// stop any action
			ph.getInstance().abortAttack();
			ph.getInstance().abortCast();
			ph.getInstance().stopMove(null);
			// start abnormal effect
			ph.getInstance().startAbnormalEffect(L2Character.ABNORMAL_EFFECT_FLAME);
			// player is paralyzed
			ph.getInstance().setIsParalyzed(true);
			// player is invulnerable
			ph.getInstance().setIsInvul(true);
			// send html index
			generateHtmlIndex(ph);
			// the timer starts to send to jail the player does not respond.
			startTimer("sendJail", ConfigData.TIME_CHECK_ANTIBOT * 1000, null, ph, false);
		}
	}
	
	@Override
	public void onTimer(String timerName, NpcHolder npc, PlayerHolder ph)
	{
		switch (timerName)
		{
			case "sendJail":
				if (ph.getAntiBotAttempts() <= 0)
				{
					sendPlayerJail(ph);
				}
				else
				{
					// the timer starts to send to jail the player does not respond.
					startTimer("sendJail", ConfigData.TIME_CHECK_ANTIBOT * 1000, null, ph, false);
					// decrease attempts
					ph.modifyAntiBotAttempts(MathType.DECREASE_BY_ONE);
					// send player html
					generateHtmlIndex(ph);
				}
				break;
		}
	}
	
	@Override
	public void onEvent(PlayerHolder ph, CharacterHolder npc, String command)
	{
		// the timer is canceled to send him to jail
		cancelTimer("sendJail", null, ph);
		
		// if the answer is correct
		if (ph.isAntiBotAnswerRight(command))
		{
			ph.getInstance().sendMessage("Correct Verification!");
			
			ph.getInstance().stopAbnormalEffect(L2Character.ABNORMAL_EFFECT_FLAME);
			// paralysis is removed when the player
			ph.getInstance().setIsParalyzed(false);
			// now player is mortal
			ph.getInstance().setIsInvul(false);
			
		}
		else
		{
			ph.getInstance().sendMessage("Incorrect verification!");
			
			// the number of failed attempts is checked
			if (ph.getAntiBotAttempts() <= 0)
			{
				sendPlayerJail(ph);
			}
			else
			{
				// the timer starts to send to jail the player does not respond.
				startTimer("sendJail", ConfigData.TIME_CHECK_ANTIBOT * 1000, null, ph, false);
				// decrease attempts
				ph.modifyAntiBotAttempts(MathType.DECREASE_BY_ONE);
				// send player html
				generateHtmlIndex(ph);
			}
		}
	}
	
	@Override
	public boolean onExitWorld(PlayerHolder ph)
	{
		// si ya esta activo el antibot no dejamos que el player salga del juego evitando el control.
		if (getTimer("sendJail", ph) != null)
		{
			return true;
		}
		return false;
	}
	
	private static synchronized void generateHtmlIndex(PlayerHolder ph)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
		hb.append("<html><body><center>");
		hb.append("<br>");
		hb.append(Html.head("ANTI BOT"));
		hb.append("<br>");
		hb.append("Has ", ph.getAntiBotAttempts(), " attemps!<br>");
		// for clarity of the images could only come out a rnd of these values
		List<Integer> aux = Arrays.asList(0, 1, 3, 4, 5);
		
		ItemIconType itemIconType1 = ItemIconType.values()[aux.get(Rnd.get(aux.size()))];
		// we get a different type
		ItemIconType itemIconType2 = ItemIconType.values()[aux.get(Rnd.get(aux.size()))];
		
		// we ensure that both random are different
		while (itemIconType1 == itemIconType2)
		{
			itemIconType2 = ItemIconType.values()[aux.get(Rnd.get(aux.size()))];
		}
		
		// we inform the type of item you have to look
		hb.append("It indicates which of these items is: <font color=LEVEL>", itemIconType1.name().toLowerCase(), "</font><br>");
		
		hb.append("<table>");
		hb.append("<tr>");
		// we generate a random column where will the correct answer.
		int rnd = Rnd.get(0, 3);
		ph.setAntiBotAnswerRight(rnd + "");
		
		for (int i = 0; i <= 3; i++)
		{
			// generate random icons.
			String icon = "";
			if (i == rnd)
			{
				icon = IconData.getRandomItemType(itemIconType1, 40);
			}
			else
			{
				icon = IconData.getRandomItemType(itemIconType2, 40);
			}
			
			hb.append("<td align=center fixwidth=32>");
			hb.append("<button action=\"bypass -h Engine AntiBot ", i, "\" width=32 height=32 back=\"", icon, "\" fore=\"", icon, "\">");
			hb.append("</td>");
		}
		hb.append("</tr>");
		hb.append("</table>");
		
		hb.append("</center></body></html>");
		
		// send player html
		sendHtml(null, hb, ph);
	}
	
	private static void sendPlayerJail(PlayerHolder ph)
	{
		if (ph.getInstance() != null)
		{
			ph.getInstance().stopAbnormalEffect(L2Character.ABNORMAL_EFFECT_FLAME);
			// paralysis is removed when the player
			ph.getInstance().setIsParalyzed(false);
			// now player is mortal
			ph.getInstance().setIsInvul(false);
			// the character is sent to jail if he did not answer
			ph.getInstance().setPunishLevel(PunishLevel.JAIL, 10); // TODO missing config
		}
	}
}
