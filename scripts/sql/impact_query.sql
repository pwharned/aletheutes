

with t1 as (select sex, cast(count(sex) as float) as group, risk, minute(timestamp) as minutes from test_data2 where date(timestamp) =(select date(max(timestamp)) from test_data2) group by sex, risk, minute(timestamp)),
t2 as
(select sex , risk, minutes, group,group/SUM(group) over (partition by sex, minutes) as ratios from t1)

select sex, risk, ratios, group, minutes, cast(ratios as float)/cast(sum(ratios) over (partition by  risk, minutes) - ratios as float) as disparate_impact from t2

WITH t1 as (SELECT risk as prediction,minute(timestamp) as minutes,hour(timestamp) as hours,day(timestamp) as days,COUNT(sex) as group,sex FROM test_data2
WHERE day(timestamp)=(SELECT day(max(timestamp)) as days FROM test_data2) GROUP BY sex , risk , minute(timestamp) , hour(timestamp) , day(timestamp)) ,
t2 as (SELECT  prediction,sex,group as group,minutes,cast(group as float)/sum(group) OVER( PARTITION BY sex,minutes,hours,days ) as ratios,hours,days FROM t1)
SELECT prediction,sex,group as group, case when sum(ratios) OVER( PARTITION BY prediction,minutes,hours,days ) -ratios =0 then 1 else ratios/(sum(ratios) OVER( PARTITION BY prediction,minutes,hours,days ) -ratios) end as disparate_impact,minutes,hours,days FROM t2








WITH t1 as (SELECT risk as prediction,max(timestamp) OVER( PARTITION BY hour(timestamp),minute(timestamp), day(timestamp) ) as time,sex FROM test_data2
WHERE day(timestamp)=(SELECT day(max(timestamp)) as days FROM test_data2)) ,t2 as (SELECT prediction,COUNT(sex) as group,sex,time FROM t1 GROUP BY sex , prediction , time) ,
t3 as (SELECT prediction,sex,group as group,time,(cast(group as float)/sum(group) OVER( PARTITION BY sex,time )) as ratios FROM t2)
SELECT prediction,sex,group as group, case when sum(ratios) OVER( PARTITION BY prediction,time )-ratios = 0 then 1 else  ratios/(sum(ratios) OVER( PARTITION BY prediction,time )-ratios) end as disparate_impact,time FROM t3



