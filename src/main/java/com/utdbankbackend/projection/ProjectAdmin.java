package com.utdbankbackend.projection;

import java.util.Set;

public interface ProjectAdmin {

    Long getId();

    String getSsn();

    String getFirstName();

    String getLastName();

    String getEmail();

    String getAddress();

    String getPhoneNumber();

    Set<String> getRoles();

    Boolean getBuiltIn();
}
