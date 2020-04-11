package main.util;

import java.util.concurrent.locks.ReentrantLock;

import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.base.ClassId;
import com.l2jfrozen.gameserver.network.L2GameClient;
import com.l2jfrozen.gameserver.network.L2GameClient.GameClientState;
import com.l2jfrozen.gameserver.thread.LoginServerThread;

/**
 * @author fissban
 */
public class UtilPlayer
{
	private static final ReentrantLock locker = new ReentrantLock();
	
	public static L2PcInstance spawnPlayer(int objectId)
	{
		locker.lock();
		
		L2PcInstance player = null;
		try
		{
			L2GameClient client = new L2GameClient(null);
			
			player = L2PcInstance.load(objectId);
			client.setActiveChar(player);
			client.setAccountName(player.getAccountName());
			client.setState(GameClientState.IN_GAME);
			client.setDetached(true);
			player.setOnline(true);
			player.setClient(client);
			player.spawnMe(player.getX(), player.getY(), player.getZ());
			LoginServerThread.getInstance().addGameServerLogin(player.getAccountName(), client);
			L2World.getInstance().addToAllPlayers(player);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			locker.unlock();
		}
		
		return player;
	}
	
	// XXX Change class
	/**
	 * Returns true if class change is possible
	 * @param  oldCID current player ClassId
	 * @param  newCID new ClassId
	 * @return        true if class change is possible
	 */
	public static final boolean validateChangeClassId(ClassId oldCID, int val)
	{
		try
		{
			return UtilPlayer.validateChangeClassId(oldCID, ClassId.values()[val]);
		}
		catch (Exception e)
		{
			// possible ArrayOutOfBoundsException
		}
		return false;
	}
	
	/**
	 * Returns true if class change is possible
	 * @param  oldCID current player ClassId
	 * @param  newCID new ClassId
	 * @return        true if class change is possible
	 */
	public static final boolean validateChangeClassId(ClassId oldCID, ClassId newCID)
	{
		if ((newCID == null) || (newCID.getRace() == null))
		{
			return false;
		}
		
		if (oldCID.equals(newCID.getParent()))
		{
			return true;
		}
		
		// if (Config.ALLOW_ENTIRE_TREE && newCID.childOf(oldCID))
		// {
		// return true;
		// }
		
		return false;
	}
	
	/**
	 * Returns minimum player level required for next class transfer
	 * @param  level - current skillId level (0 - start, 1 - first, etc)
	 * @return
	 */
	public static final int getMinLevelChangeClass(int level)
	{
		switch (level)
		{
			case 0:
				return 20;
			case 1:
				return 40;
			case 2:
				return 76;
			default:
				return Integer.MAX_VALUE;
		}
	}
}
