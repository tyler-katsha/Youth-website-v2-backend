package com.tyler.YouthEngedi.Repository;

import com.tyler.YouthEngedi.models.RoleRequest;
import com.tyler.YouthEngedi.models.User;
import com.tyler.YouthEngedi.models.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRequestRepository extends JpaRepository<RoleRequest,Long> {

    boolean existsByUserAndRequestStatus(User user, RequestStatus requestStatus);
}
