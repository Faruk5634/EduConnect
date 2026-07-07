package com.educonnect.repository;

import com.educonnect.model.Message;
import com.educonnect.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    // Hem gelen hem giden mesajları tarihe göre (en yeni en üstte) getirir
    List<Message> findBySenderOrReceiverOrderBySentAtDesc(User sender, User receiver);
}