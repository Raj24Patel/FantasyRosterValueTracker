package com.rajpatel.dynastytracker.repository;

import com.rajpatel.dynastytracker.domain.SyncLog;
import org.springframework.data.jpa.repository.JpaRepository;

/** CRUD access to sync attempt audit rows. No custom queries needed. */
public interface SyncLogRepository extends JpaRepository<SyncLog, Long> {
}
