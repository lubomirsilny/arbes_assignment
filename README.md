# Telephone Bill Calculator

This module calculates the total cost of phone calls based on a CSV call log.

![Build Status](https://github.com/lubomirsilny/arbes_assignment/actions/workflows/maven.yml/badge.svg)

## Application Logic

The calculator processes the call log according to these rules:
- Standard rate (1.00) applies between 08:00 and 16:00.
- Reduced rate (0.50) applies outside these hours.
- Calls longer than 5 minutes have a reduced rate (0.20) for every minute after the fifth one.
- The most frequently called number is free. If more numbers have the same frequency, the number with the higher arithmetic value is selected.

## Technical Implementation Details

1. Financial Precision: I used BigDecimal
2. Minute-by-Minute Calculation: To correctly handle calls that cross tariff boundaries.
3. Higher Number Wins: The free number determination explicitly compares phone numbers as numerical values when frequencies match.
4. Data Structures: Java Records are used for immutable data handling.

## How to Build and Test

The project uses Maven, Java 17. 
To build the project and run all unit and integration tests, use:

```bash
mvn clean verify
```