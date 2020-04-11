package l2r.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import l2r.gameserver.data.xml.impl.RecipeData;
import l2r.gameserver.model.ItemInfo;
import l2r.gameserver.model.L2RecipeList;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.L2EtcItem;
import l2r.gameserver.model.items.L2Item;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.items.type.CrystalType;
import l2r.gameserver.model.items.type.EtcItemType;
import l2r.gameserver.model.items.type.MaterialType;

public class SortedWareHouseWithdrawalList extends AbstractItemPacket
{
	public static final int PRIVATE = 1;
	public static final int CLAN = 2;
	public static final int CASTLE = 3; // not sure
	public static final int FREIGHT = 4; // not sure
	
	private L2PcInstance _activeChar;
	private long _playerAdena;
	private List<ItemInfo> _objects = new ArrayList<>();
	private int _whType;
	private byte _sortorder;
	private WarehouseListType _itemtype;
	
	public static enum WarehouseListType
	{
		WEAPON,
		ARMOR,
		ETCITEM,
		MATERIAL,
		RECIPE,
		AMULETT,
		SPELLBOOK,
		SHOT,
		SCROLL,
		CONSUMABLE,
		SEED,
		POTION,
		QUEST,
		PET,
		OTHER,
		ALL
	}
	
	/** sort order A..Z */
	public static final byte A2Z = 1;
	/** sort order Z..A */
	public static final byte Z2A = -1;
	/** sort order Grade non..S */
	public static final byte GRADE = 2;
	/** sort order Recipe Level 1..9 */
	public static final byte LEVEL = 3;
	/** sort order type */
	public static final byte TYPE = 4;
	/** sort order body part (wearing) */
	public static final byte WEAR = 5;
	/** Maximum Items to put into list */
	public static final int MAX_SORT_LIST_ITEMS = 300;
	
	/**
	 * This will instantiate the Warehouselist the Player asked for
	 * @param player who calls for the itemlist
	 * @param type is the Warehouse Type
	 * @param itemtype is the Itemtype to sort for
	 * @param sortorder is the integer Sortorder like 1 for A..Z (use public constant)
	 */
	public SortedWareHouseWithdrawalList(L2PcInstance player, int type, WarehouseListType itemtype, byte sortorder)
	{
		_activeChar = player;
		_whType = type;
		_itemtype = itemtype;
		_sortorder = sortorder;
		
		_playerAdena = _activeChar.getAdena();
		if (_activeChar.getActiveWarehouse() == null)
		{
			// Something went wrong!
			_log.warn("error while sending withdraw request to: " + _activeChar.getName());
			return;
		}
		
		switch (_itemtype)
		{
			case WEAPON:
				_objects = createWeaponList(_activeChar.getActiveWarehouse().getItems());
				break;
			case ARMOR:
				_objects = createArmorList(_activeChar.getActiveWarehouse().getItems());
				break;
			case ETCITEM:
				_objects = createEtcItemList(_activeChar.getActiveWarehouse().getItems());
				break;
			case MATERIAL:
				_objects = createMatList(_activeChar.getActiveWarehouse().getItems());
				break;
			case RECIPE:
				_objects = createRecipeList(_activeChar.getActiveWarehouse().getItems());
				break;
			case AMULETT:
				_objects = createAmulettList(_activeChar.getActiveWarehouse().getItems());
				break;
			case SPELLBOOK:
				_objects = createSpellbookList(_activeChar.getActiveWarehouse().getItems());
				break;
			case CONSUMABLE:
				_objects = createConsumableList(_activeChar.getActiveWarehouse().getItems());
				break;
			case SHOT:
				_objects = createShotList(_activeChar.getActiveWarehouse().getItems());
				break;
			case SCROLL:
				_objects = createScrollList(_activeChar.getActiveWarehouse().getItems());
				break;
			case SEED:
				_objects = createSeedList(_activeChar.getActiveWarehouse().getItems());
				break;
			case OTHER:
				_objects = createOtherList(_activeChar.getActiveWarehouse().getItems());
				break;
			case ALL:
			default:
				_objects = createAllList(_activeChar.getActiveWarehouse().getItems());
				break;
		}
		
		try
		{
			switch (_sortorder)
			{
				case A2Z:
				case Z2A:
					Collections.sort(_objects, new WarehouseItemNameComparator(_sortorder));
					break;
				case GRADE:
					if ((_itemtype == WarehouseListType.ARMOR) || (_itemtype == WarehouseListType.WEAPON))
					{
						Collections.sort(_objects, new WarehouseItemNameComparator(A2Z));
						Collections.sort(_objects, new WarehouseItemGradeComparator(A2Z));
					}
					break;
				case LEVEL:
					if (_itemtype == WarehouseListType.RECIPE)
					{
						Collections.sort(_objects, new WarehouseItemNameComparator(A2Z));
						Collections.sort(_objects, new WarehouseItemRecipeComparator(A2Z));
					}
					break;
				case TYPE:
					if (_itemtype == WarehouseListType.MATERIAL)
					{
						Collections.sort(_objects, new WarehouseItemNameComparator(A2Z));
						Collections.sort(_objects, new WarehouseItemTypeComparator(A2Z));
					}
					break;
				case WEAR:
					if (_itemtype == WarehouseListType.ARMOR)
					{
						Collections.sort(_objects, new WarehouseItemNameComparator(A2Z));
						Collections.sort(_objects, new WarehouseItemBodypartComparator(A2Z));
					}
					break;
			}
		}
		catch (Exception e)
		{
		}
	}
	
