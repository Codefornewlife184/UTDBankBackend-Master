package com.utdbankbackend.dto;

import java.util.Set;

import javax.persistence.Column;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ManagerDTO {


    private String ssn;



    @NotNull(message="Please provide your firstName")
    @Size(min=1,max=15)
    private String firstName;


    @NotNull(message="Please provide your lastName")
    @Size(min=1,max=15)
    private String lastName;

    @Email(message="Please provide a valid email")
    @NotNull(message="Please provide your email")
    @Size(min=5,max=100)
    private String email;


    @Size(min=4,max=60,message="Password '${validatedValue}' must be between {min} and {max} chracters long")
    private String password;


    @NotNull(message="Please provide your Address")
    @Size(min=10,max=250,message="Please enter your adress")
    private String address;

    // (555) 555 5555
    // 555-555-5555
    // 555.555.5555
    @Pattern(regexp = "^((\\(\\d{3}\\))|\\d{3})[- .]?\\d{3}[- .]?\\d{4}$",
            message = "Please provide valid phone number")
    @NotNull(message = "Please enter your phone number")
    private String phoneNumber;


    private Set<String> roles;




}
