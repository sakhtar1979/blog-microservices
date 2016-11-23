PreReq:
This readme assumes that you are either working on Mac or *Nix (or your docker host is a *Nix Virtual machine). You should also have following softwares
installed on the docker host machine:
1- Docker (during development we use Docker for Mac)
2- Java 8
3- Gradle
4- curl
5- jq
6- To run load balancer (to simulate GTM/LTM), you need to have nginx with stream module installed.
   An easy way to install on nginx (with stream module) on Mac is using Homebrew. Run following commonad
   on terminal. With Stream is used to make ngix as SSL pass through proxy where Zuul servers are on SSL.

   brew install homebrew/nginx/nginx-full --with-stream
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
6- Go to http://localhost:7979 and put http://composite-nyc:8080/hystrix.stream in the stream to monitor. In another window you
   can use http://composite-nc:8080/hystrix.stream. Please note that Turbie configuration is note
   yet working preventing us to see all Streams in one monitoring dashboard.

Steps to Run Nginx SSL pass through proxy:
1- First check if port 443 is available to nginx by running
lsof -i :443   
If not, then you have two options i.e. Kill the apps using that port or change the nginx port
from 443 to some other port in nginx-ssl-passthru.conf file. Please note that if you change the port from 443 then don't forget
to use that port in subsequent testing.

2- Run following command to start nginx
sudo nginx -c ~/projects/src/callistaenterprise/blog-microservices/nginx-ssl-passthru.conf

3- Nginx started above uses NYC zone as primary and NC for backup (Disaster onlY).

4- You can now test it (while looking at docker compose log) that all calls from Zuul all the
to microservices are going through NYC always.
Test command:
curl -H "Authorization: Bearer $TOKEN"  -ks 'https://localhost/api/product/1046' | jq .

Note: if Token is expired then please run the Token command again.

5- Now to similuate DR scenario, please stop the edgeserver in NYC (Zuul) either from command line or Kitematic
6- Now run the following command again:
curl -H "Authorization: Bearer $TOKEN"  -ks 'https://localhost/api/product/1046' | jq .

Expected Result: You should see NC zuul into action and all services will be called from NC zone.
It proves that using zone we can simulate current Data center and DR strategy. The failover will be done at GTM level in real
environment (manually for DR testing and perhaps automatically for actual Disaster scenario).

6- Bring back the previoulsy stopped Zuul NYC container by running:
docker-compose up -d

7- Now again run:
curl -H "Authorization: Bearer $TOKEN"  -ks 'https://localhost/api/product/1046' | jq .

Expected Result: You should see in the log that all calls again started to go to NYC zone (including Zuul and other services).
This is happening because NYC is primary zone.

8- Now stop following container (via command line or Kitematic)
blogmicroservices_rec-nyc

9- Again run curl -H "Authorization: Bearer $TOKEN"  -ks 'https://localhost/api/product/1046' | jq .
   and look the logs.

   Expected Result: You should see all the calls except blogmicroservices_rec-nyc are still going to NYC. blogmicroservices_rec-nyc is
   now replaced by blogmicroservices_rec-nc. This shows that zone are prefered and in case of partial failure (except Zuul), it will start
   going to other datacenter/zone.

 10- Bring back the blogmicroservices_rec-nyc by running
     docker-compose up -d


Other Useful Command:

Docker Compose:
To run:
docker-compose up -d
To check logs
docker-compose logs -f
To scale up/down:
docker-compose scale rec-nyc=2
To stop:
docker-compose stop

Nginx:
To start nginx for 443 (SSL Pass thru with backup):
sudo nginx -c ~/projects/src/callistaenterprise/blog-microservices/nginx-ssl-passthru.conf
To stop:
sudo nginx -s stop

Note: You don't need sudo if using higher port.

Application Commands:
TOKEN=$(curl -ks https://acme:acmesecret@localhost:9999/uaa/oauth/token -d grant_type=password -d client_id=acme -d username=user -d password=password | jq -r .access_token)
echo $TOKEN
curl -H "Authorization: Bearer $TOKEN"  -ks 'https://localhost/api/product/1046' | jq .


Steps to Test Hystrix:
1- cd ../blog-microservices-config
vi review-service.yml
uncomment first three lines

2- git commit -a -m "make review service slow and increase log-level to DEBUG"

3- Refresh the Spring confg context by running:
docker-compose exec rev wget -qO- localhost:8080/refresh --post-data=""["logging.level.se.callista","service.defaultMaxMs","service.defaultMinMs"]
