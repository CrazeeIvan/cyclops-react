package com.aol.simple.react.collectors.lazy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Builder;
import lombok.experimental.Wither;

import com.aol.simple.react.async.future.FastFuture;
import com.aol.simple.react.config.MaxActive;
import com.aol.simple.react.stream.traits.BlockingStream;

/**
 * This class allows a Batch of completable futures to be processed before collecting their results, to increase
 * parallelism.
 * 
 * @author johnmcclean
 *
 * @param <T> Result type
 */
@Wither
@AllArgsConstructor
@Builder
public class BatchingCollector<T> implements LazyResultConsumer<T>{

	
	@Getter
	private final Collection<FastFuture<T>> results;
	private final List<FastFuture<T>> active = new ArrayList<>();
	@Getter
	private final MaxActive maxActive;
	@Getter
	private final BlockingStream<T> blocking;
	
	/**
	 * @param maxActive Controls batch size
	 */
	public BatchingCollector(MaxActive maxActive,BlockingStream<T> blocking){
		this.maxActive = maxActive;
		this.results =null;
		this.blocking = blocking;
	}
	/**
	 * Batching Collector with default Max Active settings
	 */
	public BatchingCollector(BlockingStream<T> blocking){
		this.maxActive = MaxActive.defaultValue.factory.getInstance();
		this.results =null;
		this.blocking = blocking;
	}
	
	/* (non-Javadoc)
	 * @see java.util.function.Consumer#accept(java.lang.Object)
	 */
	@Override
	public void accept(FastFuture<T> t) {
		if(t.isDone()){
			results.add(t);
			return;
		}
		active.add(t);
		
		if(active.size()>maxActive.getMaxActive()){
			
			while(active.size()>maxActive.getReduceTo()){
				
				
				List<FastFuture<T>> toRemove = active.stream().filter(cf -> cf.isDone()).collect(Collectors.toList());
				active.removeAll(toRemove);
				results.addAll(toRemove);
				if(active.size()>maxActive.getReduceTo()){
					CompletableFuture promise=  new CompletableFuture();
					CompletableFuture.anyOf(active.toArray(new CompletableFuture[0]))
									.thenAccept(cf -> promise.complete(true))
									.exceptionally(e->{ promise.complete(true); return null;});
					
					promise.join();
				}
				
			}
		}
		
		
		
	}
	
	/* (non-Javadoc)
	 * @see com.aol.simple.react.collectors.lazy.LazyResultConsumer#getResults()
	 */
	public Collection<FastFuture<T>> getResults(){
		
		return results;
	}
	/* 
	 *	@return all results (including active)
	 * @see com.aol.simple.react.collectors.lazy.LazyResultConsumer#getAllResults()
	 */
	public Collection<FastFuture<T>> getAllResults(){
		results.addAll(active);
		return results;
	}	
	
}