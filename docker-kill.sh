docker kill $(docker ps | grep db2 | awk '{ print $1}');
docker rm $(docker ps --all | grep db2 | awk '{ print $1}');
docker volume rm $(docker volume list | awk 'NR>1 {print $2}');