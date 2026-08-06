package dev.springmind.wallet.notifications;

import dev.springmind.wallet.common.ApiException;
import dev.springmind.wallet.notifications.dto.NotificationDto;
import dev.springmind.wallet.persistence.entity.NotificationEntity;
import dev.springmind.wallet.persistence.repository.NotificationRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private static final String DEMO_USER_ID = "u1";

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<NotificationDto> list() {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(DEMO_USER_ID).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void markRead(String id) {
        NotificationEntity notification = notificationRepository
                .findById(id)
                .filter(item -> DEMO_USER_ID.equals(item.getUserId()))
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "NOTIFICATION_NOT_FOUND", "Notificação não encontrada."));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllRead() {
        List<NotificationEntity> notifications =
                notificationRepository.findByUserIdOrderByCreatedAtDesc(DEMO_USER_ID);
        for (NotificationEntity notification : notifications) {
            notification.setRead(true);
        }
        notificationRepository.saveAll(notifications);
    }

    private NotificationDto toDto(NotificationEntity notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getTitle(),
                notification.getBody(),
                notification.isRead(),
                notification.getCreatedAt());
    }
}
