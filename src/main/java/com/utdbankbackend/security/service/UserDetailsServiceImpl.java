package com.utdbankbackend.security.service;


import com.utdbankbackend.domain.User;
import com.utdbankbackend.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public UserDetails loadUserByUsername(String ssn) throws UsernameNotFoundException {

        User user= userRepository.findBySsn(ssn).orElseThrow(()->
                new UsernameNotFoundException("User not found SSN number " + ssn));

        return UserDetailsImpl.build(user);
    }
}
