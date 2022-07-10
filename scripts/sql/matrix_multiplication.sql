with t1 as ( select  row_number() over() as rowNumber, credit_risk.* from credit_risk limit 2 ),
m1 as (
select rowNumber, 1 as columnNumber,  (select col1.age as value from t1 as col1 where rowNumber = t1.rowNumber ) from t1
union all
select rowNumber, 2 as columnNumber,  (select col1.existingcreditscount as value from t1 as col1 where rowNumber = t1.rowNumber ) from t1
),
m2 as (select * from m1),


results as (
select m1.rowNumber as rowNumber, m2.columnNumber as columnNumber, sum(m1.value * m2.value) as val from m1,m2 where m1.columnNumber = m2.rowNumber group by m1.rowNumber, m2.columnNumber
)

select * from results
