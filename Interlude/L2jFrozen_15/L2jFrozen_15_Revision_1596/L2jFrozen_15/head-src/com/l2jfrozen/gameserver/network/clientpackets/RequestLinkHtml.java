
package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author zabbix Lets drink to code!
 */
public final class RequestLinkHtml extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(RequestLinkHtml.class);
	private String link;
	
	@Override
	protected void readImpl()
	{
		link = readS();
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance actor = getClient().getActiveChar();
		if (actor == null)
		{
			return;
		}
		
		if (link.contains("..") || !link.contains(".htm"))
		{
			LOGGER.warn("[RequestLinkHtml] hack? link contains prohibited characters: '" + link + "', skipped");
			return;
		}
		
		if (!actor.validateLink(link))
		{
			return;
		}
		
		final NpcHtmlMessage msg = new NpcHtmlMessage(0);
		msg.setFile(link);
		
		sendPacket(msg);
	}
	
	@Override
	public String getType()
	{
		return "[C] 20 RequestLinkHtml";
	}
}
