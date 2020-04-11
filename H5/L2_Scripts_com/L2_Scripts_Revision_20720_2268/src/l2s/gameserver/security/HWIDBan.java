package l2s.gameserver.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import l2s.gameserver.Config;
import l2s.gameserver.dao.AccountVariablesDAO;
import l2s.gameserver.network.authcomm.AuthServerCommunication;
import l2s.gameserver.network.authcomm.gs2as.ChangeAccessLevel;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.s2c.Ex2NDPasswordAckPacket;
import l2s.gameserver.network.l2.s2c.Ex2NDPasswordCheckPacket;
import l2s.gameserver.network.l2.s2c.Ex2NDPasswordVerifyPacket;
import l2s.gameserver.utils.Strings;
import java.util.ArrayList;

import l2s.gameserver.database.DatabaseFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import l2s.commons.dbutils.DbUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HWIDBan
{
	private static final Logger _log = LoggerFactory.getLogger(HWIDBan.class);
	private static ArrayList<String> _l = new ArrayList<String>();

	public static void LoadAllHWID()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			String hwid = "";
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM ban_hwid");
			rset = statement.executeQuery();
			while(rset.next())
			{
				hwid = rset.getString("hwid");
				if(hwid != "")
					_l.add(hwid);
			}	
		}	
		catch(Exception e)
		{
			_log.info("not loaded?");
		}
		finally
		{
			
			DbUtils.closeQuietly(con, statement, rset);
			_log.info("black list (Hwid) loaded size: "+_l.size()+"");
		}		
	}

	public static void addBlackList(String hwid)
	{

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO ban_hwid (hwid) VALUES(?)");
			statement.setString(1, hwid);		
			statement.execute();
		}
		catch(Exception e)
		{
			//fuck the what don't care
		}
		finally
		{
			_l.add(hwid);
			_log.info("adding hwid to black list(hwid) "+hwid+"");		
			DbUtils.closeQuietly(con, statement);
		}	
	}

	public static void delBlackList(String hwid)
	{

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE from ban_hwid WHERE hwid like ?");
			statement.setString(1, hwid);		
			statement.execute();
		}
		catch(Exception e)
		{
			//fuck the what don't care
		}
		finally
		{
			_l.remove(hwid);
			_log.info("remove hwid from black list(hwid) "+hwid+"");		
			DbUtils.closeQuietly(con, statement);
		}	
	}
	
	public static ArrayList<String> getAllBannedHwid() 
	{
		return _l;
	}	
}