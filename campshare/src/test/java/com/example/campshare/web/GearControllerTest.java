package com.example.campshare.web;

import com.example.campshare.gear.GearRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GearController.class)
@AutoConfigureMockMvc(addFilters = false)
class GearControllerTest {
 @Autowired MockMvc mockMvc;
 @MockitoBean GearRepository gearRepository;
 @Test void gearListReturnsGearsView() throws Exception {
  when(gearRepository.findAllByOrderByIdAsc()).thenReturn(List.of());
  mockMvc.perform(get("/gears")).andExpect(status().isOk()).andExpect(view().name("gears")).andExpect(model().attributeExists("gears"));
 }
}
