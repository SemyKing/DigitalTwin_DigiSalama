package com.example.demo.database.repositories;

import com.example.demo.database.models.User;
import com.example.demo.utils.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
@Qualifier("users")
public interface UserRepository extends JpaRepository<User, Long> {

	@Query("SELECT u FROM User u WHERE u.username = :username")
	Optional<User> findUserByUsername(@Param("username") String username);

	@Query("SELECT u FROM User u WHERE u.email = :email")
	Optional<User> findUserByEmail(@Param("email") String email);

	@Query("SELECT u FROM User u WHERE u.apiToken = :apiToken")
	Optional<User> findUserByApiToken(@Param("apiToken") String apiToken);

	@Query("SELECT u FROM User u WHERE u.role IS NOT NULL AND u.role.name = '" + StringUtils.ROLE_SYSTEM_ADMIN + "'")
	List<User> findSystemAdmins();

}
