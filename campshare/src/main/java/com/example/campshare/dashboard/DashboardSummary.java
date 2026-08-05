package com.example.campshare.dashboard;
public record DashboardSummary(long rentalCount, long totalSales, long currentMonthSales, long previousMonthSales, long outOfStockCount) {
    public long salesDifference() { return currentMonthSales - previousMonthSales; }
    public boolean hasPreviousMonthSales() { return previousMonthSales > 0; }
    public double salesChangeRate() { return hasPreviousMonthSales() ? (double) salesDifference() / previousMonthSales * 100 : 0; }
    public boolean salesIncreased() { return salesDifference() >= 0; }
    public String salesChangeLabel() {
        if (!hasPreviousMonthSales()) return "前月比 比較対象なし";
        return String.format("前月比 %s%.1f%%", salesIncreased() ? "+" : "", salesChangeRate());
    }
    public String salesDifferenceLabel() {
        if (!hasPreviousMonthSales()) return "先月の売上がないため増減率を表示できません";
        return salesIncreased() ? "先月より ¥" + salesDifference() + " 増加" : "先月より ¥" + (-salesDifference()) + " 減少";
    }
}
