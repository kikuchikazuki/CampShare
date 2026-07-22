package com.example.campshare.dashboard;
import org.apache.ibatis.annotations.Mapper; import org.apache.ibatis.annotations.Select;
@Mapper public interface DashboardMapper { @Select("SELECT (SELECT COUNT(*) FROM rentals) AS rentalCount, (SELECT COALESCE(SUM(total_price), 0) FROM rentals) AS totalSales, (SELECT COUNT(*) FROM gears WHERE stock_count = 0) AS outOfStockCount") DashboardSummary findSummary(); }