	/**
	 * This public method return the integer of the Sortorder by its name. If you want to have another, add the Comparator and the Constant.
	 * @param order
	 * @return the integer of the sortorder or 1 as default value
	 */
	public static byte getOrder(String order)
	{
		if (order == null)
		{
			return A2Z;
		}
		else if (order.startsWith("A2Z"))
		{
			return A2Z;
		}
		else if (order.startsWith("Z2A"))
		{
			return Z2A;
		}
		else if (order.startsWith("GRADE"))
		{
			return GRADE;
		}
		else if (order.startsWith("TYPE"))
		{
			return TYPE;
		}
		else if (order.startsWith("WEAR"))
		{
			return WEAR;
		}
		else
		{
			try
			{
				return Byte.parseByte(order);
			}
			catch (NumberFormatException ex)
			{
				return A2Z;
			}
		}
	}
	
	/**
	 * This is the common Comparator to sort the items by Name
	 */
	private static class WarehouseItemNameComparator implements Comparator<ItemInfo>
	{
		private byte order = 0;
		
		protected WarehouseItemNameComparator(byte sortOrder)
		{
			order = sortOrder;
		}
		
		@Override
		public int compare(ItemInfo o1, ItemInfo o2)
		{
			if ((o1.getItem().getType2() == L2Item.TYPE2_MONEY) && (o2.getItem().getType2() != L2Item.TYPE2_MONEY))
			{
				return (order == A2Z ? Z2A : A2Z);
			}
			if ((o2.getItem().getType2() == L2Item.TYPE2_MONEY) && (o1.getItem().getType2() != L2Item.TYPE2_MONEY))
			{
				return (order == A2Z ? A2Z : Z2A);
			}
			String s1 = o1.getItem().getName();
			String s2 = o2.getItem().getName();
			return (order == A2Z ? s1.compareTo(s2) : s2.compareTo(s1));
		}
	}
	
	/**
	 * This Comparator is used to sort by Recipe Level
	 */
	private static class WarehouseItemRecipeComparator implements Comparator<ItemInfo>
	{
		private int order = 0;
		
		private RecipeData rd = null;
		
		protected WarehouseItemRecipeComparator(int sortOrder)
		{
			order = sortOrder;
			rd = RecipeData.getInstance();
		}
		
