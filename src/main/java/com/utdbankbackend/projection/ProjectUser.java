package com.utdbankbackend.projection;
import java.util.Set;

public interface ProjectUser {


    String getSsn();

    String getFirstName();

    String getLastName();

    String getPhoneNumber();

    String getEmail();

    String getAddress();

    Set<String> getRoles();

}
