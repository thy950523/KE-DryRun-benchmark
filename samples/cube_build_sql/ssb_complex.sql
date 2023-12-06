-- 1. 制造商、城市和月分析订单收入
SELECT p_mfgr, s_city, d_yearmonth, SUM(lo_revenue) revenue
FROM lineorder, part, supplier, dates 
WHERE lo_partkey = p_partkey
AND lo_suppkey = s_suppkey
AND lo_orderdate = d_datekey
GROUP BY p_mfgr, s_city, d_yearmonth
ORDER BY revenue DESC;


-- 2. 4维分析订单收入
SELECT p_brand, p_type, s_region, d_year,SUM(lo_revenue) revenue
FROM lineorder, part, supplier, dates
WHERE lo_partkey = p_partkey
AND lo_suppkey = s_suppkey  
AND lo_orderdate = d_datekey
GROUP BY p_brand, p_type, s_region, d_year  
ORDER BY d_year, revenue DESC;

-- 3. 地区和品牌为维度的分析
SELECT d_year, s_city, p_brand, SUM(lo_revenue) revenue
FROM dates, supplier, lineorder, part
WHERE lo_orderdate = d_datekey
AND lo_suppkey = s_suppkey
AND lo_partkey = p_partkey
GROUP BY d_year, s_city, p_brand
ORDER BY d_year, revenue DESC;


-- 4. 交叉多维组合分析收入
SELECT c_city, s_city, d_year, p_brand, SUM(lo_revenue) revenue
FROM customer, supplier, lineorder, dates, part 
WHERE lo_custkey = c_custkey
AND lo_suppkey = s_suppkey
AND lo_partkey = p_partkey
AND lo_orderdate = d_datekey
GROUP BY c_city, s_city, d_year, p_brand 
ORDER BY revenue DESC;

-- 5. 按品牌、类型统计产品数量,基础分析 
SELECT p_brand, p_type, COUNT(*) num
FROM part  
GROUP BY p_brand, p_type  
ORDER BY num DESC;


-- 6. 按城市统计客户数量,TOP 5结果
SELECT c_city, COUNT(*) AS cnt FROM customer GROUP BY c_city ORDER BY cnt DESC LIMIT 5;   


-- 7. 城市年份两个维度统计的订单总收入
SELECT c_city, d_year, SUM(lo_revenue) AS rev FROM customer, lineorder, dates WHERE lo_custkey = c_custkey AND lo_orderdate = d_datekey GROUP BY c_city, d_year ORDER BY rev DESC;

-- 8. 两国之间的销售收入交叉分析
SELECT c_nation, s_nation, d_year, SUM(lo_revenue) revenue
FROM customer, supplier, lineorder, dates  
WHERE lo_custkey = c_custkey
AND lo_suppkey = s_suppkey  
AND lo_orderdate = d_datekey
GROUP BY c_nation, s_nation, d_year 
ORDER BY d_year, revenue DESC;


-- 9. 按年计算订单总收入,全局数据分析
SELECT d_year, SUM(lo_revenue) total_revenue  
FROM lineorder, dates
WHERE lo_orderdate = d_datekey 
GROUP BY d_year
ORDER BY d_year;


-- 10. 按产品类别和年份两维度统计订单总收入
SELECT p_category, d_year, SUM(lo_revenue) revenue 
FROM lineorder, dates, part 
WHERE lo_partkey = p_partkey
AND lo_orderdate = d_datekey
GROUP BY p_category, d_year
ORDER BY revenue DESC;