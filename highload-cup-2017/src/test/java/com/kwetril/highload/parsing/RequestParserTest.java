package com.kwetril.highload.parsing;

import static org.junit.Assert.assertEquals;

import com.kwetril.highload.request.UserData;
import org.junit.Test;

import java.util.regex.Matcher;

public class RequestParserTest {
    @Test
    public void testUserPattern() throws Exception {
        Matcher m1 = RequestParser.userPattern.matcher(
                "{"
                        + "\"id\": 1, "
                        + "\"email\": \"johndoe@gmail.com\", "
                        + "\"first_name\": \"Иван\", "
                        + "\"last_name\": \"Doe\", "
                        + "\"gender\": \"m\", "
                        + "\"birth_date\": 1613433600"
                        + "}"
        );
        assertEquals(true, m1.matches());
        assertEquals("1", m1.group("id"));
        assertEquals("johndoe@gmail.com", m1.group("email"));
        assertEquals("Иван", m1.group("fname"));
        assertEquals("Doe", m1.group("lname"));
        assertEquals("m", m1.group("gender"));
        assertEquals("1613433600", m1.group("bdate"));


        Matcher m2 = RequestParser.userPattern.matcher(
                "{"
                        + "\"id\": 1, "
                        + "\"email\": \"johndoe@gmail.com\", "
                        + "\"first_name\": \"John\", "
                        + "\"first_name\": \"Ivan\", "
                        + "\"last_name\": \"Doe\", "
                        + "\"gender\": \"m\", "
                        + "\"birth_date\": 1613433600"
                        + "}"
        );
        assertEquals(false, m2.matches());

        Matcher m3 = RequestParser.userPattern.matcher(
                "{"
                        + "\"gender\": \"f\", "
                        + "\"last_name\": \"Test\", "
                        + "}"
        );
        assertEquals(true, m3.matches());
        assertEquals("Test", m3.group("lname"));
        assertEquals("f", m3.group("gender"));


        Matcher m4 = RequestParser.userPattern.matcher(
                "{\"gender\":\"m\",\"birth_date\":-2183846400,\"last_name\":\"Лебетаный\",\"id\":125,\"first_name\":\"Фёдор\",\"email\":\"ososfeawwe@icloud.com\"}"
        );
        assertEquals(true, m4.matches());
        assertEquals("125", m4.group("id"));
        assertEquals("ososfeawwe@icloud.com", m4.group("email"));
        assertEquals("Фёдор", m4.group("fname"));
        assertEquals("Лебетаный", m4.group("lname"));
        assertEquals("m", m4.group("gender"));
        assertEquals("-2183846400", m4.group("bdate"));
    }

    @Test
    public void testParseNewUser() throws Exception {
        UserData user = RequestParser.parseNewUser(
                "{"
                        + "\"id\": 1, "
                        + "\"email\": \"johndoe@gmail.com\", "
                        + "\"first_name\": \"John\", "
                        + "\"last_name\": \"Doe\", "
                        + "\"gender\": \"m\", "
                        + "\"birth_date\": 1613433600"
                        + "}"
        );
        assertEquals(1, user.userId);
        assertEquals("johndoe@gmail.com", user.email);
        assertEquals("John", user.firstName);
        assertEquals("Doe", user.lastName);
        assertEquals("m", user.gender);
        assertEquals(1613433600, user.birthDate);

        UserData user1 = RequestParser.parseNewUser(
                "{"
                        + "\"id\": 1, "
                        + "\"email\": \"johndoe@gmail.com\", "
                        + "\"first_name\": \"John\", "
                        + "\"first_name\": \"Ivan\", "
                        + "\"last_name\": \"Doe\", "
                        + "\"gender\": \"m\", "
                        + "\"birth_date\": 1613433600"
                        + "}"
        );
        assertEquals(null, user1);

        UserData user2 = RequestParser.parseNewUser(
                "{"
                        + "\"gender\": \"f\", "
                        + "\"last_name\": \"Test\", "
                        + "}"
        );
        assertEquals(null, user2);
    }

    @Test
    public void testParseEditUser() throws Exception {
        UserData user = RequestParser.parseEditUser(
                "{"
                        + "\"id\": 1, "
                        + "\"email\": \"johndoe@gmail.com\", "
                        + "\"first_name\": \"John\", "
                        + "\"last_name\": \"Doe\", "
                        + "\"gender\": \"m\", "
                        + "\"birth_date\": 1613433600"
                        + "}"
        );
        assertEquals(null, user);

        UserData user1 = RequestParser.parseEditUser(
                "{"
                        + "\"id\": 1, "
                        + "\"email\": \"johndoe@gmail.com\", "
                        + "\"first_name\": \"John\", "
                        + "\"first_name\": \"Ivan\", "
                        + "\"last_name\": \"Doe\", "
                        + "\"gender\": \"m\", "
                        + "\"birth_date\": 1613433600"
                        + "}"
        );
        assertEquals(null, user1);

        UserData user2 = RequestParser.parseEditUser(
                "{"
                        + "\"gender\": \"f\", "
                        + "\"last_name\": \"Test\", "
                        + "}"
        );
        assertEquals("Test", user2.lastName);
        assertEquals("f", user2.gender);
    }
}
