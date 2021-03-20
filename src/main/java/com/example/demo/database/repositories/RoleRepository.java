package com.example.demo.database.repositories;

import com.example.demo.database.models.user.Role;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@Qualifier("roles")
public interface RoleRepository extends JpaRepository<Role, Long> {

	@Query("SELECT r FROM Role r WHERE r.name = :name")
	Role findByName(@Param("name") String name);
}
