package com.example.campshare.web;

import com.example.campshare.gear.GearRepository;
import com.example.campshare.gear.Gear;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GearController.class)
@AutoConfigureMockMvc(addFilters = false)
class GearControllerTest {
 @Autowired MockMvc mockMvc;
 @MockitoBean GearRepository gearRepository;
 @Test void gearListReturnsGearsView() throws Exception {
  when(gearRepository.findAllByOrderByIdAsc(any(Pageable.class))).thenReturn(emptyPage(0));
  mockMvc.perform(get("/gears")).andExpect(status().isOk()).andExpect(view().name("gears"))
          .andExpect(model().attributeExists("gears"))
          .andExpect(model().attribute("pageNumber", 0));
 }

 @Test void gearDetailReturnsDetailView() throws Exception {
  when(gearRepository.findById(1L)).thenReturn(Optional.of(org.mockito.Mockito.mock(com.example.campshare.gear.Gear.class)));
  mockMvc.perform(get("/gears/1")).andExpect(status().isOk()).andExpect(view().name("gear-detail")).andExpect(model().attributeExists("gear"));
 }

 @Test void gearSearchUsesSearchTermAndPreservesQuery() throws Exception {
  when(gearRepository.search(org.mockito.ArgumentMatchers.eq("テント"), any(Pageable.class))).thenReturn(emptyPage(0));
  mockMvc.perform(get("/gears").param("q", " テント "))
          .andExpect(status().isOk())
          .andExpect(view().name("gears"))
          .andExpect(model().attribute("query", "テント"));
  verify(gearRepository).search(org.mockito.ArgumentMatchers.eq("テント"), any(Pageable.class));
 }

 @Test void twentyResultsExposeExactlyTwoPages() throws Exception {
  when(gearRepository.findAllByOrderByIdAsc(any(Pageable.class)))
          .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 15), 20));
  mockMvc.perform(get("/gears"))
          .andExpect(model().attribute("totalPages", 2))
          .andExpect(model().attribute("pageNumber", 0));
 }

 private Page<Gear> emptyPage(int page) {
  return new PageImpl<>(List.of(), PageRequest.of(page, 15), 0);
 }
}
