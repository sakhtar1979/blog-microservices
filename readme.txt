PreReq:
This readme assumes that you are either working on Mac or *Nix (or your docker host is a *Nix Virtual machine). You should also have following softwares
installed on the docker host machine:
1- Docker (during development we use Docker for Mac)
2- Java 8
3- Gradle
4- curl
5- jq

This guide is using Docker for Mac and also have Kitematic. You also need to have curl and jq installed.

Steps to Run:
1- Change the IP adress in docker-compose.yml to your machine's host IP address in discovery and dicovery2.
2- Make sure that docker deamon is running on your machine.
3- Run ./build-all.sh
4- After step 3 complestes, run the command below on terminal to run all the containers.
docker-compose up -d

Wait for all the docker containers to be up and running. You can see the progress on Kitematic or better use following in a new Terminal Tab:
docker-compose logs -f

Steps to Verify everything is up:
1- Check if Eureka on http://localhost:8761/ is up and running and following applications are registered:
COMPOSITE-SERVICE, CONFIG-SERVER, EDGE-SERVER, EUREKA, PRODUCT-SERVICE, RECOMMENDATION-SERVICE, REVIEW-SERVICE

2- Check if http://localhost:8762/ (Peer Eureka Server) is up and running as well. See if all the application above are registered in this instance
   as well.
3- Now we need to get the beaer token from auth server by running following command:
TOKEN=$(curl -ks https://acme:acmesecret@localhost:9999/uaa/oauth/token -d grant_type=password -d client_id=acme -d username=user -d password=password | jq -r .access_token)

Verify the token by running following command:
echo $TOKEN
4- Now run following curl to call Product composite service (via Zuul) in NY zone:
curl -H "Authorization: Bearer $TOKEN"  -ks 'https://localhost:8443/api/product/1046' | jq .
5- Now run following curl to call Product composite service (via Zuul) in NC zone:
curl -H "Authorization: Bearer $TOKEN"  -ks 'https://localhost:9443/api/product/1046' | jq .
