package com.utdbankbackend.repository;

import com.utdbankbackend.domain.User;
import com.utdbankbackend.domain.enumeration.UserRole;
import com.utdbankbackend.dto.UserDTO;
import com.utdbankbackend.exception.BadRequestException;
import com.utdbankbackend.exception.ConflictException;
import com.utdbankbackend.exception.ResourceNotFoundException;
import com.utdbankbackend.projection.ProjectAdmin;
import com.utdbankbackend.projection.ProjectUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Repository
public interface UserRepository extends JpaRepository <User,Long>{

    Optional<ProjectUser> findBySsnOrderById(String ssn) throws ResourceNotFoundException;
    Optional<User> findBySsn(String ssn) throws ResourceNotFoundException;


    @Query("SELECT u from User u LEFT JOIN FETCH u.roles r WHERE r.name= ?1")
    List<ProjectAdmin>findAllByRole(UserRole userRole);


    List<ProjectAdmin>findAllBy();


    Boolean existsBySsn(String ssn) throws ConflictException;

    Boolean existsByEmail(String email) throws ConflictException;


    List<User> findByLastNameStartingWith(String lastName);

    List<User> findByLastNameContaining(String lastName);


    @Modifying
    @Query("UPDATE User u "+
            "SET u.firstName=:firstName, u.lastName=:lastName,u.email=:email, u.address=:address, u.phoneNumber=:phoneNumber "+
            " WHERE u.ssn=:ssn")
    void update(@Param("ssn") String ssn,
                 @Param("firstName") String firstName,
                @Param("lastName") String lastName,
                @Param("email") String email,
                @Param("address") String address,
                @Param("phoneNumber") String phoneNumber
                ) throws BadRequestException;



}
