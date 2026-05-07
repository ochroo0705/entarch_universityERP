package com.edusys.backend.repository;

import com.edusys.backend.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    interface IdNameView {
        Long getId();
        String getFirstName();
        String getLastName();
    }

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    long countByUsernameStartingWith(String prefix);

    @Query(value = "SELECT COUNT(*) FROM users WHERE (role_flags & :flag) <> 0", nativeQuery = true)
    long countByRoleFlagSet(@Param("flag") int flag);

    @Query(
        value = "SELECT id AS id, first_name AS firstName, last_name AS lastName " +
            "FROM users " +
            "WHERE (role_flags & :flag) <> 0 " +
            "ORDER BY last_name, first_name, id",
        nativeQuery = true
    )
    List<IdNameView> findIdAndNameByRoleFlagSet(@Param("flag") int flag);

    @Query(
        value = "SELECT * FROM users WHERE (role_flags & 1) <> 0 ORDER BY last_name, first_name, id",
        nativeQuery = true
    )
    List<User> findAllStudentsOrderByName();

    @Query(
        value = "SELECT * FROM users WHERE (role_flags & 8) <> 0 ORDER BY id LIMIT 1",
        nativeQuery = true
    )
    Optional<User> findFirstAdminUser();

    @EntityGraph(attributePaths = {})
    List<User> findByIdInOrderByLastNameAscFirstNameAscIdAsc(List<Long> ids);
}
