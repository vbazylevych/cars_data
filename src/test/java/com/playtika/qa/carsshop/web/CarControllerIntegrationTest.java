package com.playtika.qa.carsshop.web;

import com.playtika.qa.carsshop.domain.*;
import com.playtika.qa.carsshop.service.CarService;
import com.playtika.qa.carsshop.web.exceptions.NotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(CarController.class)
public class CarControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CarService carService;

    @Test
    public void getExistingCar() throws Exception {
        Optional<CarInStore> response = Optional.of(new CarInStore(new Car("5"),
                new CarInfo(1, "cont")));
        when(carService.get(1)).thenReturn(response);

        mockMvc.perform(get("/cars/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("price").value("1"))
                .andExpect(jsonPath("contact").value("cont"));
    }

    @Test
    public void getNotExistingCar() throws Exception {
        when(carService.get(1)).thenReturn(Optional.empty());

        mockMvc.perform(get("/cars/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void addCar() throws Exception {
        Car car = new Car("1");
        CarInStore carInStore = new CarInStore(car, new CarInfo(10, "cont"));
        when(carService.add(any())).thenReturn(carInStore);

        String jsonString = "{\"plate_number\": 1} ";
        mockMvc.perform(post("/cars?price=10&contact=cont")
                .content(jsonString)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(content().json("0"));
    }

    @Test
    public void ifContactParamIsMissedThrowException() throws Exception {
        mockMvc.perform(post("/cars?price=10").content("{}")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void ifPriceParamIsMissedThrowException() throws Exception {
        mockMvc.perform(post("/cars?contact=jj")
                .content("{}")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getAllCarsReturnsOneCar() throws Exception {
        Map<Long, CarInStore> storedCars = new ConcurrentHashMap<>();
        storedCars.put(1L, new CarInStore(new Car(1, "xxx", "opel", "red", 2000),
                new CarInfo(1, "con")));
        when(carService.getAll()).thenReturn(storedCars.values());

        mockMvc.perform(get("/cars")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$[0].car.plateNumber").value("xxx"))
                .andExpect(jsonPath("$[0].car.color").value("red"))
                .andExpect(jsonPath("$[0].car.model").value("opel"))
                .andExpect(jsonPath("$[0].car.year").value(2000))
                .andExpect(jsonPath("$[0].car.id").value(1))
                .andExpect(jsonPath("$[0].carInfo.price").value(1))
                .andExpect(jsonPath("$[0].carInfo.contact").value("con"));
    }

    @Test
    public void getSeveralCars() throws Exception {
        Map<Long, CarInStore> storedCars = new ConcurrentHashMap<>();
        storedCars.put(1L, new CarInStore(new Car(10, "xxx", "opel", "red", 1), new CarInfo(1, "con1")));
        storedCars.put(2L, new CarInStore(new Car(20, "xxx1", "mazda", "blue", 2), new CarInfo(2, "con2")));
        storedCars.put(3L, new CarInStore(new Car(30, "xxx2", "reno", "black", 3), new CarInfo(3, "con3")));
        when(carService.getAll()).thenReturn(storedCars.values());

        mockMvc.perform(get("/cars")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$[0].car.plateNumber").value("xxx"))
                .andExpect(jsonPath("$[0].carInfo.price").value(1))
                .andExpect(jsonPath("$[1].carInfo.contact").value("con2"))
                .andExpect(jsonPath("$[1].car.color").value("blue"))
                .andExpect(jsonPath("$[2].car.year").value(3));
    }

    @Test
    public void getNotExistingCars() throws Exception {
        Map<Long, CarInStore> storedCars = new ConcurrentHashMap<>();
        when(carService.getAll()).thenReturn(storedCars.values());

        mockMvc.perform(get("/cars").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(content().json("[]"));
    }

    @Test
    public void deleteCar() throws Exception {
        mockMvc.perform(delete("/cars/3")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void rejectExistingDealreturnOk() throws Exception {
        mockMvc.perform(post("/deal/reject/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void rejectNotExistingDealReturnsNotFound() throws Exception {
        doThrow(new NotFoundException("Deal not found!"))
                .when(carService).rejectDeal(1);

        mockMvc.perform(post("/deal/reject/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void acceptBestDealReturnOk() throws Exception {
        User user = new User("kot", "krot", "con1");
        BestDealResponse response = new BestDealResponse(user, 100500, 2);
        when(carService.acceptTheBestDeal(1))
                .thenReturn(response);

        mockMvc.perform(post("/deal/accept/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.user.name").value("kot"))
                .andExpect(jsonPath("$.user.surname").value("krot"))
                .andExpect(jsonPath("$.user.contact").value("con1"))
                .andExpect(jsonPath("$.price").value(100500))
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    public void acceptBestNotExistingDealReturnNotFound() throws Exception {
        doThrow(new NotFoundException("Open ads with id 1 not found!"))
                .when(carService).acceptTheBestDeal(1);

        mockMvc.perform(post("/deal/accept/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void rejectDealBadRequest() throws Exception {
        mockMvc.perform(post("/deal/accept/one")
                .content("{}")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void acceptDealBadRequest() throws Exception {
        mockMvc.perform(post("/deal/accept/two")
                .content("{}")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addDeal() throws Exception {

        when(carService.openNewDeal(any(), anyInt(), anyLong()))
                .thenReturn(1L);

        String jsonString = "{\"name\": \"kot\", \"surname\": \"krot\", \"contact\": \"con1\"} ";
        mockMvc.perform(post("/deal/?price=100500&adsId=1")
                .content(jsonString)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(content().json("1"));
    }

    @Test
    public void addDealThrowsNotFound() throws Exception {

        when(carService.openNewDeal(any(), anyInt(), anyLong()))
                .thenThrow(NotFoundException.class);

        String jsonString = "{\"name\": \"kot\", \"surname\": \"krot\", \"contact\": \"con1\"} ";
        mockMvc.perform(post("/deal/?price=100500&adsId=1")
                .content(jsonString)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
