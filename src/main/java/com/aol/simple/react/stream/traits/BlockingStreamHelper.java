package com.aol.simple.react.stream.traits;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Stream;

import com.aol.simple.react.async.future.FastFuture;
import com.aol.simple.react.exceptions.ExceptionSoftener;
import com.aol.simple.react.exceptions.FilteredExecutionPathException;
import com.aol.simple.react.stream.EagerStreamWrapper;
import com.aol.simple.react.stream.LazyStreamWrapper;
import com.aol.simple.react.stream.MissingValue;

public class BlockingStreamHelper {
	final static ExceptionSoftener exceptionSoftener = ExceptionSoftener.singleton.factory
			.getInstance();
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static <R> R block(BlockingStream blocking,final Collector collector, final EagerStreamWrapper lastActive) {
		Stream<CompletableFuture> stream = lastActive.stream();
		
		return (R) stream.map((future) -> {
			return  BlockingStreamHelper.getSafe(future,blocking.getErrorHandler());
		}).filter(v -> v != MissingValue.MISSING_VALUE).collect(collector);
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static <R> R block(BlockingStream blocking,final Collector collector, final LazyStreamWrapper lastActive) {
		Stream<FastFuture> stream = lastActive.stream();
		
		return (R)((LazyStream)blocking).run(collector);
		
			
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static <R> R aggregateResults(final Collector collector,
			final List<FastFuture> completedFutures, Optional<Consumer<Throwable>> errorHandler) {
		return (R) completedFutures.stream().map(next -> getSafe(next,errorHandler))
				.filter(v -> v != MissingValue.MISSING_VALUE).collect(collector);
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static <R> R aggregateResultsCompletable(final Collector collector,
			final List<CompletableFuture> completedFutures, Optional<Consumer<Throwable>> errorHandler) {
		return (R) completedFutures.stream().map(next -> getSafe(next,errorHandler))
				.filter(v -> v != MissingValue.MISSING_VALUE).collect(collector);
	}
	//FIXME FOR LAZYFUTURESTREAM implement as an onFail setting on the future during terminal op
	static void capture(final Exception e,Optional<Consumer<Throwable>> errorHandler) {
		errorHandler.ifPresent((handler) -> {
			if (!(e.getCause() instanceof FilteredExecutionPathException)) {
				handler.accept(e.getCause());
			}
		});
	}
	@SuppressWarnings("rawtypes")
	static Object getSafe(final FastFuture next,Optional<Consumer<Throwable>> errorHandler) {
		try {
			return next.join();
		} catch (RuntimeException e) {
			capture(e,errorHandler);
		} catch (Exception e) {
			capture(e,errorHandler);
		}

		return MissingValue.MISSING_VALUE;
	}
	@SuppressWarnings("rawtypes")
	static Object getSafe(final CompletableFuture next,Optional<Consumer<Throwable>> errorHandler) {
		try {
			return next.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			capture(e,errorHandler);
			exceptionSoftener.throwSoftenedException(e);
		} catch (RuntimeException e) {
			capture(e,errorHandler);
		} catch (Exception e) {
			capture(e,errorHandler);
		}

		return MissingValue.MISSING_VALUE;
	}

}
