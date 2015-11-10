package ru.myrest.rate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Perminov Sergey
 *
 */
@Controller
@EnableAutoConfiguration
public class CurrencyRateController 
{
	private final static Logger logger = LoggerFactory.getLogger(CurrencyRateController.class);

	
	@RequestMapping(value = {"/currency/api/rate", "/currency/api/rate/{code}", "/currency/api/rate/{code}/{date}"}, 
			method = RequestMethod.GET, produces="application/json")
    @ResponseBody 
    ResponseEntity<String> getRate(@PathVariable Map<String, String> pathVarsMap) {
		try {
			String code = pathVarsMap.get("code");
			String date = pathVarsMap.get("date");
			logger.trace("Получен запрос с параметрами code: {}, date: {}.", code, date);
			String error = validatePathVariables(code, date);
			if (error != null) {
				logger.warn(error);
				return new ResponseEntity<String>(error, HttpStatus.BAD_REQUEST);
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date currencyDate = null;
			if (date != null) {
				currencyDate = sdf.parse(date);
			} else {
				currencyDate = DateUtils.addDays(new Date(), 1);
			}
			Map<String, CurrencyRate> currs = CBWSUtil.getCbRates(currencyDate);
			if (currs.isEmpty()) {
				String errorMess = String.format("Курсы валют не найдены на дату = %tF.", currencyDate);
				logger.warn(errorMess);
				return new ResponseEntity<String>(errorMess, HttpStatus.BAD_REQUEST);
			}
			if (!currs.containsKey(code)) {
				String errorMess = String.format("Курс валюты для кода '%s' не задан.", code);
				logger.warn(errorMess);
				return new ResponseEntity<String>(errorMess, HttpStatus.BAD_REQUEST);
			}
			ObjectMapper objMapper = new ObjectMapper();
			CurrencyRate rate = currs.get(code);
	        return new ResponseEntity<String>(objMapper.writeValueAsString(rate), HttpStatus.OK);
		} catch (Throwable e) {
			logger.error("Ошибка при оработке запроса.", e);
			return new ResponseEntity<String>("На сервере произошла ошибка.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
    }
	
	private String validatePathVariables(String code, String date) {
		if (code == null) {
			String error = "В запросе не указан код валюты.";
			return error;
		}
		if (!Pattern.matches("[A-Z]{3,3}", code)) {
			String error = String.format("Код валюты должен состоять из трех латинских символов в верхнем регистре: %s.", code);
			return error;
		}
		if (date != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			boolean isValidDate = true;
			if (date.length() != 10) {
				isValidDate = false;
			} else {
				try {
					sdf.parse(date);
				} catch (ParseException e) {
					isValidDate = false;
				}
			}
			if (!isValidDate) {
				String error = String.format("Формат даты должен быть yyyy-MM-dd: %s.", date);
				return error;
				
			}
		}
		return null;
	}
	
	public static void main( String[] args )
    {
		SpringApplication.run(CurrencyRateController.class, args);
		logger.info("Server started!");
    }
}
