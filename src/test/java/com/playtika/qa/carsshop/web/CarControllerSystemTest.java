package com.playtika.qa.carsshop.web;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class CarControllerSystemTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Before
    public void init() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }


    @Test
    public void addCar() throws Exception {
        String firstCar = "{\"plateNumber\": \"1\", \"color\": \"\", \"model\": \"\", \"year\": 2000 } ";
        addCarInStore(firstCar);
    }

    @Test
    public void getCar() throws Exception {
        String firstCar = "{\"plateNumber\": \"2\", \"color\": \"\", \"model\": \"\", \"year\": 2000 } ";
        String id = addCarInStore(firstCar);

        mockMvc.perform(get("/cars/" + id)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("price").value("10"))
                .andExpect(jsonPath("contact").value("cont"));
    }


    @Test
    public void getAllCars() throws Exception {
        String firstCar = "{\"plateNumber\": \"3\", \"color\": \"\", \"model\": \"\", \"year\": 2000 } ";
        String secondCar = "{\"plateNumber\": \"4\", \"color\": \"\", \"model\": \"\", \"year\": 2000 } ";
        addCarInStore(firstCar);
        addCarInStore(secondCar);

        mockMvc.perform(get("/cars")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void delerteCar() throws Exception {
        String firstCar = "{\"plateNumber\": \"5\", \"color\": \"\", \"model\": \"\", \"year\": 2000 } ";
        String id = addCarInStore(firstCar);

        mockMvc.perform(delete("/cars/" + id)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private String addCarInStore(String car) throws Exception {
        return mockMvc.perform(post("/cars?price=10&contact=cont")
                .content(car)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Test
    public void rejectExistingDealReturnsOk() throws Exception {
        String firstCar = "{\"plateNumber\": \"6\", \"color\": \"\", \"model\": \"\", \"year\": 2000 } ";
        addCarInStore(firstCar);

        String jsonString = "{\"name\": \"kot\", \"surname\": \"krot\", \"contact\": \"con1\"}";
        String adsId = addDeal(jsonString, "100500");

        mockMvc.perform(post("/deal/reject/" + adsId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void chooseBestDealReturnsOk() throws Exception {
        String firstCar = "{\"plateNumber\": \"7\", \"color\": \"\", \"model\": \"\", \"year\": 2000 } ";
        addCarInStore(firstCar);

        String jsonString1 = "{\"name\": \"kot\", \"surname\": \"krot\", \"contact\": \"con1\"} ";
        addDeal(jsonString1, "100500");
        String jsonString2 = "{\"name\": \"kot2\", \"surname\": \"krot2\", \"contact\": \"con2\"} ";
        addDeal(jsonString2, "100501");

        mockMvc.perform(post("/deal/accept/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.user.name").value("kot2"))
                .andExpect(jsonPath("$.user.surname").value("krot2"))
                .andExpect(jsonPath("$.user.contact").value("con2"))
                .andExpect(jsonPath("$.price").value(100501));
    }

    private String addDeal(String jsonString, String price) throws Exception {
        return mockMvc.perform(post("/deal/?price=" + price + "&adsId=1")
                .content(jsonString)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

}




