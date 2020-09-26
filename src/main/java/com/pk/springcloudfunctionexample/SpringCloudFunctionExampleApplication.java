package com.pk.springcloudfunctionexample;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringCloudFunctionExampleApplication {
	public static void main(String[] args) throws IOException {
		SpringApplication.run(SpringCloudFunctionExampleApplication.class, args);
		System.in.read();
	}
}
