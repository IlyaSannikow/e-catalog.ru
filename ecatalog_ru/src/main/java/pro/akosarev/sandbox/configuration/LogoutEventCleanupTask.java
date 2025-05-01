package pro.akosarev.sandbox.configuration;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.akosarev.sandbox.repository.UserLogoutEventRepository;

import java.util.Date;

@Component
public class LogoutEventCleanupTask {

    private final UserLogoutEventRepository userLogoutEventRepository;

    public LogoutEventCleanupTask(UserLogoutEventRepository userLogoutEventRepository) {
        this.userLogoutEventRepository = userLogoutEventRepository;
    }

    // Очищаем записи старше 30 дней каждый день в 3:00
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupOldLogoutEvents() {
        Date cutoffDate = new Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000);
        userLogoutEventRepository.deleteOldEvents(cutoffDate);
    }
}
