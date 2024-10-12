package com.utdbankbackend.service;
import java.sql.Timestamp;
import java.util.*;

import com.utdbankbackend.domain.ModifyInformation;
import com.utdbankbackend.exception.AuthException;
import com.utdbankbackend.projection.ProjectAdmin;
import com.utdbankbackend.projection.ProjectUser;
import com.utdbankbackend.repository.ModifyInformationRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.utdbankbackend.domain.Role;
import com.utdbankbackend.domain.User;
import com.utdbankbackend.domain.enumeration.UserRole;
import com.utdbankbackend.dto.ManagerDTO;
import com.utdbankbackend.dto.UserDTO;
import com.utdbankbackend.exception.BadRequestException;
import com.utdbankbackend.exception.ConflictException;
import com.utdbankbackend.exception.ResourceNotFoundException;
import com.utdbankbackend.repository.RoleRepository;
import com.utdbankbackend.repository.UserRepository;

@AllArgsConstructor
@Service
public class UserService {
    private UserRepository userRepository;


    private RoleRepository roleRepository;

    private ModifyInformationRepository modifyInformationRepository;

    private PasswordEncoder passwordEncoder;


    private final ModifyInformation modifyInformation = new ModifyInformation();



    private final static String USER_NOT_FOUND_MSG = "user with id %d not found";
    private final static String SSN_NOT_FOUND_MSG = "user with ssn %s not found";


    //************** visitor register

    public void register(User user) throws BadRequestException {

        if (userRepository.existsBySsn(user.getSsn())) {
            throw new ConflictException("Error: Ssn is already in use!");
        }

        String encodedPassword = passwordEncoder.encode(user.getPassword());

        user.setPassword(encodedPassword);
        user.setBuiltIn(false);


        Set<Role> roles = new HashSet<>();
        Role customerRole = roleRepository.findByName(UserRole.ROLE_CUSTOMER)
                .orElseThrow(() -> new ResourceNotFoundException("Error: Role is not found."));
        roles.add(customerRole);


        user.setRoles(roles);

        String createdBy = modifyInformation.setModifiedBy(user.getFirstName(), user.getLastName(), user.getRole());

        Timestamp createdDate = modifyInformation.setDate();

        ModifyInformation modifyInformation = new ModifyInformation(createdBy, createdDate,
                createdBy, createdDate);

        modifyInformationRepository.save(modifyInformation);

        user.setModifyInformationId(modifyInformation);
        userRepository.save(user);
    }

    //************** visitor login

    public void login(String ssn, String password) throws AuthException {
        try {
            Optional<User> user = userRepository.findBySsn(ssn);


            if (!BCrypt.checkpw(password, user.get().getPassword()))
                throw new AuthException("invalid credentials");
        } catch (Exception e) {
            throw new AuthException("invalid credentials");
        }
    }


    //**************user findById


    public User findById(Long id) throws ResourceNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User can not found with this id : "+id));

