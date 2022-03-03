CONTAINER_NAME="elastic_wing"

docker stop ${CONTAINER_NAME}
docker commit $(docker ps -l -q) lehmansarahm/scipy-notebook:latest
docker login && docker push lehmansarahm/scipy-notebook:latest
docker start ${CONTAINER_NAME}