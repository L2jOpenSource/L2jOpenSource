package net.sf.l2j.loginserver.data.manager;

import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import net.sf.l2j.commons.logging.CLogger;

public class IpBanManager
{
	private static final CLogger LOGGER = new CLogger(GameServerManager.class.getName());
	
	private final Map<InetAddress, Long> _bannedIps = new ConcurrentHashMap<>();
	
	protected IpBanManager()
	{
		// Load file.
		final Path file = Paths.get("config", "banned_ips.properties");
		if (file == null)
		{
			LOGGER.warn("banned_ips.properties is missing. Ban listing is skipped.");
			return;
		}
		
		// Load each line, dropping the ones containing #.
		try (Stream<String> stream = Files.lines(file))
		{
			stream.filter(l -> !l.contains("#")).forEach(l ->
			{
				try
				{
					_bannedIps.putIfAbsent(InetAddress.getByName(l), 0L);
				}
				catch (Exception e)
				{
					LOGGER.error("Invalid ban address ({}).", l);
				}
			});
		}
		catch (Exception e)
		{
			LOGGER.error("Error while reading banned_ips.properties.", e);
		}
		LOGGER.info("Loaded {} banned IP(s).", _bannedIps.size());
	}
	
	public Map<InetAddress, Long> getBannedIps()
	{
		return _bannedIps;
	}
	
	/**
	 * Add the {@link InetAddress} set as parameter to the ban list, with the given duration.
	 * @param address : The {@link InetAddress} to ban.
	 * @param duration : The timer in milliseconds. 0 means it is permanently banned.
	 */
	public void addBanForAddress(InetAddress address, long duration)
	{
		// Add current time, but only if parameter is > 0.
		if (duration > 0)
			duration += System.currentTimeMillis();
		
		_bannedIps.putIfAbsent(address, duration);
	}
	
	/**
	 * @param address : The {@link InetAddress} to test.
	 * @return True if the {@link InetAddress} set as parameter is actually banned, otherwise false.<br>
	 *         <br>
	 *         If the timer exists, compare with actual time. If too old, remove the ban. 0 timers never expire.
	 */
	public boolean isBannedAddress(InetAddress address)
	{
		if (address == null)
			return true;
		
		final Long time = _bannedIps.get(address);
		if (time != null)
		{
			if (time > 0 && time < System.currentTimeMillis())
			{
				// Remove the ban from memory.
				_bannedIps.remove(address);
				
				LOGGER.info("Removed expired ip address ban {}.", address.getHostAddress());
				return false;
			}
			return true;
		}
		return false;
	}
	
	public static IpBanManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final IpBanManager INSTANCE = new IpBanManager();
	}
}