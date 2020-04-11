package l2r.log.formatter;

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.L2GameClient;
import l2r.util.StringUtil;

/**
 * @author vGodFather
 */
public class AccountFormatter
{
	public static String format(String message, Object[] params)
	{
		StringBuilder output = StringUtil.startAppend(30 + message.length() + ((params == null) ? 0 : params.length * 10), new String[]
		{
			message
		});
		
		if (params != null)
		{
			for (Object p : params)
			{
				if (p == null)
				{
					continue;
				}
				
				StringUtil.append(output, " ");
				
				if (p instanceof L2GameClient)
				{
					final L2GameClient client = (L2GameClient) p;
					String address = null;
					try
					{
						if (!client.isDetached())
						{
							address = client.getConnection().getInetAddress().getHostAddress();
						}
					}
					catch (Exception e)
					{
					}
					
					switch (client.getState())
					{
						case IN_GAME:
							if (client.getActiveChar() != null)
							{
								StringUtil.append(output, client.getActiveChar().getName());
								StringUtil.append(output, "(", String.valueOf(client.getActiveChar().getObjectId()), ") ");
							}
							break;
						case AUTHED:
							if (client.getAccountName() != null)
							{
								StringUtil.append(output, "Account:", client.getAccountName(), " ");
							}
							break;
						case CONNECTED:
							if (address != null)
							{
								StringUtil.append(output, address);
							}
							break;
						default:
							throw new IllegalStateException("Missing state on switch");
					}
				}
				else if (p instanceof L2PcInstance)
				{
					L2PcInstance player = (L2PcInstance) p;
					StringUtil.append(output, player.getName());
					StringUtil.append(output, "(", String.valueOf(player.getObjectId()), ")");
				}
				else
				{
					StringUtil.append(output, p.toString());
				}
			}
		}
		return output.toString();
	}
}
