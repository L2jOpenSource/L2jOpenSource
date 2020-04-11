package l2r.gameserver.handler;

/**
 * @author vGodFather
 * @param <K>
 * @param <V>
 */
public interface IHandler<K, V>
{
	@SuppressWarnings("unchecked")
	default void registerByClass(Class<?> clazz) throws Exception
	{
		final Object object = clazz.getDeclaredConstructor().newInstance();
		registerHandler((K) object);
	}
	
	public void registerHandler(K object);
	
	public void removeHandler(K handler);
	
	public K getHandler(V val);
	
	public int size();
}
