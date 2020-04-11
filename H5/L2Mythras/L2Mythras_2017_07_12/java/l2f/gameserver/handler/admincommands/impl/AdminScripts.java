package l2f.gameserver.handler.admincommands.impl;

import l2f.gameserver.handler.admincommands.IAdminCommandHandler;
import l2f.gameserver.model.Player;


public class AdminScripts implements IAdminCommandHandler
{
    private static enum Commands
    {
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
    {
    @SuppressWarnings("unused")
        Commands command = (Commands) comm;

        if (!activeChar.isGM())
            return false;

        return true;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Enum[] getAdminCommandEnum()
    {
        return Commands.values();
    }
}