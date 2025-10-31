package dev.kuku.authsome.services.notifier.api;

import dev.kuku.authsome.services.notifier.api.model.IdentityType;

/**
 * Service interface for sending notifications to users.
 * <p>
 * Provides functionality to send messages through various channels
 * such as email, SMS, or push notifications.
 */
public interface NotifierService {

    /**
     * Sends a notification to the specified identity.
     *
     * @param identityType the type of identity/channel to use for notification
     * @param identity     the identity value (email address, phone number, etc.)
     * @param subject      the subject/title of the notification
     * @param context      the body/content of the notification
     */
    void sendNotification(IdentityType identityType, String identity, String subject, String context);
}