package handlers.playeractions;

import l2r.gameserver.handler.IPlayerActionHandler;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2ServitorInstance;
import l2r.gameserver.model.holders.ActionDataHolder;
import l2r.gameserver.network.SystemMessageId;

/**
 * Servitor stop action player action handler.
 * @author vGodFather
 */
public final class ServitorStop implements IPlayerActionHandler
{
	@Override
	public void useAction(L2PcInstance activeChar, ActionDataHolder data, boolean ctrlPressed, boolean shiftPressed)
	{
		if ((activeChar.getSummon() == null) || !activeChar.getSummon().isServitor())
		{
			activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_A_SERVITOR);
			return;
		}
		
		final L2ServitorInstance servitor = (L2ServitorInstance) activeChar.getSummon();
		
		if (servitor.isBetrayed())
		{
			activeChar.sendPacket(SystemMessageId.YOUR_PET_SERVITOR_IS_UNRESPONSIVE_AND_WILL_NOT_OBEY_ANY_ORDERS);
			return;
		}
		
		servitor.cancelAction();
	}
}
