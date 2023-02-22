package be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Interface;

public abstract class ResponsibilityChain<T> {

	protected ResponsibilityChain<T> next;
	
	@SafeVarargs
	public final ResponsibilityChain<T> build(ResponsibilityChain<T>... handlers) {
		ResponsibilityChain<T> current = this;
		for(ResponsibilityChain<T> nextHandler:handlers) {
			current.next(nextHandler);
			current = nextHandler;
		}
		return this;
	}
	
	public void next(ResponsibilityChain<T> handler) {
		next = handler;
	}
	
	public abstract void handle(T request);
	
}
