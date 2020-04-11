package l2r.gameserver.model.entity.olympiad.tasks;

import java.util.List;

import l2r.Config;
import l2r.gameserver.instancemanager.AntiFeedManager;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;

/**
 * @author vGodFather
 */
public final class AnnounceUnregToTeam implements Runnable
{
	private final List<Integer> _team;
	
	public AnnounceUnregToTeam(List<Integer> t)
	{
		_team = t;
	}
	
	@Override
	public final void run()
	{
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_BEEN_DELETED_FROM_THE_WAITING_LIST_OF_A_GAME);
		for (int objectId : _team)
		{
			L2PcInstance teamMember = L2World.getInstance().getPlayer(objectId);
			if (teamMember != null)
			{
				teamMember.sendPacket(sm);
				if (Config.L2JMOD_DUALBOX_CHECK_MAX_OLYMPIAD_PARTICIPANTS_PER_IP > 0)
				{
					AntiFeedManager.getInstance().removePlayer(AntiFeedManager.OLYMPIAD_ID, teamMember);
				}
			}
		}
	}
}