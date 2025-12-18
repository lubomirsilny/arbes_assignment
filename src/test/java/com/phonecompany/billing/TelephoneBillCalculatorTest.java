package com.phonecompany.billing;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link TelephoneBillCalculator}.
 * Covers standard scenarios, edge cases, tariff boundaries, and promo logic.
 */
class TelephoneBillCalculatorTest {

    /**
     * Verifies that the CSV parsing logic accepts valid input.
     */
    @Test
    void testParsingCSV() {
        TelephoneBillCalculatorImpl calculator = new TelephoneBillCalculatorImpl();
        String csv = "420774577453,13-01-2020 18:10:15,13-01-2020 18:12:57\n" +
                "420776562353,18-01-2020 08:59:20,18-01-2020 09:10:00";

        calculator.calculate(csv);
    }

    /**
     * The most frequent number should have no cost.
     * If many, a higher number win.
     */
    @Test
    void testMostFrequentNumberI() {
        TelephoneBillCalculator calculator = new TelephoneBillCalculatorImpl();

        String phoneLog = """
                420774577453,13-01-2020 18:10:15,13-01-2020 18:12:57
                420776562353,18-01-2020 08:59:20,18-01-2020 09:10:00
                """;

        BigDecimal result = calculator.calculate(phoneLog);

        assertThat(result).isEqualByComparingTo("1.50");
    }

    /**
     * Verifies that the "Free Number" promo applies to ALL calls made to that number,
     * not just the first one.
     * <p>
     * Scenario:
     * - Number '222' is called twice (most frequent) -> All calls to '222' are free.
     * - Number '111' is called once -> Standard rate applies.
     */
    @Test
    void testMostFrequentNumberII() {
        TelephoneBillCalculator calculator = new TelephoneBillCalculatorImpl();

        String phoneLog = """
                111,13-01-2020 08:00:00,13-01-2020 08:03:00
                222,13-01-2020 08:00:00,13-01-2020 08:10:00
                222,13-01-2020 09:00:00,13-01-2020 09:02:00
                """;

        BigDecimal result = calculator.calculate(phoneLog);

        assertThat(result).isEqualByComparingTo("3.00");
    }

    /**
     * Test for edge cases and pricing rules.
     * <p>
     * Scenarios covered:
     * <ul>
     * <li><b>Time Boundary (Low -> High):</b> Call spanning across 08:00:00.</li>
     * <li><b>Time Boundary (High -> Low):</b> Call spanning across 16:00:00.</li>
     * <li><b>Long Duration (> 5 min):</b> Reduced rate (0.20 CZK) applied after the 5th minute.</li>
     * <li><b>Rounding:</b> Duration rounding to the next full minute (e.g., 61s = 2 min).</li>
     * <li><b>Higher Number Wins:</b> Determines free number based on the highest arithmetic value when frequencies match.</li>
     * </ul>
     */
    @Test
    void testMasterScenarios_AllEdgeCases() {
        TelephoneBillCalculator calculator = new TelephoneBillCalculatorImpl();

        String phoneLog = """
                123456789,13-01-2020 07:59:00,13-01-2020 08:01:00
                123456789,13-01-2020 15:59:00,13-01-2020 16:01:00
                123456789,13-01-2020 10:00:00,13-01-2020 10:07:00
                123456789,13-01-2020 17:54:56,13-01-2020 18:05:35
                123456789,13-01-2020 12:00:00,13-01-2020 12:01:01
                999999999,13-01-2020 20:00:00,13-01-2020 20:01:00
                999999999,13-01-2020 20:00:00,13-01-2020 20:01:00
                999999999,13-01-2020 20:00:00,13-01-2020 20:01:00
                999999999,13-01-2020 20:00:00,13-01-2020 20:01:00
                999999999,13-01-2020 20:00:00,13-01-2020 20:01:00
                """;

        BigDecimal result = calculator.calculate(phoneLog);

        assertThat(result).isEqualByComparingTo("14.10");
    }
}