        return user;
    }

    public ProjectUser findBySsn(String ssn) throws ResourceNotFoundException {
        return  userRepository.findBySsnOrderById(ssn)
                .orElseThrow(() -> new ResourceNotFoundException("User can not found with this ssn : "+ssn));


    }


    //************** getAllUser employee manager

    public List<ProjectAdmin> fetchAllUsers(String ssn) {
        User admin= userRepository.findBySsn(ssn).orElseThrow(()->
                new ResourceNotFoundException(String.format(SSN_NOT_FOUND_MSG,ssn)));

        List<UserRole>rolesAdmin= getRoleList(admin);

        if (rolesAdmin.contains(UserRole.ROLE_MANAGER)) {
            return userRepository.findAllBy();
        }else {

            return userRepository.findAllByRole(UserRole.ROLE_CUSTOMER);
        }
    }



    //************** update user(self)

    public void updateUser(String ssn, UserDTO userDTO) throws BadRequestException {

        boolean emailExists = userRepository.existsByEmail(userDTO.getEmail());
        User userDetails= userRepository.findBySsn(ssn).orElseThrow(()->
                new ResourceNotFoundException(String.format(SSN_NOT_FOUND_MSG,ssn)));


        if (emailExists &&  !userDTO.getEmail().equals(userDetails.getEmail())) {
            throw new ConflictException("Error: Email is already in use!");
        }

        if (userDetails.getBuiltIn()) {
            throw new ConflictException("Error: You dont have permission to update user!");
        }

        String lastModifiedBy
        = modifyInformation.setModifiedBy(userDTO.getFirstName(),userDTO.getLastName(),userDetails.getRole());

        Timestamp lastModifiedDate=
                modifyInformation.setDate();

        ModifyInformation modifyInformation=
                new ModifyInformation(userDetails.getModifyInformationId().getId(),lastModifiedBy, lastModifiedDate);

        modifyInformationRepository.save(modifyInformation);
        userRepository.update(ssn,userDTO.getFirstName(),userDTO.getLastName()
                ,userDTO.getEmail(),userDTO.getAddress(),userDTO.getPhoneNumber());
    }




    //**************update password(self)

    public void updatePassword(String ssn,String newPassword,String oldPassword) throws BadRequestException{
        User user = userRepository.findBySsn(ssn).orElseThrow(()->
                new ResourceNotFoundException(String.format(SSN_NOT_FOUND_MSG,ssn)));


        if(user.getBuiltIn()) {
            throw new ResourceNotFoundException("You dont have permission to update password");
        }

        if(!(BCrypt.hashpw(oldPassword, user.getPassword()).equals(user.getPassword()))) {
            throw new BadRequestException("Your password does not match");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);

    }

    //************** update userById

    public void updateUserById(String adminSsn,Long id, ManagerDTO managerDTO) throws BadRequestException {
        boolean emailExists = userRepository.existsByEmail(managerDTO.getEmail());
        boolean ssnExists = userRepository.existsBySsn(managerDTO.getSsn());

        User userDetails = userRepository.findById(id) .orElseThrow(() ->
                new ResourceNotFoundException(String.format(USER_NOT_FOUND_MSG, id)));


        User adminDetails = userRepository.findBySsn(adminSsn).orElseThrow(() ->
                new ResourceNotFoundException(String.format(SSN_NOT_FOUND_MSG, adminSsn)));


        if (emailExists && !managerDTO.getEmail().equals(userDetails.getEmail())){
            throw new ConflictException("Error: Email is already in use!");
        }

        if (ssnExists && !managerDTO.getSsn().equals(userDetails.getSsn())){
            throw new ConflictException("Error: Ssn is already in use!");
        }

        if (userDetails.getBuiltIn()){
            throw new ConflictException("Error: You dont have permission to update user!");
        }

        List<UserRole> rolesAdmin = getRoleList(adminDetails);
        List<UserRole> rolesUser = getRoleList(userDetails);

        //password kısmını bos birakinca eski password 'u atıyor
        if (managerDTO.getPassword() == null) {
            managerDTO.setPassword(userDetails.getPassword());
        }else {
            String encodedPassword = passwordEncoder.encode(managerDTO.getPassword());
            managerDTO.setPassword(encodedPassword);
        }


        Set<String> userRoles = managerDTO.getRoles();
        Set<Role> roles = addRoles(userRoles);

        String lastModifiedBy = modifyInformation.setModifiedBy(adminDetails.getFirstName(),
                adminDetails.getLastName(), adminDetails.getRole());

        Timestamp lastModifiedDate = modifyInformation.setDate();

        ModifyInformation modifyInformation = new ModifyInformation(userDetails.getModifyInformationId().getId(),
                lastModifiedBy, lastModifiedDate);


        modifyInformationRepository.save(modifyInformation);

        User user;
        if (rolesAdmin.contains(UserRole.ROLE_MANAGER)) {
            user = new User(id, managerDTO.getSsn(), managerDTO.getFirstName(), managerDTO.getLastName(),
                    managerDTO.getEmail(), managerDTO.getPassword(), managerDTO.getAddress(), managerDTO.getPhoneNumber(),
                    userDetails.getModifyInformationId(), roles);
        }
        else if (rolesUser.contains(UserRole.ROLE_CUSTOMER)){
            user = new User(id, managerDTO.getSsn(), managerDTO.getFirstName(), managerDTO.getLastName(),
                    managerDTO.getEmail(), managerDTO.getPassword(), managerDTO.getAddress(), managerDTO.getPhoneNumber(),
                    userDetails.getModifyInformationId(), userDetails.getRole());
        }
        else
            throw new BadRequestException(String.format("You dont have permission to update user with id %d", id));
        
        
        

        userRepository.save(user);

    }

    //************** delete userbyid


    public void removeById(Long id) throws ResourceNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(USER_NOT_FOUND_MSG, id)));
        if (user.getBuiltIn()) {
            throw new ResourceNotFoundException("You dont have permission to delete this user");
        }
        userRepository.deleteById(id);
    }


    //************** search user


    public List<User> searchUserByLastName(String lastName) {
        return userRepository.findByLastNameStartingWith(lastName);
    }
    public List<User> searchUserByLastNameContain(String lastName) {
        return userRepository.findByLastNameContaining(lastName);
    }


    //************** addRoles


    public Set<Role> addRoles(Set<String> userRoles) {
        Set<Role> roles = new HashSet<>();
        if (userRoles == null) {
            Role userRole = roleRepository.findByName(UserRole.ROLE_CUSTOMER)
                    .orElseThrow(() -> new RuntimeException("Error: Role not found"));
            roles.add(userRole);
        } else {
            userRoles.forEach(role -> {
                switch (role) {
                    case "Manager":
                        Role adminRole = roleRepository.findByName(UserRole.ROLE_MANAGER)
                                .orElseThrow(() -> new RuntimeException("Error: Role not found"));
                        roles.add(adminRole);
                        break;
                    case "Employee":
                        Role managerRole = roleRepository.findByName(UserRole.ROLE_EMPLOYEE)
                                .orElseThrow(() -> new RuntimeException("Error: Role not found"));
                        roles.add(managerRole);
                        break;
                    default:
                        Role customerRole = roleRepository.findByName(UserRole.ROLE_CUSTOMER)
                                .orElseThrow(() -> new RuntimeException("Error: Role not found"));
                        roles.add(customerRole);
                }
            });
        }
        return roles;
    }

    public List<UserRole> getRoleList(User user) {
        List<UserRole> roles = new ArrayList<>();
        for (Role role: user.getRole()){
            roles.add(role.getName());
        }
        return roles;
    }


}
