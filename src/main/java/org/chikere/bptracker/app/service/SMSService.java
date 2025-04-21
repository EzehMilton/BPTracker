package org.chikere.bptracker.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for sending SMS notifications (mocked implementation)
 */
@Service
@Slf4j
public class SMSService {

    @Value("${app.sms.enabled:true}")
    private boolean smsEnabled;

    @Value("${app.sms.provider:mock}")
    private String smsProvider;

    /**
     * Send an SMS message to a phone number
     * @param phoneNumber the recipient's phone number
     * @param message the message to send
     * @return true if the message was sent successfully, false otherwise
     */
    public boolean sendSMS(String phoneNumber, String message) {
        if (!smsEnabled) {
            log.info("SMS notifications are disabled");
            return false;
        }

        // In a real implementation, this would integrate with an SMS gateway
        // For now, we just log the message
        log.info("MOCK SMS via {}: To: {}, Message: {}", smsProvider, phoneNumber, message);
        
        // Simulate successful sending
        return true;
    }

    /**
     * Check if SMS notifications are enabled
     * @return true if SMS notifications are enabled, false otherwise
     */
    public boolean isSmsEnabled() {
        return smsEnabled;
    }

    /**
     * Get the SMS provider name
     * @return the SMS provider name
     */
    public String getSmsProvider() {
        return smsProvider;
    }
}