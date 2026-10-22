package com.yurii.pavlenko.myassistant.currency.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CurrencyLogicTest {

    private static ExchangeRates rates() {
        Map<String, Double> perDollar = new HashMap<>();
        perDollar.put("EUR", 0.5);
        perDollar.put("ILS", 4.0);
        perDollar.put("JPY", 150.0);
        return new ExchangeRates("USD", perDollar, 0);
    }

    private static BigDecimal parse(String text) {
        return AmountParser.parse(text);
    }

    @Test
    public void convertsThroughTheBaseCurrency() {
        assertEquals(2.0, rates().rate("EUR", "USD"), 1e-9);
        assertEquals(8.0, rates().rate("EUR", "ILS"), 1e-9);
        assertEquals(0, new BigDecimal("80").compareTo(rates().convert(new BigDecimal("10"), "EUR", "ILS")));
    }

    @Test
    public void listsCodesAlphabeticallyIncludingTheBase() {
        assertEquals("[EUR, ILS, JPY, USD]", rates().codes().toString());
    }

    @Test
    public void defaultPairIsHomeCurrencyAgainstTheDollar() {
        CurrencyPair pair = DefaultPair.choose("ILS", rates());
        assertEquals("ILS", pair.getFrom());
        assertEquals("USD", pair.getTo());
    }

    @Test
    public void dollarCountriesAndUnknownCountriesGetEuro() {
        assertEquals("EUR", DefaultPair.choose("USD", rates()).getTo());
        assertEquals("USD", DefaultPair.choose(null, rates()).getFrom());
        assertEquals("USD", DefaultPair.choose("XXX", rates()).getFrom());
    }

    @Test
    public void swapReversesThePair() {
        assertEquals(new CurrencyPair("USD", "ILS"), new CurrencyPair("ILS", "USD").swapped());
    }

    @Test
    public void parsesAmountsWithEitherDecimalSeparator() {
        assertEquals(0, new BigDecimal("1234.5").compareTo(parse("1234.5")));
        assertEquals(0, new BigDecimal("1234.5").compareTo(parse("1234,5")));
        assertEquals(0, new BigDecimal("0.5").compareTo(parse(".5")));
        assertEquals(0, new BigDecimal("7").compareTo(parse("7")));
    }

    @Test
    public void parsesGroupedAmounts() {
        assertEquals(0, new BigDecimal("1234567.89").compareTo(parse("1,234,567.89")));
        assertEquals(0, new BigDecimal("1234567.89").compareTo(parse("1.234.567,89")));
        assertEquals(0, new BigDecimal("1234567").compareTo(parse("1 234 567")));
        assertEquals(0, new BigDecimal("1234567").compareTo(parse("1,234,567")));
    }

    @Test
    public void rejectsTextThatIsNotAnAmount() {
        assertTrue(parse("") == null);
        assertTrue(parse("abc") == null);
        assertTrue(parse("-5") == null);
        assertTrue(parse(".") == null);
        assertTrue(parse("1.2.3,4,5x") == null);
        assertTrue(parse("12345678901234567890") == null);
    }

    @Test
    public void conversionUsesTheTypedAmount() {
        Conversion conversion = Conversion.of(rates(), new CurrencyPair("USD", "ILS"), "2,5");
        assertEquals(0, new BigDecimal("10").compareTo(conversion.getResult()));
        assertEquals(4.0, conversion.getRate(), 1e-9);
        assertTrue(Conversion.of(rates(), new CurrencyPair("USD", "ILS"), "x") == null);
    }

    @Test
    public void formatsMoneyAndRates() {
        assertEquals("1,234.57", MoneyFormat.amount(new BigDecimal("1234.5678"), Locale.US));
        assertEquals("0.000123", MoneyFormat.amount(new BigDecimal("0.000123"), Locale.US));
        assertEquals("0.00", MoneyFormat.amount(BigDecimal.ZERO, Locale.US));
        assertEquals("3.6543", MoneyFormat.rate(3.654321, Locale.US));
        assertEquals("0.000267", MoneyFormat.rate(0.0002671, Locale.US));
        assertEquals("149.53", MoneyFormat.rate(149.5312, Locale.US));
    }

    @Test
    public void labelsKnownAndUnknownCurrencies() {
        assertTrue(MoneyFormat.label("ILS", Locale.ENGLISH).startsWith("ILS · "));
        assertEquals("ZZZ", MoneyFormat.label("ZZZ", Locale.ENGLISH));
    }
}
