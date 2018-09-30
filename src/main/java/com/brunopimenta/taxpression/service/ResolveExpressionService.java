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

import com.brunopimenta.taxpression.exception.KeyNotFoundException;

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
			Map<String, String> storedExpressions) throws ScriptException {
		Map<String, String> expressions = getAllExpressions( informedValues, storedExpressions );

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
			return getExpressionByKey( expressions, getFirstKey( expressionKey ) );

		String nextKey = getNextKey( expression );
		return expression.replace( getExpressionKey( nextKey ), getExpressionByKey( expressions, expressionKey ) );
	}

	private String getExpressionByKey(Map<String, String> expressions, String key) {
		String expression = expressions.get( key );
		if (Objects.isNull( expression ))
			throw new KeyNotFoundException( "Key [" + key + "] was not found!" );
		return expression;
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
