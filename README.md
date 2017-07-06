(build by jenkins, triggred by jobdsl)
**Fraud detection system**

The application is fed with authentication log record which contains IP address, datetime, authentication result(OK or FAIL) and username.
It should detect fraud IP address based on certain strategies.

Current fraud strategies:
1) LoginAttemptsWithinSecondsLimitPolicy - This will detect an IP address which attempted a failed login 5 or more times within a 5 minute period.
2) BlackListedIpPolicy is just using blacklisted IP address

It is obviously an artificial problem and not intended to be used in production.
The sole reason of this application is to practice and demonstrate **in-memory cache**, **concurrency** features and how to **test concurrent application** in java.

**Libraries used**
In-memory cache is implemented using [DelayedQueue](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/DelayQueue.html)
which is queue of DelayedCleanUpJob instances to keep track of IP addresses, and remove it when it is expired automatically.
 

**Some notes about implementation.**

FraudDetector delegates fraud detection to
FraudPolicy implementations. Thus, at any time, new policy can be added without 
any change in the rest of the code.

Please note that code implemented in the multithreaded way so it should run 
under high concurrency without problem.

On the other hand, to prevent memory leakage, as failed login attempts are saved in the internal cache,
after a certain configurable time, they are scheduled for deletion.
Please note, if IP is kept being updated with failed attempts continuously, the growing list might also potentially 
bring to memory leakage, I deliberately don't clean it, assuming once fraud is detected
the IP should ideally be blocked from accessing the site by adding it to blacklist and then by applying that strategy

Whenever threads might clash in the race for the object access, there are locks which should take care of everything
to go smoothly, in fact there are couple tests covering those scenarios:
DelayedCleanUpJobTest.raceConditionWhenCleanUpThreadDoesNotRemoveIpAsFailedLoginIsBeingUpdatedByOtherThread
and
InMemoryCacheRepositoryTest.raceConditionWhenUpdateDoesNotHappenAtFirstTryAsOtherThreadUpdatingFailedLogin.

To demonstrate how application works I have added DemoTest.demo method which should be run manually.
It creates 20 threads with pseudo different IP and then every thread sends request every second for 5 times.
     
In fact generated IP could be same as the one already created by other thread and this can be controlled  
by updating randomNum with higher values to eliminate chances of duplication. On the other hand it would be nice to see what
happens when IPs clash, to demonstrate no dead lock or any other locking issues exist.
     
Approximately upon 3rd request, if all IPs are unique, detectFraud should detect fraud IP as it configured by SecondsAndAttemptsLimit.
     
Every failed attempt creates a clean up job which is executed upon expiration and should delete IP from cache
if there were no further updates for this IP after job has been created.
Eventually all IPs should be deleted from the cache once stopped being updated, thus preventing any memory leakage.
