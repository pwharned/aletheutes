WITH model as (WITH
 TRAINING AS (SELECT t.*, t.prediction as target, ROW_NUMBER() OVER() AS ROW FROM scored_credit  AS T order by random() limit 1000  ),
RATES (LEARN_RATE, COUNT) AS (SELECT .001 AS LEARN_RATE, (SELECT COUNT(*) FROM TRAINING) AS COUNT FROM SYSIBM.SYSDUMMY1),
LEARNING (ITERATION, B1, INTERCEPT, MSE, M1, C) AS (SELECT 1, CAST(0.0 AS DOUBLE),CAST(0.0 AS DOUBLE),CAST(0.0 AS DOUBLE),CAST(0.0 AS DOUBLE),CAST(0.0 AS DOUBLE) FROM SYSIBM.SYSDUMMY1
 UNION ALL SELECT A.ITERATION +1, A.M1, A.C, T.MSE, T.M1, T.C FROM LEARNING A,
TABLE
 (
 SELECT A.ITERATION,
AVG(g.differences*g.differences) AS MSE,
A.M1-(SUM(G.DIFFERENCES*G.loanduration)/(SELECT COUNT FROM RATES))*(SELECT LEARN_RATE from RATES) AS M1,
A.C-(SUM(g.differences)/(SELECT COUNT from RATES))*(SELECT LEARN_RATE FROM RATES) AS C
 FROM
TABLE ( SELECT
((((Z.loanduration*A.B1))+A.INTERCEPT)-z.target) as differences,
  z.loanduration
, z.target
 from training as z
)
as g) t
 WHERE A.ITERATION <= 10000)
  SELECT * FROM LEARNING
 WHERE ITERATION >1 ORDER BY MSE LIMIT 1 ),result as (SELECT loanduration,scoring_id, scoring_timestamp, prediction as target FROM scored_credit)

 SELECT max(scoring_timestamp),
 avg( (((model.b1*result.loanduration)+model.intercept)-result.target)*(((model.b1*result.loanduration)+model.intercept)-result.target) )
 from model, result
 group by
 day(scoring_timestamp),
 hour(scoring_timestamp)
