package it.unicam.cs.ids.repositories;

import it.unicam.cs.ids.models.StaffUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IStaffUserRepository extends JpaRepository<StaffUser, Long> {

}