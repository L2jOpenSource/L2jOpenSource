package com.l2jfrozen.loginserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;

public class BruteProtector
{
	private static final Logger LOGGER = Logger.getLogger(BruteProtector.class);
	private static final Map<String, ArrayList<Integer>> clients = new HashMap<>();
	
	public static boolean canLogin(final String ip)
	{
		if (!clients.containsKey(ip))
		{
			clients.put(ip, new ArrayList<Integer>());
			clients.get(ip).add((int) (System.currentTimeMillis() / 1000));
			return true;
		}
		
		clients.get(ip).add((int) (System.currentTimeMillis() / 1000));
		
		/*
		 * I am not quite sure because we can have a number of NATed clients with single IP if (currentAttemptTime - lastAttemptTime <= 2) // Time between last login attempt and current less or equal than 2 seconds return false;
		 */
		if (clients.get(ip).size() < Config.BRUT_LOGON_ATTEMPTS)
		{
			return true;
		}
		
		// Calculating average time difference between attempts
		int lastTime = 0;
		int avg = 0;
		for (final int i : clients.get(ip))
		{
			if (lastTime == 0)
			{
				lastTime = i;
				continue;
			}
			avg += i - lastTime;
			lastTime = i;
		}
		avg = avg / (clients.get(ip).size() - 1);
		
		// Minimum average time difference (in seconds) between attempts
		if (avg < Config.BRUT_AVG_TIME)
		{
			LOGGER.warn("IP " + ip + " has " + avg + " seconds between login attempts. Possible BruteForce.");
			// Deleting 2 first elements because if ban will disappear user should have a possibility to logon
			synchronized (clients.get(ip))
			{
				clients.get(ip).remove(0);
				clients.get(ip).remove(0);
			}
			
			return false; // IP have to be banned
		}
		
		synchronized (clients.get(ip))
		{
			clients.get(ip).remove(0);
		}
		
		return true;
	}
}
