package com.aol.simple.react.collectors.lazy;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;

import com.aol.simple.react.async.future.FastFuture;
import com.aol.simple.react.stream.MissingValue;
import com.aol.simple.react.stream.lazy.ParallelReductionConfig;
import com.aol.simple.react.stream.traits.BlockingStream;

/**
 * Perform incremental (optionally parallel) reduction on a stream
 * 
 * @author johnmcclean
 *
 * @param <T> Data type
 */
@Getter
@AllArgsConstructor
public class IncrementalReducer<T> {
	private final LazyResultConsumer<T> consumer;
	private final BlockingStream<T> blocking;
	private final ParallelReductionConfig config;
	
	public void forEach(Consumer<? super T> c, Function<FastFuture,T> safeJoin){
		if(consumer.getResults().size()>config.getBatchSize()){
			forEachResults(consumer.getResults(),c);
		}
	}
	public void forEachResults( Collection<FastFuture<T>> results,Consumer<? super T> c) {
		Stream<FastFuture<T>> stream = results.stream();//consumer.getResults().stream();
		Stream<FastFuture<T>> streamToUse = this.config.isParallel() ? stream.parallel() : stream;
		streamToUse.map(f->f.join()).filter(v -> v != MissingValue.MISSING_VALUE).forEach(c);
		consumer.getResults().clear();
	}
	public  T reduce(T identity, BinaryOperator<T> accumulator){
		if(consumer.getResults().size()>config.getBatchSize()){
			 return reduceResults(consumer.getResults(),identity, accumulator);
		}
		
		return identity;
	}
	public T reduceResults( Collection<FastFuture<T>> results,T identity,
			BinaryOperator<T> accumulator) {
		Stream<FastFuture<T>> stream = results.stream();
		 Stream<FastFuture<T>> streamToUse = this.config.isParallel() ? stream.parallel() : stream;
		 T result = streamToUse.map(f->f.join())
					.filter(v -> v != MissingValue.MISSING_VALUE).reduce(identity, accumulator);
		consumer.getResults().clear();
		return result;
	}
	public  Optional<T> reduce( BinaryOperator<T> accumulator){
		if(consumer.getResults().size()>config.getBatchSize()){
			 return reduceResults(consumer.getResults(), accumulator);
		}
		
		return Optional.empty();
	}
	public Optional<T> reduceResults( Collection<FastFuture<T>> results,
			BinaryOperator<T> accumulator) {
		Stream<FastFuture<T>> stream = results.stream();
		 Stream<FastFuture<T>> streamToUse = this.config.isParallel() ? stream.parallel() : stream;
		 Optional<T> result = streamToUse.map(f->f.join())
					.filter(v -> v != MissingValue.MISSING_VALUE).reduce( accumulator);
		consumer.getResults().clear();

		return result;
	}
	public <U> U reduce(U identity, BiFunction<U,? super T,U> accumulator, BinaryOperator<U> combiner){
		if(consumer.getResults().size()>config.getBatchSize()){
			 return reduceResults(consumer.getResults(),identity, accumulator,combiner);
		}
		return identity;
	}
	public <U> U reduceResults( Collection<FastFuture<T>> results, U identity, BiFunction<U,? super T,U> accumulator, BinaryOperator<U> combiner){
		Stream<FastFuture<T>> stream = results.stream();
		 Stream<FastFuture<T>> streamToUse = this.config.isParallel() ? stream.parallel() : stream;
		 U result = streamToUse.map(f->f.join())
					.filter(v -> v != MissingValue.MISSING_VALUE).reduce(identity, accumulator,combiner);
		consumer.getResults().clear();
		return result;
	}
}
