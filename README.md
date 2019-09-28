# CryptoAlerter

Java and Spring-boot based personal project to test out AWS services. uses a basic delta-tracking algortihm to alert user about ideal times to buy and sell.

Required the creation of:
 - RDS Postgress db with 2 tables (prices and trades) to store historical prices and user trades
 - SNS topic to alert user
 - ECR docker registry to store docker image

Deployed through manualy loading docker image in an ec2 instance

need to run `maven clean package` and then `docker build .` to build docker container
