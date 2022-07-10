WITH transformed AS (
	SELECT 
		S.*, 
		cast(CASE WHEN S.iris = 'setosa' THEN 0.0 WHEN S.iris = 'versicolor' then 1.0 when s.iris='virginica' then 2.0 END as double) AS target
	FROM samples AS S 
	order by sepal_length
),
training AS (
  SELECT  t.*, row_number() over() as row FROM transformed as t ORDER BY random()
),

rates (learn_rate, count) as (select 0.025 as learn_rate, (select count(*) from training) as count from sysibm.sysdummy1),
learning (iteration,b1,intercept,  mse, m, c) as 
(
select 1, cast(0.05 as double), cast(0.2 as double),0.0, cast(0.0 as double), cast(0.0 as double) from sysibm.sysdummy1
	union all
select a.iteration + 1, a.m, a.c, t.mse, t.m,t.c
from 
  learning a
, TABLE 
(
  select 
  a.iteration as iteration,
  avg((train.target-((train.sepal_length*a.b1)+a.intercept))*(train.target-((train.sepal_length*a.b1)+a.intercept))) as mse,
  a.m -(sum((((train.sepal_length *a.b1)+a.intercept)-train.target)*train.sepal_length)/(select count from rates)) *(select learn_rate from rates)  as m,
  a.c- (sum((((train.sepal_length *a.b1)+a.intercept)-train.target))/(select count from rates))*(select learn_rate from rates)  as c
    --(sum((t.target - ((t.sepal_length *a.b1)+a.intercept)))/(select count from rates))*(select learn_rate from rates)  as c_new
  from training as train 
) t
where a.iteration < 100000
)
select * from learning order by mse