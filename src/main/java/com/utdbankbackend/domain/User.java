package com.utdbankbackend.domain;
import javax.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.utdbankbackend.domain.enumeration.UserRole;
import lombok.*;
import javax.persistence.Entity;
import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Pattern(regexp = "^(?!000|666)[0-8][0-9]{2}-(?!00)[0-9]{2}-(?!0000)[0-9]{4}$", message = "Please enter valid SSN")
    @Size(min = 11, max= 11, message = "SSN should be exact 9 characters")
    @NotNull(message = "Please enter your SSN")
    @Column(nullable = false, unique = true, length = 11)
    private String ssn;

    @NotBlank(message = "Please provide not blank password")
    @NotNull(message = "Please provide your password")
    @Column(length = 120, nullable = false)
    @Size(min=4, max=60, message= "Your password '${validatedValue}' be between (min) characters long")
    private String password;

    @Size(max = 20)
    @NotNull(message = "Please enter your first name")
    @Column(nullable = false, length = 20)
    private String firstName;

    @Size(max = 20)
    @NotNull(message = "Please enter your last name")
    @Column(nullable = false, length = 20)
    private String lastName;

    @Pattern(regexp = "^((\\(\\d{3}\\))|\\d{3})[- .]?\\d{3}[- .]?\\d{4}$",
            message = "Please enter valid phone number")
    @Size(min = 14, max = 14, message = "Phone number should be exact 10 characters")
    @NotNull(message = "Please enter your phone number")
    @Column(nullable = false, length = 14)
    private String phoneNumber;

    @Email(message = "Please enter valid email")
    @Size(min = 5, max = 150)
    @NotNull(message = "Please enter your email")
    @Column(unique = true,nullable = false,  length = 150)
    private String email;

    @Size(max = 250)
    @NotNull(message = "Please enter your address")
    @Column(nullable = false, length = 250)
    private String address;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "modify_information_id", referencedColumnName = "id")
    private ModifyInformation modifyInformationId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="user_roles"
            , joinColumns = @JoinColumn(name="user_id")
            , inverseJoinColumns = @JoinColumn(name="role_id"))
    private Set<Role> roles = new HashSet<>();

    @Column(nullable = false, updatable=false)
    private Boolean builtIn;


    public Set<Role> getRole() {
        return roles;
    }
    public Set<String> getRoles() {
        Set<String> roles1 = new HashSet<>();
        Role[] role = roles.toArray(new Role[roles.size()]);
        for (int i = 0; i < roles.size(); i++) {
            if (role[i].getName().equals(UserRole.ROLE_MANAGER)) {
                roles1.add("Manager");
            } else if (role[i].getName().equals(UserRole.ROLE_EMPLOYEE)) {
                roles1.add("Employee");
            } else {
                roles1.add("Customer");
            }
        }
        return roles1;
    }


    @JsonCreator
    public User(Long id, String ssn, String firstName, String lastName, String email, String password, String address,
                String phoneNumber, ModifyInformation modifyInformationId, Set<Role> roles) {
        this.id = id;
        this.ssn = ssn;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.modifyInformationId = modifyInformationId;
        this.roles = roles;
    }

}
/*
    public User(String firstName, String lastName, String email, String password, String phoneNumber,
                String address, String ssn, Boolean builtIn, Set<Role> roles) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.ssn = ssn;
        this.roles = roles;
        this.builtIn = builtIn;
    }*/
   /*public String getFullName() {
        return  firstName + " " + lastName;
    }*/


