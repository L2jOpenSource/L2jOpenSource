package l2r.gameserver.scripts.handlers.voicedcommandhandlers;

import l2r.gameserver.Announcements;
import l2r.gameserver.handler.IVoicedCommandHandler;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.clientpackets.Say2;
import l2r.gameserver.network.serverpackets.CreatureSay;

public class VoteVCmd implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"voteenable",
		"votedisable",
		"yes",
		"no",
		"votes"
	};
	
	public static int yes = 0;
	public static int no = 0;
	
	public static void setVote(int yeses, int noes)
	{
		yes = yes + yeses;
		no = no + noes;
	}
	
	public void voteclean()
	{
		yes = 0;
		no = 0;
	}
	
	public static int getYes()
	{
		return yes;
	}
	
	public static int getNo()
	{
		return no;
	}
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params)
	{
		if (command.equalsIgnoreCase("voteenable"))
		{
			if (!activeChar.isGM())
				return false;
			
			voteclean();
			Announcements.getInstance().announceToAll("Voting system has started, all players can vote now by pressing .yes or .no");
			for (L2PcInstance all : L2World.getInstance().getPlayers())
				all.setVoter(true);
		}
		else if (command.equalsIgnoreCase("votedisable"))
		{
			if (!activeChar.isGM())
				return false;
			
			Announcements.getInstance().announceToAll("Voting system has ended.");
			for (L2PcInstance all : L2World.getInstance().getPlayers())
				all.setVoter(false);
		}
		else if (command.equalsIgnoreCase("yes"))
		{
			if (!activeChar.isVoter() || activeChar.isGM())
			{
				activeChar.sendMessage("You are unable to vote when you are not a voter.");
				return false;
			}
			activeChar.sendMessage("You have successfully voted.");
			activeChar.setVoter(false);
			setVote(1, 0);
			for (L2PcInstance allgms : L2World.getInstance().getAllGMs())
				allgms.sendPacket(new CreatureSay(0, Say2.SHOUT, "Voting Info", activeChar.getName() + " has voted Yes."));
		}
		else if (command.equalsIgnoreCase("no"))
		{
			{
				if (!activeChar.isVoter() || activeChar.isGM())
				{
					activeChar.sendMessage("You are unable to vote when you are not a voter");
					return false;
				}
				activeChar.sendMessage("You have voted successfully");
				activeChar.setVoter(false);
				setVote(0, 1);
				for (L2PcInstance allgms : L2World.getInstance().getAllGMs())
					allgms.sendPacket(new CreatureSay(0, Say2.SHOUT, "Voting Info", activeChar.getName() + " has voted No."));
			}
			
		}
		else if (command.equalsIgnoreCase("votes"))
		{
			if (!activeChar.isGM())
				return false;
			
			activeChar.sendMessage("Yes: " + getYes());
			activeChar.sendMessage("No: " + getNo());
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}