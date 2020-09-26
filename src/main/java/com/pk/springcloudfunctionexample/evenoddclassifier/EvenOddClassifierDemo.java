package com.pk.springcloudfunctionexample.evenoddclassifier;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Component
@Configuration
@Slf4j
@RequiredArgsConstructor
public class EvenOddClassifierDemo implements ApplicationRunner {

    private final ApplicationContext ctxt;

    @Override
    public void run(ApplicationArguments args) throws Exception {
		FunctionCatalog catalog = (FunctionCatalog)ctxt.getBean("functionCatalog");
		Function<Flux<Integer>, Tuple2<Flux<Integer>, Flux<Integer>>> intClassifier = catalog.lookup("evenOddClassifier");
		Tuple2<Flux<Integer>, Flux<Integer>> evenAndOdd = intClassifier.apply(intFlux());
		Flux<Integer> evenFlux = evenAndOdd.getT1();
		Flux<Integer> oddFlux = evenAndOdd.getT2();

		evenFlux.subscribe(n -> log.info("Received Even: {}", n));
		oddFlux.subscribe(n -> log.info("Received Odd: {}", n));
	}
	
	@Bean
	public Function<Flux<Integer>, Tuple2<Flux<Integer>, Flux<Integer>>> evenOddClassifier() {
		return inFlux -> {
			Flux<Integer> conFlux = inFlux.publish().autoConnect(2); // auto subscribe after 2 connections
			Flux<Integer> evenFlux = conFlux.filter(n -> n % 2 == 0).subscribeOn(Schedulers.elastic());
			Flux<Integer> oddFlux = conFlux.filter(n -> n % 2 == 1).subscribeOn(Schedulers.elastic());
			return Tuples.of(evenFlux, oddFlux);
		};
	}

	public static Flux<Integer> intFlux() {
		return Flux.fromStream(Stream.generate(incrementNumberSupplier())).doOnEach(
			n -> log.info("Emitting: {}", n.get())
		);
	}

	public static Supplier<Integer> incrementNumberSupplier() {
		AtomicInteger integer = new AtomicInteger(0);
		return () -> {
			// try {
			// 	Thread.sleep(500);
			// } catch (InterruptedException e) {
			// 	e.printStackTrace();
			// }
			return integer.incrementAndGet();
		};
	}
}
