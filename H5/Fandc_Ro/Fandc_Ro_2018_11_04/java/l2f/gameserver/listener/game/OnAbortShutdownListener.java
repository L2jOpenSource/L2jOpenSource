package l2f.gameserver.listener.game;

import l2f.gameserver.listener.GameListener;
import l2f.gameserver.Shutdown;

public interface OnAbortShutdownListener extends GameListener
{
	void onAbortShutdown(Shutdown.ShutdownMode p0, int p1);
}
