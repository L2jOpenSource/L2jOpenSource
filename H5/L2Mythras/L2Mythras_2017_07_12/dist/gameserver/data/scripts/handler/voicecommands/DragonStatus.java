package handler.voicecommands;

import l2f.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2f.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.quest.QuestState;
import l2f.gameserver.scripts.ScriptFile;
import quests._254_LegendaryTales;

public class DragonStatus implements IVoicedCommandHandler, ScriptFile
{
	private String[] _commandList = new String[]
	{ "7rb" };

	@Override
	public void onLoad()
	{
		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
	}

	@Override
	public void onReload()
	{
	}

	@Override
	public void onShutdown()
	{
	}

	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{
		QuestState qs = player.getQuestState(_254_LegendaryTales.class);
		if (qs == null)
		{
			player.sendMessage("LegendaryTales: innactive");
			return false;
		}
		QuestState st = player.getQuestState(qs.getQuest().getName());
		int var = st.getInt("RaidsKilled");
		_254_LegendaryTales.checkKilledRaids(player, var);
		return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}
