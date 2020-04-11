package l2s.gameserver.security.hwid;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HwidLogging
{
	private static final Logger _log = LoggerFactory.getLogger(HwidLogging.class);
  
	public HwidLogging() 
	{
		_logs = new ConcurrentHashMap<Integer, List<SimpleLog>>();
	}
  
	public void addNewLog(SimpleLog log)
	{
	}
  
	public List<SimpleLog> getMyLogs(Player player)
	{
		return null;
	}
  
	private List<SimpleLog> loadLogs(int objId)
	{
		List<SimpleLog> playerLogs = new ArrayList<SimpleLog>();
		return playerLogs;
	}
	
	private static HwidLogging _instance;
	private Map<Integer, List<SimpleLog>> _logs;
	
	public static HwidLogging getInstance()
	{
		if(_instance == null)
			_instance = new HwidLogging();
		return _instance;
	}
	
	public static class SimpleLog
	{
		public int _charObjId;
		public String _hwid;
		public String _msg;
		public long _time;
	}
}
