package be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Interface;

/**
 * An abstract class representing a responsibility chain with generic types so that it can be used in different contexts
 * @author colincyr
 *
 * @param <T> the generic type you will want to use for this chain
 */
public abstract class ResponsibilityChain<T> {

	protected ResponsibilityChain<T> next;
	
	/**
	 * This is a constructor that take an ellipse of handlers
	 * @param handlers an iterable element that contains as much T handlers as you wish
	 * @return this (this class was originally planned to use the Builder Design Pattern)
	 */
	@SafeVarargs
	public final ResponsibilityChain<T> build(ResponsibilityChain<T>... handlers) {
		ResponsibilityChain<T> current = this;
		for(ResponsibilityChain<T> nextHandler:handlers) {
			current.next(nextHandler);
			current = nextHandler;
		}
		return this;
	}
	
	/**
	 * Method that allows the emulation of the Builder Design Pattern by taking an handler as parameter
	 * Setting it as next of "this" and returning the handler so that the "setNext" methods can be chained
	 * @param handler the next handler of the chain
	 * @return the next handler of the chain with "this" being set as the parent of that handler
	 */
	public final ResponsibilityChain<T> setNext(ResponsibilityChain<T> handler){
		this.next(handler);
		return handler;
	}
	
	/**
	 * The next method that should be called by your implementation (your handler) if it doesn't end the chain
	 * @param handler a chain implementation
	 */
	public void next(ResponsibilityChain<T> handler) {
		next = handler;
	}
	
	/**
	 * The method that your handler will need to override and that will contain the logic
	 * @param request the data that will be passed to your handler
	 */
	public abstract void handle(T request);
	
}
