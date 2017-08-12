package com.kwetril.highload.parsing;

import com.kwetril.highload.request.UserData;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestParser {
    static Pattern userPattern = Pattern.compile("^\\s*\\{(\\s*(" +
            "(\"id\"\\s*:\\s*(?<id>\\d+)\\s*,?)" +
            "|(\"first_name\"\\s*:\\s*\"(?<fname>\\p{L}+)\"\\s*,?)" +
            "|(\"last_name\"\\s*:\\s*\"(?<lname>\\p{L}+)\"\\s*,?)" +
            "|(\"birth_date\"\\s*:\\s*(?<bdate>-?\\d+)\\s*,?)" +
            "|(\"gender\"\\s*:\\s*\"(?<gender>(m|f))\"\\s*,?)" +
            "|(\"email\"\\s*:\\s*\"(?<email>[\\w@\\.-]+)\"\\s*,?)" +
            ")\\s*){1,6}\\}\\s*$");

    public static UserData parseNewUser(String userJson) {
        try {
            Matcher matcher = userPattern.matcher(userJson);
            if (!matcher.matches()) {
                return null;
            } else if (matcher.group("id") == null || matcher.group("fname") == null
                    || matcher.group("lname") == null || matcher.group("gender") == null
                    || matcher.group("email") == null || matcher.group("bdate") == null) {
                return null;
            } else {
                UserData result = new UserData();
                result.userId = Integer.parseInt(matcher.group("id"));
                result.firstName = matcher.group("fname");
                result.lastName = matcher.group("lname");
                result.gender = matcher.group("gender");
                result.email = matcher.group("email");
                result.birthDate = Long.parseLong(matcher.group("bdate"));
                return result;
            }
        }
        catch (Exception e) {
            return null;
        }
    }

    public static UserData parseEditUser(String userJson) {
        try {
            Matcher matcher = userPattern.matcher(userJson);
            if (!matcher.matches()) {
                return null;
            } else if (matcher.group("id") != null) {
                return null;
            } else {
                UserData result = new UserData();
                if (matcher.group("fname") != null) {
                    result.firstName = matcher.group("fname");
                }
                if (matcher.group("lname") != null) {
                    result.lastName = matcher.group("lname");
                }
                if (matcher.group("gender") != null) {
                    result.gender = matcher.group("gender");
                }
                if (matcher.group("email") != null) {
                    result.email = matcher.group("email");
                }
                if (matcher.group("bdate") != null) {
                    result.birthDate = Long.parseLong(matcher.group("bdate"));
                }
                return result;
            }
        }
        catch (Exception e) {
            return null;
        }
    }
}
