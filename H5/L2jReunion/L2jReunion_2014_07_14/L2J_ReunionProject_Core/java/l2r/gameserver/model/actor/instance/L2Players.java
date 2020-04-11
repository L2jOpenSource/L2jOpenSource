package l2r.gameserver.model.actor.instance;

import l2r.gameserver.model.L2Object;

public interface L2Players
{
	public void storePlayer(L2Object player);
	
	public void removePlayer(L2Object player);
	
	public L2PcInstance get(int objId);
	
	public void run();
}
