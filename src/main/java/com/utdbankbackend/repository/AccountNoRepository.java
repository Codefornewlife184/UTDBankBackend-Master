package com.utdbankbackend.repository;


import com.utdbankbackend.domain.AccountNo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;



@Transactional
@Repository
public interface AccountNoRepository extends JpaRepository<AccountNo, Long> {


}