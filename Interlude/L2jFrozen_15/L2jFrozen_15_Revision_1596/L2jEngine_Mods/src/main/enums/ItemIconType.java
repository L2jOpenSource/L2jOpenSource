package main.enums;

/**
 * @author fissban
 */
public enum ItemIconType
{
	WEAPON("Icon.weapon_"),
	ARMOR("Icon.armor_"),
	ETCITEM("Icon.etc_"),
	SHIELD("Icon.shield_"),
	RECIPE("Icon.etc_recipe"),
	POTION("Icon.etc_potion_");
	
	String searchItem;
	
	private ItemIconType(String searchItem)
	{
		this.searchItem = searchItem;
	}
	
	public String getSearchItem()
	{
		return searchItem;
	}
}
