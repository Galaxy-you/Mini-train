package com.mini12306.repository;

import com.mini12306.model.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {
    List<Passenger> findByUserId(Long userId);
    void deleteByUserIdAndId(Long userId, Long id);
}
