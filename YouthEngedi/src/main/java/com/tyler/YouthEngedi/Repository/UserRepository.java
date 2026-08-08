package com.tyler.YouthEngedi.Repository;

import com.tyler.YouthEngedi.models.User;
import com.tyler.YouthEngedi.models.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String email);
    long countByRolesContains(Role role);

}
