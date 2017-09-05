package com.kwetril.highload.request;

public class UserData {
    public int userId;
    public long birthDate;
    public String email;
    public String firstName;
    public String lastName;
    public String gender;
    public String json;

    public void computeJson() {
        json = String.format("{\"id\":%s,\"email\":\"%s\",\"first_name\":\"%s\",\"last_name\":\"%s\",\"gender\":\"%s\",\"birth_date\":%s}",
                userId, email, firstName, lastName, gender, birthDate);
    }

    @Override
    public String toString() {
        return json;
        //return String.format("{\"id\":%s,\"email\":\"%s\",\"first_name\":\"%s\",\"last_name\":\"%s\",\"gender\":\"%s\",\"birth_date\":%s}",
        //        userId, email, firstName, lastName, gender, birthDate);
    }
}
