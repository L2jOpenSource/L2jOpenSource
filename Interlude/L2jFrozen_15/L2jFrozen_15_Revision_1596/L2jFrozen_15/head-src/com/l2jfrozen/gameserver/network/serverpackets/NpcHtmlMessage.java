package com.l2jfrozen.gameserver.network.serverpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.cache.HtmCache;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.L2GameClient;

/**
 * The HTML parser in the client knowns these standard and non-standard tags and attributes<br>
 * <li>VOLUMN<br>
 * <li>UNKNOWN<br>
 * <li>UL<br>
 * <li>U<br>
 * <li>TT<br>
 * <li>TR<br>
 * <li>TITLE<br>
 * <li>TEXTCODE<br>
 * <li>TEXTAREA <br>
 * <li>TD<br>
 * <li>TABLE<br>
 * <li>SUP<br>
 * <li>SUB<br>
 * <li>STRIKE<br>
 * <li>SPIN<br>
 * <li>SELECT<br>
 * <li>RIGHT<br>
 * <li>PRE<br>
 * <li>P<br>
 * <li>OPTION<br>
 * <li>OL<br>
 * <li>MULTIEDIT<br>
 * <li>LI<br>
 * <li>LEFT<br>
 * <li>INPUT<br>
 * <li>IMG<br>
 * <li>I <br>
 * <li>HTML<br>
 * <li>H7<br>
 * <li>H6<br>
 * <li>H5<br>
 * <li>H4<br>
 * <li>H3<br>
 * <li>H2<br>
 * <li>H1<br>
 * <li>FONT<br>
 * <li>EXTEND<br>
 * <li>EDIT<br>
 * <li>COMMENT<br>
 * <li>COMBOBOX<br>
 * <li>CENTER<br>
 * <li>BUTTON<br>
 * <li>BR<br>
 * <li>BODY<br>
 * <li>BAR<br>
 * <li>ADDRESS<br>
 * <li>A<br>
 * <li>SEL<br>
 * <li>LIST<br>
 * <li>VAR<br>
 * <li>FORE<br>
 * <li>READONL<br>
 * <li>ROWS<br>
 * <li>VALIGN<br>
 * <li>FIXWIDTH<br>
 * <li>BORDERCOLORLI<br>
 * <li>BORDERCOLORDA<br>
 * <li>BORDERCOLOR<br>
 * <li>BORDER<br>
 * <li>BGCOLOR<br>
 * <li>BACKGROUND<br>
 * <li>ALIGN<br>
 * <li>VALU<br>
 * <li>READONLY<br>
 * <li>MULTIPLE<br>
 * <li>SELECTED<br>
 * <li>TYP <br>
 * <li>TYPE<br>
 * <li>MAXLENGTH<br>
 * <li>CHECKED<br>
 * <li>SRC<br>
 * <li>Y<br>
 * <li>X<br>
 * <li>QUERYDELAY<br>
 * <li>NOSCROLLBAR<br>
 * <li>IMGSRC<br>
 * <li>B<br>
 * <li>FG<br>
 * <li>SIZE<br>
 * <li>FACE<br>
 * <li>COLOR<br>
 * <li>DEFFON<br>
 * <li>DEFFIXEDFONT<br>
 * <li>WIDTH<br>
 * <li>VALUE <br>
 * <li>TOOLTIP<br>
 * <li>NAME<br>
 * <li>MIN<br>
 * <li>MAX<br>
 * <li>HEIGHT<br>
 * <li>DISABLED<br>
 * <li>ALIGN<br>
 * <li>MSG<br>
 * <li>LINK<br>
 * <li>HREF<br>
 * <li>ACTION<br>
 * .
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class NpcHtmlMessage extends L2GameServerPacket
{
	// d S
	// d is usually 0, S is the html text starting with <html> and ending with </html>
	//
	private static final Logger LOGGER = Logger.getLogger(NpcHtmlMessage.class);
	private final int npcObjId;
	private String html;
	private final boolean validate = true;
	private String filePath;
	
	public NpcHtmlMessage(final int npcObjId, final String text)
	{
		this.npcObjId = npcObjId;
		setHtml(text);
	}
	
	/**
	 * Instantiates a new npc html message.
	 * @param npcObjId the npc obj id
	 */
	public NpcHtmlMessage(final int npcObjId)
	{
		this.npcObjId = npcObjId;
	}
	
	@Override
	public void runImpl()
	{
		if (Config.BYPASS_VALIDATION && validate)
		{
			buildBypassCache(getClient().getActiveChar());
			buildLinksCache(getClient().getActiveChar());
		}
	}
	
	/**
	 * Sets the html.
	 * @param text the new html
	 */
	public void setHtml(String text)
	{
		if (text == null)
		{
			LOGGER.warn("Html is null! this will crash the client!");
			html = "<html><body></body></html>";
			return;
		}
		
		if (text.length() > 8192)
		{
			LOGGER.warn("Html is too long! this will crash the client!");
			html = "<html><body>Html was too long,<br>Try to use DB for this action</body></html>";
			return;
		}
		
		html = text; // html code must not exceed 8192 bytes
	}
	
	/**
	 * Sets the file.
	 * @param  path the path
	 * @return      true, if successful
	 */
	public boolean setFile(final String path)
	{
		final String content = HtmCache.getInstance().getHtm(path);
		filePath = path;
		
		if (content == null)
		{
			setHtml("<html><body>My Text is missing:<br>" + path + "</body></html>");
			LOGGER.warn("missing html page " + path);
			return false;
		}
		
		setHtml(content);
		return true;
	}
	
	/**
	 * Replace.
	 * @param pattern the pattern
	 * @param value   the value
	 */
	public void replace(final String pattern, final String value)
	{
		html = html.replaceAll(pattern, value);
	}
	
	public void replace(final String pattern, final boolean val)
	{
		replace(pattern, String.valueOf(val));
	}
	
	public void replace(final String pattern, final int val)
	{
		replace(pattern, String.valueOf(val));
	}
	
	public void replace(final String pattern, final long val)
	{
		replace(pattern, String.valueOf(val));
	}
	
	public void replace(final String pattern, final double val)
	{
		replace(pattern, String.valueOf(val));
	}
	
	/**
	 * Builds the bypass cache.
	 * @param activeChar the active char
	 */
	private final void buildBypassCache(final L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return;
		}
		
		activeChar.clearBypass();
		for (int i = 0; i < html.length(); i++)
		{
			int start = html.indexOf("\"bypass ", i);
			int finish = html.indexOf("\"", start + 1);
			
			if (start < 0 || finish < 0)
			{
				break;
			}
			
			if (html.substring(start + 8, start + 10).equals("-h"))
			{
				start += 11;
			}
			else
			{
				start += 8;
			}
			
			i = finish;
			int finish2 = html.indexOf("$", start);
			
			if (finish2 < finish && finish2 > 0)
			{
				activeChar.addBypass2(html.substring(start, finish2).trim());
			}
			else
			{
				activeChar.addBypass(html.substring(start, finish).trim());
			}
		}
	}
	
	/**
	 * Builds the links cache.
	 * @param activeChar the active char
	 */
	private final void buildLinksCache(final L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return;
		}
		
		activeChar.clearLinks();
		final int len = html.length();
		for (int i = 0; i < len; i++)
		{
			final int start = html.indexOf("link", i);
			final int finish = html.indexOf("\"", start);
			
			if (start < 0 || finish < 0)
			{
				break;
			}
			
			i = start;
			activeChar.addLink(html.substring(start + 5, finish).trim());
		}
	}
	
	public void processHtml(L2GameClient paramGameClient)
	{
		L2PcInstance localPlayer = paramGameClient.getActiveChar();
		if (filePath != null)
		{
			if (localPlayer != null)
			{
				if (localPlayer.isGM())
				{
					localPlayer.sendMessage("HTML: " + filePath.replaceAll("data/html/", ""));
				}
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x0f);
		
		writeD(npcObjId);
		writeS(html);
		writeD(0x00);
	}
	
	/**
	 * Gets the content.
	 * @return the content
	 */
	public String getContent()
	{
		return html;
	}
	
	@Override
	public String getType()
	{
		return "[S] 0f NpcHtmlMessage";
	}
}
