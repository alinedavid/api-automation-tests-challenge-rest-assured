package Entities;

import Entities.Booking;
import Entities.BookingDates;
import Entities.User;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.filter.log.ErrorLoggingFilter;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Objects;

import static io.restassured.RestAssured.given;
import static io.restassured.config.LogConfig.logConfig;
import static io.restassured.module.jsv.JsonSchemaValidator.*;
import static org.hamcrest.Matchers.*;

public class BookingTests {
    public static Faker faker;
    private static RequestSpecification request;
    private static Booking booking;
    private static BookingDates bookingDates;
    private static User user;

    @BeforeAll
    public static void Setup(){
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";
        faker = new Faker();
        user = new User(faker.name().username(),
                faker.name().firstName(),
                faker.name().lastName(),
                faker.internet().safeEmailAddress(),
                faker.internet().password(8,10),
                faker.phoneNumber().toString());

        bookingDates = new BookingDates("2018-01-02", "2018-01-03");
        booking = new Booking(user.getFirstName(), user.getLastName(),
                (float)faker.number().randomDouble(2, 50, 100000),
                true,bookingDates,
                "");
        RestAssured.filters(new RequestLoggingFilter(),new ResponseLoggingFilter(), new ErrorLoggingFilter());
    }

    @BeforeEach
    void setRequest(){
        request = given().config(RestAssured.config().logConfig(logConfig().enableLoggingOfRequestAndResponseIfValidationFails()))
                .contentType(ContentType.JSON)
                .auth().preemptive().basic("admin", "password123");
    }

    @Test
    public void getAllBookingsById_returnOk(){
            Response response = request
                                    .when()
                                        .get("/booking")
                                    .then()
                                        .extract()
                                        .response();


        Assertions.assertNotNull(response);
        Assertions.assertEquals(200, response.statusCode());
    }
    @Test
    public void deleteBookingById_returnOk(){
            Response response = request
                                    .when()
                                        .delete("/booking/" + getId())
                                    .then()
                    .extract()
                                        .response();


        Assertions.assertNotNull(response);
        Assertions.assertEquals(201, response.statusCode());
    }

    private String getId(){
        List<BookingId> response = request
                .when()
                .get("/booking")
                .as(new TypeRef<List<BookingId>>() {});
        System.out.println(response.get(0).toString());
        return response.get(0).getBookingid();
    }

    @Test
    public void  getAllBookingsByUserFirstName_BookingExists_returnOk(){
                    request
                        .when()
                            .queryParam("firstName", "Carol")
                            .get("/booking")
                        .then()
                            .assertThat()
                            .statusCode(200)
                            .contentType(ContentType.JSON)
                        .and()
                        .body("results", hasSize(greaterThan(0)));

    }

    @Test
    public void  CreateBooking_WithValidData_returnOk(){

        Booking test = booking;
        given().config(RestAssured.config().logConfig(logConfig().enableLoggingOfRequestAndResponseIfValidationFails()))
                    .contentType(ContentType.JSON)
                        .when()
                        .body(booking)
                        .post("/booking")
                        .then()
                        .body(matchesJsonSchemaInClasspath("createBookingRequestSchema.json"))
                        .and()
                        .assertThat()
                        .statusCode(200)
                        .contentType(ContentType.JSON).and().time(lessThan(2000L));

    }
    @Test
    public void updateBooking(){
        Response response = request
                .when()
                .body(booking)
                .put("/booking/213")
                .then()
                .body(matchesJsonSchemaInClasspath("createBookingRequestSchema.json"))
                .extract()
                .response();


        Assertions.assertNotNull(response);
        Assertions.assertEquals(200, response.statusCode());
    }

    @Test
    public void patchBooking(){
        Response response = request
                .when()
                .body(booking)
                .patch("/booking/213")
                .then()
                .body(matchesJsonSchemaInClasspath("createBookingRequestSchema.json"))
                .extract()
                .response();


        Assertions.assertNotNull(response);
        Assertions.assertEquals(200, response.statusCode());
    }
    @Test
    public void ping(){
        Response response = request
                .when()
                .get()
                .then()
                .extract()
                .response();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(200, response.statusCode());
    }
}

 class BookingId {
    private String bookingid;

     public String getBookingid() {
         return bookingid;
     }

     public void setBookingid(String bookingid) {
         this.bookingid = bookingid;
     }
 }
