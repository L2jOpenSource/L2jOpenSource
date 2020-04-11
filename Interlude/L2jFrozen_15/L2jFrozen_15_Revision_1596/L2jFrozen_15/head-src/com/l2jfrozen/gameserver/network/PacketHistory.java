package com.l2jfrozen.gameserver.network;

/**
 * @author luisantonioa
 */

import java.util.Date;
import java.util.Map;

import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

class PacketHistory
{
	protected Map<Class<?>, Long> info;
	protected long timeStamp;
	
	protected static final XMLFormat<PacketHistory> PACKET_HISTORY_XML = new XMLFormat<PacketHistory>(PacketHistory.class)
	{
		/**
		 * @see javolution.xml.XMLFormat#read(javolution.xml.XMLFormat.InputElement, java.lang.Object)
		 */
		@Override
		public void read(final InputElement xml, final PacketHistory packetHistory) throws XMLStreamException
		{
			packetHistory.timeStamp = xml.getAttribute("time-stamp", 0);
			packetHistory.info = xml.<Map<Class<?>, Long>> get("info");
		}
		
		/**
		 * @see javolution.xml.XMLFormat#write(java.lang.Object, javolution.xml.XMLFormat.OutputElement)
		 */
		@Override
		public void write(final PacketHistory packetHistory, final OutputElement xml) throws XMLStreamException
		{
			xml.setAttribute("time-stamp", new Date(packetHistory.timeStamp).toString());
			
			for (final Class<?> cls : packetHistory.info.keySet())
			{
				xml.setAttribute(cls.getSimpleName(), packetHistory.info.get(cls));
			}
		}
	};
}
