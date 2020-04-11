package l2r.log.filter;

import java.util.Arrays;
import java.util.List;

import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.log.formatter.AccountFormatter;
import l2r.log.formatter.ChatLogFormatter;
import l2r.log.formatter.DamageFormatter;
import l2r.log.formatter.EnchantFormatter;
import l2r.log.formatter.ItemLogFormatter;
import l2r.log.formatter.OlympiadFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vGodFather
 */
public class Log
{
	private static List<Integer> skipList = Arrays.asList(3031, 10410, 10411, 10411);
	
	private static final Logger _logItems = LoggerFactory.getLogger("item");
	private static final Logger _logItemEnchant = LoggerFactory.getLogger("enchantitem");
	private static final Logger _logSkillEnchant = LoggerFactory.getLogger("enchantskill");
	private static final Logger _logPlayerDrop = LoggerFactory.getLogger("playerdrop");
	private static final Logger _logOlympiadResult = LoggerFactory.getLogger("olympiad");
	private static final Logger _logMDamages = LoggerFactory.getLogger("mdam");
	private static final Logger _logPDamages = LoggerFactory.getLogger("pdam");
	private static final Logger _logChats = LoggerFactory.getLogger("chat");
	private static final Logger _logDonationsWin = LoggerFactory.getLogger("donation-win");
	private static final Logger _logDonationsUnix = LoggerFactory.getLogger("donation-unix");
	private static final Logger _logDonationsConvert = LoggerFactory.getLogger("donation-convert");
	private static final Logger _logAccounting = LoggerFactory.getLogger("account");
	private static final Logger _logAcountFails = LoggerFactory.getLogger("acount-fails");
	private static final Logger _logAcountSuccess = LoggerFactory.getLogger("acount-success");
	
	private static final Logger _log = LoggerFactory.getLogger("gameserver");
	
	public static void LogPlayerDonationConvert(String arg)
	{
		_logDonationsConvert.info(arg);
	}
	
	public static void LogPlayerDonationWin(String arg)
	{
		_logDonationsWin.info(arg);
	}
	
	public static void LogPlayerDonationUnix(String arg)
	{
		_logDonationsUnix.info(arg);
	}
	
	public static void LogPlayerDrop(String arg)
	{
		_logPlayerDrop.info(arg);
	}
	
	public static void LogPlayerItems(String arg, Object[] obj)
	{
		for (Object p : obj)
		{
			if (p == null)
			{
				continue;
			}
			if (p instanceof L2ItemInstance)
			{
				L2ItemInstance item = (L2ItemInstance) p;
				
				// Skip log of potions or elixirs
				if (item.isPotion() || item.isElixir() || skipList.contains(item.getId()))
				{
					return;
				}
			}
		}
		
		_logItems.info(ItemLogFormatter.format(arg, obj));
	}
	
	public static void LogPlayerItemEnchant(String arg, Object[] obj)
	{
		_logItemEnchant.info(EnchantFormatter.format(arg, obj));
	}
	
	public static void LogPlayerSkillEnchant(String arg, Object[] obj)
	{
		_logSkillEnchant.info(EnchantFormatter.format(arg, obj));
	}
	
	public static void LogOlympiadResult(String arg, Object[] obj)
	{
		_logOlympiadResult.info(OlympiadFormatter.format(arg, obj));
	}
	
	public static void LogAccount(String arg, Object[] obj)
	{
		_logAccounting.info(AccountFormatter.format(arg, obj));
	}
	
	public static void LogPlayerAccountFails(String arg)
	{
		_logAcountFails.info(arg);
	}
	
	public static void LogPlayerAccountSuccess(String arg)
	{
		_logAcountSuccess.info(arg);
	}
	
	public static void LogPlayerMDamages(String arg, Object[] obj)
	{
		_logMDamages.info(DamageFormatter.format(arg, obj));
	}
	
	public static void LogPlayerPDamages(String arg, Object[] obj)
	{
		_logPDamages.info(DamageFormatter.format(arg, obj));
	}
	
	public static void LogPlayerChats(String arg, Object[] obj)
	{
		_logChats.info(ChatLogFormatter.format(arg, obj));
	}
	
	public static void info(String msg)
	{
		info(msg, null);
	}
	
	public static void info(String msg, Exception e)
	{
		if (e != null)
		{
			_log.info(msg, e);
		}
		else
		{
			_log.info(msg);
		}
		
		// ConsoleTab.appendMessage(ConsoleFilter.Info, msg);
	}
	
	public static void warning(String msg)
	{
		warning(msg, null);
	}
	
	public static void warning(String msg, Exception e)
	{
		if (e != null)
		{
			_log.warn(msg, e);
		}
		else
		{
			_log.warn(msg);
		}
		
		// ConsoleTab.appendMessage(ConsoleFilter.Warnings, msg);
	}
	
	public static void warning(String msg, Throwable e)
	{
		_log.warn(msg, e);
		
		// ConsoleTab.appendMessage(ConsoleFilter.Warnings, msg);
	}
	
	public static void error(String msg)
	{
		error(msg, null);
	}
	
	public static void error(String msg, Exception e)
	{
		if (e != null)
		{
			_log.error(msg, e);
		}
		else
		{
			_log.error(msg);
		}
		
		// ConsoleTab.appendMessage(ConsoleFilter.Errors, msg);
	}
	
	public static void error(String msg, Throwable e)
	{
		_log.error(msg, e);
		
		// ConsoleTab.appendMessage(ConsoleFilter.Errors, msg);
	}
	
	public static void announcements(String msg)
	{
		_log.info(msg);
		
		// ConsoleTab.appendMessage(ConsoleFilter.Announcements, msg);
	}
}
