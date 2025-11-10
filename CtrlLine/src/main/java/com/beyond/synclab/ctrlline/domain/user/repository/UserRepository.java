package com.beyond.synclab.ctrlline.domain.user.repository;

import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);

    Optional<Users> findByEmpNo(String empNo);

    @Query("SELECT u.empNo FROM Users u WHERE u.empNo LIKE :prefix% ORDER BY u.empNo DESC")
    List<String> findEmpNosByPrefix(@Param("prefix") String prefix);

    boolean existsByEmail(String email);

    Page<Users> findAll(Specification<Users> spec, Pageable pageable);
}
