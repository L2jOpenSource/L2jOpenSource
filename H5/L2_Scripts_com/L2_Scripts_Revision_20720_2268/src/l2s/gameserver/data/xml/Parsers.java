package l2s.gameserver.data.xml;

import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.string.ItemNameHolder;
import l2s.gameserver.data.string.SkillDescHolder;
import l2s.gameserver.data.string.SkillNameHolder;
import l2s.gameserver.data.string.StringsHolder;
import l2s.gameserver.data.xml.holder.BuyListHolder;
import l2s.gameserver.data.xml.holder.MultiSellHolder;
import l2s.gameserver.data.xml.holder.ProductHolder;
import l2s.gameserver.data.xml.parser.*;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.data.xml.holder.SkillHolder;

/**
 * @author VISTALL
 * @date  20:55/30.11.2010
 */
public abstract class Parsers
{
	public static void parseAll()
	{
		HtmCache.getInstance().reload();
		StringsHolder.getInstance().load();
		ItemNameHolder.getInstance().load();
		SkillNameHolder.getInstance().load();
		SkillDescHolder.getInstance().load();
		//
		SkillHolder.getInstance().load(); // - SkillParser.getInstance();
		OptionDataParser.getInstance().load();
		VariationDataParser.getInstance().load();
		ItemParser.getInstance().load();
		RecipeParser.getInstance().load();
		//
		LevelBonusParser.getInstance().load();
		PlayerTemplateParser.getInstance().load();
		ClassDataParser.getInstance().load();
		NpcParser.getInstance().load();

		DomainParser.getInstance().load();
		RestartPointParser.getInstance().load();

		StaticObjectParser.getInstance().load();
		DoorParser.getInstance().load();
		ZoneParser.getInstance().load();
		SpawnParser.getInstance().load();
		InstantZoneParser.getInstance().load();

		ReflectionManager.getInstance();
		//
		AirshipDockParser.getInstance().load();
		SkillAcquireParser.getInstance().load();
		//
		ResidenceParser.getInstance().load();
		EventParser.getInstance().load();
		FightClubMapParser.getInstance().load();
		// support(cubic & agathion)
		CubicParser.getInstance().load();
		//
		BuyListHolder.getInstance();
		MultiSellHolder.getInstance();
		ProductHolder.getInstance();
		// AgathionParser.getInstance();
		// item support
		HennaParser.getInstance().load();
		EnchantItemParser.getInstance().load();
		EnchantStoneParser.getInstance().load();
		SoulCrystalParser.getInstance().load();
		ArmorSetsParser.getInstance().load();
		FishDataParser.getInstance().load();

		PremiumAccountParser.getInstance().load();
		
		// etc
		PetitionGroupParser.getInstance().load();

		// Fake players
		FakeItemParser.getInstance().load();
		FakePlayersParser.getInstance().load();
	}
}
