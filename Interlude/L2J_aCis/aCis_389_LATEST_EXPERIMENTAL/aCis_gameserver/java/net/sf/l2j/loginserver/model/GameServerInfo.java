package net.sf.l2j.loginserver.model;

import net.sf.l2j.commons.network.StatusType;

import net.sf.l2j.loginserver.GameServerThread;

public class GameServerInfo
{
	private int _id;
	private final byte[] _hexId;
	private boolean _isAuthed;
	
	private GameServerThread _gst;
	private StatusType _status;
	
	private String _hostName;
	private int _port;
	
	private boolean _isPvp;
	private boolean _isTestServer;
	private boolean _isShowingClock;
	private boolean _isShowingBrackets;
	
	private int _ageLimit;
	private int _maxPlayers;
	
	public GameServerInfo(int id, byte[] hexId, GameServerThread gst)
	{
		_id = id;
		_hexId = hexId;
		_gst = gst;
		_status = StatusType.DOWN;
	}
	
	public GameServerInfo(int id, byte[] hexId)
	{
		this(id, hexId, null);
	}
	
	public int getId()
	{
		return _id;
	}
	
	public void setId(int id)
	{
		_id = id;
	}
	
	public byte[] getHexId()
	{
		return _hexId;
	}
	
	public boolean isAuthed()
	{
		return _isAuthed;
	}
	
	public void setAuthed(boolean isAuthed)
	{
		_isAuthed = isAuthed;
	}
	
	public GameServerThread getGameServerThread()
	{
		return _gst;
	}
	
	public void setGameServerThread(GameServerThread gst)
	{
		_gst = gst;
	}
	
	public StatusType getStatus()
	{
		return _status;
	}
	
	public void setStatus(StatusType status)
	{
		_status = status;
	}
	
	public String getHostName()
	{
		return _hostName;
	}
	
	public void setHostName(String hostName)
	{
		_hostName = hostName;
	}
	
	public int getPort()
	{
		return _port;
	}
	
	public void setPort(int port)
	{
		_port = port;
	}
	
	public int getMaxPlayers()
	{
		return _maxPlayers;
	}
	
	public void setMaxPlayers(int maxPlayers)
	{
		_maxPlayers = maxPlayers;
	}
	
	public boolean isPvp()
	{
		return _isPvp;
	}
	
	public void setPvp(boolean isPvp)
	{
		_isPvp = isPvp;
	}
	
	public boolean isTestServer()
	{
		return _isTestServer;
	}
	
	public void setTestServer(boolean isTestServer)
	{
		_isTestServer = isTestServer;
	}
	
	public boolean isShowingClock()
	{
		return _isShowingClock;
	}
	
	public void setShowingClock(boolean isShowingClock)
	{
		_isShowingClock = isShowingClock;
	}
	
	public boolean isShowingBrackets()
	{
		return _isShowingBrackets;
	}
	
	public void setShowingBrackets(boolean isShowingBrackets)
	{
		_isShowingBrackets = isShowingBrackets;
	}
	
	public int getAgeLimit()
	{
		return _ageLimit;
	}
	
	public void setAgeLimit(int ageLimit)
	{
		_ageLimit = ageLimit;
	}
	
	public void setDown()
	{
		setAuthed(false);
		setPort(0);
		setGameServerThread(null);
		setStatus(StatusType.DOWN);
	}
	
	public int getCurrentPlayerCount()
	{
		return (_gst == null) ? 0 : _gst.getPlayerCount();
	}
}