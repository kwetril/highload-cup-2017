package com.kwetril.highload.parsing;

import com.kwetril.highload.request.LocationData;
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

    static Pattern locationPattern = Pattern.compile("^\\s*\\{(\\s*(" +
            "(\"id\"\\s*:\\s*(?<id>\\d+)\\s*,?)" +
            "|(\"country\"\\s*:\\s*\"(?<country>[\\p{L} -]+)\"\\s*,?)" +
            "|(\"city\"\\s*:\\s*\"(?<city>\\p{L}+)\"\\s*,?)" +
            "|(\"distance\"\\s*:\\s*(?<distance>\\d+)\\s*,?)" +
            "|(\"place\"\\s*:\\s*\"(?<place>\\p{L}+)\"\\s*,?)" +
            ")\\s*){1,5}\\}\\s*$");

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

    public static LocationData parseNewLocation(String locationJson) {
        try {
            Matcher matcher = locationPattern.matcher(locationJson);
            if (!matcher.matches()) {
                return null;
            } else if (matcher.group("id") == null || matcher.group("distance") == null
                    || matcher.group("country") == null || matcher.group("city") == null
                    || matcher.group("place") == null) {
                return null;
            } else {
                LocationData result = new LocationData();
                result.locationId = Integer.parseInt(matcher.group("id"));
                result.country = matcher.group("country");
                result.city = matcher.group("city");
                result.place = matcher.group("place");
                result.distance = Integer.parseInt(matcher.group("distance"));
                return result;
            }
        }
        catch (Exception e) {
            return null;
        }
    }

    public static LocationData parseEditLocation(String locationJson) {
        try {
            Matcher matcher = locationPattern.matcher(locationJson);
            if (!matcher.matches()) {
                return null;
            } else if (matcher.group("id") != null) {
                return null;
            } else {
                LocationData result = new LocationData();
                if (matcher.group("country") != null) {
                    result.country = matcher.group("country");
                }
                if (matcher.group("city") != null) {
                    result.city = matcher.group("city");
                }
                if (matcher.group("place") != null) {
                    result.place = matcher.group("place");
                }
                if (matcher.group("distance") != null) {
                    result.distance = Integer.parseInt(matcher.group("distance"));
                }
                return result;
            }
        }
        catch (Exception e) {
            return null;
        }
    }


}
