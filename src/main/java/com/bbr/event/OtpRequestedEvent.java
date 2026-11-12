package com.bbr.event;

/** Published when a user needs a login code. */
public record OtpRequestedEvent(String email, String code) {}
