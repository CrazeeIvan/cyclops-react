package com.aol.simple.react.collectors.lazy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;

import com.aol.simple.react.async.future.FastFuture;
import com.aol.simple.react.config.MaxActive;
/**
 * A collector that periodically joins active completablefutures
 * but does not store the results
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
@Wither
//@AllArgsConstructor
public class EmptyCollector<T> implements LazyResultConsumer<T> {
	

	private final List<FastFuture<T>> active = new ArrayList<>();
	@Getter
	private final MaxActive maxActive;
//	private final Function<FastFuture, T> safeJoin; //FIXME Doesn't look like a safejoin!
	
	EmptyCollector(){
		maxActive = MaxActive.defaultValue.factory.getInstance();
		//safeJoin = cf -> (T)cf.join();
	}
	public EmptyCollector(MaxActive maxActive){
		this.maxActive = maxActive;
	//	this.safeJoin = safeJoin;//cf -> (T)cf.join();
	}
	
	/* 
	 *	@param t Result type
	 * @see java.util.function.Consumer#accept(java.lang.Object)
	 */
	@Override
	public void accept(FastFuture<T> t) {
		if(t.isDone()){
		//	if(t.isCompletedExceptionally())
		//		handleExceptions(t);
			return;
		}
		System.out.println("Not done!");
		active.add(t);
		
		if(active.size()>maxActive.getMaxActive()){
			
			while(active.size()>maxActive.getReduceTo()){
				
				
				
				List<FastFuture> toRemove = active.stream()
												  .filter(cf -> cf.isDone())
												  .peek(this::handleExceptions)
												  .collect(Collectors.toList());
				
				active.removeAll(toRemove);
				if(active.size()>maxActive.getReduceTo()){
					CompletableFuture promise=  new CompletableFuture();
					FastFuture.xOf(active.size()-maxActive.getReduceTo(),() -> promise.complete(true),
										active.toArray(new FastFuture[0]));
					
					promise.join();
				}
				
					
			}
		}
		
		
		
	}

	private void handleExceptions(FastFuture cf){
		if(cf.isCompletedExceptionally()){
			// safeJoin.apply(cf);
		}
	}
	
	@Override
	public EmptyCollector<T> withResults(Collection<FastFuture<T>> t) {
		
		return this.withMaxActive(maxActive);
	}

	/* 
	 *	@return empty list
	 * @see com.aol.simple.react.collectors.lazy.LazyResultConsumer#getResults()
	 */
	@Override
	public Collection<FastFuture<T>> getResults() {
	//	active.stream().forEach(cf ->  safeJoin.apply(cf));
		active.clear();
		return new ArrayList<>();
	}
	/* 
	 *	@return empty list
	 * @see com.aol.simple.react.collectors.lazy.LazyResultConsumer#getAllResults()
	 */
	public Collection<FastFuture<T>> getAllResults() {
		return getResults();
	}

	
	
}