		@Override
		public int compare(ItemInfo o1, ItemInfo o2)
		{
			if ((o1.getItem().getType2() == L2Item.TYPE2_MONEY) && (o2.getItem().getType2() != L2Item.TYPE2_MONEY))
			{
				return (order == A2Z ? Z2A : A2Z);
			}
			if ((o2.getItem().getType2() == L2Item.TYPE2_MONEY) && (o1.getItem().getType2() != L2Item.TYPE2_MONEY))
			{
				return (order == A2Z ? A2Z : Z2A);
			}
			if (((o1.getItem() instanceof L2EtcItem) && (o1.getItem().getItemType() == EtcItemType.RECIPE)) && ((o2.getItem() instanceof L2EtcItem) && (o2.getItem().getItemType() == EtcItemType.RECIPE)))
			{
				try
				{
					L2RecipeList rp1 = rd.getRecipeByItemId(o1.getItem().getId());
					L2RecipeList rp2 = rd.getRecipeByItemId(o2.getItem().getId());
					
					if (rp1 == null)
					{
						return (order == A2Z ? A2Z : Z2A);
					}
					if (rp2 == null)
					{
						return (order == A2Z ? Z2A : A2Z);
					}
					
					Integer i1 = rp1.getLevel();
					Integer i2 = rp2.getLevel();
					
					return (order == A2Z ? i1.compareTo(i2) : i2.compareTo(i1));
				}
				catch (Exception e)
				{
					return 0;
				}
			}
			
			String s1 = o1.getItem().getName();
			String s2 = o2.getItem().getName();
			return (order == A2Z ? s1.compareTo(s2) : s2.compareTo(s1));
		}
	}
	
	/**
	 * This Comparator is used to sort the Items by BodyPart
	 */
	private static class WarehouseItemBodypartComparator implements Comparator<ItemInfo>
	{
		private byte order = 0;
		
		protected WarehouseItemBodypartComparator(byte sortOrder)
		{
			order = sortOrder;
		}
		
		@Override
		public int compare(ItemInfo o1, ItemInfo o2)
		{
			if ((o1.getItem().getType2() == L2Item.TYPE2_MONEY) && (o2.getItem().getType2() != L2Item.TYPE2_MONEY))
			{
				return (order == A2Z ? Z2A : A2Z);
			}
			if ((o2.getItem().getType2() == L2Item.TYPE2_MONEY) && (o1.getItem().getType2() != L2Item.TYPE2_MONEY))
			{
				return (order == A2Z ? A2Z : Z2A);
			}
			Integer i1 = o1.getItem().getBodyPart();
			Integer i2 = o2.getItem().getBodyPart();
			return (order == A2Z ? i1.compareTo(i2) : i2.compareTo(i1));
		}
	}
	
	/**
	 * This Comparator is used to sort by the Item Grade (e.g. Non..S-Grade)
	 */
	private static class WarehouseItemGradeComparator implements Comparator<ItemInfo>
	{
		byte order = 0;
		
		protected WarehouseItemGradeComparator(byte sortOrder)
		{
			order = sortOrder;
		}
		
		@Override
		public int compare(ItemInfo o1, ItemInfo o2)
		{
			if ((o1.getItem().getType2() == L2Item.TYPE2_MONEY) && (o2.getItem().getType2() != L2Item.TYPE2_MONEY))
			{
				return (order == A2Z ? Z2A : A2Z);
			}
			if ((o2.getItem().getType2() == L2Item.TYPE2_MONEY) && (o1.getItem().getType2() != L2Item.TYPE2_MONEY))
			{
				return (order == A2Z ? A2Z : Z2A);
			}
			CrystalType i1 = o1.getItem().getItemGrade();
			CrystalType i2 = o2.getItem().getItemGrade();
			return (order == A2Z ? i1.compareTo(i2) : i2.compareTo(i1));
		}
	}
	
	/**
	 * This Comparator will sort by Item Type. Unfortunatly this will only have a good result if the Database Table for the ETCITEM.TYPE column is fixed!
	 */
	private static class WarehouseItemTypeComparator implements Comparator<ItemInfo>
	{
		byte order = 0;
		
