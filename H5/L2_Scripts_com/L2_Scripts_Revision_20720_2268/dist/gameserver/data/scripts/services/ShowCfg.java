package services;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;

public class ShowCfg extends Functions
{

	public void showConfiguration()
	{
		Player player = getSelf();
		IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler("cfg");
		if(vch != null)
		{
			vch.useVoicedCommand("cfg", player, null);
		}	
	}
}