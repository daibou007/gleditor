package animation.world;

/**
 * ����
 * 
 * @author ����
 * 
 * @time 2012-9-24
 * 
 */
public interface Prototype<E> {
	public abstract E clone();

	public abstract E clone(E obj);
}
