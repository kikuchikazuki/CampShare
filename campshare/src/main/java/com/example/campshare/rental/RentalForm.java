package com.example.campshare.rental;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class RentalForm {

    @NotNull(message = "開始日を選択してください。")
    private LocalDate startDate;

    @NotNull(message = "終了日を選択してください。")
    private LocalDate endDate;

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
