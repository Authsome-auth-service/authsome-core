package dev.kuku.authsome.services.notifier.impl;

import dev.kuku.authsome.services.notifier.api.NotifierService;
import dev.kuku.authsome.services.notifier.api.model.IdentityType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotifierServiceImpl implements NotifierService {
    @Override
    public void sendNotification(IdentityType identityType, String identity, String subject, String context) {
        log.debug("sendNotification({}, {}, {}, {})", identityType, identity, subject, context);

        System.out.println("\n" + "=".repeat(80));
        System.out.println("📧 NOTIFICATION");
        System.out.println("=".repeat(80));
        System.out.println("Type     : " + identityType);
        System.out.println("To       : " + identity);
        System.out.println("Subject  : " + subject);
        System.out.println("-".repeat(80));
        System.out.println("Content  :");
        System.out.println(context);
        System.out.println("=".repeat(80) + "\n");
    }
}