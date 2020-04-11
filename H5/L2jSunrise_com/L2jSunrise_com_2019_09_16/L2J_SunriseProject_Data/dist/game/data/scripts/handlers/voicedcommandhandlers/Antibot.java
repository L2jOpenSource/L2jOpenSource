package handlers.voicedcommandhandlers;

import java.util.StringTokenizer;

import l2r.gameserver.handler.IVoicedCommandHandler;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import gr.sr.antibotEngine.AntibotSystem;
import gr.sr.antibotEngine.dynamicHtmls.GenerateHtmls;
import gr.sr.configsEngine.configs.impl.AntibotConfigs;

/**
 * @author -=DoctorNo=-
 */
public class Antibot implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"antibot",
		"captcha",
		"farmcaptcha",
		"enchantcaptcha",
		"enchantbot"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.equalsIgnoreCase("antibot") && activeChar.isFarmBot())
		{
			try
			{
				StringTokenizer st = new StringTokenizer(target);
				String newpass = "";
				String botAnswer = "NULL";
				if (st.hasMoreTokens())
				{
					newpass = st.nextToken();
					if (AntibotConfigs.ENABLE_DOUBLE_PROTECTION)
					{
						botAnswer = st.nextToken();
					}
				}
				
				AntibotSystem.checkFarmCaptchaCode(activeChar, newpass, botAnswer);
			}
			catch (Exception e)
			{
				AntibotSystem.refreshImage(activeChar, true, true);
			}
		}
		if (command.equalsIgnoreCase("enchantbot") && activeChar.isEnchantBot())
		{
			try
			{
				StringTokenizer st = new StringTokenizer(target);
				String newpass = "";
				String botAnswer = "NULL";
				if (st.hasMoreTokens())
				{
					newpass = st.nextToken();
					if (AntibotConfigs.ENABLE_DOUBLE_PROTECTION)
					{
						botAnswer = st.nextToken();
					}
				}
				
				AntibotSystem.checkEnchantCaptchaCode(activeChar, newpass, botAnswer);
			}
			catch (Exception e)
			{
				GenerateHtmls.captchaHtml(activeChar, "ENCHANT");
			}
		}
		if (command.equalsIgnoreCase("farmcaptcha") || (command.equalsIgnoreCase("captcha") && activeChar.isFarmBot()))
		{
			AntibotSystem.refreshImage(activeChar, false, true);
			return false;
		}
		if (command.equalsIgnoreCase("enchantcaptcha") && activeChar.isEnchantBot())
		{
			AntibotSystem.refreshImage(activeChar, false, false);
			return false;
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}