# logparser

High level doc:
Main - main class
FileReader- reads the file as the per the path passed in args
DbHelper - has relevant db access logic
AccessLog- Java model where values parsed from the log file are set before saving to db
main/resources/META-INF/persistence.xml - JPA persistence unit setup here. Replace javax.persistence.jdbc.url,javax.persistence.jdbc.user,javax.persistence.jdbc.password with local db values before building a jar file

To run this project in the commandline execute 


java -cp "uber-logparser-0.0.1-SNAPSHOT.jar" com.wallethub.logparser.Main --threshold=100 --accesslog=/Users/jmaina/Downloads/Java_MySQL_Test/access.log --startDate=2017-01-01.15:00:00 --duration=hourly --threshold=200
 args --threshold=100
 
 **NB** The jar file expects MySQL username/password to be root/r00t respectively
 
 *To build a new jar file, execute mvn package then go to target/ folder*
 
 
The following are the two db tables for the app:

ACCESSLOG
============
This table has the parsed log data

====NB: I've used to JPA to persist Java models to the db and so running this SQL isn't necessary

CREATE TABLE ACCESSLOG (id bigint NOT NULL AUTO_INCREMENT,log_date datetime DEFAULT NULL,last_updated_at datetime DEFAULT NULL,
                    ip text DEFAULT NULL,
                    request text DEFAULT NULL,
                    ip request DEFAULT NULL,
                    status int DEFAULT 0,
                    user_agent text DEFAULT NULL,
                    PRIMARY KEY (id)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;


====NB: Run this script to be able to log blocked ips based on daily and monthly limits

CREATE TABLE blocked_ips (id bigint NOT NULL AUTO_INCREMENT,ip text DEFAULT NULL,total integer NULL,comment text DEFAULT NULL,
last_updated_at datetime DEFAULT CURRENT_TIMESTAMP,PRIMARY KEY (id)) ENGINE=InnoDB DEFAULT CHARSET=utf8;


SQL TEST QUERIES
=================
Find IPs that mode more than a certain number of requests for a given time period:
Java string: "select a.*, CASE WHEN total > 200 THEN 'Hourly limit exceeded' WHEN total >500 THEN 'Dialy limit exceeded' ELSE '' END as comment from (SELECT ip,count(ip) as total FROM accesslog  where LOGDATE BETWEEN '"
				+ startDate + "' AND '" + endDate + "' group by ip) a  where total>" + threshold + ";"
				
				
e.g select a.*, CASE WHEN total > 200 THEN 'Hourly limit exceeded' WHEN total >500 THEN 'Daily limit exceeded' ELSE '' END as comment from (SELECT ip,count(ip) as total FROM accesslog  where LOGDATE BETWEEN '2017-01-01.15:00:00' AND '2017-01-01.16:00:00' group by ip) a  where total>200;

The results of the query above are also loaded to another MySQL table with comments on why ip is blocked as follows:

INSERT INTO blocked_ips (ip,total,comment) select a.*, CASE WHEN total > 200 THEN 'Hourly limit exceeded' WHEN total >500 THEN 'Daily limit exceeded' ELSE '' END as comment from (SELECT ip,count(ip) as total FROM accesslog  where LOGDATE BETWEEN '2017-01-01.15:00:00' AND '2017-01-01.16:00:00' group by ip) a  where total>200;

Find requests made by a given IP
select ip from accesslog where ip=:ip
