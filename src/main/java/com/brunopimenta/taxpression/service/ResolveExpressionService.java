package com.brunopimenta.taxpression.service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResolveExpressionService {

	private Logger LOGGER = LoggerFactory.getLogger( ResolveExpressionService.class );
	private static final Pattern EXPRESSION_KEY_PATTERN = Pattern.compile( "\\{(\\w|-)+}" );

	private ScriptEngine scriptEngine;

	@Autowired
	public ResolveExpressionService(ScriptEngine scriptEngine) {
		this.scriptEngine = scriptEngine;
	}

	public BigDecimal calculate(String desiredValue, Map<String, String> informedValues,
			Map<String, String> storedExpressions)
			throws ScriptException {
		Map<String, String> expressions = getAllExpressions( informedValues, storedExpressions );

		// 1- cria a key pelo valor de entrada
		// 2- PEga na lista de expressoes a expressao dessa key

		// 3- Extrai da expressao a primeira ocorrencia de key
		// 4- Pega o valor dessa key na lista de expressoes
		// 5- sobrescreve a expressao substituindo a chave pela expressao obtida

		// 6- Extrai a primeira chave dessa expressao
		// 7- pega a expressao referente a essa chave
		// 8- substitui na expressao essa chave pelo valor dela

		String resolvedExpression = resolveCompleteExpression( desiredValue, null, expressions );

		return convertExpression( resolvedExpression );
	}

	private String getFirstKey(String desiredValue) {
		return "value" + desiredValue;
	}

	private String resolveCompleteExpression(String expressionKey, String expression, Map<String, String> expressions) {
		if (Strings.isBlank( expressionKey ))
			return expression;

		String resolvedExpression = getExpression( expressionKey, expression, expressions );
		return resolveCompleteExpression( getNextKey( resolvedExpression ), resolvedExpression, expressions );
	}

	private String getExpression(String expressionKey, String expression, Map<String, String> expressions) {
		if (Objects.isNull( expression ))
			return expressions.get( getFirstKey( expressionKey ) );

		String nextKey = getNextKey( expression );
		return expression.replace( getExpressionKey( nextKey ), expressions.get( expressionKey ) );
	}

	private String getNextKey(String expression) {
		Matcher matcher = EXPRESSION_KEY_PATTERN.matcher( expression );
		String nextExpression = "";
		if (matcher.find())
			nextExpression = matcher.group();
		return nextExpression.replace( "{", "" ).replace( "}", "" );
	}

	private String getExpressionKey(String desiredValue) {
		return "{" + desiredValue + "}";
	}

	private Map<String, String> getAllExpressions(Map<String, String> informedValues,
			Map<String, String> storedExpressions) {
		TreeMap<String, String> expressions = new TreeMap<>( String.CASE_INSENSITIVE_ORDER );
		expressions.putAll( informedValues );
		expressions.putAll( storedExpressions );
		return expressions;
	}

	private BigDecimal convertExpression(String expression) throws ScriptException {
		try {
			Double result = (Double) scriptEngine.eval( expression );
			return BigDecimal.valueOf( result );
		} catch (ScriptException ex) {
			LOGGER.error( "Error parsing expression [{}]. Error: [{}]", expression, ex.getMessage() );
			throw (ex);
		}
	}

}
