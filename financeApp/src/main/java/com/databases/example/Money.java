/* Class that handles how money is represented
 * Mostly just formats monetary values
 */

package com.databases.example;

import java.math.BigDecimal;
import static java.math.BigDecimal.ZERO;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class Money{
	protected String value;
	  private static int ROUNDING_MODE = BigDecimal.ROUND_HALF_UP;
	  
	public Money(String value){
		this.value=value;
	}
	
	public Money(float value){
		this.value=value+"";
	}
	
	public Money(BigDecimal value){
		this.value=value+"";
	}
	
	public BigDecimal getBigDecimal(Locale locale){
		BigDecimal result = new BigDecimal(value).setScale(Currency.getInstance(locale).getDefaultFractionDigits(), ROUNDING_MODE);		
		return result;
	}
	
	public String getNumberFormat(Locale locale){
		BigDecimal result = this.getBigDecimal(locale);
		DecimalFormat formatter = (DecimalFormat)NumberFormat.getCurrencyInstance();
		String symbol = formatter.getCurrency().getSymbol();
		formatter.setNegativePrefix(symbol+"-");
		formatter.setNegativeSuffix("");
		String nf = formatter.format(result);
		return nf;
	}
	
	public boolean isPositive(Locale locale){
		BigDecimal result = new BigDecimal(value).setScale(Currency.getInstance(locale).getDefaultFractionDigits(), ROUNDING_MODE);		
		return result.compareTo(ZERO)>=0;
	}
	
//	double result = 50.00 + 5.45 - 30.67;
//	BigDecimal result2 = new BigDecimal(result).setScale(Currency.getInstance(getResources().getConfiguration().locale).getDefaultFractionDigits(), BigDecimal.ROUND_HALF_UP);
//	Log.e("Accounts-calculateBalance", "BigDecimal: " + result2);
//	NumberFormat form = NumberFormat.getCurrencyInstance();
//	Log.e("Accounts-calculateBalance", "Amount: " + form.format(result2));

}//End Money