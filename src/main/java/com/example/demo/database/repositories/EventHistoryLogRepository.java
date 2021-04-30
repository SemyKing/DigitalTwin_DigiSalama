package com.example.demo.database.repositories;

import com.example.demo.database.models.EventHistoryLog;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Qualifier("event_history_logs")
public interface EventHistoryLogRepository extends JpaRepository<EventHistoryLog, Long> {

}
