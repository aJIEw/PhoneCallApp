package com.ajiew.phonecallapp.listenphonecall;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CallListenerServiceTest {

    @Test
    public void formatPhoneNumber_returnsUnknownWhenNull() {
        assertEquals("未知号码", CallListenerService.formatPhoneNumber(null));
    }

    @Test
    public void formatPhoneNumber_returnsUnknownWhenEmpty() {
        assertEquals("未知号码", CallListenerService.formatPhoneNumber(""));
    }

    @Test
    public void formatPhoneNumber_formatsElevenDigitNumber() {
        assertEquals("133-8892-9579", CallListenerService.formatPhoneNumber("13388929579"));
    }

    @Test
    public void formatPhoneNumber_keepsNonElevenDigitNumber() {
        assertEquals("10086", CallListenerService.formatPhoneNumber("10086"));
    }
}
