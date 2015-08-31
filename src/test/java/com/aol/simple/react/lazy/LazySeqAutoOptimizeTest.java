package com.aol.simple.react.lazy;

import static com.aol.simple.react.stream.traits.LazyFutureStream.parallel;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import static org.hamcrest.Matchers.greaterThan;
import org.junit.Test;
import static org.junit.Assert.assertThat;
import com.aol.simple.react.stream.lazy.LazyReact;
import com.aol.simple.react.stream.traits.LazyFutureStream;

public class LazySeqAutoOptimizeTest extends LazySeqTest {
	@Override
	protected <U> LazyFutureStream<U> of(U... array) {
		return new LazyReact()
							.autoOptimiseOn()
							.of(array);
	}
	@Override
	protected <U> LazyFutureStream<U> ofThread(U... array) {
		return new LazyReact()
							.autoOptimiseOn()
							.of(array);
	}

	@Override
	protected <U> LazyFutureStream<U> react(Supplier<U>... array) {
		return new LazyReact().autoOptimiseOn()
								.react(array);
	}
	
	@Test
	public void multi(){
		Set<Long> threads = of(1,2,3,4)
								.map(i->i+2)
								.map(i->i*3)
								.peek(i-> sleep(50))
								.map(i->Thread.currentThread().getId())
								.toSet();
		
		assertThat(threads.size(),greaterThan(0));
	}
	@Test
	public void longRunForEach(){
		new LazyReact().autoOptimiseOn().range(0, 1_000_000)
						.map(i->i+2)
						.map(i->Thread.currentThread().getId())
					//	.peek(System.out::println)
						.forEach(a-> {});
		System.out.println("Finished!");
	}
	@Test
	public void longRun(){
		new LazyReact().autoOptimiseOn().range(0, 1_000_000)
						.map(i->i+2)
						.map(i->Thread.currentThread().getId())
					//	.peek(System.out::println)
						.runOnCurrent();
		System.out.println("Finished!");
	}
}
