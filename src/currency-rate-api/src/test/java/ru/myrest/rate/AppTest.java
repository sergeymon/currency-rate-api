package ru.myrest.rate;

import java.util.Date;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() throws Exception
    {
    	Map<String, CurrencyRate> rates = CBWSUtil.getCbRates(new Date());
    	CurrencyRate rate = rates.get("USD");
    	System.out.println(rate.getCode() + " " + rate.getRate() + " " + rate.getDate());
    	assertNotNull(rate);
    }
    
    public void testOne() throws Exception
    {
    	System.out.println(String.format("sfsdf sdfssdfsdf sd%tF", new Date()));
    }
}
