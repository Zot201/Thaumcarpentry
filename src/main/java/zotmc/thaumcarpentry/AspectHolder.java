package zotmc.thaumcarpentry;

import com.google.common.base.Supplier;

public interface AspectHolder<T> extends Supplier<T> {
	
	public String name();
	
	public boolean isEnabled();
	
	public void registerObjectTag();
	
}
