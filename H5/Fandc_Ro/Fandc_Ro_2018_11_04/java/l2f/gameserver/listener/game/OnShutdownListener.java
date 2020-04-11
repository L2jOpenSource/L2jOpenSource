package l2f.gameserver.listener.game;

import l2f.gameserver.listener.GameListener;
import l2f.gameserver.Shutdown;

public interface OnShutdownListener extends GameListener
{
	void onShutdown(Shutdown.ShutdownMode p0);
}