		protected WarehouseItemTypeComparator(byte sortOrder)
		{
			order = sortOrder;
		}
		
		@Override
		public int compare(ItemInfo o1, ItemInfo o2)
		{
			if ((o1.getItem().getType2() == L2Item.TYPE2_MONEY) && (o2.getItem().getType2() != L2Item.TYPE2_MONEY))
			{
				return (order == A2Z ? Z2A : A2Z);
			}
			if ((o2.getItem().getType2() == L2Item.TYPE2_MONEY) && (o1.getItem().getType2() != L2Item.TYPE2_MONEY))
			{
				return (order == A2Z ? A2Z : Z2A);
			}
			try
			{
				MaterialType i1 = o1.getItem().getMaterialType();
				MaterialType i2 = o2.getItem().getMaterialType();
				return (order == A2Z ? i1.compareTo(i2) : i2.compareTo(i1));
			}
			catch (Exception e)
			{
				return 0;
			}
		}
	}
	
	/**
	 * This method is used to limit the given Warehouse List to:
	 * <li>Weapon</li>
	 * <li>Arrow</li>
	 * <li>Money</li>
	 * @param _items complete Warehouse List
	 * @return limited Item List
	 */
	private List<ItemInfo> createWeaponList(L2ItemInstance[] _items)
	{
		List<ItemInfo> list = new ArrayList<>();
		for (L2ItemInstance item : _items)
		{
			if (item.isWeapon() || (item.getItem().getType2() == L2Item.TYPE2_WEAPON) || (item.isEtcItem() && (item.getItemType() == EtcItemType.ARROW)) || (item.getItem().getType2() == L2Item.TYPE2_MONEY))
			{
				if (list.size() < MAX_SORT_LIST_ITEMS)
				{
					list.add(new ItemInfo(item));
				}
				else
				{
					continue;
				}
			}
		}
		return list;
	}
	
	/**
	 * This method is used to limit the given Warehouse List to:
	 * <li>Armor</li>
	 * <li>Money</li>
	 * @param _items complete Warehouse List
	 * @return limited Item List
	 */
	private List<ItemInfo> createArmorList(L2ItemInstance[] _items)
	{
		List<ItemInfo> list = new ArrayList<>();
		for (L2ItemInstance item : _items)
		{
			if (item.isArmor() || (item.getItem().getType2() == L2Item.TYPE2_MONEY))
			{
				if (list.size() < MAX_SORT_LIST_ITEMS)
				{
					list.add(new ItemInfo(item));
				}
				else
				{
					continue;
				}
			}
		}
		return list;
	}
	
	/**
	 * This method is used to limit the given Warehouse List to:
	 * <li>Everything which is no Weapon/Armor</li>
	 * <li>Money</li>
	 * @param _items complete Warehouse List
	 * @return limited Item List
	 */
	private List<ItemInfo> createEtcItemList(L2ItemInstance[] _items)
	{
		List<ItemInfo> list = new ArrayList<>();
		for (L2ItemInstance item : _items)
		{
			if (item.isEtcItem() || (item.getItem().getType2() == L2Item.TYPE2_MONEY))
			{
				if (list.size() < MAX_SORT_LIST_ITEMS)
				{
					list.add(new ItemInfo(item));
				}
				else
				{
					continue;
				}
			}
		}
		return list;
	}
	
	/**
	 * This method is used to limit the given Warehouse List to:
	 * <li>Materials</li>
	 * <li>Money</li>
	 * @param _items complete Warehouse List
	 * @return limited Item List
	 */
	private List<ItemInfo> createMatList(L2ItemInstance[] _items)
	{
		List<ItemInfo> list = new ArrayList<>();
		for (L2ItemInstance item : _items)
		{
			if ((item.isEtcItem() && (item.getEtcItem().getItemType() == EtcItemType.MATERIAL)) || (item.getItem().getType2() == L2Item.TYPE2_MONEY))
			{
				if (list.size() < MAX_SORT_LIST_ITEMS)
				{
					list.add(new ItemInfo(item));
				}
				else
				{
					continue;
				}
			}
		}
		return list;
	}
	
