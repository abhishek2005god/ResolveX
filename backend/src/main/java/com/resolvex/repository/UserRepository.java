package com.resolvex.repository;

import com.resolvex.entity.User;
import com.resolvex.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    List<User> findByRoleName(RoleName roleName);
    List<User> findByDepartmentId(Long departmentId);

    @Query("SELECT u FROM User u WHERE u.role.name = 'EMPLOYEE' AND u.active = true")
    List<User> findAllActiveEmployees();

    @Query("SELECT u FROM User u JOIN TeamMember tm ON tm.user.id = u.id WHERE tm.team.id = :teamId AND u.active = true")
    List<User> findActiveMembersByTeamId(@Param("teamId") Long teamId);
}
