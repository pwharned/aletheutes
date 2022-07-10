-- !preview conn=DBI::dbConnect(RSQLite::SQLite())

with mytable (n) as 
(
select 1 from sysibm.sysdummy1
	union all
select n + 1 from mytable where n < 10
)

, 

mytable2 (x, multiple) as 
(
select 1, 1 from sysibm.sysdummy1
	union all
select a.x + 1, g.multiple
from 
  mytable2 a
  
, table
(
select sum(t.n*a.x) as multiple 
from mytable as t
,
 TABLE 
(
  select 
  max (z.max_iter) as y 
  from mytable as t, TABLE 
(
  select a.x as max_iter
  from mytable as t 
) as z 
) as v 
) as g 
where a.x < 3 
)
select * 
from mytable2