package net.sf.l2j.commons.logging.filter;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

import net.sf.l2j.commons.util.ArraysUtil;

import net.sf.l2j.gameserver.enums.items.EtcItemType;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

public class ItemFilter implements Filter
{
	private static final String EXCLUDE_PROCESS = "Consume";
	private static final EtcItemType[] EXCLUDE_TYPE =
	{
		EtcItemType.ARROW,
		EtcItemType.SHOT,
		EtcItemType.HERB
	};
	
	@Override
	public boolean isLoggable(LogRecord record)
	{
		if (!record.getLoggerName().equals("item"))
			return false;
		
		final String[] messageList = record.getMessage().split(":");
		if (messageList.length < 2 || !EXCLUDE_PROCESS.contains(messageList[1]))
			return true;
		
		final ItemInstance item = ((ItemInstance) record.getParameters()[1]);
		if (!ArraysUtil.contains(EXCLUDE_TYPE, item.getItemType()))
			return true;
		
		return false;
	}
}