	/**
	 * This method is used to limit the given Warehouse List to:
	 * <li>Recipes</li>
	 * <li>Money</li>
	 * @param _items complete Warehouse List
	 * @return limited Item List
	 */
	private List<ItemInfo> createRecipeList(L2ItemInstance[] _items)
	{
		List<ItemInfo> list = new ArrayList<>();
		for (L2ItemInstance item : _items)
		{
			if ((item.isEtcItem() && (item.getEtcItem().getItemType() == EtcItemType.RECIPE)) || (item.getItem().getType2() == L2Item.TYPE2_MONEY))
			{
				if (list.size() < MAX_SORT_LIST_ITEMS)
				{
					list.add(new ItemInfo(item));
				}
				else
				{
					continue;
				}
			}
		}
		return list;
	}
	
	/**
	 * This method is used to limit the given Warehouse List to:
	 * <li>Amulett</li>
	 * <li>Money</li>
	 * @param _items complete Warehouse List
	 * @return limited Item List
	 */
	private List<ItemInfo> createAmulettList(L2ItemInstance[] _items)
	{
		List<ItemInfo> list = new ArrayList<>();
		for (L2ItemInstance item : _items)
		{
			if ((item.isEtcItem() && (item.getItemName().toUpperCase().startsWith("AMULET"))) || (item.getItem().getType2() == L2Item.TYPE2_MONEY))
			{
				if (list.size() < MAX_SORT_LIST_ITEMS)
				{
					list.add(new ItemInfo(item));
				}
				else
				{
					continue;
				}
			}
		}
		return list;
	}
	
	/**
	 * This method is used to limit the given Warehouse List to:
	 * <li>Spellbook & Dwarven Drafts</li>
	 * <li>Money</li>
	 * @param _items complete Warehouse List
	 * @return limited Item List
	 */
	private List<ItemInfo> createSpellbookList(L2ItemInstance[] _items)
	{
		List<ItemInfo> list = new ArrayList<>();
		for (L2ItemInstance item : _items)
		{
			if ((item.isEtcItem() && (!item.getItemName().toUpperCase().startsWith("AMULET"))) || (item.getItem().getType2() == L2Item.TYPE2_MONEY))
			{
				if (list.size() < MAX_SORT_LIST_ITEMS)
				{
					list.add(new ItemInfo(item));
				}
				else
				{
					continue;
				}
			}
		}
		return list;
	}
	
	/**
	 * This method is used to limit the given Warehouse List to:
	 * <li>Consumables (Potions, Shots, ...)</li>
	 * <li>Money</li>
	 * @param _items complete Warehouse List
	 * @return limited Item List
	 */
	private List<ItemInfo> createConsumableList(L2ItemInstance[] _items)
	{
		List<ItemInfo> list = new ArrayList<>();
		for (L2ItemInstance item : _items)
		{
			if ((item.isEtcItem() && ((item.getEtcItem().getItemType() == EtcItemType.SCROLL) || (item.getEtcItem().getItemType() == EtcItemType.SHOT))) || (item.getItem().getType2() == L2Item.TYPE2_MONEY))
			{
				if (list.size() < MAX_SORT_LIST_ITEMS)
				{
					list.add(new ItemInfo(item));
				}
				else
				{
					continue;
				}
			}
		}
		return list;
	}
	
