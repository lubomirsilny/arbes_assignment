package com.phonecompany.billing;

import java.time.LocalDateTime;

public record PhoneCall(String phoneNumber, LocalDateTime start, LocalDateTime end) {
}