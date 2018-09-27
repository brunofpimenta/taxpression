package com.brunopimenta.taxpression;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import javax.script.ScriptException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.brunopimenta.taxpression.service.ResolveExpressionService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ResolveExpressionServiceTests {

	@Autowired
	private ResolveExpressionService resolveExpressionService;

	@Test
	public void calculateWithOneVariable() throws ScriptException {
		BigDecimal result = resolveExpressionService.calculate( "IPI", Collections.singletonMap( "aliqIPI", "5" ),
				Collections.singletonMap( "valueIPI", "{aliqIPI}/100" ) );

		assertThat( result, is( BigDecimal.valueOf( 0.05 ) ) );
	}

	@Test
	public void calculateOnlyICMS() throws ScriptException {
		Map<String, String> expressions = new TreeMap<>();
		expressions.put( "valueICMS", "({valueBaseCalculo} * {aliqICMS})" );
		expressions.put( "valueTotal", "({valueTotalProducts} - {valueICMS})" );
		expressions.put( "valueBaseCalculo", "({valueTotalProducts})" );

		Map<String, String> declaredValues = new TreeMap<>();
		declaredValues.put( "aliqICMS", "0.25" );
		declaredValues.put( "valueTotalProducts", "100.0" );

		String desiredValue = "total";

		BigDecimal result = resolveExpressionService.calculate( desiredValue, declaredValues, expressions );

		assertThat( result, is( BigDecimal.valueOf( 75.0 ) ) );

	}

}
