docker logs $(docker ps | grep db2 | awk '{ print $1}');
