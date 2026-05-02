package com.github.gwiman.mini_mes_backend.notification.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 알림 JPA 레포지토리.
 */
public interface NotificationRepository extends JpaRepository<AppNotification, Long> {

    List<AppNotification> findTop50ByRecipientUsernameOrderByCreatedAtDesc(String recipientUsername);

    long countByRecipientUsernameAndIsReadFalse(String recipientUsername);

    @Modifying
    @Query("UPDATE AppNotification n SET n.isRead = true WHERE n.recipientUsername = :username AND n.isRead = false")
    void markAllReadByUsername(@Param("username") String username);
}
