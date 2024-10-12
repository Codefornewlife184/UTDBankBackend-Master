package com.utdbankbackend.controller;

import com.utdbankbackend.dto.ManagerDTO;

import com.utdbankbackend.projection.ProjectAdmin;
import com.utdbankbackend.projection.ProjectUser;
import com.utdbankbackend.security.jwt.JwtUtils;
import lombok.AllArgsConstructor;

import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.utdbankbackend.domain.User;
import com.utdbankbackend.dto.UserDTO;
import com.utdbankbackend.service.UserService;

@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@AllArgsConstructor
@RestController
@Produces(MediaType.APPLICATION_JSON)
@RequestMapping()

public class UserController {

    private UserService userService;

    private ModelMapper modelMapper;

    private AuthenticationManager authenticationManager;

    private JwtUtils jwtUtils;


    //************** visitor register

    @PostMapping("/register")
    public ResponseEntity<Map<String, Boolean>> registerUser(@Valid @RequestBody User user) {
        userService.register(user);
        Map<String, Boolean> map = new HashMap<>();
        map.put("User registered successfully!", true);
        return new ResponseEntity<>(map, HttpStatus.CREATED);
    }

    //************** visitor login

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> authenticateUser(@RequestBody Map<String, Object> userMap) {
        String ssn = (String) userMap.get("ssn");
        String password = (String) userMap.get("password");

        userService.login(ssn, password);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(ssn, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        Map<String, String> map = new HashMap<>();
        map.put("token", jwt);
        return new ResponseEntity<>(map, HttpStatus.OK);
    }


    //**************getUser(self)

    @GetMapping("/user")
    @PreAuthorize("hasRole('MANAGER') or hasRole('CUSTOMER') or hasRole('EMPLOYEE')")
    public ResponseEntity<ProjectUser> getUserBySsn(HttpServletRequest request) {
        String ssn = (String) request.getAttribute("ssn");
        ProjectUser user = userService.findBySsn(ssn);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }


    //**************update password(self)

    @PatchMapping("/user/password")
    @PreAuthorize("hasRole('MANAGER') or hasRole('CUSTOMER') or hasRole('EMPLOYEE')")
    public ResponseEntity<Map<String, Boolean>> updatePassword(HttpServletRequest request,
                                                               @RequestBody Map<String,String> userMap){
        String ssn = (String) request.getAttribute("ssn");
        String newPassword = userMap.get("newPassword");
        String oldPassword = userMap.get("oldPassword");
        userService.updatePassword(ssn,newPassword,oldPassword);
        Map<String, Boolean> map = new HashMap<>();
        map.put("Password changed successfully", true);
        return new ResponseEntity<>(map,HttpStatus.OK);
    }

    //************** update user(self)

    @PutMapping("/user/update")
    @PreAuthorize("hasRole('MANAGER') or hasRole('CUSTOMER') or hasRole('EMPLOYEE')")
    public ResponseEntity<Map<String, Boolean>> updateUser(HttpServletRequest request,
                                                           @Valid @RequestBody UserDTO userDTO) {
        String ssn= (String) request.getAttribute("ssn");

        userService.updateUser(ssn, userDTO);
        Map<String, Boolean> map = new HashMap<>();
        map.put("User updated successfully", true);
        return new ResponseEntity<>(map, HttpStatus.OK);
    }


    //************** getAllUser employee manager

    @GetMapping("/user/auth/all")
    @PreAuthorize("hasRole('MANAGER')  or hasRole('EMPLOYEE')")
    public ResponseEntity<List<ProjectAdmin>> getAllUsers(HttpServletRequest request) {
        String ssn= (String) request.getAttribute("ssn");
        List<ProjectAdmin> users = userService.fetchAllUsers(ssn);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }
    //************** getUserById employee manager

    @GetMapping("/user/{id}/auth")
    @PreAuthorize("hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<User> getUserByIdAdmin(@PathVariable Long id) {
        User user = userService.findById(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }



    //************** update userIdByManager

    @PutMapping("user/{id}/auth")
    @PreAuthorize("hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<Map<String, Boolean>> updateUserById(HttpServletRequest request,
                                                               @PathVariable Long id,
                                                               @Valid @RequestBody ManagerDTO managerDTO) {

        String adminSsn= (String) request.getAttribute("ssn");

        userService.updateUserById(adminSsn,id, managerDTO);
        Map<String, Boolean> map = new HashMap<>();
        map.put("User updated successfully", true);
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    //************** delete userbyid

    @DeleteMapping("/user/{id}/auth")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Map<String, Boolean>> deleteUser(@PathVariable Long id) {
        userService.removeById(id);

        Map<String, Boolean> map = new HashMap<>();
        map.put("User deleted successfully", true);
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    //************** search user

    @GetMapping("/user/search")
    @PreAuthorize("hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<List<UserDTO>> searchUserByLastName(
            @RequestParam("lastname") String lastName) {
        List<User> userList = userService.searchUserByLastName(lastName);
        List<UserDTO> userDTOList = userList.stream().map(this::convertToDTO).collect(Collectors.toList());
        return new ResponseEntity<>(userDTOList, HttpStatus.OK);
    }
    //************** search contain

    @GetMapping("/user/search/contain")
    @PreAuthorize("hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<List<UserDTO>> searchUserByLastNameContain(
            @RequestParam("lastname") String lastName) {
        List<User> userList = userService.searchUserByLastNameContain(lastName);
        List<UserDTO> userDTOList = userList.stream().map(this::convertToDTO).collect(Collectors.toList());
        return new ResponseEntity<>(userDTOList, HttpStatus.OK);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        return userDTO;
    }



}
