package com.kwetril.highload.request;

/**
 * Created by a.klimovich on 12.08.2017.
 */
public class UserData {
    public int userId;
    public long birthDate;
    public String email;
    public String firstName;
    public String lastName;
    public String gender;

    @Override
    public String toString() {
        return String.format("{\"id\":%s,\"email\":\"%s\",\"first_name\":%s,\"last_name\":\"%s\",\"gender\":\"%s\",\"birth_date\":%s}",
                userId, email, firstName, lastName, gender, birthDate);

    }
}
