package org.eventrails.server.domain.repository;

import org.eventrails.server.domain.model.EventConsumerState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComponentEventConsumingStateRepository extends JpaRepository<EventConsumerState, String> {
}