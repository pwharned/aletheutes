
--scoring_id,checkingstatus,loanduration,credithistory,loanpurpose,loanamount,existingsavings,employmentduration,installmentpercent,sex,othersonloan,currentresidenceduration,ownsproperty,age,installmentplans,housing,existingcreditscount,job,dependents,telephone,foreignworker,prediction,scoring_timestamp
create table scored_data (
scoring_id FLOAT,
checkingstatus FLOAT,
loanduration FLOAT,
credithistory FLOAT,
loanpurpose FLOAT,
loanamount FLOAT,
existingsavings FLOAT,
employmentduration FLOAT,
installmentpercent FLOAT,
sex FLOAT,
othersonloan FLOAT,
currentresidenceduration FLOAT,
ownsproperty FLOAT,
age FLOAT,
installmentplans FLOAT,
housing FLOAT,
existingcreditscount FLOAT,
job FLOAT,
dependents FLOAT,
telephone FLOAT,
foreignworker FLOAT,
prediction INT,
scoring_timestamp TIMESTAMP

)
as (with csv_file as (

select * from external '/var/custom/scored.csv'

(
scoring_id FLOAT,
checkingstatus FLOAT,
loanduration FLOAT,
credithistory FLOAT,
loanpurpose FLOAT,
loanamount FLOAT,
existingsavings FLOAT,
employmentduration FLOAT,
installmentpercent FLOAT,
sex FLOAT,
othersonloan FLOAT,
currentresidenceduration FLOAT,
ownsproperty FLOAT,
age FLOAT,
installmentplans FLOAT,
housing FLOAT,
existingcreditscount FLOAT,
job FLOAT,
dependents FLOAT,
telephone FLOAT,
foreignworker FLOAT,
prediction INT,
scoring_timestamp TIMESTAMP

)
using (DELIMITER ',' format text)
)

select * from csv_file offset 1 rows) with data;
