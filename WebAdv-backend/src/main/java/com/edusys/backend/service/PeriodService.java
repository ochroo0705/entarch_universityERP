package com.edusys.backend.service;

import com.edusys.backend.model.Period;
import com.edusys.backend.repository.PeriodRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PeriodService {

    private final PeriodRepository periodRepository;

    public PeriodService(PeriodRepository periodRepository) {
        this.periodRepository = periodRepository;
    }

    public Period save(Period p) {
        return periodRepository.save(p);
    }

    public Optional<Period> findById(Long id) {
        return periodRepository.findById(Math.toIntExact(id));
    }

    public List<Period> findAll() {
        return periodRepository.findAll();
    }

    public void delete(Long id) {
        periodRepository.deleteById(Math.toIntExact(id));
    }
}
