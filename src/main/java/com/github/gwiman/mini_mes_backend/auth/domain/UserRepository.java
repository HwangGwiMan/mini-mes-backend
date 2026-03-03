package com.github.gwiman.mini_mes_backend.auth.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByUsername(String username);

	Optional<User> findByEmployeeId(Long employeeId);

	boolean existsByUsername(String username);

	boolean existsByUsernameAndIdNot(String username, Long id);
}