	/**
	 * This method is used to limit the given Warehouse List to:
	 * <li>Shots</li>
	 * <li>Money</li>
	 * @param _items complete Warehouse List
	 * @return limited Item List
	 */
	private List<ItemInfo> createShotList(L2ItemInstance[] _items)
	{
		List<ItemInfo> list = new ArrayList<>();
		for (L2ItemInstance item : _items)
		{
			if ((item.isEtcItem() && (item.getEtcItem().getItemType() == EtcItemType.SHOT)) || (item.getItem().getType2() == L2Item.TYPE2_MONEY))
			{
				if (list.size() < MAX_SORT_LIST_ITEMS)
				{
					list.add(new ItemInfo(item));
				}
				else
				{
					continue;
				}
			}
		}
		return list;
	}
	
	/**
	 * This method is used to limit the given Warehouse List to:
	 * <li>Scrolls/Potions</li>
	 * <li>Money</li>
	 * @param _items complete Warehouse List
	 * @return limited Item List
	 */
	private List<ItemInfo> createScrollList(L2ItemInstance[] _items)
	{
		List<ItemInfo> list = new ArrayList<>();
		for (L2ItemInstance item : _items)
		{
			if ((item.isEtcItem() && (item.getEtcItem().getItemType() == EtcItemType.SCROLL)) || (item.getItem().getType2() == L2Item.TYPE2_MONEY))
			{
				if (list.size() < MAX_SORT_LIST_ITEMS)
				{
					list.add(new ItemInfo(item));
				}
				else
				{
					continue;
				}
			}
		}
		return list;
	}
	
	/**
	 * This method is used to limit the given Warehouse List to:
	 * <li>Seeds</li>
	 * <li>Money</li>
	 * @param _items complete Warehouse List
	 * @return limited Item List
	 */
	private List<ItemInfo> createSeedList(L2ItemInstance[] _items)
	{
		List<ItemInfo> list = new ArrayList<>();
		for (L2ItemInstance item : _items)
		{
			if ((item.isEtcItem() && (item.getEtcItem().getItemType() == EtcItemType.SEED)) || (item.getItem().getType2() == L2Item.TYPE2_MONEY))
			{
				if (list.size() < MAX_SORT_LIST_ITEMS)
				{
					list.add(new ItemInfo(item));
				}
				else
				{
					continue;
				}
			}
		}
		return list;
	}
	
	/**
	 * This method is used to limit the given Warehouse List to:
	 * <li>Everything which is no Weapon/Armor, Material, Recipe, Spellbook, Scroll or Shot</li>
	 * <li>Money</li>
	 * @param _items complete Warehouse List
	 * @return limited Item List
	 */
	private List<ItemInfo> createOtherList(L2ItemInstance[] _items)
	{
		List<ItemInfo> list = new ArrayList<>();
		for (L2ItemInstance item : _items)
		{
			if ((item.isEtcItem() && ((item.getEtcItem().getItemType() != EtcItemType.MATERIAL) && (item.getEtcItem().getItemType() != EtcItemType.RECIPE) && (item.getEtcItem().getItemType() != EtcItemType.SCROLL) && (item.getEtcItem().getItemType() != EtcItemType.SHOT))) || (item.getItem().getType2() == L2Item.TYPE2_MONEY))
			{
				if (list.size() < MAX_SORT_LIST_ITEMS)
				{
					list.add(new ItemInfo(item));
				}
				else
				{
					continue;
				}
			}
		}
		return list;
	}
	
	/**
	 * This method is used to limit the given Warehouse List to:
	 * <li>no limit</li> This may sound strange but we return the given Array as a List<ItemInfo>
	 * @param _items complete Warehouse List
	 * @return limited Item List
	 */
	private List<ItemInfo> createAllList(L2ItemInstance[] _items)
	{
		List<ItemInfo> list = new ArrayList<>();
		for (L2ItemInstance item : _items)
		{
			if (list.size() < MAX_SORT_LIST_ITEMS)
			{
				list.add(new ItemInfo(item));
			}
			else
			{
				continue;
			}
		}
		return list;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x42);
		
		writeH(_whType);
		writeQ(_playerAdena);
		writeH(_objects.size());
		
		for (ItemInfo item : _objects)
		{
			writeItem(item);
			writeD(item.getObjectId());
		}
	}
}
