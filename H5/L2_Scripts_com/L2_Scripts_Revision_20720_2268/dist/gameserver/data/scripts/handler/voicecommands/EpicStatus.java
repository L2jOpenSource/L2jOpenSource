package handler.voicecommands;

import bosses.*;
import l2s.commons.map.hash.TIntStringHashMap;
import l2s.gameserver.Config;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.DominionSiegeEvent;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.SubUnit;
import l2s.gameserver.model.pledge.UnitMember;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.*;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.scripts.ScriptFile;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.TimeUtils;
import l2s.gameserver.utils.Util;

import java.util.*;

/**
 * @author Bonux
**/
public class EpicStatus extends Functions implements IVoicedCommandHandler, ScriptFile
{
	private static final String[] COMMANDS = new String[] { "epic" };

	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{
		player.sendMessage("---------------------------------------------------------------------------");
		player.sendMessage(player.isLangRus() ? "Статус эпических боссов:" : "Status of epic bosses:");
		player.sendMessage("---------------------------------------------------------------------------");

		sendStatus(player, player.isLangRus() ? "Антарас" : "Antharas", AntharasManager.getState().isAlive() ? -1 : AntharasManager.getState().getRespawnDate());
		sendStatus(player, player.isLangRus() ? "Валакас" : "Valakas", ValakasManager.getState().isAlive() ? -1 : ValakasManager.getState().getRespawnDate());
		sendStatus(player, player.isLangRus() ? "Баюм" : "Baium", BaiumManager.getState().isAlive() ? -1 : BaiumManager.getState().getRespawnDate());
		// TODO: Frintezza
		sendStatus(player, player.isLangRus() ? "Сейлрен" : "Sailren", SailrenManager.getState().isAlive() ? -1 : SailrenManager.getState().getRespawnDate());
		sendStatus(player, player.isLangRus() ? "Байлор" : "Baylor", BaylorManager.getState().isAlive() ? -1 : BaylorManager.getState().getRespawnDate());
		sendStatus(player, player.isLangRus() ? "Белет" : "Beleth", BelethManager.isAlive() ? -1 : BelethManager.getRespawnTime());

		player.sendMessage("---------------------------------------------------------------------------");
		return true;
	}

	public void sendStatus(Player player, String name, long respawnTime) {
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append(": ");
		if (respawnTime > 0) {
			sb.append(player.isLangRus() ? "мертв" : "dead");
			sb.append(" (Возродиться: ");
			sb.append(TimeUtils.toSimpleFormat(respawnTime));
			sb.append(")");
		} else
			sb.append(player.isLangRus() ? "живой" : "alive");
		player.sendMessage(sb.toString());
	}

	@Override
	public void onLoad()
	{
		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
	}

	@Override
	public void onReload()
	{
		//
	}

	@Override
	public void onShutdown()
	{
		//
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return COMMANDS;
	}
}