package handler.voicecommands;

import services.community.CommunityNpcs;
import bosses.AntharasManager;
import bosses.BaiumManager;
import bosses.ValakasManager;
import l2f.gameserver.Config;
import l2f.gameserver.data.htm.HtmCache;
import l2f.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2f.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2f.gameserver.instancemanager.RaidBossSpawnManager;
import l2f.gameserver.instancemanager.ServerVariables;
import l2f.gameserver.model.Player;
import l2f.gameserver.network.serverpackets.ShowBoard;
import l2f.gameserver.scripts.ScriptFile;

public class Epics implements IVoicedCommandHandler, ScriptFile
{
	private String[] _commandList = new String[] { 
			"epicAntharas",
			"epicValakas",
			"epicBaium",
			"epicBeleth",
			"epicQueenAnt",
			"epicOrfen"
	};

	@Override
	public void onLoad()
	{
		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
	}

	@Override
	public void onReload()
	{	}

	@Override
	public void onShutdown()
	{	}

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		if(activeChar == null)
			return false;
		
		String html =  HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "epicsRespawn/template.htm", activeChar);
		
		if (command.equals("epicAntharas"))
		{
			html = html.replace("%img%", "Btns.epic_29068");
			html = html.replace("%respawn%", CommunityNpcs.convertRespawnDate(AntharasManager.getState().getRespawnDate()));
			html = html.replace("%loc1%", "153368 119528 -3808 1000000");
			html = html.replace("%loc2%", "152472 120184 -3808 1000000");
			html = html.replace("%loc3%", "152184 119160 -3800 1000000");
			html = html.replace("%loc4%", "150856 118040 -3688 1000000");
			html = html.replace("%loc5%", "149944 116616 -3704 1000000");
			html = html.replace("%loc6%", "149240 115560 -3704 1000000");
			ShowBoard.separateAndSend(html, activeChar);
			
			return true;
		}
		if (command.equals("epicValakas"))
		{
			html = html.replace("%img%", "Btns.epic_29028");
			html = html.replace("%respawn%", CommunityNpcs.convertRespawnDate(ValakasManager.getState().getRespawnDate()));
			html = html.replace("%loc1%", "182744 -115048 -3336 1000000");
			html = html.replace("%loc2%", "184584 -115768 -3328 1000000");
			html = html.replace("%loc3%", "183544 -117592 -3336 1000000");
			html = html.replace("%loc4%", "181880 -116168 -3336 1000000");
			html = html.replace("%loc5%", "181928 -117944 -3320 -3704 1000000");
			html = html.replace("%loc6%", "181928 -117944 -3320 1000000");
			ShowBoard.separateAndSend(html, activeChar);
			
			return true;
		}
		if (command.equals("epicBaium"))
		{
			html = html.replace("%img%", "Btns.epic_29020");
			html = html.replace("%respawn%", CommunityNpcs.convertRespawnDate(BaiumManager.getState().getRespawnDate()));
			html = html.replace("%loc1%", "114168 13352 9560 1000000");
			html = html.replace("%loc2%", "111896 15640 9560 1000000");
			html = html.replace("%loc3%", "113368 14840 9560 1000000");
			html = html.replace("%loc4%", "114744 17224 9000 1000000");
			html = html.replace("%loc5%", "115736 16344 9000 1000000");
			html = html.replace("%loc6%", "113496 14968 9000 1000000");
			ShowBoard.separateAndSend(html, activeChar);
			
			return true;
		}
		if (command.equals("epicBeleth"))
		{
			html = html.replace("%img%", "Btns.epic_29118");
			html = html.replace("%respawn%", CommunityNpcs.convertRespawnDate(ServerVariables.getLong("BelethKillTime", 0L)));
			html = html.replace("%loc1%", "17896 282760 -9704 1000000");
			html = html.replace("%loc2%", "17928 283832 -9704 1000000");
			html = html.replace("%loc3%", "19320 283896 -9704 1000000");
			html = html.replace("%loc4%", "18920 284248 -9704 1000000");
			html = html.replace("%loc5%", "8952 251944 -2032 1000000");
			html = html.replace("%loc6%", "9144 250728 -1984 1000000");
			ShowBoard.separateAndSend(html, activeChar);
			
			return true;
		}
		if (command.equals("epicQueenAnt"))
		{
			html = html.replace("%img%", "Btns.epic_29001");
			html = html.replace("%respawn%", CommunityNpcs.convertRespawnDate(RaidBossSpawnManager.getInstance().getRespawntime(29001)*1000L));
			html = html.replace("%loc1%", "-21880 184488 -5720 1000000");
			html = html.replace("%loc2%", "-22392 183000 -5720 1000000");
			html = html.replace("%loc3%", "-21096 183208 -5720 1000000");
			html = html.replace("%loc4%", "-19496 183848 -5600 1000000");
			html = html.replace("%loc5%", "-21768 185768 -5600 1000000");
			html = html.replace("%loc6%", "-23896 183976 -5600 1000000");
			ShowBoard.separateAndSend(html, activeChar);
			
			return true;
		}
		if (command.equals("epicOrfen"))
		{
			html = html.replace("%img%", "Btns.epic_29014");
			html = html.replace("%respawn%", CommunityNpcs.convertRespawnDate(RaidBossSpawnManager.getInstance().getRespawntime(29014)*1000L));
			html = html.replace("%loc1%", "54152 18024 -5456 1000000");
			html = html.replace("%loc2%", "56344 18312 -5464 1000000");
			html = html.replace("%loc3%", "56488 17016 -5440 1000000");
			html = html.replace("%loc4%", "54552 16088 -5488 1000000");
			html = html.replace("%loc5%", "51016 18568 -5160 1000000");
			html = html.replace("%loc6%", "50440 15928 -5056 1000000");
			ShowBoard.separateAndSend(html, activeChar);
			
			return true;
		}
			
		return false;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}
