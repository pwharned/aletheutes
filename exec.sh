docker exec -it $(docker ps | grep db2 | awk '{ print $1 }') /bin/sh
