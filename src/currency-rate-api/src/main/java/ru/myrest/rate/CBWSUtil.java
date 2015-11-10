package ru.myrest.rate;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang3.time.DateUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ru.myrest.cb.ws.client.DailyInfo;
import ru.myrest.cb.ws.client.DailyInfoSoap;
import ru.myrest.cb.ws.client.GetCursOnDateXMLResponse.GetCursOnDateXMLResult;

public class CBWSUtil {
	
	public static Map<String, CurrencyRate> getCbRates(Date rateDate)
    		throws DatatypeConfigurationException, MalformedURLException {
    	Map<String, CurrencyRate> rates = new HashMap<String, CurrencyRate>();
    	
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date trimRateDate = DateUtils.truncate(rateDate, Calendar.DATE);
		String strRateDate = sdf.format(trimRateDate);
    	GregorianCalendar gregory = new GregorianCalendar();
    	gregory.setTime(trimRateDate);

    	XMLGregorianCalendar inDate = DatatypeFactory.newInstance()
    	        .newXMLGregorianCalendar(gregory);
    	
    	URL url = new URL("http://www.cbr.ru/DailyInfoWebServ/DailyInfo.asmx?wsdl");
    	DailyInfo cbService = new DailyInfo(url);
    	DailyInfoSoap cbSoap = cbService.getDailyInfoSoap();
    	GetCursOnDateXMLResult result = cbSoap.getCursOnDateXML(inDate);
    	Element element = (Element) result.getContent().get(0);
    	Node node = element.getFirstChild();
    	while(node != null) {
    		NodeList childrens = node.getChildNodes();
    		String code = null;
    		String rate = null;
    		for (int i = 0; i < childrens.getLength(); i++) {
				Node item = childrens.item(i);
				if ("VchCode".equals(item.getNodeName())) {
					code = item.getFirstChild().getNodeValue().trim();
				} else if ("Vcurs".equals(item.getNodeName())) {
					rate = item.getFirstChild().getNodeValue().trim();
				}
			}
    		if (code != null && rate != null) {
    			CurrencyRate rateItem = new CurrencyRate(code, rate, strRateDate);
    			rates.put(code, rateItem);
			}
    		node = node.getNextSibling();
    	}
		return rates;
	}
}
