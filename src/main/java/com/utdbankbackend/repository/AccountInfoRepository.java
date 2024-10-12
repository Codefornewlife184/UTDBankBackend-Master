package com.utdbankbackend.repository;

import com.utdbankbackend.domain.AccountInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountInfoRepository extends JpaRepository<AccountInformation, Long> {


}
