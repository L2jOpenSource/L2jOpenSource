package com.l2jfrozen.gameserver.handler.custom;

import java.util.HashMap;
import java.util.Map;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.handler.voicedcommandhandlers.Repair;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * This 'Bypass Handler' is a handy tool indeed!<br>
 * Basically, you can send any custom bypass commmands to it from ANY npc and it will call the appropriate function.<br>
 * <strong>Example:</strong><br>
 * <button value=" Request" action="bypass -h custom_command" width=110 height=36 back="L2UI_ct1.button_df" fore="L2UI_ct1.button_df">
 * @author JStar
 */
public class CustomBypassHandler
{
	private static CustomBypassHandler _instance = null;
	private final Map<String, ICustomByPassHandler> _handlers;
	
	private CustomBypassHandler()
	{
		_handlers = new HashMap<>();
		
		registerCustomBypassHandler(new ExtractableByPassHandler());
		
		if (Config.CHARACTER_REPAIR)
		{
			registerCustomBypassHandler(new Repair());
		}
	}
	
	public static CustomBypassHandler getInstance()
	{
		if (_instance == null)
		{
			_instance = new CustomBypassHandler();
		}
		
		return _instance;
	}
	
	/**
	 * @param handler as ICustomByPassHandler
	 */
	public void registerCustomBypassHandler(ICustomByPassHandler handler)
	{
		for (String s : handler.getByPassCommands())
		{
			_handlers.put(s, handler);
		}
	}
	
	/**
	 * Handles player's Bypass request to the Custom Content.
	 * @param player
	 * @param command
	 */
	public void handleBypass(L2PcInstance player, String command)
	{
		String cmd = "";
		String params = "";
		int iPos = command.indexOf(" ");
		
		if (iPos != -1)
		{
			cmd = command.substring(7, iPos);
			params = command.substring(iPos + 1);
		}
		else
		{
			cmd = command.substring(7);
		}
		
		ICustomByPassHandler ch = _handlers.get(cmd);
		
		if (ch != null)
		{
			ch.handleCommand(cmd, player, params);
		}
	}
}