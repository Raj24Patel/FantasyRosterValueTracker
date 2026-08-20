package com.rajpatel.dynastytracker.repository;

import com.rajpatel.dynastytracker.domain.SyncLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncLogRepository extends JpaRepository<SyncLog, Long> {
}
