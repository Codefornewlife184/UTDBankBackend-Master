package com.utdbankbackend.repository;


import com.utdbankbackend.domain.AccountInformation;
import com.utdbankbackend.domain.ModifyInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModifyInformationRepository extends JpaRepository<ModifyInformation, Long> {

}
