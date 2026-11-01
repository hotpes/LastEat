package com.lasteat.closingdeal.repository;

import com.lasteat.closingdeal.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

}
