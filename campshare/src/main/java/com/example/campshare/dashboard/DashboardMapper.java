package com.example.campshare.dashboard;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DashboardMapper {
    @Select("SELECT (SELECT COUNT(*) FROM rentals) AS rentalCount, (SELECT COALESCE(SUM(total_price), 0) FROM rentals) AS totalSales, (SELECT COALESCE(SUM(total_price), 0) FROM rentals WHERE created_at >= date_trunc('month', CURRENT_DATE)) AS currentMonthSales, (SELECT COALESCE(SUM(total_price), 0) FROM rentals WHERE created_at >= date_trunc('month', CURRENT_DATE - INTERVAL '1 month') AND created_at < date_trunc('month', CURRENT_DATE)) AS previousMonthSales, (SELECT COUNT(*) FROM gears WHERE stock_count = 0) AS outOfStockCount")
    DashboardSummary findSummary();

    @Select("SELECT u.display_name AS userName, g.name AS gearName, to_char(r.start_date, 'YYYY/MM/DD') || ' - ' || to_char(r.end_date, 'YYYY/MM/DD') AS rentalPeriod, r.total_price AS totalPrice FROM rentals r JOIN users u ON u.id = r.user_id JOIN gears g ON g.id = r.gear_id WHERE lower(u.display_name) LIKE lower(concat('%', #{userName}, '%')) AND lower(g.name) LIKE lower(concat('%', #{gearName}, '%')) ORDER BY r.created_at DESC")
    List<DashboardReservationRow> findReservations(@Param("userName") String userName, @Param("gearName") String gearName);

    @Select("SELECT name AS gearName, category, stock_count AS stockCount FROM gears WHERE stock_count = 0 AND lower(name) LIKE lower(concat('%', #{gearName}, '%')) AND lower(category) LIKE lower(concat('%', #{category}, '%')) ORDER BY id")
    List<OutOfStockGearRow> findOutOfStockGears(@Param("gearName") String gearName, @Param("category") String category);
}
