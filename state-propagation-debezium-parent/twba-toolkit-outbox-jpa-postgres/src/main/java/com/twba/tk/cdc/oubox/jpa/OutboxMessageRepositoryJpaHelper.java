package com.twba.tk.cdc.oubox.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboxMessageRepositoryJpaHelper extends JpaRepository<OutboxMessageEntity, String> {
}
