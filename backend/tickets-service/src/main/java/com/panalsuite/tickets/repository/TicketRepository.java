package com.panalsuite.tickets.repository;

import com.panalsuite.tickets.model.Ticket;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TicketRepository extends MongoRepository<Ticket, Long> {
    List<Ticket> findByPeopleInCharge(Long idOperator);
}
