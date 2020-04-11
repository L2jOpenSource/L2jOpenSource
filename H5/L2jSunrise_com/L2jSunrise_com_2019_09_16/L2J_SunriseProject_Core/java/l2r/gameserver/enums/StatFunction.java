package l2r.gameserver.enums;

/**
 * @author vGodFather
 */
public enum StatFunction
{
	ADD("Add", 30),
	DIV("Div", 20),
	ENCHANT("Enchant", 0),
	ENCHANTHP("EnchantHp", 40),
	MUL("Mul", 20),
	SET("Set", 0),
	SHARE("Share", 30),
	SUB("Sub", 30),
	BASEMUL("BaseMul", 20),;
	
	String _name;
	int _order;
	
	StatFunction(String name, int order)
	{
		_name = name;
		_order = order;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public int getOrder()
	{
		return _order;
	}
}
