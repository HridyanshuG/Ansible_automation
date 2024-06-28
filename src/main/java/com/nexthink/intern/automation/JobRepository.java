package com.nexthink.intern.automation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface JobRepository extends JpaRepository<Job, Long> {

}