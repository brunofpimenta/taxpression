package com.brunopimenta.taxpression;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TaxpressionApplication {

	public static void main(String[] args) {
		SpringApplication.run( TaxpressionApplication.class, args );
	}

	@Bean
	public ScriptEngine createScriptEngine() {
		ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
		return scriptEngineManager.getEngineByName( "JavaScript" );
	}

}
