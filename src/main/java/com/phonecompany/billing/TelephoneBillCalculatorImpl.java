package com.phonecompany.billing;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.temporal.ChronoUnit;

public class TelephoneBillCalculatorImpl implements TelephoneBillCalculator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    /**
     * Calculates the total cost of phone calls based on the provided CSV log.
     * <p>
     * The process involves:
     * <ol>
     * <li>Parsing the CSV string into objects.</li>
     * <li>Identifying the most frequent phone number (which is free of charge).</li>
     * <li>Calculating the price for each remaining call individually.</li>
     * </ol>
     *
     * @param phoneLog String containing the call log in CSV format.
     * @return The total calculated price as BigDecimal.
     */
    @Override
    public BigDecimal calculate(String phoneLog) {
        if (phoneLog == null || phoneLog.isBlank()) {
            return BigDecimal.ZERO;
        }

        List<PhoneCall> calls = parseLog(phoneLog);
        String freeNumber = getMostFrequentPhoneNumber(calls);
        BigDecimal totalBill = BigDecimal.ZERO;

        for (PhoneCall call : calls) {
            if (call.phoneNumber().equals(freeNumber)) {
                continue;
            }
            totalBill = totalBill.add(calculateCallPrice(call));
        }

        return totalBill;
    }

    /**
     * Parses the input CSV string into a list of PhoneCall objects.
     * Expected CSV format: phone number, start time, end time.
     *
     * @param phoneLog String containing the call log (lines separated by new line).
     * @return A list of parsed {@link PhoneCall} objects.
     */
    private List<PhoneCall> parseLog(String phoneLog) {
        List<PhoneCall> calls = new ArrayList<>();
        String[] lines = phoneLog.split("\\r?\\n");

        for (String line : lines) {
            if (line.isBlank()) continue;

            String[] parts = line.split(",");
            String phoneNumber = parts[0];
            LocalDateTime start = LocalDateTime.parse(parts[1], DATE_FORMATTER);
            LocalDateTime end = LocalDateTime.parse(parts[2], DATE_FORMATTER);

            calls.add(new PhoneCall(phoneNumber, start, end));
        }
        return calls;
    }

    /**
     * Identifies the most frequently called phone number.
     * Calls to this number are not charged.
     * <p>
     * If multiple numbers have the same highest frequency,
     * the one with the highest arithmetic value is selected.
     *
     * @param calls List of all phone calls.
     * @return The phone number eligible for the free promo, or null if the list is empty.
     */
    private String getMostFrequentPhoneNumber(List<PhoneCall> calls) {
        if (calls.isEmpty()) {
            return null;
        }

        Map<String, Long> frequencies = calls.stream()
                .collect(Collectors.groupingBy(PhoneCall::phoneNumber, Collectors.counting()));

        long maxFrequency = frequencies.values().stream()
                .max(Long::compare)
                .orElse(0L);

        return frequencies.entrySet().stream()
                .filter(entry -> entry.getValue() == maxFrequency)
                .map(Map.Entry::getKey)
                .max((num1, num2) -> {
                    BigDecimal v1 = new BigDecimal(num1);
                    BigDecimal v2 = new BigDecimal(num2);
                    return v1.compareTo(v2);
                }).orElse(null);
    }

    /**
     * Calculates the price for a single phone call based on the tariff rules.
     * <p>
     * The logic iterates through each started minute because the rate depends on:
     * <ul>
     * <li>Time of day (Standard rate 08:00-16:00 vs. Reduced rate).</li>
     * <li>Call duration (Reduced rate applies after 5 minutes).</li>
     * </ul>
     *
     * @param call The phone call to be priced.
     * @return The calculated price for the call.
     */
    private BigDecimal calculateCallPrice(PhoneCall call) {
        BigDecimal price = BigDecimal.ZERO;
        long durationInMinutes = getDurationInMinutes(call);

        for (int i = 0; i < durationInMinutes; i++) {

            if (i >= 5) {
                price = price.add(new BigDecimal("0.20"));
            } else {

                LocalDateTime minuteStart = call.start().plusMinutes(i);
                int hour = minuteStart.getHour();

                if (hour >= 8 && hour < 16) {
                    price = price.add(new BigDecimal("1.00"));
                } else {
                    price = price.add(new BigDecimal("0.50"));
                }
            }
        }
        return price;
    }

    /**
     * Calculates the call duration in started minutes.
     *
     * @param call The phone call.
     * @return Total duration in minutes (rounded up).
     */
    private long getDurationInMinutes(PhoneCall call) {
        long seconds = ChronoUnit.SECONDS.between(call.start(), call.end());

        if (seconds % 60 == 0) {
            return seconds / 60;
        } else {
            return (seconds / 60) + 1;
        }
    }
}